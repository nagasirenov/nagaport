import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** Real javac attribution of every named GuiTextField construction in supplied source roots. */
public final class TextFieldScanner {
    static String b64(String s) { return Base64.getEncoder().encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
    public static void main(String[] args) throws Exception {
        JavaCompiler c = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fm = c.getStandardFileManager(diagnostics, null, null);
        JavacTask task = (JavacTask) c.getTask(null, fm, diagnostics,
            Arrays.asList("-proc:none", "-source", "8", "-classpath", args[1], "-Xmaxerrs", "100000"),
            null, fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
        Trees trees = Trees.instance(task);
        List<CompilationUnitTree> units = new ArrayList<>();
        for (CompilationUnitTree u : task.parse()) units.add(u);
        task.analyze();
        for (CompilationUnitTree unit : units) {
            final String source = unit.getSourceFile().getCharContent(true).toString();
            new TreePathScanner<Void, Void>() {
                String text(Tree t) {
                    long s = trees.getSourcePositions().getStartPosition(unit, t);
                    long e = trees.getSourcePositions().getEndPosition(unit, t);
                    return s < 0 || e < s ? "" : source.substring((int)s, (int)e);
                }
                public Void visitNewClass(NewClassTree n, Void ignored) {
                    if (!n.getIdentifier().toString().endsWith("GuiTextField")) return super.visitNewClass(n, ignored);
                    TypeMirror type = trees.getTypeMirror(new TreePath(getCurrentPath(), n.getIdentifier()));
                    Element constructor = trees.getElement(getCurrentPath());
                    String owner = "", method = "", assignment = "";
                    List<String> loops = new ArrayList<>();
                    for (TreePath p = getCurrentPath().getParentPath(); p != null; p = p.getParentPath()) {
                        Tree t = p.getLeaf();
                        if (t instanceof AssignmentTree && assignment.isEmpty()) assignment = text(((AssignmentTree)t).getVariable());
                        if (t instanceof VariableTree && assignment.isEmpty()) assignment = ((VariableTree)t).getName().toString();
                        if (t instanceof ForLoopTree) {
                            ForLoopTree loop = (ForLoopTree)t;
                            loops.add(loop.getInitializer() + ";" + loop.getCondition() + ";" + loop.getUpdate());
                        }
                        if (t instanceof MethodTree && method.isEmpty()) method = ((MethodTree)t).getName().toString();
                        if (t instanceof ClassTree && owner.isEmpty()) owner = ((ClassTree)t).getSimpleName().toString();
                    }
                    List<String> types = new ArrayList<>();
                    List<String> arguments = new ArrayList<>();
                    for (ExpressionTree a : n.getArguments()) {
                        types.add(String.valueOf(trees.getTypeMirror(new TreePath(getCurrentPath(), a))));
                        arguments.add(b64(text(a)));
                    }
                    String executable = constructor instanceof ExecutableElement ? constructor.getEnclosingElement() + ":" + constructor : "unresolved";
                    System.out.println("NEW\t" + Paths.get(unit.getSourceFile().toUri()) + "\t" + trees.getSourcePositions().getStartPosition(unit, n) + "\t" + trees.getSourcePositions().getEndPosition(unit, n) + "\t" + type + "\t" + n.getArguments().size() + "\t" + (n.getClassBody() == null) + "\t" + owner + "\t" + method + "\t" + b64(assignment) + "\t" + b64(loops.toString()) + "\t" + String.join(",", arguments) + "\t" + b64(types.toString()) + "\t" + b64(executable));
                    return super.visitNewClass(n, ignored);
                }
                public Void visitMethodInvocation(MethodInvocationTree n, Void ignored) {
                    ExpressionTree select = n.getMethodSelect();
                    String name = select instanceof MemberSelectTree ? ((MemberSelectTree)select).getIdentifier().toString() : select.toString();
                    if (name.equals("setGuiResponder") || name.equals("setResponderEntryValue") || name.equals("getId")) {
                        Element e = trees.getElement(getCurrentPath());
                        System.out.println("CALL\t" + Paths.get(unit.getSourceFile().toUri()) + "\t" + b64(text(n)) + "\t" + b64(String.valueOf(e == null ? null : e.getEnclosingElement())));
                    }
                    return super.visitMethodInvocation(n, ignored);
                }
            }.scan(unit, null);
        }
        System.err.println("Attribution errors (not a whole-mod compile): " + diagnostics.getDiagnostics().stream().filter(d -> d.getKind() == Diagnostic.Kind.ERROR).count());
    }
}
