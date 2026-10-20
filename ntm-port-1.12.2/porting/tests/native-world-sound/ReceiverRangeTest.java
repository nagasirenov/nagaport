import java.net.URI;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** Independent javac AST/type test: nested receivers must never be selected by regex suffix. */
public final class ReceiverRangeTest {
 static int checks;static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static final String HELPER="com.hbm.sound.WorldSoundEffects.play";
 static final String PREFIX="import net.minecraft.world.World; import net.minecraft.entity.player.EntityPlayer; class ReceiverFixture { static class Holder { World world; EntityPlayer getPlayer(){return null;} Holder next(){return this;} } static Holder context(){return null;} static World world(){return null;} static int index(){return 0;} static void run(World w, Holder ctx, World[] worlds, boolean flag, World other, Object entity) {\n";
 static final String SUFFIX="\n} }";
 static final class Input extends SimpleJavaFileObject {
  final String source;Input(String source){super(URI.create("string:///ReceiverFixture.java"),Kind.SOURCE);this.source=source;}
  public CharSequence getCharContent(boolean ignore){return source;}
 }
 static final class Call {String receiver;String args;String selected;String type;String owner;long start,end,receiverStart,receiverEnd;}
 static final class Parsed {List<Call> calls=new ArrayList<>();long errors;}
 static Parsed parse(String source,String cp,boolean nativeMode)throws Exception{
  DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-source","8","-classpath",cp),null,Arrays.asList(new Input(source)));
  final Trees trees=Trees.instance(task);final SourcePositions pos=trees.getSourcePositions();final List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree unit:task.parse())units.add(unit);task.analyze();Parsed result=new Parsed();
  for(final CompilationUnitTree unit:units)new TreePathScanner<Void,Void>(){
   public Void visitMethodInvocation(MethodInvocationTree t,Void v){
    if(t.getMethodSelect() instanceof MemberSelectTree){MemberSelectTree m=(MemberSelectTree)t.getMethodSelect();if(m.getIdentifier().contentEquals(nativeMode?"play":"playSoundEffect")){
     Call c=new Call();c.start=pos.getStartPosition(unit,t);c.end=pos.getEndPosition(unit,t);ExpressionTree receiver=nativeMode?t.getArguments().get(0):m.getExpression();TreePath rp=nativeMode?new TreePath(getCurrentPath(),receiver):new TreePath(new TreePath(getCurrentPath(),m),receiver);
     c.receiverStart=pos.getStartPosition(unit,receiver);c.receiverEnd=pos.getEndPosition(unit,receiver);c.receiver=source.substring((int)c.receiverStart,(int)c.receiverEnd);c.type=String.valueOf(trees.getTypeMirror(rp));c.selected=m.toString();Element selected=trees.getElement(new TreePath(getCurrentPath(),m));c.owner=selected instanceof ExecutableElement?selected.getEnclosingElement().toString():"<unresolved>";
     List<? extends ExpressionTree> args=t.getArguments();int first=nativeMode?1:0;c.args=source.substring((int)pos.getStartPosition(unit,args.get(first)),(int)pos.getEndPosition(unit,args.get(args.size()-1)));result.calls.add(c);
    }}return super.visitMethodInvocation(t,v);
   }
  }.scan(unit,null);
  result.errors=diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count();fm.close();return result;
 }
 public static void main(String[] args)throws Exception{
  String[] receivers={"w","ctx.world","ctx.getPlayer().world","context().next().getPlayer().world","world()","worlds[index()]","((EntityPlayer) entity).world","(flag ? w : other)"};StringBuilder statements=new StringBuilder();for(String receiver:receivers)statements.append(receiver).append(".playSoundEffect(1D, 2D, 3D, \"hbm:block.crateOpen\", 0.5F, 1F);\n");String original=PREFIX+statements+SUFFIX;
  Parsed before=parse(original,args[0],false);check(before.calls.size()==receivers.length,"all original nested receiver AST nodes");check(before.errors==receivers.length,"only genuinely missing legacy methods fail before migration");StringBuilder edited=new StringBuilder(original);
  for(int i=before.calls.size()-1;i>=0;i--){Call c=before.calls.get(i);check(c.receiver.equals(receivers[i]),"javac captures entire receiver "+receivers[i]);check(c.type.equals("net.minecraft.world.World"),"native World receiver type before");edited.replace((int)c.start,(int)c.end,HELPER+"("+c.receiver+", "+c.args+")");}
  String corrected=edited.toString();Parsed after=parse(corrected,args[0],true);check(after.errors==0,"all nested/member/array/cast/conditional positives compile natively");check(after.calls.size()==before.calls.size(),"all positives retained");
  for(int i=0;i<after.calls.size();i++){Call a=after.calls.get(i),b=before.calls.get(i);check(a.receiver.equals(b.receiver),"complete first argument equals original receiver");check(a.args.equals(b.args),"all six original args byte-for-byte");check(a.type.equals("net.minecraft.world.World")&&a.owner.equals("com.hbm.sound.WorldSoundEffects"),"real native helper destination attribution");check(a.selected.equals(HELPER),"no leftover receiver prefix");}
  String[] bad={corrected.replace(HELPER+"(ctx.getPlayer().world, ","ctx.getPlayer()."+HELPER+"(world, "),corrected.replace(HELPER+"(context().next().getPlayer().world, ","context().next().getPlayer()."+HELPER+"(world, "),corrected.replace(HELPER+"(ctx.getPlayer().world, ",HELPER+"(w, ")};
  for(int i=0;i<bad.length;i++){Parsed broken=parse(bad[i],args[0],true);boolean rejected=broken.errors>0||broken.calls.size()!=before.calls.size();for(int j=0;j<Math.min(broken.calls.size(),before.calls.size());j++)rejected|=!broken.calls.get(j).receiver.equals(before.calls.get(j).receiver)||!broken.calls.get(j).selected.equals(HELPER)||!broken.calls.get(j).owner.equals("com.hbm.sound.WorldSoundEffects");check(rejected,"reject nested receiver mutation "+i);}
  System.out.println("PASS ReceiverRangeTest: "+checks+" checks;8 complete javac-attributed receivers before/after, native destination owners,3 independent negative controls including reported regression and compiling wrong-receiver mutation");
 }
}
