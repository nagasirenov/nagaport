import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.tools.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** Partial-source javac attribution, explicitly not a successful mod compilation. */
public final class QueryAttribution {
    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args[0]);
        List<java.io.File> files = new ArrayList<>();
        for (String line : Files.readAllLines(Paths.get(args[1]))) files.add(root.resolve(line).toFile());
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);
        JavacTask task = (JavacTask) compiler.getTask(null, manager, diagnostics,
                Arrays.asList("-proc:none", "-implicit:none", "-sourcepath", args[3], "-classpath", args[2], "-Xmaxerrs", "100000"), null,
                manager.getJavaFileObjectsFromFiles(files));
        List<CompilationUnitTree> units = new ArrayList<>();
        for (CompilationUnitTree unit : task.parse()) units.add(unit);
        task.analyze();
        Trees trees = Trees.instance(task);
        for (CompilationUnitTree unit : units) new TreePathScanner<Void, Void>() {
            public Void visitMethodInvocation(MethodInvocationTree tree, Void ignored) {
                if (tree.getMethodSelect() instanceof MemberSelectTree) {
                    MemberSelectTree select = (MemberSelectTree) tree.getMethodSelect();
                    if (select.getIdentifier().contentEquals("isReplaceable")) {
                        long start = trees.getSourcePositions().getStartPosition(unit, tree);
                        TreePath path = new TreePath(new TreePath(getCurrentPath(), select), select.getExpression());
                        System.out.println(root.relativize(Paths.get(unit.getSourceFile().toUri())) + "\t" + unit.getLineMap().getLineNumber(start)
                                + "\t" + tree.getArguments().size() + "\t" + trees.getTypeMirror(path));
                    }
                }
                return super.visitMethodInvocation(tree, ignored);
            }
        }.scan(unit, null);
        System.err.println("PARTIAL_ATTRIBUTION_DIAGNOSTICS=" + diagnostics.getDiagnostics().size() + "; not a successful project compile");
        manager.close();
    }
}
