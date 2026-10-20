import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.tools.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** Syntax-only inventory. This does NOT claim successful whole-project attribution. */
public final class QuerySourceScan {
    static String b64(String s) { return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8)); }
    public static void main(String[] args) throws Exception {
        final Path root = Paths.get(args[0]);
        List<java.io.File> files = new ArrayList<>();
        Files.walk(root).filter(p -> p.toString().endsWith(".java")).sorted().forEach(p -> files.add(p.toFile()));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        JavacTask task = (JavacTask) compiler.getTask(null, manager, null, Arrays.asList("-proc:none"), null, manager.getJavaFileObjectsFromFiles(files));
        final Trees trees = Trees.instance(task);
        for (final CompilationUnitTree unit : task.parse()) {
            final String file = root.relativize(Paths.get(unit.getSourceFile().toUri())).toString();
            final String source = unit.getSourceFile().getCharContent(true).toString();
            final String pkg = unit.getPackageName() == null ? "" : unit.getPackageName().toString();
            final Map<String,String> imports = new TreeMap<>();
            for (ImportTree imp : unit.getImports()) if (!imp.isStatic()) {
                String name = imp.getQualifiedIdentifier().toString();
                imports.put(name.substring(name.lastIndexOf('.') + 1), name);
            }
            new TreePathScanner<Void, Void>() {
                String owner = "";
                long start(Tree tree) { return trees.getSourcePositions().getStartPosition(unit, tree); }
                long end(Tree tree) { return trees.getSourcePositions().getEndPosition(unit, tree); }
                String raw(Tree tree) { return source.substring((int)start(tree), (int)end(tree)); }
                public Void visitClass(ClassTree tree, Void ignored) {
                    String previous = owner;
                    owner = previous.isEmpty() ? pkg + "." + tree.getSimpleName() : previous + "$" + tree.getSimpleName();
                    String parent = tree.getExtendsClause() == null ? "" : tree.getExtendsClause().toString();
                    if (!parent.isEmpty() && !parent.contains(".")) parent = imports.containsKey(parent) ? imports.get(parent) : pkg + "." + parent;
                    System.out.println("CLASS\t" + file + "\t" + owner + "\t" + parent);
                    super.visitClass(tree, ignored);
                    owner = previous;
                    return null;
                }
                public Void visitMethod(MethodTree tree, Void ignored) {
                    if (tree.getName().contentEquals("isReplaceable")) {
                        List<? extends VariableTree> p = tree.getParameters();
                        StringJoiner types = new StringJoiner(",");
                        for (VariableTree v : p) types.add(v.getType().toString());
                        System.out.println("METHOD\t" + file + "\t" + owner + "\t" + types + "\t" + start(p.get(0)) + "\t" + end(p.get(p.size()-1)) + "\t" + b64(raw(tree)));
                    }
                    return super.visitMethod(tree, ignored);
                }
                public Void visitMethodInvocation(MethodInvocationTree tree, Void ignored) {
                    if (tree.getMethodSelect() instanceof MemberSelectTree) {
                        MemberSelectTree select = (MemberSelectTree) tree.getMethodSelect();
                        if (select.getIdentifier().contentEquals("isReplaceable") && tree.getArguments().size() == 4) {
                            List<? extends ExpressionTree> p = tree.getArguments();
                            System.out.println("CALL\t" + file + "\t" + unit.getLineMap().getLineNumber(start(tree)) + "\t" + start(p.get(0)) + "\t" + end(p.get(3)) + "\t" + b64(raw(select.getExpression())) + "\t" + b64(raw(p.get(0))) + "\t" + b64(raw(p.get(1))) + "\t" + b64(raw(p.get(2))) + "\t" + b64(raw(p.get(3))));
                        }
                    }
                    return super.visitMethodInvocation(tree, ignored);
                }
            }.scan(unit, null);
        }
        manager.close();
    }
}
