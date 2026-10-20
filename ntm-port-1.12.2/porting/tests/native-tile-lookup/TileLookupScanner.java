import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Fail-closed attribution of exact native World receivers and integer coordinates. */
public final class TileLookupScanner {
 public static void main(String[] args)throws Exception {
  final boolean nativeMode=args.length>2;
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(errors,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  final Trees trees=Trees.instance(task); final SourcePositions positions=trees.getSourcePositions();
  List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> e:errors.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse failure: "+e);
  task.analyze();
  TypeElement world=task.getElements().getTypeElement("net.minecraft.world.World");
  int overloads=0;
  for(Element e:task.getElements().getAllMembers(world))if(e.getKind()==ElementKind.METHOD&&e.getSimpleName().contentEquals("getTileEntity")) {
   ExecutableElement m=(ExecutableElement)e; if(m.getParameters().size()!=1||!m.getParameters().get(0).asType().toString().equals("net.minecraft.util.math.BlockPos"))throw new AssertionError("Unexpected native overload: "+m);overloads++;
  }
  if(overloads!=1)throw new AssertionError("Native overload count: "+overloads);
  for(final CompilationUnitTree unit:units)new TreePathScanner<Void,Void>() {
   public Void visitMethodInvocation(MethodInvocationTree t,Void v) {
    List<? extends ExpressionTree> coordinates=t.getArguments();
    NewClassTree position=null;
    if(nativeMode&&coordinates.size()==1&&coordinates.get(0) instanceof NewClassTree) {position=(NewClassTree)coordinates.get(0);coordinates=position.getArguments();}
    if(t.getMethodSelect() instanceof MemberSelectTree&&coordinates.size()==3&&(!nativeMode||position!=null)) {
     MemberSelectTree m=(MemberSelectTree)t.getMethodSelect();
     if(m.getIdentifier().contentEquals("getTileEntity")) {
      TreePath mp=new TreePath(getCurrentPath(),m);TypeMirror r=trees.getTypeMirror(new TreePath(mp,m.getExpression()));
      boolean ok=r!=null&&r.getKind()==TypeKind.DECLARED&&r.toString().equals("net.minecraft.world.World");
      Element receiverElement=trees.getElement(new TreePath(mp,m.getExpression()));
      ok &= receiverElement instanceof VariableElement;
      List<String> types=new ArrayList<>();for(ExpressionTree a:coordinates){TypeMirror ty=trees.getTypeMirror(new TreePath(getCurrentPath(),a));types.add(String.valueOf(ty));ok &= ty!=null&&ty.getKind()==TypeKind.INT;}
      // Reject erroneous descendants even when javac recovers an outer arithmetic result as int.
      final boolean[] resolved={true};
      for(ExpressionTree a:coordinates)new TreePathScanner<Void,Void>() {
       public Void scan(Tree tree,Void value) {
        if(tree instanceof ExpressionTree) { TreePath path=new TreePath(getCurrentPath(),tree); TypeMirror ty=trees.getTypeMirror(path); if(ty==null||ty.getKind()==TypeKind.ERROR)resolved[0]=false; }
        return super.scan(tree,value);
       }
      }.scan(new TreePath(getCurrentPath(),a),null);
      ok &= resolved[0];
      if(nativeMode) {
       Element selected=trees.getElement(mp);Element ctor=trees.getElement(new TreePath(getCurrentPath(),position));
       ok &= selected instanceof ExecutableElement&&selected.getEnclosingElement().toString().equals("net.minecraft.world.World");
       ok &= ctor instanceof ExecutableElement&&ctor.getEnclosingElement().toString().equals("net.minecraft.util.math.BlockPos")&&ctor.toString().equals("BlockPos(int,int,int)");
      }
      if(ok) {long start=positions.getStartPosition(unit,t),end=positions.getEndPosition(unit,t),a=positions.getStartPosition(unit,coordinates.get(0)),b=positions.getEndPosition(unit,coordinates.get(2));
       System.out.println(Paths.get(unit.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+a+"\t"+b+"\t"+r+"\t"+String.join(",",types));
      }
     }
    }
    return super.visitMethodInvocation(t,v);
   }
  }.scan(unit,null);
  System.err.println("Attribution diagnostics, not compile success: "+errors.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
