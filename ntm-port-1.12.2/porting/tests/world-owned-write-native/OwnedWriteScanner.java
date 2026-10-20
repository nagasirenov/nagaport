import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Fail-closed attribution: native World receiver and exact vanilla Blocks fields. */
public final class OwnedWriteScanner {
 public static void main(String[] args) throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler(); DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(errors,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  final Trees trees=Trees.instance(task); final SourcePositions pos=trees.getSourcePositions();
  List<CompilationUnitTree> units=new ArrayList<>(); for(CompilationUnitTree u:task.parse()) units.add(u);
  for(Diagnostic<?> d:errors.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse failure: "+d);
  task.analyze();
  for(final CompilationUnitTree unit:units)new TreePathScanner<Void,Void>(){
   public Void visitVariable(VariableTree a,Void v){
    Element e=trees.getElement(getCurrentPath());if(e instanceof VariableElement&&e.getEnclosingElement().toString().equals("com.hbm.blocks.ModBlocks")&&Arrays.asList("brick_red","steel_poles","pole_top").contains(e.getSimpleName().toString())&&a.getInitializer()!=null)throw new AssertionError("unexpected field initializer "+a);return super.visitVariable(a,v);
   }
   public Void visitAssignment(AssignmentTree a,Void v){
    Element e=trees.getElement(new TreePath(getCurrentPath(),a.getVariable()));
    if(e instanceof VariableElement&&e.getEnclosingElement().toString().equals("com.hbm.blocks.ModBlocks")&&Arrays.asList("brick_red","steel_poles","pole_top").contains(e.getSimpleName().toString())){
     Tree rhs=a.getExpression(); while(rhs instanceof MethodInvocationTree){Element builder=trees.getElement(TreePath.getPath(unit,rhs));if(!(builder instanceof ExecutableElement)||!builder.getEnclosingElement().toString().equals("net.minecraft.block.Block")||!Arrays.asList("setTranslationKey","setHardness","setResistance","setCreativeTab").contains(builder.getSimpleName().toString()))throw new AssertionError("unreviewed builder "+builder);Tree method=((MethodInvocationTree)rhs).getMethodSelect();if(!(method instanceof MemberSelectTree))throw new AssertionError("nonbuilder assignment "+a);rhs=((MemberSelectTree)method).getExpression();}
     if(!(rhs instanceof NewClassTree))throw new AssertionError("nonconstructor assignment "+a);
     NewClassTree n=(NewClassTree)rhs;Element ctor=trees.getElement(TreePath.getPath(unit,n));
     System.err.println("FIELD\t"+e.getSimpleName()+"\t"+ctor.getEnclosingElement()+"\t"+Paths.get(unit.getSourceFile().toUri())+"\t"+pos.getStartPosition(unit,a)+"\t"+pos.getEndPosition(unit,a));
    } return super.visitAssignment(a,v);
   }
   public Void visitMethodInvocation(MethodInvocationTree call,Void v){
    List<? extends ExpressionTree> aa=call.getArguments();
    if(call.getMethodSelect() instanceof MemberSelectTree&&(aa.size()==4||aa.size()==6)){
     MemberSelectTree select=(MemberSelectTree)call.getMethodSelect();
     if(select.getIdentifier().contentEquals("setBlock")){
      TreePath sp=new TreePath(getCurrentPath(),select); TypeMirror receiver=trees.getTypeMirror(new TreePath(sp,select.getExpression()));
      Element re=trees.getElement(new TreePath(sp,select.getExpression()));
      boolean ok=receiver!=null&&receiver.getKind()==TypeKind.DECLARED&&receiver.toString().equals("net.minecraft.world.World")&&re instanceof VariableElement;
      Element block=trees.getElement(new TreePath(getCurrentPath(),aa.get(3)));
      ok &= block instanceof VariableElement&&block.getEnclosingElement().toString().equals("com.hbm.blocks.ModBlocks")&&block.getModifiers().contains(Modifier.STATIC);
      int metadata=0,flags=3;
      if(aa.size()==6){
       ok &= aa.get(4) instanceof LiteralTree&&aa.get(5) instanceof LiteralTree;
       if(ok){Object m=((LiteralTree)aa.get(4)).getValue(),f=((LiteralTree)aa.get(5)).getValue();ok=m instanceof Integer&&f instanceof Integer;if(ok){metadata=(Integer)m;flags=(Integer)f;ok=metadata>=0&&metadata<16&&flags>=0&&flags<8;}}
      }
      for(int i=0;i<aa.size();i++)if(i!=3){TypeMirror t=trees.getTypeMirror(new TreePath(getCurrentPath(),aa.get(i)));ok &= t!=null&&t.getKind()==TypeKind.INT;}
      final boolean[] resolved={true};
      for(ExpressionTree a:aa)new TreePathScanner<Void,Void>(){
       public Void scan(Tree tree,Void value){if(tree instanceof ExpressionTree){TypeMirror t=trees.getTypeMirror(new TreePath(getCurrentPath(),tree));if(t==null||t.getKind()==TypeKind.ERROR)resolved[0]=false;}return super.scan(tree,value);}
      }.scan(new TreePath(getCurrentPath(),a),null);
      if(ok&&resolved[0]&&getCurrentPath().getParentPath().getLeaf() instanceof ExpressionStatementTree){
       StringBuilder out=new StringBuilder(Paths.get(unit.getSourceFile().toUri()).toString());out.append('\t').append(pos.getStartPosition(unit,call)).append('\t').append(pos.getEndPosition(unit,call)).append('\t').append(unit.getLineMap().getLineNumber(pos.getStartPosition(unit,call))).append('\t').append(block.getSimpleName()).append('\t').append(metadata).append('\t').append(flags).append('\t').append(aa.size());
       out.append('\t').append(pos.getStartPosition(unit,select.getExpression())).append('\t').append(pos.getEndPosition(unit,select.getExpression()));
       out.append('\t').append(pos.getStartPosition(unit,getCurrentPath().getParentPath().getLeaf())).append('\t').append(pos.getEndPosition(unit,getCurrentPath().getParentPath().getLeaf()));
       for(ExpressionTree a:aa)out.append('\t').append(pos.getStartPosition(unit,a)).append('\t').append(pos.getEndPosition(unit,a));
       System.out.println(out);
      }
     }
    }
    return super.visitMethodInvocation(call,v);
   }
  }.scan(unit,null);
  System.err.println("Attribution diagnostics, NOT whole-source compile success: "+errors.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
