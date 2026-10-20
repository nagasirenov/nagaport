import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Typed owner/receiver/argument attribution. Metadata-domain approval remains the exact reviewed manifest, not this broader candidate census. */
public final class RegisteredWriteScanner {
 public static void main(String[] args) throws Exception {
  final Set<String> selected=new HashSet<>(Files.readAllLines(Paths.get(args[2])));
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler(); DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(errors,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  final Trees trees=Trees.instance(task); final SourcePositions pos=trees.getSourcePositions();
  List<CompilationUnitTree> units=new ArrayList<>(); for(CompilationUnitTree u:task.parse()) units.add(u);
  for(Diagnostic<?> d:errors.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse failure: "+d);
  task.analyze();
  for(final CompilationUnitTree unit:units)new TreePathScanner<Void,Void>(){
   boolean target(Element e){return e instanceof VariableElement&&e.getEnclosingElement().toString().equals("com.hbm.blocks.ModBlocks")&&e.getModifiers().contains(Modifier.STATIC)&&selected.contains(e.getSimpleName().toString());}
   Tree unwrap(Tree rhs){while(rhs instanceof ParenthesizedTree||rhs instanceof TypeCastTree){if(rhs instanceof ParenthesizedTree)rhs=((ParenthesizedTree)rhs).getExpression();else{TypeMirror type=trees.getTypeMirror(TreePath.getPath(unit,((TypeCastTree)rhs).getType()));if(type==null||!Arrays.asList("com.hbm.blocks.BlockBase","com.hbm.blocks.generic.BlockStepEffect").contains(type.toString()))throw new AssertionError("unreviewed cast "+rhs);rhs=((TypeCastTree)rhs).getExpression();}}return rhs;}
   public Void visitVariable(VariableTree a,Void v){Element e=trees.getElement(getCurrentPath());
    if(e instanceof VariableElement&&e.getEnclosingElement().toString().equals("com.hbm.world.dungeon.DesertAtom001")&&e.getSimpleName().contentEquals("Block4")){Element initial=a.getInitializer()==null?null:trees.getElement(new TreePath(getCurrentPath(),a.getInitializer()));if(!target(initial)||!initial.getSimpleName().contentEquals("crashed_balefire"))throw new AssertionError("alias initial value changed");System.err.println("ALIAS\t"+e+"\t"+initial.getEnclosingElement()+"."+initial.getSimpleName());}
if(target(e)&&a.getInitializer()!=null)throw new AssertionError("field initializer "+a);return super.visitVariable(a,v);}
   public Void visitAssignment(AssignmentTree a,Void v){
    Element e=trees.getElement(new TreePath(getCurrentPath(),a.getVariable()));if(e instanceof VariableElement&&e.getEnclosingElement().toString().equals("com.hbm.world.dungeon.DesertAtom001")&&e.getSimpleName().contentEquals("Block4"))throw new AssertionError("alias reassigned");if(target(e)){
     Tree rhs=unwrap(a.getExpression());
     while(rhs instanceof MethodInvocationTree){MethodInvocationTree call=(MethodInvocationTree)rhs;Element b=trees.getElement(TreePath.getPath(unit,rhs));if(!(b instanceof ExecutableElement))throw new AssertionError("unresolved builder "+rhs);String key=b.getEnclosingElement()+"."+b.getSimpleName();
      if(key.equals("com.hbm.blocks.PlainCubeTextures.bind")){if(call.getArguments().size()!=2)throw new AssertionError(rhs);rhs=unwrap(call.getArguments().get(0));continue;}
      String owner=b.getEnclosingElement().toString(),name=b.getSimpleName().toString();boolean ok=owner.equals("net.minecraft.block.Block")&&Arrays.asList("setTranslationKey","setHardness","setResistance","setCreativeTab","setLightLevel","setBlockUnbreakable").contains(name);
      ok|=owner.equals("com.hbm.blocks.BlockBase")&&Arrays.asList("setBlockName","setSoundType","setBlockTextureName").contains(name);
      ok|=owner.equals("com.hbm.blocks.generic.BlockStepEffect")&&Arrays.asList("noFortune","withSoundType").contains(name);
      ok|=owner.equals("com.hbm.blocks.generic.BlockDecoCT")&&name.equals("noFortune");
      if(!ok||!(call.getMethodSelect()instanceof MemberSelectTree))throw new AssertionError("unreviewed identity builder "+key);rhs=unwrap(((MemberSelectTree)call.getMethodSelect()).getExpression());
     }
     if(!(rhs instanceof NewClassTree)||((NewClassTree)rhs).getClassBody()!=null)throw new AssertionError("not exact constructor "+a);Element ctor=trees.getElement(TreePath.getPath(unit,rhs));if(!(ctor instanceof ExecutableElement))throw new AssertionError("unresolved constructor "+rhs);
     System.err.println("FIELD\t"+e.getSimpleName()+"\t"+ctor.getEnclosingElement()+"\t"+Paths.get(unit.getSourceFile().toUri())+"\t"+pos.getStartPosition(unit,a)+"\t"+pos.getEndPosition(unit,a));
    }return super.visitAssignment(a,v);
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
      ok &= target(block)||(block instanceof VariableElement&&block.getEnclosingElement().toString().equals("com.hbm.world.dungeon.DesertAtom001")&&block.getSimpleName().contentEquals("Block4"))||(block instanceof VariableElement&&block.getSimpleName().contentEquals("this")&&block.getEnclosingElement().toString().equals("com.hbm.blocks.generic.BlockNTMSand"));
      int metadata=0,flags=3;
      if(aa.size()==6){metadata=-1;flags=-1;}
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
       out.append('\t').append(block.getEnclosingElement());System.out.println(out);
      }
     }
    }
    return super.visitMethodInvocation(call,v);
   }
  }.scan(unit,null);
  System.err.println("Attribution diagnostics, NOT whole-source compile success: "+errors.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
