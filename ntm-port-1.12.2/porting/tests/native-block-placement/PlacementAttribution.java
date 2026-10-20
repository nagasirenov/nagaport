import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Full-source parse/analyze only, not bytecode compilation or project build. */
public final class PlacementAttribution {
 public static void main(String[] args)throws Exception {
  Path root=Paths.get(args[0]);List<java.io.File> files=new ArrayList<>();Files.walk(root.resolve("src/main/java")).filter(p->p.toString().endsWith(".java")).sorted().forEach(p->files.add(p.toFile()));
  JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();StandardJavaFileManager manager=c.getStandardFileManager(diagnostics,null,StandardCharsets.UTF_8);
  JavacTask task=(JavacTask)c.getTask(null,manager,diagnostics,Arrays.asList("-proc:none","-implicit:none","-sourcepath",args[2],"-classpath",args[1],"-Xmaxerrs","100000"),null,manager.getJavaFileObjectsFromFiles(files));
  List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);task.analyze();Trees trees=Trees.instance(task);TypeElement block=task.getElements().getTypeElement("net.minecraft.block.Block");
  for(CompilationUnitTree u:units)new TreePathScanner<Void,Void>() {
   String owner="";
   public Void visitClass(ClassTree tree,Void ignored){String save=owner;Element e=trees.getElement(getCurrentPath());owner=e==null?tree.getSimpleName().toString():e.toString();super.visitClass(tree,ignored);owner=save;return null;}
   public Void visitMethod(MethodTree tree,Void ignored){
    if(tree.getName().contentEquals("canPlaceBlockAt")){
     Element e=trees.getElement(getCurrentPath());boolean nativeOverride=false;
     if(e instanceof ExecutableElement)for(Element parent:block.getEnclosedElements())if(parent instanceof ExecutableElement&&parent.getSimpleName().contentEquals("canPlaceBlockAt"))nativeOverride|=task.getElements().overrides((ExecutableElement)e,(ExecutableElement)parent,(TypeElement)e.getEnclosingElement());
     System.out.println("METHOD\t"+root.relativize(Paths.get(u.getSourceFile().toUri()))+"\t"+owner+"\t"+e+"\t"+nativeOverride);
    }return super.visitMethod(tree,ignored);
   }
   public Void visitMethodInvocation(MethodInvocationTree tree,Void ignored){
    ExpressionTree sel=tree.getMethodSelect();String name=sel instanceof MemberSelectTree?((MemberSelectTree)sel).getIdentifier().toString():sel instanceof IdentifierTree?((IdentifierTree)sel).getName().toString():"";
    if(name.equals("canPlaceBlockAt")){
     Element e=trees.getElement(new TreePath(getCurrentPath(),sel));String receiver="this:"+owner;
     if(sel instanceof MemberSelectTree)receiver=String.valueOf(trees.getTypeMirror(new TreePath(new TreePath(getCurrentPath(),sel),((MemberSelectTree)sel).getExpression())));
     StringJoiner types=new StringJoiner(",");for(ExpressionTree a:tree.getArguments())types.add(String.valueOf(trees.getTypeMirror(new TreePath(getCurrentPath(),a))));
     long start=trees.getSourcePositions().getStartPosition(u,tree);
     System.out.println("CALL\t"+root.relativize(Paths.get(u.getSourceFile().toUri()))+"\t"+u.getLineMap().getLineNumber(start)+"\t"+tree.getArguments().size()+"\t"+receiver+"\t"+types+"\t"+(e==null?"null":e.getEnclosingElement())+"\t"+e);
    }return super.visitMethodInvocation(tree,ignored);
   }
  }.scan(u,null);
  Map<String,Integer> counts=new TreeMap<>();for(Diagnostic<?> d:diagnostics.getDiagnostics())counts.put(d.getCode(),counts.getOrDefault(d.getCode(),0)+1);
  System.err.println("FULL_SOURCE_ATTRIBUTION_UNITS="+files.size()+"; DIAGNOSTICS="+diagnostics.getDiagnostics().size()+"; "+counts+"; analyze only, not successful compilation");manager.close();
 }
}
