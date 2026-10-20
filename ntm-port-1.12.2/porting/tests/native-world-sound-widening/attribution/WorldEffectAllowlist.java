import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.tools.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Parses the exact original production ModSoundEvents static initializer, not a fixture. */
public final class WorldEffectAllowlist {
 public static void main(String[] args)throws Exception {
  String source=new String(Files.readAllBytes(Paths.get(args[0])),StandardCharsets.UTF_8);
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
  DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,StandardCharsets.UTF_8);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-source","8"),null,Arrays.asList(new WorldSoundWideningAttribution.Source("com/hbm/main/ModSoundEvents.java",source)));
  for(CompilationUnitTree unit:task.parse())new TreePathScanner<Void,Void>(){
   @Override public Void visitMethodInvocation(MethodInvocationTree t,Void x){
    if(t.getMethodSelect() instanceof IdentifierTree&&((IdentifierTree)t.getMethodSelect()).getName().contentEquals("worldEffect")&&t.getArguments().size()==2&&t.getArguments().get(0) instanceof LiteralTree){
     TreePath p=getCurrentPath();boolean initializer=false;while(p!=null){if(p.getLeaf() instanceof BlockTree&&((BlockTree)p.getLeaf()).isStatic())initializer=true;p=p.getParentPath();}
     if(!initializer)throw new AssertionError("Declaration is not in production static initializer");
     System.out.println("hbm:"+((LiteralTree)t.getArguments().get(0)).getValue()+"\t"+t.getArguments().get(1));
    }
    return super.visitMethodInvocation(t,x);
   }
  }.scan(unit,null);
  for(Diagnostic<?>d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError(d);
  fm.close();
 }
}
