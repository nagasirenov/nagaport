import java.nio.file.*;import java.util.*;import javax.tools.*;import javax.lang.model.element.*;import javax.lang.model.type.*;import com.sun.source.tree.*;import com.sun.source.util.*;
public class NativeWriteScanner {
 public static void main(String[] a)throws Exception {
 JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject>d=new DiagnosticCollector<>();StandardJavaFileManager f=c.getStandardFileManager(d,null,null);
 JavacTask task=(JavacTask)c.getTask(null,f,d,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,f.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
 Trees trees=Trees.instance(task);List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);for(Diagnostic<?>e:d.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError(e);task.analyze();
 for(CompilationUnitTree u:units)new TreePathScanner<Void,Void>(){public Void visitMethodInvocation(MethodInvocationTree t,Void v){
 Element e=trees.getElement(new TreePath(getCurrentPath(),t.getMethodSelect()));
 if(e instanceof ExecutableElement&&e.getEnclosingElement().toString().equals("com.hbm.world.WorldBlockState")&&e.getSimpleName().contentEquals("write")){
 if(!e.toString().equals("write(net.minecraft.world.World,int,int,int,net.minecraft.block.state.IBlockState,int)"))throw new AssertionError(e);
 System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+trees.getSourcePositions().getStartPosition(u,t)+"\t"+e);
 }return super.visitMethodInvocation(t,v);}}.scan(u,null);System.err.println("Unrelated unfinished-port diagnostics: "+d.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());f.close();
 }
}
