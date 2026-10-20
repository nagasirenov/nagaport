import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** Compiler-attributed, statement-only native payload migration. No textual discovery. */
public final class PayloadScanner {
    static final String PREFIX = "net.minecraftforge.event.entity.living.";
    static final List<String> OWNERS = Arrays.asList(PREFIX + "LivingHurtEvent", PREFIX + "LivingAttackEvent");
    static final Set<String> METHODS = new HashSet<>(Arrays.asList("getAmount", "getSource", "setAmount"));
    static String b64(String value) { return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8)); }
    public static void main(String[] args) throws Exception {
        final boolean post = args.length > 2 && args[2].equals("post");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null, null);
        JavacTask task = (JavacTask) compiler.getTask(null, files, diagnostics,
                Arrays.asList("-proc:none", "-source", "8", "-classpath", new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8).trim(), "-Xmaxerrs", "100000"),
                null, files.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
        Trees trees = Trees.instance(task);
        SourcePositions positions = trees.getSourcePositions();
        List<CompilationUnitTree> units = new ArrayList<>();
        for (CompilationUnitTree unit : task.parse()) units.add(unit);
        for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics())
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) throw new AssertionError("parse failure: " + diagnostic);
        task.analyze();
        Types types = task.getTypes();
        final Set<String> overridden = new HashSet<>();
        final Set<String> sourceDescendants = new TreeSet<>();
        for (CompilationUnitTree unit : units) new TreePathScanner<Void, Void>() {
            @Override public Void visitClass(ClassTree declaration, Void unused) {
                Element e = trees.getElement(getCurrentPath());
                if (e instanceof TypeElement)
                    for (String owner : OWNERS) {
                        TypeElement root = task.getElements().getTypeElement(owner);
                        if (root != null && types.isSubtype(types.erasure(e.asType()), types.erasure(root.asType())))
                            sourceDescendants.add(((TypeElement)e).getQualifiedName().toString());
                    }
                return super.visitClass(declaration, unused);
            }
            @Override public Void visitMethod(MethodTree method, Void unused) {
                if (METHODS.contains(method.getName().toString())) {
                    Element e = trees.getElement(getCurrentPath());
                    if (e != null && e.getEnclosingElement() instanceof TypeElement)
                        for (String owner : OWNERS) {
                            TypeElement root = task.getElements().getTypeElement(owner);
                            if (root != null && types.isSubtype(types.erasure(e.getEnclosingElement().asType()), types.erasure(root.asType()))) overridden.add(owner);
                        }
                }
                return super.visitMethod(method, unused);
            }
        }.scan(unit, null);
        for (String owner : OWNERS) {
            TypeElement type = task.getElements().getTypeElement(owner);
            if (type == null) throw new AssertionError("missing native owner " + owner);
            int fieldCount = 0, methodCount = 0;
            boolean hurt = owner.endsWith("LivingHurtEvent");
            for (Element member : type.getEnclosedElements()) {
                String name = member.getSimpleName().toString();
                if (member.getKind() == ElementKind.FIELD && (name.equals("amount") || name.equals("source"))) {
                    String expected = name.equals("amount") ? "float" : "net.minecraft.util.DamageSource";
                    if (!member.asType().toString().equals(expected) || !member.getModifiers().contains(Modifier.PRIVATE)
                            || member.getModifiers().contains(Modifier.FINAL) != (!hurt || name.equals("source"))) throw new AssertionError("unexpected native payload " + member);
                    fieldCount++;
                }
                if (member.getKind() == ElementKind.METHOD && METHODS.contains(name)) {
                    ExecutableElement method = (ExecutableElement) member;
                    if (!method.getModifiers().contains(Modifier.PUBLIC) || method.getModifiers().contains(Modifier.STATIC)) throw new AssertionError("not native instance accessor");
                    if (name.equals("setAmount")) {
                        if (!hurt || !method.getReturnType().toString().equals("void") || method.getParameters().size() != 1 || !method.getParameters().get(0).asType().toString().equals("float")) throw new AssertionError("unexpected setter signature");
                    } else if (!method.getParameters().isEmpty() || !method.getReturnType().toString().equals(name.equals("getAmount") ? "float" : "net.minecraft.util.DamageSource")) throw new AssertionError("unexpected getter signature");
                    methodCount++;
                }
            }
            if (fieldCount != 2 || methodCount != (hurt ? 3 : 2)) throw new AssertionError("unexpected native shape " + owner);
        }
        for (CompilationUnitTree unit : units) {
            final String source = unit.getSourceFile().getCharContent(true).toString();
            final String path = Paths.get(unit.getSourceFile().toUri()).toString();
            new TreePathScanner<Void, Void>() {
                String method() {
                    for (TreePath p = getCurrentPath(); p != null; p = p.getParentPath())
                        if (p.getLeaf() instanceof MethodTree) return ((MethodTree)p.getLeaf()).getName().toString();
                    return "";
                }
                String owner(TreePath receiverPath) {
                    if (!(receiverPath.getLeaf() instanceof IdentifierTree)) return null;
                    Element receiver = trees.getElement(receiverPath);
                    TypeMirror type = trees.getTypeMirror(receiverPath);
                    if (receiver == null || receiver.getKind() != ElementKind.PARAMETER || type == null || type.getKind() != TypeKind.DECLARED) return null;
                    String owner = ((TypeElement)((DeclaredType)type).asElement()).getQualifiedName().toString();
                    return OWNERS.contains(owner) && !overridden.contains(owner) ? owner : null;
                }
                void emit(long start, long end, String replacement, String kind, String owner, String receiver, long group) {
                    if (start < 0 || end < start || end > source.length()) throw new AssertionError("invalid source span");
                    System.out.println(path + "\t" + start + "\t" + end + "\t" + b64(replacement) + "\t" + kind + "\t" + owner + "\t" + receiver + "\t" + method() + "\t" + b64(source.substring((int)start, (int)end)) + "\t" + group);
                }
                @Override public Void visitImport(ImportTree tree, Void unused) { return null; }
                @Override public Void visitMethodInvocation(MethodInvocationTree tree, Void unused) {
                    if (post && tree.getMethodSelect() instanceof MemberSelectTree) {
                        MemberSelectTree select = (MemberSelectTree)tree.getMethodSelect();
                        String name = select.getIdentifier().toString();
                        String owner = owner(new TreePath(new TreePath(getCurrentPath(), select), select.getExpression()));
                        Element e = trees.getElement(getCurrentPath());
                        if (owner != null && METHODS.contains(name) && e != null && e.getKind() == ElementKind.METHOD && e.getEnclosingElement().toString().equals(owner)) {
                            long end = positions.getEndPosition(unit, select);
                            emit(end - name.length(), end, name, "NATIVE_CALL", owner, select.getExpression().toString(), -1);
                        }
                    }
                    return super.visitMethodInvocation(tree, unused);
                }
                @Override public Void visitMemberSelect(MemberSelectTree tree, Void unused) {
                    if (post) return super.visitMemberSelect(tree, unused);
                    String field = tree.getIdentifier().toString();
                    if (!field.equals("ammount") && !field.equals("source")) return super.visitMemberSelect(tree, unused);
                    String owner = owner(new TreePath(getCurrentPath(), tree.getExpression()));
                    if (owner == null) return super.visitMemberSelect(tree, unused);
                    String receiver = tree.getExpression().toString();
                    Tree written = tree;
                    TreePath parentPath = getCurrentPath().getParentPath();
                    while (parentPath != null && parentPath.getLeaf() instanceof ParenthesizedTree) { written = parentPath.getLeaf(); parentPath = parentPath.getParentPath(); }
                    Tree parent = parentPath == null ? null : parentPath.getLeaf();
                    boolean simple = parent instanceof AssignmentTree && ((AssignmentTree)parent).getVariable() == written;
                    boolean compound = parent instanceof CompoundAssignmentTree && ((CompoundAssignmentTree)parent).getVariable() == written;
                    if (simple || compound) {
                        if (!owner.endsWith("LivingHurtEvent") || !field.equals("ammount") || written != tree || !(parentPath.getParentPath().getLeaf() instanceof ExpressionStatementTree)) return super.visitMemberSelect(tree, unused);
                        ExpressionTree rhs = simple ? ((AssignmentTree)parent).getExpression() : ((CompoundAssignmentTree)parent).getExpression();
                        String operator = simple ? "=" : parent.getKind() == Tree.Kind.MULTIPLY_ASSIGNMENT ? "*=" : parent.getKind() == Tree.Kind.MINUS_ASSIGNMENT ? "-=" : null;
                        if (operator == null) return super.visitMemberSelect(tree, unused);
                        long start = positions.getStartPosition(unit, parent), end = positions.getEndPosition(unit, parent), rhsStart = positions.getStartPosition(unit, rhs);
                        // Validate the already attributed prefix, preserving all comments/trivia outside the approved spans.
                        if (!source.substring((int)start, (int)rhsStart).replaceAll("\\s+", "").equals(receiver + ".ammount" + operator)) return super.visitMemberSelect(tree, unused);
                        String replacement = simple ? receiver + ".setAmount(" : receiver + ".setAmount((float) (" + receiver + ".getAmount() " + operator.charAt(0) + " (";
                        emit(start, rhsStart, replacement, simple ? "SET" : operator.equals("*=") ? "MULTIPLY" : "SUBTRACT", owner, receiver, start);
                        emit(end, end, simple ? ")" : ")))", "CLOSE", owner, receiver, start);
                        return super.visitMemberSelect(tree, unused);
                    }
                    if (parent instanceof UnaryTree) {
                        Tree.Kind kind = parent.getKind();
                        if (kind == Tree.Kind.PREFIX_INCREMENT || kind == Tree.Kind.PREFIX_DECREMENT || kind == Tree.Kind.POSTFIX_INCREMENT || kind == Tree.Kind.POSTFIX_DECREMENT) return super.visitMemberSelect(tree, unused);
                    }
                    long end = positions.getEndPosition(unit, tree), start = end - field.length();
                    if (!source.substring((int)start, (int)end).equals(field)) throw new AssertionError("field token mismatch");
                    emit(start, end, field.equals("ammount") ? "getAmount()" : "getSource()", field.equals("ammount") ? "READ_AMOUNT" : "READ_SOURCE", owner, receiver, -1);
                    return super.visitMemberSelect(tree, unused);
                }
            }.scan(unit, null);
        }
        System.err.println("Source event descendant declarations: " + sourceDescendants.size() + " " + sourceDescendants);
        if (!overridden.isEmpty()) System.err.println("Excluded source override closures: " + overridden);
        System.err.println("Attribution diagnostics (unfinished source, not compile success): " + diagnostics.getDiagnostics().stream().filter(e -> e.getKind() == Diagnostic.Kind.ERROR).count());
        files.close();
    }
}
