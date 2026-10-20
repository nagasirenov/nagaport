import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** The legacy getter is absent from 1.12.2: attribute its exact server receiver,
 * then verify native getter + map call ownership separately after migration. */
public class PlayerChunkScanner {
    public static void main(String[] args) throws Exception {
        boolean nativeMode = args[2].equals("native");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        StandardJavaFileManager files = compiler.getStandardFileManager(errors, null, null);
        JavacTask task = (JavacTask) compiler.getTask(null, files, errors,
            Arrays.asList("-proc:none", "-source", "8", "-classpath",
                new String(Files.readAllBytes(Paths.get(args[1]))).trim(), "-Xmaxerrs", "100000"),
            null, files.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
        Trees trees = Trees.instance(task);
        SourcePositions positions = trees.getSourcePositions();
        List<CompilationUnitTree> units = new ArrayList<>();
        for (CompilationUnitTree unit : task.parse()) units.add(unit);
        for (Diagnostic<?> error : errors.getDiagnostics())
            if (error.getKind() == Diagnostic.Kind.ERROR) throw new AssertionError("parse error: " + error);
        task.analyze();
        for (CompilationUnitTree unit : units) new TreePathScanner<Void, Void>() {
            boolean owned(TreePath path, String owner, String signature) {
                Element element = trees.getElement(path);
                return element instanceof ExecutableElement
                    && element.getEnclosingElement().toString().equals(owner)
                    && element.toString().equals(signature);
            }
            public Void visitMethodInvocation(MethodInvocationTree call, Void unused) {
                if (!(call.getMethodSelect() instanceof MemberSelectTree)) return super.visitMethodInvocation(call, unused);
                MemberSelectTree method = (MemberSelectTree) call.getMethodSelect();
                if (!method.getIdentifier().contentEquals("markBlockForUpdate")
                    || !(method.getExpression() instanceof MethodInvocationTree)) return super.visitMethodInvocation(call, unused);
                MethodInvocationTree getter = (MethodInvocationTree) method.getExpression();
                if (!(getter.getMethodSelect() instanceof MemberSelectTree) || !getter.getArguments().isEmpty()) return super.visitMethodInvocation(call, unused);
                MemberSelectTree get = (MemberSelectTree) getter.getMethodSelect();
                if (!get.getIdentifier().contentEquals(nativeMode ? "getPlayerChunkMap" : "getPlayerManager")) return super.visitMethodInvocation(call, unused);
                TreePath methodPath = new TreePath(getCurrentPath(), method);
                TreePath getterPath = new TreePath(methodPath, getter);
                TreePath getPath = new TreePath(getterPath, get);
                TypeMirror receiver = trees.getTypeMirror(new TreePath(getPath, get.getExpression()));
                boolean ok = receiver != null && receiver.getKind() == TypeKind.DECLARED
                    && receiver.toString().equals("net.minecraft.world.WorldServer");
                List<? extends ExpressionTree> coordinates = call.getArguments();
                TreePath coordinateParent = getCurrentPath();
                if (nativeMode) {
                    ok &= owned(methodPath, "net.minecraft.server.management.PlayerChunkMap", "markBlockForUpdate(net.minecraft.util.math.BlockPos)")
                        && owned(getPath, "net.minecraft.world.WorldServer", "getPlayerChunkMap()");
                    if (coordinates.size() != 1 || !(coordinates.get(0) instanceof NewClassTree)) return super.visitMethodInvocation(call, unused);
                    NewClassTree position = (NewClassTree) coordinates.get(0);
                    coordinateParent = new TreePath(getCurrentPath(), position);
                    ok &= owned(coordinateParent, "net.minecraft.util.math.BlockPos", "BlockPos(int,int,int)");
                    coordinates = position.getArguments();
                }
                ok &= coordinates.size() == 3;
                final boolean[] resolved = {true};
                for (ExpressionTree argument : coordinates) {
                    TreePath path = new TreePath(coordinateParent, argument);
                    TypeMirror type = trees.getTypeMirror(path);
                    ok &= type != null && type.getKind() == TypeKind.INT;
                    new TreePathScanner<Void, Void>() {
                        public Void scan(Tree tree, Void value) {
                            if (tree instanceof ExpressionTree) {
                                TypeMirror type = trees.getTypeMirror(new TreePath(getCurrentPath(), tree));
                                if (type == null || type.getKind() == TypeKind.ERROR) resolved[0] = false;
                            }
                            return super.scan(tree, value);
                        }
                    }.scan(path, null);
                }
                if (ok && resolved[0]) System.out.println(Paths.get(unit.getSourceFile().toUri())
                    + "\t" + positions.getStartPosition(unit, call) + "\t" + positions.getEndPosition(unit, call)
                    + "\t" + receiver + "\t" + (nativeMode ? "resolved native getter/map/int constructor" : "legacy getter absent; typed exact WorldServer/int arguments"));
                return super.visitMethodInvocation(call, unused);
            }
        }.scan(unit, null);
        System.err.println("Attribution errors in unfinished source, not a successful full compilation: "
            + errors.getDiagnostics().stream().filter(e -> e.getKind() == Diagnostic.Kind.ERROR).count());
        files.close();
    }
}
