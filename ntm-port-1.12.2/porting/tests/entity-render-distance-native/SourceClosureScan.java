import com.sun.source.tree.*;
import com.sun.source.util.*;
import javax.tools.*;
import java.nio.file.*;
import java.util.*;
public final class SourceClosureScan {
 public static void main(String[] args)throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();StandardJavaFileManager fm=compiler.getStandardFileManager(null,null,null);
  List<String> paths=Files.readAllLines(Paths.get(args[0]));DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
  JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none"),null,fm.getJavaFileObjectsFromStrings(paths));
  Trees trees=Trees.instance(task);SourcePositions positions=trees.getSourcePositions();List<String> rows=new ArrayList<>();
  for(CompilationUnitTree cu:task.parse())new TreePathScanner<Void,Void>() {
   String owner="",method="";String file=Paths.get(cu.getSourceFile().toUri()).toString();
   long line(Tree tree){return cu.getLineMap().getLineNumber(positions.getStartPosition(cu,tree));}
   public Void visitClass(ClassTree t,Void v){String previous=owner;owner=t.getSimpleName().toString();
    if(t.getExtendsClause()!=null)rows.add("CLASS\t"+owner+"\t"+t.getExtendsClause()+"\t"+file);
    super.visitClass(t,v);owner=previous;return null;}
   public Void visitMethod(MethodTree t,Void v){String previous=method;method=t.getName().toString();
    if(method.equals("isInRangeToRenderDist"))rows.add("RANGE\t"+owner+"\t"+file+"\t"+line(t));
    super.visitMethod(t,v);method=previous;return null;}
   public Void visitMemberSelect(MemberSelectTree t,Void v){if(t.getIdentifier().contentEquals("renderDistanceWeight"))rows.add("ACCESS\t"+owner+"\t"+method+"\t"+getCurrentPath().getParentPath().getLeaf().getKind()+"\t"+file+"\t"+line(t)+"\t"+t);return super.visitMemberSelect(t,v);}
   public Void visitVariable(VariableTree t,Void v){if(t.getName().contentEquals("renderDistanceWeight"))rows.add("DECL\t"+owner+"\t"+file+"\t"+line(t));return super.visitVariable(t,v);}
  }.scan(cu,null);
  for(Diagnostic<?> d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError(d.toString());
  Collections.sort(rows);for(String row:rows)System.out.println(row);
 }
}
