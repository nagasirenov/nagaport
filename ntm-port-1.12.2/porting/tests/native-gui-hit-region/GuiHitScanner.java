import java.nio.file.*;import java.util.*;import javax.tools.*;import javax.lang.model.element.*;import javax.lang.model.type.*;import com.sun.source.tree.*;import com.sun.source.util.*;
public class GuiHitScanner {
 public static void main(String[]a)throws Exception{
  JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> ds=new DiagnosticCollector<>();StandardJavaFileManager fm=c.getStandardFileManager(ds,null,null);
  JavacTask t=(JavacTask)c.getTask(null,fm,ds,Arrays.asList("-proc:none","-source","8","-Xmaxerrs","100000","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim()),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
  List<CompilationUnitTree> us=new ArrayList<>();for(CompilationUnitTree u:t.parse())us.add(u);t.analyze();Trees trees=Trees.instance(t);
  for(CompilationUnitTree u:us)new TreePathScanner<Void,Void>(){
   public Void visitMethodInvocation(MethodInvocationTree n,Void v){
    if(n.getMethodSelect() instanceof MemberSelectTree){MemberSelectTree s=(MemberSelectTree)n.getMethodSelect();String name=s.getIdentifier().toString();if(name.equals("func_146978_c")||name.equals("isPointInRegion")){
     TreePath select=new TreePath(getCurrentPath(),s);TreePath receiver=new TreePath(select,s.getExpression());TypeMirror ty=trees.getTypeMirror(receiver);Element target=null;
     if(ty!=null&&ty.getKind()==TypeKind.DECLARED){for(Element e:t.getElements().getAllMembers((TypeElement)((DeclaredType)ty).asElement()))if(e.getSimpleName().contentEquals("isPointInRegion")){if(target!=null)throw new AssertionError("overloaded target");target=e;}}
     if(target==null||!target.getEnclosingElement().toString().equals("net.minecraft.client.gui.inventory.GuiContainer")||!target.toString().equals("isPointInRegion(int,int,int,int,int,int)"))throw new AssertionError("wrong owner "+target);
     for(ExpressionTree arg:n.getArguments())if(!trees.getTypeMirror(new TreePath(getCurrentPath(),arg)).toString().equals("int"))throw new AssertionError("not int");
     if(name.equals("isPointInRegion")&&!target.equals(trees.getElement(select)))throw new AssertionError("wrong actual native dispatch");
     long start=trees.getSourcePositions().getStartPosition(u,n),end=trees.getSourcePositions().getEndPosition(u,n);System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+n+"\t"+target.getEnclosingElement());
    }}return super.visitMethodInvocation(n,v);
   }
  }.scan(u,null);
  System.err.println("Unfinished-tree attribution errors: "+ds.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
