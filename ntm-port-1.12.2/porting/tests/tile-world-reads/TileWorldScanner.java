import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Select zero-argument unresolved legacy calls only on proven native TileEntity receivers. */
public final class TileWorldScanner {
 public static void main(String[] a)throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(errors,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
  final Trees trees=Trees.instance(task);final SourcePositions pos=trees.getSourcePositions();
  List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> e:errors.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse failure: "+e);
  task.analyze();final Types types=task.getTypes();
  final TypeElement base=task.getElements().getTypeElement("net.minecraft.tileentity.TileEntity");
  for(final CompilationUnitTree u:units)new TreePathScanner<Void,Void>(){
   public Void visitClass(ClassTree c,Void v){Element e=trees.getElement(getCurrentPath());if(e instanceof TypeElement&&types.isSubtype(e.asType(),base.asType())){
    System.err.println("TILE_OWNER\t"+((TypeElement)e).getQualifiedName());
    for(Element m:e.getEnclosedElements())if(m.getKind()==ElementKind.METHOD&&Arrays.asList("getWorld","getWorldObj").contains(m.getSimpleName().toString())&&((ExecutableElement)m).getParameters().isEmpty()){System.err.println("SOURCE_OVERRIDE\t"+e+"\t"+m);
     if(!e.toString().equals("com.hbm.tileentity.machine.TileEntityAirPump")||!m.getSimpleName().contentEquals("getWorld"))throw new AssertionError("Unapproved source override: "+e+" "+m);
     MethodTree mt=trees.getTree((ExecutableElement)m);if(mt==null||!mt.getBody().toString().replaceAll("\\s+","").equals("{returnthis.world;}"))throw new AssertionError("AirPump body drift");
     final int[] fields={0};new TreePathScanner<Void,Void>(){public Void visitMemberSelect(MemberSelectTree t,Void v){Element f=trees.getElement(getCurrentPath());if(f!=null&&f.getKind()==ElementKind.FIELD&&f.getSimpleName().contentEquals("world")){if(!f.getEnclosingElement().toString().equals("net.minecraft.tileentity.TileEntity"))throw new AssertionError("hidden world");fields[0]++;}return super.visitMemberSelect(t,v);}}.scan(trees.getPath(m),null);if(fields[0]!=1)throw new AssertionError("world field attribution");
    }
   }return super.visitClass(c,v);}
  }.scan(u,null);
  for(final CompilationUnitTree u:units){final String src=u.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>(){
    boolean tile(TypeMirror type){
     if(type==null||type.getKind()!=TypeKind.DECLARED)return false;
     TypeElement owner=(TypeElement)((DeclaredType)type).asElement();
     if(!types.isSubtype(type,base.asType()))return false;
     for(Element m:task.getElements().getAllMembers(owner))if(m.getKind()==ElementKind.METHOD&&Arrays.asList("getWorld","getWorldObj").contains(m.getSimpleName().toString())&&(m.getSimpleName().contentEquals("getWorldObj")||((ExecutableElement)m).getParameters().isEmpty())){
      String decl=((TypeElement)m.getEnclosingElement()).getQualifiedName().toString();
      if(!decl.equals("net.minecraft.tileentity.TileEntity")&&!decl.equals("com.hbm.tileentity.machine.TileEntityAirPump"))return false;
      if(m.getSimpleName().contentEquals("getWorldObj"))return false;
     }
     for(Element m:base.getEnclosedElements())if(m.getKind()==ElementKind.METHOD&&m.getSimpleName().contentEquals("getWorld")&&((ExecutableElement)m).getParameters().isEmpty())return m.getModifiers().contains(Modifier.PUBLIC)&&((ExecutableElement)m).getReturnType().toString().equals("net.minecraft.world.World");
     return false;
    }
    public Void visitMethodInvocation(MethodInvocationTree t,Void v){
     Tree sel=t.getMethodSelect();String name=sel instanceof MemberSelectTree?((MemberSelectTree)sel).getIdentifier().toString():sel.toString();
     if(name.equals(a.length>2?"getWorld":"getWorldObj")&&t.getArguments().isEmpty()&&t.getTypeArguments().isEmpty()){
      TypeMirror receiver=null;
      if(sel instanceof MemberSelectTree){Tree ex=((MemberSelectTree)sel).getExpression();TreePath ep=new TreePath(new TreePath(getCurrentPath(),sel),ex);if(!(trees.getElement(ep) instanceof TypeElement))receiver=trees.getTypeMirror(ep);}
      else {TreePath p=getCurrentPath();boolean stat=false;while(p!=null&&!(p.getLeaf() instanceof ClassTree)){Tree n=p.getLeaf();if(n instanceof MethodTree&&((MethodTree)n).getModifiers().getFlags().contains(Modifier.STATIC))stat=true;if(n instanceof BlockTree&&((BlockTree)n).isStatic())stat=true;if(n instanceof VariableTree&&((VariableTree)n).getModifiers().getFlags().contains(Modifier.STATIC))stat=true;p=p.getParentPath();}if(!stat&&p!=null){Element e=trees.getElement(p);if(e!=null)receiver=e.asType();}}
      if(tile(receiver)&&!(a.length==2&&trees.getElement(new TreePath(getCurrentPath(),sel)) instanceof ExecutableElement)){
       if(a.length>2){Element e=trees.getElement(new TreePath(getCurrentPath(),sel));if(!(e instanceof ExecutableElement))throw new AssertionError("unresolved destination");String decl=((TypeElement)e.getEnclosingElement()).getQualifiedName().toString();if(!decl.equals("net.minecraft.tileentity.TileEntity")&&!decl.equals("com.hbm.tileentity.machine.TileEntityAirPump"))throw new AssertionError(decl);}
       long end=pos.getEndPosition(u,sel),start=end-name.length();if(start>=0&&src.substring((int)start,(int)end).equals(name))System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+start+"\t"+end+"\tgetWorld\t"+receiver+"\t"+name);
      }
     }return super.visitMethodInvocation(t,v);
    }
   }.scan(u,null);
  }
  System.err.println("Attribution diagnostics (unfinished source, not compile success): "+errors.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
