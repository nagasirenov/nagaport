import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Audit only fixed compiler-reported locations; not a general text replacement. */
public final class RemainingWorldScanner {
 static final String ENTITY="net.minecraft.entity.Entity", TILE="net.minecraft.tileentity.TileEntity", MAP="net.minecraft.world.gen.MapGenBase", CHUNK="net.minecraft.world.chunk.Chunk";
 public static void main(String[] args)throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler(); DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  Trees trees=Trees.instance(task);SourcePositions positions=trees.getSourcePositions();List<CompilationUnitTree> units=new ArrayList<>();
  for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse error: "+d);
  task.analyze();Set<String> targets=new HashSet<>(Files.readAllLines(Paths.get(args[2])));boolean after=args.length>3&&args[3].equals("after");
  Set<String> chunkChildren=new TreeSet<>();
  for(final CompilationUnitTree u:units){String file=Paths.get(u.getSourceFile().toUri()).toString(),rel=file.substring(file.indexOf("src/main/java/"));String src=u.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>() {
    boolean inherited(TypeMirror type,String base) {
     Set<String> visited=new HashSet<>();
     while(type!=null&&type.getKind()==TypeKind.DECLARED){TypeElement e=(TypeElement)((DeclaredType)type).asElement();String name=e.getQualifiedName().toString();if(!visited.add(name))return false;if(name.equals(base))return true;type=e.getSuperclass();}return false;
    }
    String family(TypeMirror type){for(String base:Arrays.asList(ENTITY,TILE,MAP,CHUNK))if(inherited(type,base))return base;return "";}
    boolean safe(TypeMirror type,String base) {
     if(type==null||type.getKind()!=TypeKind.DECLARED)return false;
     TypeElement first=(TypeElement)((DeclaredType)type).asElement();
     for(Element f:task.getElements().getAllMembers(first))if(f.getKind()==ElementKind.FIELD&&(f.getSimpleName().contentEquals("worldObj")||(f.getSimpleName().contentEquals("world")&&!f.getEnclosingElement().toString().equals(base))))return false;
     while(type!=null&&type.getKind()==TypeKind.DECLARED){TypeElement e=(TypeElement)((DeclaredType)type).asElement();if(e.getQualifiedName().contentEquals(base)){
       for(Element f:e.getEnclosedElements())if(f.getKind()==ElementKind.FIELD&&f.getSimpleName().contentEquals("world"))return f.asType().toString().equals("net.minecraft.world.World")&&(base.equals(CHUNK)?f.getModifiers().contains(Modifier.PRIVATE):f.getModifiers().contains(Modifier.PUBLIC)||f.getModifiers().contains(Modifier.PROTECTED));return false;
      }
      for(Element f:e.getEnclosedElements())if(f.getKind()==ElementKind.FIELD&&(f.getSimpleName().contentEquals("worldObj")||f.getSimpleName().contentEquals("world")))return false;
      type=e.getSuperclass();
     }return false;
    }
    boolean read(Tree node){TreePath p=getCurrentPath().getParentPath();while(p!=null&&p.getLeaf() instanceof ParenthesizedTree){node=p.getLeaf();p=p.getParentPath();}if(p==null)return false;Tree t=p.getLeaf();if(t instanceof AssignmentTree&&((AssignmentTree)t).getVariable()==node)return false;if(t instanceof CompoundAssignmentTree&&((CompoundAssignmentTree)t).getVariable()==node)return false;switch(t.getKind()){case PREFIX_INCREMENT:case PREFIX_DECREMENT:case POSTFIX_INCREMENT:case POSTFIX_DECREMENT:return false;default:return true;}}
    boolean target(Tree t){return targets.contains(rel+":"+u.getLineMap().getLineNumber(positions.getStartPosition(u,t)));}
    String owner(){for(TreePath p=getCurrentPath();p!=null;p=p.getParentPath())if(p.getLeaf() instanceof ClassTree){Element e=trees.getElement(p);return e==null?"?":e.toString();}return "?";}
    void emit(Tree t,String next,String recv,String base){long end=positions.getEndPosition(u,t),start=end-(after?5:8);String token=after?"world":"worldObj";if(start<0||!src.substring((int)start,(int)end).equals(token))throw new AssertionError("unexpected token");System.out.println(rel+"\t"+start+"\t"+end+"\t"+next+"\t"+recv+"\t"+base+"\t"+owner()+"\tREAD");}
    public Void visitClass(ClassTree t,Void v){Element e=trees.getElement(getCurrentPath());if(e instanceof TypeElement&&inherited(e.asType(),CHUNK)){
      chunkChildren.add(e.toString());for(Element m:e.getEnclosedElements())if(m.getKind()==ElementKind.METHOD&&(m.getSimpleName().contentEquals("getWorld")||m.getSimpleName().contentEquals("getWorldObj")))throw new AssertionError("source Chunk override: "+e+"."+m);
     }return super.visitClass(t,v);}
    public Void visitImport(ImportTree t,Void v){return null;}
    public Void visitMemberSelect(MemberSelectTree t,Void v){
     if(target(t)&&t.getIdentifier().contentEquals(after?"world":"worldObj")){
      if(!read(t))throw new AssertionError("write: "+rel+":"+t);TreePath rp=new TreePath(getCurrentPath(),t.getExpression());TypeMirror type=trees.getTypeMirror(rp);Element expression=trees.getElement(rp);String base=family(type);
      if(after){Element field=trees.getElement(getCurrentPath());if(field!=null&&field.getKind()==ElementKind.FIELD&&Arrays.asList(ENTITY,TILE,MAP).contains(field.getEnclosingElement().toString()))emit(t,"verified",type.toString(),field.getEnclosingElement().toString());}
      else if(!(expression instanceof TypeElement)&&!base.isEmpty()&&safe(type,base))emit(t,base.equals(CHUNK)?"getWorld()":"world",type.toString(),base);
      else System.err.println("DEFERRED\t"+rel+":"+u.getLineMap().getLineNumber(positions.getStartPosition(u,t))+"\t"+t+"\t"+type);
     }return super.visitMemberSelect(t,v);
    }
    public Void visitMethodInvocation(MethodInvocationTree t,Void v){if(after&&target(t)&&t.getMethodSelect() instanceof MemberSelectTree){MemberSelectTree m=(MemberSelectTree)t.getMethodSelect();if(m.getIdentifier().contentEquals("getWorld")&&t.getArguments().isEmpty()){
      Element e=trees.getElement(new TreePath(getCurrentPath(),m));if(e==null||!e.getEnclosingElement().toString().equals(CHUNK))throw new AssertionError("wrong getter binding: "+e);long end=positions.getEndPosition(u,t),start=end-10;System.out.println(rel+"\t"+start+"\t"+end+"\tverified\t"+CHUNK+"\t"+CHUNK+"\t"+owner()+"\tREAD");
     }}return super.visitMethodInvocation(t,v);}
    public Void visitIdentifier(IdentifierTree t,Void v){if(!after&&target(t)&&t.getName().contentEquals("worldObj")){
      if(!read(t))throw new AssertionError("write: "+rel+":"+t);Element binding=trees.getElement(getCurrentPath());if(binding!=null&&(binding.getKind()==ElementKind.FIELD||binding.getKind()==ElementKind.LOCAL_VARIABLE||binding.getKind()==ElementKind.PARAMETER))throw new AssertionError("bound legacy token: "+binding);
      TreePath p=getCurrentPath();int classes=0;
      while(p!=null){Tree n=p.getLeaf();if(n instanceof MethodTree&&((MethodTree)n).getModifiers().getFlags().contains(Modifier.STATIC))throw new AssertionError("static method");if(n instanceof BlockTree&&((BlockTree)n).isStatic())throw new AssertionError("static block");if(n instanceof VariableTree&&((VariableTree)n).getModifiers().getFlags().contains(Modifier.STATIC))throw new AssertionError("static field");
       if(n instanceof ClassTree){TypeElement e=(TypeElement)trees.getElement(p);String base=family(e.asType());if(base.equals(TILE)&&safe(e.asType(),base)){emit(t,classes==0?"this.world":e.getSimpleName()+".this.world",e.toString(),base);return null;}
        for(Element f:task.getElements().getAllMembers(e))if(f.getKind()==ElementKind.FIELD&&(f.getSimpleName().contentEquals("worldObj")||f.getSimpleName().contentEquals("world")))throw new AssertionError("lexical shadow: "+e+"."+f);
        if(e.getModifiers().contains(Modifier.STATIC))throw new AssertionError("static nested class");classes++;
       }p=p.getParentPath();
      }throw new AssertionError("unknown unqualified owner: "+rel+":"+t);
     }return super.visitIdentifier(t,v);}
   }.scan(u,null);
  }
  if(args.length>4){List<String> rows=new ArrayList<>();for(Diagnostic<? extends JavaFileObject> d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR){String f=d.getSource()==null?"?":Paths.get(d.getSource().toUri()).toString();if(f.contains("src/main/java/"))f=f.substring(f.indexOf("src/main/java/"));rows.add(f+":"+d.getLineNumber()+":"+d.getColumnNumber()+" "+d.getMessage(java.util.Locale.ROOT).replace("\n"," | "));}Files.write(Paths.get(args[4]),rows);}
  System.err.println("Source Chunk descendants: "+chunkChildren);
  System.err.println("Attribution diagnostics (unfinished source, NOT compilation success): "+diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
