import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.tools.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** Partial-source javac attribution, explicitly not a successful mod compilation. */
public final class NormalCubeAttribution {
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
        javax.lang.model.util.Elements elements = task.getElements();
        javax.lang.model.element.TypeElement block = elements.getTypeElement("net.minecraft.block.Block");
        for (CompilationUnitTree unit : units) new TreePathScanner<Void, Void>() {
            public Void visitMethod(MethodTree tree, Void ignored) {
                if(tree.getName().contentEquals("canProvidePower")) {
                    javax.lang.model.element.Element element = trees.getElement(getCurrentPath());
                    boolean overrides = false;
                    if(element instanceof javax.lang.model.element.ExecutableElement) for(javax.lang.model.element.Element member : block.getEnclosedElements())
                        if(member.getSimpleName().contentEquals("canProvidePower") && member instanceof javax.lang.model.element.ExecutableElement)
                            overrides |= elements.overrides((javax.lang.model.element.ExecutableElement)element, (javax.lang.model.element.ExecutableElement)member, (javax.lang.model.element.TypeElement)element.getEnclosingElement());
                    System.out.println("OWNER\t" + root.relativize(Paths.get(unit.getSourceFile().toUri())) + "\t" + element + "\t" + overrides);
                }
                return super.visitMethod(tree, ignored);
            }
            public Void visitMethodInvocation(MethodInvocationTree tree, Void ignored) {
                if (tree.getMethodSelect() instanceof MemberSelectTree) {
                    MemberSelectTree select = (MemberSelectTree) tree.getMethodSelect();
                    if (select.getIdentifier().contentEquals("isNormalCube")) {
                        long start = trees.getSourcePositions().getStartPosition(unit, tree);
                        TreePath path = new TreePath(new TreePath(getCurrentPath(), select), select.getExpression());
                        System.out.println(root.relativize(Paths.get(unit.getSourceFile().toUri())) + "\t" + unit.getLineMap().getLineNumber(start)
                                + "\t" + tree.getArguments().size() + "\t" + trees.getTypeMirror(path)
                                + "\t" + (tree.getArguments().size()==1 ? trees.getTypeMirror(new TreePath(getCurrentPath(),tree.getArguments().get(0))) : "legacy-no-state")
                                + "\t" + trees.getElement(new TreePath(getCurrentPath(),select)));
                    }
                }
                return super.visitMethodInvocation(tree, ignored);
            }
        }.scan(unit, null);
        System.err.println("PARTIAL_ATTRIBUTION_DIAGNOSTICS=" + diagnostics.getDiagnostics().size() + "; not a successful project compile");
        manager.close();
    }
}
