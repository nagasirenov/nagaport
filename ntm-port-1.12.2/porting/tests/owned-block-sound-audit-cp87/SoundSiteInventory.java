import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.tools.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** Parse-only exhaustive call inventory. Receiver types come from the sealed compiler log. */
public final class SoundSiteInventory {
    static String clean(Object s) { return String.valueOf(s).replace("\t", " ").replace("\r", " ").replace("\n", " "); }
    public static void main(String[] args) throws Exception {
        final Path root = Paths.get(args[0]).toAbsolutePath();
        List<String> files = new ArrayList<>();
        try (java.util.stream.Stream<Path> walk = Files.walk(root.resolve("src/main/java"))) {
            walk.filter(p -> p.toString().endsWith(".java")).sorted().forEach(p -> files.add(p.toString()));
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);
        JavacTask task = (JavacTask) ToolProvider.getSystemJavaCompiler().getTask(null, fm, diagnostics,
                Arrays.asList("-proc:none", "-encoding", "UTF-8", "-source", "8"), null, fm.getJavaFileObjectsFromStrings(files));
        final SourcePositions positions = Trees.instance(task).getSourcePositions();
        int units = 0;
        System.out.println("path\tline\towner\tmethod\treceiver\targument\tfull_invocation");
        for (final CompilationUnitTree unit : task.parse()) {
            units++;
            final String source = unit.getSourceFile().getCharContent(true).toString();
            new TreePathScanner<Void, Void>() {
                public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
                    ExpressionTree selection = node.getMethodSelect();
                    String name = selection instanceof MemberSelectTree ? ((MemberSelectTree)selection).getIdentifier().toString() : selection.toString();
                    if (name.equals("setStepSound")) {
                        String owner = "", method = "";
                        for (TreePath p = getCurrentPath(); p != null; p = p.getParentPath()) {
                            if (method.isEmpty() && p.getLeaf() instanceof MethodTree) method = ((MethodTree)p.getLeaf()).getName().toString();
                            if (owner.isEmpty() && p.getLeaf() instanceof ClassTree) owner = ((ClassTree)p.getLeaf()).getSimpleName().toString();
                        }
                        long start = positions.getStartPosition(unit, node), end = positions.getEndPosition(unit, node);
                        String receiver = selection instanceof MemberSelectTree ? ((MemberSelectTree)selection).getExpression().toString() : "<implicit-this>";
                        System.out.println(root.relativize(Paths.get(unit.getSourceFile().toUri())) + "\t" + unit.getLineMap().getLineNumber(start) + "\t" + owner + "\t" + method + "\t" + clean(receiver) + "\t" + clean(node.getArguments()) + "\t" + clean(source.substring((int)start, (int)end)));
                    }
                    return super.visitMethodInvocation(node, unused);
                }
            }.scan(unit, null);
        }
        long errors = diagnostics.getDiagnostics().stream().filter(d -> d.getKind() == Diagnostic.Kind.ERROR).count();
        if (errors != 0) throw new AssertionError("Parse errors: " + errors);
        System.err.println("PASS parsed " + units + " production Java units; zero syntax errors; no attribution/full compile was run.");
        fm.close();
    }
}
