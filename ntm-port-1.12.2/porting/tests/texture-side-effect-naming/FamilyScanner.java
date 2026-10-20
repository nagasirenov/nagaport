import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** Attributes the whole actual source graph; compiler errors are reported, not waived. */
public final class FamilyScanner {
 static final String P = "com.hbm.items.tool.";
 static final List<String> ROOTS = Arrays.asList("ItemBlowtorch", "ItemBoltgun", "ItemModMinecart", "ItemCraftingDegradation");
 public static void main(String[] args) throws Exception {
  JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
  DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
  StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null, null);
  JavacTask task = (JavacTask) compiler.getTask(null, files, diagnostics, Arrays.asList("-proc:none", "-source", "8", "-classpath", new String(Files.readAllBytes(Paths.get(args[1]))).trim(), "-Xmaxerrs", "100000"), null, files.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  Trees trees = Trees.instance(task);
  List<CompilationUnitTree> units = new ArrayList<>();
  for (CompilationUnitTree unit : task.parse()) units.add(unit);
  task.analyze();
  for (CompilationUnitTree unit : units) new TreePathScanner<Void,Void>() {
   Element element(Tree tree) { return trees.getElement(TreePath.getPath(unit, tree)); }
   boolean family(TypeMirror type) {
    if (type == null || type.getKind() != TypeKind.DECLARED) return false;
    for (String root : ROOTS) if (task.getTypes().isSubtype(type, task.getElements().getTypeElement(P + root).asType())) return true;
    return false;
   }
   String file() { String path = unit.getSourceFile().toUri().getPath(); return path.substring(path.indexOf("/src/main/java/") + 1); }
   String source(Tree tree) {
    long start = trees.getSourcePositions().getStartPosition(unit, tree), end = trees.getSourcePositions().getEndPosition(unit, tree);
    try { return unit.getSourceFile().getCharContent(true).subSequence((int)start, (int)end).toString().replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t"); }
    catch (Exception ex) { throw new RuntimeException(ex); }
   }
   String executable(Tree tree) {
    Element e = element(tree);
    return e instanceof ExecutableElement ? e.getEnclosingElement() + "#" + e + ":" + ((ExecutableElement)e).getReturnType() : "UNRESOLVED";
   }
   public Void visitClass(ClassTree node, Void unused) {
    Element e = element(node);
    if (e instanceof TypeElement && family(e.asType())) System.out.println("CLASS\t" + e + "\t" + ((TypeElement)e).getSuperclass() + "\t" + file());
    return super.visitClass(node, unused);
   }
   public Void visitMethod(MethodTree node, Void unused) {
    Element e = element(node);
    if (e instanceof ExecutableElement && family(e.getEnclosingElement().asType()) && (node.getName().toString().contains("Name") || node.getName().toString().contains("TranslationKey") || node.getName().toString().contains("Icon")))
     System.out.println("DECL\t" + executable(node) + "\t" + file() + "\t" + source(node));
    return super.visitMethod(node, unused);
   }
   public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
    if (node.getMethodSelect() instanceof MemberSelectTree) {
     MemberSelectTree select = (MemberSelectTree)node.getMethodSelect();
     String name = select.getIdentifier().toString();
     ExpressionTree receiver = select.getExpression();
     TypeMirror type = trees.getTypeMirror(TreePath.getPath(unit, receiver));
     TreePath enclosing = getCurrentPath();
     while (enclosing != null && !(enclosing.getLeaf() instanceof ClassTree)) enclosing = enclosing.getParentPath();
     Element owner = enclosing == null ? null : trees.getElement(enclosing);
     boolean familySuper = receiver.toString().equals("super") && owner != null && family(owner.asType());
     if ((family(type) || familySuper) && (name.equals("setUnlocalizedName") || name.equals("setTranslationKey") || name.equals("setTextureName") || name.equals("getIconString")))
      System.out.println("CALL\t" + file() + "\t" + unit.getLineMap().getLineNumber(trees.getSourcePositions().getStartPosition(unit, node)) + "\t" + type + "\t" + executable(node) + "\t" + source(receiver) + "\t" + source(node));
    }
    return super.visitMethodInvocation(node, unused);
   }
  }.scan(unit, null);
  System.err.println("Sources=" + units.size() + "; native javac error diagnostics=" + diagnostics.getDiagnostics().stream().filter(d -> d.getKind() == Diagnostic.Kind.ERROR).count() + "; attribution only, not build success");
 }
}
