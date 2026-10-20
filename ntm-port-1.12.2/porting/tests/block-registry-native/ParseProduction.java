import java.util.Arrays;
import javax.tools.*;
import com.sun.source.util.JavacTask;
/** Parse the entire changed files without pretending dependency attribution succeeded. */
public final class ParseProduction {
    public static void main(String[] paths) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if(compiler==null) throw new AssertionError("JDK compiler required");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try(StandardJavaFileManager files=compiler.getStandardFileManager(diagnostics,null,null)) {
            JavacTask task=(JavacTask)compiler.getTask(null,files,diagnostics,
                    Arrays.asList("-proc:none", "-source", "8", "-target", "8"),null,files.getJavaFileObjects(paths));
            for(Object ignored:task.parse()) { }
            for(Diagnostic<?> diagnostic:diagnostics.getDiagnostics())
                if(diagnostic.getKind()==Diagnostic.Kind.ERROR) throw new AssertionError(diagnostic.toString());
        }
        System.out.println("PASS complete Java 8 syntax parse for ModBlocks and BlockBase; NOT a dependency/semantic compile");
    }
}
