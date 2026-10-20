import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Select only discarded simple assignments to the genuine native ItemStack field. */
public final class CountAssignmentScanner {
 public static void main(String[] args)throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler(); DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  final Trees trees=Trees.instance(task); final SourcePositions p=trees.getSourcePositions();
  List<CompilationUnitTree> units=new ArrayList<>(); for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("Parse failure "+d);
  task.analyze();
  for(final CompilationUnitTree u:units) {
   final String source=u.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>() {
    public Void visitAssignment(AssignmentTree a,Void ignored) {
     if(getCurrentPath().getParentPath().getLeaf().getKind()==Tree.Kind.EXPRESSION_STATEMENT && a.getVariable() instanceof MemberSelectTree) {
      MemberSelectTree field=(MemberSelectTree)a.getVariable(); TreePath fp=new TreePath(getCurrentPath(),field);
      Element symbol=trees.getElement(fp); TypeMirror receiver=trees.getTypeMirror(new TreePath(fp,field.getExpression()));
      if(field.getIdentifier().contentEquals("stackSize") && symbol!=null && symbol.getKind()==ElementKind.FIELD && symbol.getEnclosingElement() instanceof TypeElement && ((TypeElement)symbol.getEnclosingElement()).getQualifiedName().contentEquals("net.minecraft.item.ItemStack") && receiver!=null && receiver.getKind()==TypeKind.DECLARED && receiver.toString().equals("net.minecraft.item.ItemStack") && !(trees.getElement(new TreePath(fp,field.getExpression())) instanceof TypeElement)) {
       long s=p.getStartPosition(u,a),e=p.getEndPosition(u,a),rs=p.getStartPosition(u,field.getExpression()),re=p.getEndPosition(u,field.getExpression()),vs=p.getStartPosition(u,a.getExpression()),ve=p.getEndPosition(u,a.getExpression());
       if(s>=0 && e<=source.length() && re>=rs && vs>=re && ve==e && source.substring((int)re,(int)p.getEndPosition(u,field)).equals(".stackSize") && source.substring((int)p.getEndPosition(u,field),(int)vs).matches("\\s*=\\s*"))
        System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+s+"\t"+e+"\t"+rs+"\t"+re+"\t"+vs+"\t"+ve+"\t"+symbol.getEnclosingElement()+"\t"+receiver);
      }
     }
     return super.visitAssignment(a,ignored);
    }
   }.scan(u,null);
  }
  System.err.println("Expected unfinished-source attribution errors: "+diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count()); fm.close();
 }
}
