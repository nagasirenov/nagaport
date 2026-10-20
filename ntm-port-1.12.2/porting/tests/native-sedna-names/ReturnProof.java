import java.util.*;import javax.tools.*;import com.sun.source.tree.*;import com.sun.source.util.*;
public class ReturnProof {
 public static void main(String[] a)throws Exception {
 JavaCompiler c=ToolProvider.getSystemJavaCompiler();StandardJavaFileManager f=c.getStandardFileManager(null,null,null);JavacTask t=(JavacTask)c.getTask(null,f,null,Arrays.asList("-proc:none"),null,f.getJavaFileObjects(a[0]));final Set<String> found=new HashSet<>();
 final Map<String,String> assignments=new HashMap<>();assignments.put("setDefaultAmmo","this.defaultAmmo = new ItemStack(ModItems.ammo_standard, amount, ammo.ordinal())");assignments.put("setDefaultAmmoExpensive","this.isDefaultExpensive = true");assignments.put("setNameMutator","this.LAMBDA_NAME_MUTATOR = lambda");
 for(CompilationUnitTree u:t.parse())new TreeScanner<Void,Void>() {public Void visitMethod(MethodTree n,Void v){String name=n.getName().toString();if(assignments.containsKey(name)){
 List<? extends StatementTree>s=n.getBody().getStatements();if(s.size()!=2)throw new AssertionError("branch or extra effect: "+name);
 if(s.get(0).getKind()!=Tree.Kind.EXPRESSION_STATEMENT)throw new AssertionError(name);ExpressionTree e=((ExpressionStatementTree)s.get(0)).getExpression();if(e.getKind()!=Tree.Kind.ASSIGNMENT||!e.toString().equals(assignments.get(name)))throw new AssertionError("changed assignment "+e);
 if(s.get(1).getKind()!=Tree.Kind.RETURN)throw new AssertionError(name);String expected=name.equals("setDefaultAmmoExpensive")?"setDefaultAmmo(ammo, amount)":"this";if(!((ReturnTree)s.get(1)).getExpression().toString().equals(expected))throw new AssertionError("changed receiver return");if(!found.add(name))throw new AssertionError("duplicate method");}
 return super.visitMethod(n,v);}}.scan(u,null);if(found.size()!=3)throw new AssertionError(found);System.out.println("PASS three exact two-statement builders: unchanged assignment then same receiver return, expensive branch delegates to proved setDefaultAmmo, no alternate control flow");
 }
}
