import java.nio.file.*;import java.util.*;import javax.tools.*;import javax.lang.model.element.*;import com.sun.source.tree.*;import com.sun.source.util.*;
public class SubtypeDestinationScanner {
 public static void main(String[] a)throws Exception {
 JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject>d=new DiagnosticCollector<>();StandardJavaFileManager f=c.getStandardFileManager(d,null,null);JavacTask t=(JavacTask)c.getTask(null,f,d,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,f.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));Trees trees=Trees.instance(t);List<CompilationUnitTree>units=new ArrayList<>();for(CompilationUnitTree u:t.parse())units.add(u);t.analyze();
 for(CompilationUnitTree u:units)if(u.getSourceFile().getName().endsWith("/ModItemsArmor.java"))new TreePathScanner<Void,Void>(){
 String desc(Tree n){TreePath p=TreePath.getPath(u,n);Element e=trees.getElement(p);return n.getKind()+":"+trees.getTypeMirror(p)+":"+(e==null?"null":e.getEnclosingElement()+"#"+e);}
 public Void visitMethodInvocation(MethodInvocationTree n,Void v){if(n.getMethodSelect() instanceof MemberSelectTree){MemberSelectTree ms=(MemberSelectTree)n.getMethodSelect();if(ms.getIdentifier().contentEquals("setTranslationKey")||ms.getIdentifier().contentEquals("setUnlocalizedName")) {
 String recv=ms.getExpression().toString();if(ms.getExpression() instanceof MethodInvocationTree && ((MethodInvocationTree)ms.getExpression()).getMethodSelect() instanceof MemberSelectTree && ((MemberSelectTree)((MethodInvocationTree)ms.getExpression()).getMethodSelect()).getIdentifier().contentEquals("cloneStats") && ((MemberSelectTree)((MethodInvocationTree)ms.getExpression()).getMethodSelect()).getExpression() instanceof NewClassTree && !recv.startsWith("new ArmorFSB(")){
 System.out.println("CALL\t"+n.getArguments()+"\t"+desc(n));
 new TreeScanner<Void,Void>(){public Void scan(Tree x,Void q){if(x!=null)System.out.println("NODE\t"+x.toString().replace("\n","\\n")+"\t"+desc(x));return super.scan(x,q);}}.scan(ms.getExpression(),null);
 for(ExpressionTree arg:n.getArguments())System.out.println("ARG\t"+arg+"\t"+desc(arg));
 }}}return super.visitMethodInvocation(n,v);}
 }.scan(u,null);System.err.println("Independent AST errors: "+d.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).count());
 }
}
