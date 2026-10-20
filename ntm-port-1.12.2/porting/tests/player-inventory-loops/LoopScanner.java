import java.nio.file.*;import java.util.*;import javax.tools.*;import javax.lang.model.element.*;import javax.lang.model.type.*;import com.sun.source.tree.*;import com.sun.source.util.*;
/** Attributes only direct native InventoryPlayer list foreach sources. */
public class LoopScanner {
 public static void main(String[] args)throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject>d=new DiagnosticCollector<>();StandardJavaFileManager fm=compiler.getStandardFileManager(d,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,d,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  final Trees trees=Trees.instance(task);final SourcePositions pos=trees.getSourcePositions();List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);for(Diagnostic<?> x:d.getDiagnostics())if(x.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError(x);task.analyze();
  for(final CompilationUnitTree u:units)new TreePathScanner<Void,Void>(){
   public Void visitEnhancedForLoop(EnhancedForLoopTree loop,Void unused){
    if(loop.getExpression() instanceof MemberSelectTree && loop.getStatement() instanceof BlockTree){
     MemberSelectTree field=(MemberSelectTree)loop.getExpression();TreePath fp=new TreePath(getCurrentPath(),field);Element e=trees.getElement(fp);TypeMirror receiver=trees.getTypeMirror(new TreePath(fp,field.getExpression()));TypeMirror variable=trees.getTypeMirror(new TreePath(getCurrentPath(),loop.getVariable()));
     if(e!=null && e.getKind()==ElementKind.FIELD && e.getEnclosingElement().toString().equals("net.minecraft.entity.player.InventoryPlayer") && (field.getIdentifier().contentEquals("mainInventory")||field.getIdentifier().contentEquals("armorInventory")) && receiver!=null && receiver.toString().equals("net.minecraft.entity.player.InventoryPlayer") && !(trees.getElement(new TreePath(fp,field.getExpression())) instanceof TypeElement) && variable!=null && variable.toString().equals("net.minecraft.item.ItemStack"))
      System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+pos.getStartPosition(u,loop)+"\t"+pos.getStartPosition(u,loop.getStatement())+"\t"+pos.getEndPosition(u,loop)+"\t"+loop.getVariable().getName()+"\t"+field.getIdentifier());
    }return super.visitEnhancedForLoop(loop,unused);
   }
  }.scan(u,null);
  System.err.println("Unfinished source attribution diagnostics: "+d.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
