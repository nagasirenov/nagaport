import java.nio.file.*;import java.util.*;import javax.tools.*;import javax.lang.model.element.*;import javax.lang.model.type.*;import com.sun.source.tree.*;import com.sun.source.util.*;
public class WorldAirScanner {
 public static void main(String[]a)throws Exception{
  JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> ds=new DiagnosticCollector<>();StandardJavaFileManager fm=c.getStandardFileManager(ds,null,null);
  JavacTask t=(JavacTask)c.getTask(null,fm,ds,Arrays.asList("-proc:none","-source","8","-Xmaxerrs","100000","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim()),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
  List<CompilationUnitTree> us=new ArrayList<>();for(CompilationUnitTree u:t.parse())us.add(u);t.analyze();Trees trees=Trees.instance(t);
  for(CompilationUnitTree u:us)new TreePathScanner<Void,Void>(){
   public Void visitMethodInvocation(MethodInvocationTree n,Void v){
    if(n.getMethodSelect() instanceof MemberSelectTree){MemberSelectTree s=(MemberSelectTree)n.getMethodSelect();String name=s.getIdentifier().toString();if(name.equals("isAirBlock")&&((a.length<3&&n.getArguments().size()==3)||(a.length==3&&n.getArguments().size()==1&&n.getArguments().get(0).toString().startsWith("new net.minecraft.util.math.BlockPos(")))){
     TreePath select=new TreePath(getCurrentPath(),s);TypeMirror ty=trees.getTypeMirror(new TreePath(select,s.getExpression()));Element target=null;
     if(ty==null||!ty.toString().equals("net.minecraft.world.World"))throw new AssertionError("unexpected receiver "+ty);
     for(Element e:t.getElements().getAllMembers((TypeElement)((DeclaredType)ty).asElement()))if(e.getSimpleName().contentEquals("isAirBlock")){if(target!=null)throw new AssertionError("overloaded target");target=e;}
     if(target==null||!target.getEnclosingElement().toString().equals("net.minecraft.world.World")||!target.toString().equals("isAirBlock(net.minecraft.util.math.BlockPos)"))throw new AssertionError("wrong owner "+target);
     if(a.length<3){for(ExpressionTree arg:n.getArguments())if(!trees.getTypeMirror(new TreePath(getCurrentPath(),arg)).toString().equals("int"))throw new AssertionError("not int");}else{if(!target.equals(trees.getElement(select)))throw new AssertionError("not actual native target");NewClassTree pos=(NewClassTree)n.getArguments().get(0);Element ctor=trees.getElement(new TreePath(getCurrentPath(),pos));if(ctor==null||!ctor.toString().equals("BlockPos(int,int,int)"))throw new AssertionError("wrong constructor "+ctor);}
     long start=trees.getSourcePositions().getStartPosition(u,n.getArguments().get(0)),end=trees.getSourcePositions().getEndPosition(u,n.getArguments().get(n.getArguments().size()-1));
     System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+n+"\t"+target.getEnclosingElement());
    }}return super.visitMethodInvocation(n,v);
   }
  }.scan(u,null);
  System.err.println("Unfinished-tree attribution errors: "+ds.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
