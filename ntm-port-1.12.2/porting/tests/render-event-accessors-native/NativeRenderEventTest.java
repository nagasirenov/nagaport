import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import sun.misc.Unsafe;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.eventhandler.Event;
/** Reconstructed evidence source. Historical reports are not proof of this file's execution. */
public class NativeRenderEventTest {
 static int checks; static String trace="";
 static void ok(boolean value,String label) {checks++;if(!value)throw new AssertionError(label);}
 static void same(Object a,Object b,String label) {
  if(a instanceof Float&&b instanceof Float) ok(Float.floatToRawIntBits((Float)a)==Float.floatToRawIntBits((Float)b),label);
  else if(a instanceof Double&&b instanceof Double) ok(Double.doubleToRawLongBits((Double)a)==Double.doubleToRawLongBits((Double)b),label);
  else if(a instanceof Integer&&b instanceof Integer) ok(a.equals(b),label);
  else ok(a==b,label);
 }
 static <T> T receiver(T e) {trace+="R";return e;}
 static RenderGameOverlayEvent failure() {trace+="X";throw new IllegalStateException("fixture");}
 static class OverrideOverlay extends RenderGameOverlayEvent {
  final ScaledResolution alternate;
  OverrideOverlay(ScaledResolution first,ScaledResolution second) {super(1,first);alternate=second;}
  public ScaledResolution getResolution() {trace+="G";return alternate;}
  public float getPartialTicks() {trace+="T";return 0.25F;}
 }
 static Unsafe unsafe() throws Exception {Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return (Unsafe)f.get(null);}
 static Map<String,Object> samples(float tick,double coordinate,boolean nil) throws Exception {
  Unsafe u=unsafe(); ScaledResolution resolution=nil?null:(ScaledResolution)u.allocateInstance(ScaledResolution.class);
  RenderGlobal global=nil?null:(RenderGlobal)u.allocateInstance(RenderGlobal.class);
  EntityPig pig=nil?null:(EntityPig)u.allocateInstance(EntityPig.class);
  EntityPlayer player=nil?null:(EntityPlayer)u.allocateInstance(EntityOtherPlayerMP.class);
  RenderPig renderer=nil?null:(RenderPig)u.allocateInstance(RenderPig.class);
  EntityRenderer viewRenderer=nil?null:(EntityRenderer)u.allocateInstance(EntityRenderer.class);
  RayTraceResult target=nil?null:new RayTraceResult(new Vec3d(1,2,3),net.minecraft.util.EnumFacing.NORTH,new BlockPos(4,5,6));
  RenderGameOverlayEvent root=new RenderGameOverlayEvent(tick,resolution);
  Map<String,Object> values=new HashMap<>();values.put(RenderGameOverlayEvent.class.getName(),root);
  values.put(RenderGameOverlayEvent.Pre.class.getName(),new RenderGameOverlayEvent.Pre(root,RenderGameOverlayEvent.ElementType.CROSSHAIRS));
  values.put(RenderGameOverlayEvent.Post.class.getName(),new RenderGameOverlayEvent.Post(root,RenderGameOverlayEvent.ElementType.HOTBAR));
  values.put(RenderWorldLastEvent.class.getName(),new RenderWorldLastEvent(global,tick));
  RenderLivingEvent.Pre<EntityPig> living=new RenderLivingEvent.Pre<>(pig,renderer,coordinate,-coordinate,coordinate+2);
  values.put(RenderLivingEvent.class.getName(),living);values.put(RenderLivingEvent.Pre.class.getName(),living);
  DrawBlockHighlightEvent highlight=new DrawBlockHighlightEvent(global,player,target,7,tick);values.put(DrawBlockHighlightEvent.class.getName(),highlight);
  EntityViewRenderEvent.FogDensity fog=new EntityViewRenderEvent.FogDensity(viewRenderer,pig,null,coordinate,0.75F);
  values.put(EntityViewRenderEvent.class.getName(),fog);values.put(EntityViewRenderEvent.FogDensity.class.getName(),fog);
  values.put(EntityViewRenderEvent.FogColors.class.getName(),new EntityViewRenderEvent.FogColors(viewRenderer,pig,null,coordinate,0.25F,0.5F,0.75F));
  same(root.getResolution(),resolution,"overlay input identity");same(root.getPartialTicks(),tick,"overlay tick bits");same(root.getType(),null,"root event type null");
  same(highlight.getContext(),global,"highlight context identity");same(highlight.getPlayer(),player,"highlight player identity");same(highlight.getTarget(),target,"highlight target identity");same(highlight.getPartialTicks(),tick,"highlight tick bits");same(highlight.getSubID(),7,"highlight subID");
  same(living.getEntity(),pig,"living input identity");same(living.getRenderer(),renderer,"living renderer identity");same(living.getX(),coordinate,"living X bits");same(living.getY(),-coordinate,"living Y bits");same(living.getZ(),coordinate+2,"living Z bits");
  same(fog.getEntity(),pig,"view entity identity");same(fog.getRenderer(),viewRenderer,"view renderer identity");same(fog.getRenderPartialTicks(),coordinate,"view ticks bits");
  return values;
 }
 static Class<?> owner(String name) throws Exception {return Class.forName(name);}
 public static void main(String[] args) throws Exception {
  NativeRenderContext.Context context=new NativeRenderContext.Context();
  NativeRenderContext.initializeNativeClientSettings();
  net.minecraft.init.Bootstrap.register();
  checks+=HandlerProof.run();
  List<String[]> rules=new ArrayList<>();for(String s:Files.readAllLines(Paths.get(args[1]))) rules.add(s.split("\\t"));
  float[] ticks={0F,-0F,0.375F,1F,Float.POSITIVE_INFINITY,Float.intBitsToFloat(0x7fc00031)};
  double[] coordinates={0D,-0D,-17.375D,1D,Double.NEGATIVE_INFINITY,Double.longBitsToDouble(0x7ff8000000000031L)};
  for(int i=0;i<ticks.length;i++)for(boolean nil:new boolean[]{false,true}) {
   Map<String,Object> samples=samples(ticks[i],coordinates[i],nil);
   for(String[] rule:rules) {
    Class<?> c=owner(rule[0]);Field field=c.getDeclaredField(rule[1]);field.setAccessible(true);Method method=c.getMethod(rule[2]);Object e=samples.get(rule[0]);
    ok(Modifier.isPrivate(field.getModifiers())&&Modifier.isFinal(field.getModifiers()),"immutable native payload field");
    same(method.invoke(e),field.get(e),"actual constructor field/getter exact payload "+rule[0]+"."+rule[1]);
    same(method.invoke(e),method.invoke(e),"repeated getter stability");
    Event event=(Event)e;boolean cancelable=event.isCancelable(),canceled=event.isCanceled();Event.Result result=event.getResult();method.invoke(e);
    ok(event.isCancelable()==cancelable&&event.isCanceled()==canceled&&event.getResult()==result,"accessor leaves event state unchanged");
   }
   for(Method m:SelectedReads.class.getDeclaredMethods()) {
    Object e=samples.get(m.getParameterTypes()[0].getName());String getter=SelectedReads.GETTERS[Integer.parseInt(m.getName().substring(4))];
    same(m.invoke(null,e),e.getClass().getMethod(getter).invoke(e),"compiled selected production token "+m.getName());
   }
   RenderGameOverlayEvent root=(RenderGameOverlayEvent)samples.get(RenderGameOverlayEvent.class.getName());
   for(RenderGameOverlayEvent.ElementType type:RenderGameOverlayEvent.ElementType.values()) {
    RenderGameOverlayEvent.Pre pre=new RenderGameOverlayEvent.Pre(root,type);RenderGameOverlayEvent.Post post=new RenderGameOverlayEvent.Post(root,type);
    same(pre.getType(),type,"native Pre type");same(post.getType(),type,"native Post type");same(pre.getResolution(),root.getResolution(),"Pre resolution identity");same(post.getResolution(),root.getResolution(),"Post resolution identity");same(pre.getPartialTicks(),ticks[i],"Pre tick bits");same(post.getPartialTicks(),ticks[i],"Post tick bits");ok(!post.isCancelable(),"native Post remains noncancelable");
   }
  }
  for(String[] rule:rules) {
   Class<?> c=owner(rule[0]);Field field=c.getDeclaredField(rule[1]);String desc=org.objectweb.asm.Type.getDescriptor(field.getType());
   ClassNode node=new ClassNode();new ClassReader(c.getResourceAsStream(c.getSimpleName()+".class")).accept(node,0);MethodNode getter=null;
   for(Object item:node.methods) {MethodNode m=(MethodNode)item;if(m.name.equals(rule[2])&&m.desc.equals("()"+desc))getter=m;}
   List<AbstractInsnNode> code=new ArrayList<>();for(AbstractInsnNode x=getter.instructions.getFirst();x!=null;x=x.getNext())if(x.getOpcode()>=0)code.add(x);
   ok(code.size()==3&&code.get(0) instanceof VarInsnNode&&code.get(0).getOpcode()==Opcodes.ALOAD&&((VarInsnNode)code.get(0)).var==0,"exact native getter prefix");
   FieldInsnNode read=(FieldInsnNode)code.get(1);ok(read.getOpcode()==Opcodes.GETFIELD&&read.owner.equals(rule[0].replace('.','/'))&&read.name.equals(rule[1])&&read.desc.equals(desc),"exact native getter field");
   int ret=field.getType()==float.class?Opcodes.FRETURN:field.getType()==double.class?Opcodes.DRETURN:field.getType()==int.class?Opcodes.IRETURN:Opcodes.ARETURN;ok(code.get(2).getOpcode()==ret,"exact native getter return");
  }
  Map<String,String> parents=new HashMap<>();Map<String,Set<String>> declarations=new HashMap<>();int jars=0;
  for(String path:new String(Files.readAllBytes(Paths.get(args[0]))).trim().split(java.io.File.pathSeparator)) {
   if(!path.endsWith(".jar")||!Files.isRegularFile(Paths.get(path)))continue;jars++;
   try(JarFile jar=new JarFile(path)) {Enumeration<JarEntry> entries=jar.entries();while(entries.hasMoreElements()) {
    JarEntry entry=entries.nextElement();if(!entry.getName().endsWith(".class"))continue;ClassReader cr=new ClassReader(jar.getInputStream(entry));parents.put(cr.getClassName(),cr.getSuperName());
    cr.accept(new ClassVisitor(Opcodes.ASM5) {public MethodVisitor visitMethod(int access,String name,String desc,String signature,String[] exceptions) {if(desc.startsWith("()"))declarations.computeIfAbsent(cr.getClassName(),key->new HashSet<>()).add(name+desc);return null;}},ClassReader.SKIP_CODE);
   }}
  }
  for(String[] rule:rules) {
   String root=rule[0].replace('.','/'),signature=rule[2]+"()"+org.objectweb.asm.Type.getDescriptor(owner(rule[0]).getDeclaredField(rule[1]).getType());
   for(Map.Entry<String,Set<String>> entry:declarations.entrySet())if(entry.getValue().contains(signature)) {
    String c=entry.getKey();Set<String> seen=new HashSet<>();while(c!=null&&seen.add(c)) {if(c.equals(root)) {ok(entry.getKey().equals(root),"no dependency getter override: "+entry.getKey());break;}c=parents.get(c);}
   }
  }
  ScaledResolution first=(ScaledResolution)unsafe().allocateInstance(ScaledResolution.class),second=(ScaledResolution)unsafe().allocateInstance(ScaledResolution.class);RenderGameOverlayEvent event=new RenderGameOverlayEvent(0.5F,first);
  trace="";same(receiver(event).getResolution(),first,"receiver identity");ok(trace.equals("R"),"receiver once");
  trace="";try{receiver((RenderGameOverlayEvent)null).getResolution();throw new AssertionError();}catch(NullPointerException expected){ok(trace.equals("R"),"null receiver timing");}
  trace="";try{failure().getResolution();throw new AssertionError();}catch(IllegalStateException expected){ok(trace.equals("X"),"receiver exception timing");}
  OverrideOverlay override=new OverrideOverlay(first,second);trace="";same(receiver(override).getResolution(),second,"external override negative witness");ok(trace.equals("RG"),"virtual subclass dispatch retained, never cached or reflected");
  trace="";RenderGameOverlayEvent.Pre inherited=new RenderGameOverlayEvent.Pre(override,RenderGameOverlayEvent.ElementType.ALL);same(inherited.getResolution(),second,"native parent constructor virtual resolution dispatch");same(inherited.getPartialTicks(),0.25F,"native parent constructor virtual ticks dispatch");ok(trace.equals("TG"),"native parent constructor getter order");
  System.out.println("PASS "+checks+" native event/selected-token/bytecode assertions; "+jars+" dependency jars inspected; not complete renderer or game lifecycle");context.close();System.exit(0);
 }
}
