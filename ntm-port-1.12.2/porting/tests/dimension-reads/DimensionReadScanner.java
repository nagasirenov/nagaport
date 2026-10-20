import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Compiler-proven provider read selections; never select writes or domain-field shadows. */
public final class DimensionReadScanner {
 public static void main(String[] a)throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(errors,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
  final Trees trees=Trees.instance(task);final SourcePositions pos=trees.getSourcePositions();
  List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> e:errors.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse failure: "+e);
  task.analyze();final Types types=task.getTypes();
  final TypeElement base=task.getElements().getTypeElement("net.minecraft.world.WorldProvider");
  for(final CompilationUnitTree u:units)new TreePathScanner<Void,Void>(){
   public Void visitClass(ClassTree c,Void v){Element e=trees.getElement(getCurrentPath());if(e instanceof TypeElement&&types.isSubtype(e.asType(),base.asType())){
    System.err.println("PROVIDER_OWNER\t"+((TypeElement)e).getQualifiedName());
    for(Element m:e.getEnclosedElements())if(m.getKind()==ElementKind.METHOD&&m.getSimpleName().contentEquals("getDimension")&&((ExecutableElement)m).getParameters().isEmpty())throw new AssertionError("Source getDimension override: "+e);
   }return super.visitClass(c,v);}
  }.scan(u,null);
  for(final CompilationUnitTree u:units){final String src=u.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>(){
    boolean tile(TypeMirror type){
     if(type==null||type.getKind()!=TypeKind.DECLARED)return false;
     TypeElement owner=(TypeElement)((DeclaredType)type).asElement();
     for(Element f:task.getElements().getAllMembers(owner)){
      if(f.getKind()==ElementKind.FIELD&&Arrays.asList("dimensionId").contains(f.getSimpleName().toString())&&!((TypeElement)f.getEnclosingElement()).getQualifiedName().contentEquals("net.minecraft.world.WorldProvider"))return false;
      if(f.getKind()==ElementKind.METHOD&&f.getSimpleName().contentEquals("getDimension")&&((ExecutableElement)f).getParameters().isEmpty()&&!((TypeElement)f.getEnclosingElement()).getQualifiedName().contentEquals("net.minecraft.world.WorldProvider"))return false;
     }
     Set<String> seen=new HashSet<>();while(type!=null&&type.getKind()==TypeKind.DECLARED){
      TypeElement e=(TypeElement)((DeclaredType)type).asElement();if(!seen.add(e.getQualifiedName().toString()))return false;
      for(Element f:e.getEnclosedElements())if(f.getKind()==ElementKind.FIELD&&Arrays.asList("dimensionId").contains(f.getSimpleName().toString())&&!((TypeElement)f.getEnclosingElement()).getQualifiedName().contentEquals("net.minecraft.world.WorldProvider"))return false;
      if(e.getQualifiedName().contentEquals("net.minecraft.world.WorldProvider")){
       for(Element f:e.getEnclosedElements())if(f.getKind()==ElementKind.METHOD&&f.getSimpleName().contentEquals("getDimension")&&((ExecutableElement)f).getParameters().isEmpty())return ((ExecutableElement)f).getReturnType().toString().equals("int")&&f.getModifiers().contains(Modifier.PUBLIC);
       return false;
      }type=e.getSuperclass();
     }return false;
    }
    void emit(Tree t,String old,String prefix,String receiver){Element b=trees.getElement(getCurrentPath());if(b==null||b.getKind()!=ElementKind.FIELD||!((TypeElement)b.getEnclosingElement()).getQualifiedName().contentEquals("net.minecraft.world.WorldProvider")||!b.asType().toString().equals("int"))return;long end=pos.getEndPosition(u,t),start=end-old.length();if(start>=0&&end<=src.length()&&src.substring((int)start,(int)end).equals(old))System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+prefix+"getDimension()\t"+receiver+"\t"+old);}
    boolean write(){Tree leaf=getCurrentPath().getLeaf();TreePath p=getCurrentPath().getParentPath();
     while(p!=null&&p.getLeaf() instanceof ParenthesizedTree){leaf=p.getLeaf();p=p.getParentPath();}
     if(p==null)return false;Tree t=p.getLeaf();
     if(t instanceof AssignmentTree)return ((AssignmentTree)t).getVariable()==leaf;
     if(t instanceof CompoundAssignmentTree)return ((CompoundAssignmentTree)t).getVariable()==leaf;
     return t instanceof UnaryTree&&Arrays.asList(Tree.Kind.PREFIX_INCREMENT,Tree.Kind.PREFIX_DECREMENT,Tree.Kind.POSTFIX_INCREMENT,Tree.Kind.POSTFIX_DECREMENT).contains(t.getKind());
    }
    public Void visitMethodInvocation(MethodInvocationTree t,Void v){
     if(a.length>2&&t.getArguments().isEmpty()){
      Element e=trees.getElement(getCurrentPath());if(e instanceof ExecutableElement&&e.getSimpleName().contentEquals("getDimension")&&((TypeElement)e.getEnclosingElement()).getQualifiedName().contentEquals("net.minecraft.world.WorldProvider")){
       long end=pos.getEndPosition(u,t),start=end-14;
       if(start>=0&&src.substring((int)start,(int)end).equals("getDimension()"))System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+e.getEnclosingElement());
      }
     }return super.visitMethodInvocation(t,v);
    }
    public Void visitImport(ImportTree t,Void v){return null;}
    public Void visitMemberSelect(MemberSelectTree t,Void v){String old=t.getIdentifier().toString();if(Arrays.asList("dimensionId").contains(old)&&!write()){
     TreePath p=new TreePath(getCurrentPath(),t.getExpression());TypeMirror receiver=trees.getTypeMirror(p);Element e=trees.getElement(p);
     if(!(e instanceof TypeElement)&&tile(receiver))emit(t,old,"",receiver.toString());
    }return super.visitMemberSelect(t,v);}
    public Void visitIdentifier(IdentifierTree t,Void v){String old=t.getName().toString();if(Arrays.asList("dimensionId").contains(old)&&!write()){
     Element binding=trees.getElement(getCurrentPath());if(binding!=null&&(binding.getKind()==ElementKind.LOCAL_VARIABLE||binding.getKind()==ElementKind.PARAMETER||(binding.getKind()==ElementKind.FIELD&&!((TypeElement)binding.getEnclosingElement()).getQualifiedName().contentEquals("net.minecraft.world.WorldProvider"))))return null;
     TreePath p=getCurrentPath();while(p!=null&&!(p.getLeaf() instanceof ClassTree)){
      Tree n=p.getLeaf();if(n instanceof MethodTree&&((MethodTree)n).getModifiers().getFlags().contains(Modifier.STATIC))return null;
      if(n instanceof VariableTree&&((VariableTree)n).getModifiers().getFlags().contains(Modifier.STATIC))return null;
      if(n instanceof BlockTree&&((BlockTree)n).isStatic())return null;p=p.getParentPath();
     }
     if(p!=null){Element e=trees.getElement(p);if(e instanceof TypeElement&&tile(e.asType()))emit(t,old,"this.",e.asType().toString());}
    }return super.visitIdentifier(t,v);}
   }.scan(u,null);
  }
  System.err.println("Attribution diagnostics (unfinished source, not compile success): "+errors.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
