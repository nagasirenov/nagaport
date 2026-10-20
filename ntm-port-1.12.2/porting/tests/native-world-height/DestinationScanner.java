import java.nio.file.*;import java.util.*;import javax.tools.*;import javax.lang.model.element.*;import javax.lang.model.type.*;import com.sun.source.tree.*;import com.sun.source.util.*;
public class DestinationScanner {
 public static void main(String[] a)throws Exception {
 JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject>d=new DiagnosticCollector<>();StandardJavaFileManager f=c.getStandardFileManager(d,null,null);
 JavacTask task=(JavacTask)c.getTask(null,f,d,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,f.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
 Trees trees=Trees.instance(task);List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);for(Diagnostic<?>e:d.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError(e);task.analyze();
 for(CompilationUnitTree u:units)new TreePathScanner<Void,Void>(){public Void visitMethodInvocation(MethodInvocationTree t,Void v){
 Element e=trees.getElement(new TreePath(getCurrentPath(),t.getMethodSelect()));
 if(e instanceof ExecutableElement&&e.getEnclosingElement().toString().equals("com.hbm.world.WorldHeight")&&e.getSimpleName().contentEquals("read")){
 if(!e.toString().equals("read(net.minecraft.world.World,int,int)"))throw new AssertionError(e);
 List<? extends ExpressionTree> args=t.getArguments();if(args.size()!=3)throw new AssertionError(t);
 for(int i=0;i<3;i++){TypeMirror ty=trees.getTypeMirror(new TreePath(getCurrentPath(),args.get(i)));if(ty==null||ty.getKind()==TypeKind.ERROR)throw new AssertionError(t);if(i==0&&!ty.toString().equals("net.minecraft.world.World"))throw new AssertionError(ty);if(i>0&&ty.getKind()!=TypeKind.INT)throw new AssertionError(ty);}
 boolean remote=false;String method="";Tree child=t;
 for(TreePath p=getCurrentPath().getParentPath();p!=null;child=p.getLeaf(),p=p.getParentPath()){
  if(p.getLeaf() instanceof IfTree){IfTree branch=(IfTree)p.getLeaf();if(branch.getThenStatement()==child&&branch.getCondition().toString().equals("(this.world.isRemote)"))remote=true;}
  if(p.getLeaf() instanceof MethodTree)method=((MethodTree)p.getLeaf()).getName().toString();
 }
 SourcePositions sp=trees.getSourcePositions();System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+sp.getStartPosition(u,t)+"\t"+sp.getStartPosition(u,args.get(0))+"\t"+sp.getEndPosition(u,args.get(0))+"\t"+sp.getStartPosition(u,args.get(1))+"\t"+sp.getEndPosition(u,args.get(2))+"\t"+method+"\t"+remote);
 }return super.visitMethodInvocation(t,v);}}.scan(u,null);System.err.println("Unrelated unfinished-port diagnostics: "+d.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());f.close();
 }
}
