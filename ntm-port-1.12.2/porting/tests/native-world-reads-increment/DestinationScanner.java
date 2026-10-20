import java.nio.file.*;import java.util.*;import javax.tools.*;import javax.lang.model.element.*;import javax.lang.model.type.*;import com.sun.source.tree.*;import com.sun.source.util.*;
public class DestinationScanner {
 public static void main(String[] a)throws Exception {
 JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject>d=new DiagnosticCollector<>();StandardJavaFileManager f=c.getStandardFileManager(d,null,null);
 JavacTask task=(JavacTask)c.getTask(null,f,d,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,f.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
 Trees trees=Trees.instance(task);List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);for(Diagnostic<?>e:d.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError(e);task.analyze();
 for(CompilationUnitTree u:units)new TreePathScanner<Void,Void>(){public Void visitMethodInvocation(MethodInvocationTree t,Void v){
 Element e=trees.getElement(new TreePath(getCurrentPath(),t.getMethodSelect()));
 if(e instanceof ExecutableElement&&e.getEnclosingElement().toString().equals("com.hbm.world.WorldBlockState")&&e.getSimpleName().contentEquals("read")){
 if(!e.toString().equals("read(net.minecraft.world.World,int,int,int)"))throw new AssertionError(e);
 
 if(t.getArguments().size()!=4)throw new AssertionError(t);
 List<? extends ExpressionTree> args=t.getArguments();
 for(int i=0;i<4;i++){javax.lang.model.type.TypeMirror ty=trees.getTypeMirror(new TreePath(getCurrentPath(),args.get(i)));if(ty==null||ty.getKind()==javax.lang.model.type.TypeKind.ERROR)throw new AssertionError(t);if(i==0&&!ty.toString().equals("net.minecraft.world.World"))throw new AssertionError(ty);if(i>0&&ty.getKind()!=javax.lang.model.type.TypeKind.INT)throw new AssertionError(ty);}
 TreePath parent=getCurrentPath().getParentPath();
 if(parent.getLeaf() instanceof MemberSelectTree && ((MemberSelectTree)parent.getLeaf()).getIdentifier().contentEquals("getBlock")) {
 Element owner=trees.getElement(parent);if(!(owner instanceof ExecutableElement)||!owner.getEnclosingElement().toString().equals("net.minecraft.block.state.IBlockState"))throw new AssertionError(owner);
 SourcePositions sp=trees.getSourcePositions();
 System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+sp.getStartPosition(u,t)+"\t"+sp.getStartPosition(u,args.get(0))+"\t"+sp.getEndPosition(u,args.get(0))+"\t"+sp.getStartPosition(u,args.get(1))+"\t"+sp.getEndPosition(u,args.get(3)));
 }

 }return super.visitMethodInvocation(t,v);}}.scan(u,null);System.err.println("Unrelated unfinished-port diagnostics: "+d.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());f.close();
 }
}
