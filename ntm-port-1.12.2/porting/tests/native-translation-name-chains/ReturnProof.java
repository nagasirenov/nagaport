import java.util.*;import javax.tools.*;import com.sun.source.tree.*;import com.sun.source.util.*;
public class ReturnProof {
 public static void main(String[] a)throws Exception {
 JavaCompiler c=ToolProvider.getSystemJavaCompiler();StandardJavaFileManager f=c.getStandardFileManager(null,null,null);JavacTask t=(JavacTask)c.getTask(null,f,null,Arrays.asList("-proc:none"),null,f.getJavaFileObjects(a[0]));final int[] found={0};
 for(CompilationUnitTree u:t.parse())new TreeScanner<Void,Void>() {public Void visitMethod(MethodTree n,Void v){if(n.getName().contentEquals("cloneStats")){
 List<? extends StatementTree>s=n.getBody().getStatements();if(s.size()!=15)throw new AssertionError(s.size());
 for(int i=0;i<14;i++){if(s.get(i).getKind()!=Tree.Kind.EXPRESSION_STATEMENT)throw new AssertionError("branch or early return");ExpressionTree e=((ExpressionStatementTree)s.get(i)).getExpression();if(i<13&&e.getKind()!=Tree.Kind.ASSIGNMENT)throw new AssertionError("nonassignment");if(i==13&&!e.toString().equals("this.setRadResist(original.radResist)"))throw new AssertionError("different call");}
 if(s.get(14).getKind()!=Tree.Kind.RETURN||!((ReturnTree)s.get(14)).getExpression().toString().equals("this"))throw new AssertionError("not same receiver");found[0]++;}
 return super.visitMethod(n,v);}}.scan(u,null);if(found[0]!=1)throw new AssertionError(found[0]);System.out.println("PASS exact cloneStats: 13 assignments, one ignored-result setRadResist invocation, terminal return this, no alternate return or control flow");
 }
}
