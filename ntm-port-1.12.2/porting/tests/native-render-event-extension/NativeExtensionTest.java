import java.lang.reflect.*;
import java.util.*;
import sun.misc.Unsafe;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.*;
public final class NativeExtensionTest {
 static int checks; static String trace="";
 static void ok(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
 static void same(Object a,Object b,String m){if(a instanceof Float&&b instanceof Float)ok(Float.floatToRawIntBits((Float)a)==Float.floatToRawIntBits((Float)b),m);else ok(a==b,m);}
 static Unsafe unsafe()throws Exception{Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return (Unsafe)f.get(null);}
 static class Frame extends EntityItemFrame { ItemStack value; Frame(ItemStack v){super((net.minecraft.world.World)null);value=v;} public ItemStack getDisplayedItem(){return value;} }
 static <T>T receiver(T t){trace+="R";return t;}
 static RenderHandEvent fail(){trace+="X";throw new IllegalStateException();}
 static class Custom extends RenderHandEvent {Custom(){super(null,1F,0);}public float getPartialTicks(){return 2F;}}
 public static void main(String[] args)throws Exception{
  try(NativeRenderContext.Context gl=new NativeRenderContext.Context()){
   NativeRenderContext.initializeNativeClientSettings();net.minecraft.init.Bootstrap.register();
   Unsafe u=unsafe();RenderPlayer renderer=(RenderPlayer)u.allocateInstance(RenderPlayer.class);
   ItemStack stack=new ItemStack(net.minecraft.init.Items.APPLE,3,7);
   int[] bits={0,0x80000000,0x3ec00000,0x3f800000,0x7f800000,0xff800000,0x7fc00031,0xffc00123};
   for(int bit:bits)for(boolean nil:new boolean[]{false,true}){
    float tick=Float.intBitsToFloat(bit);RenderPlayer rr=nil?null:renderer;
    Map<Class<?>,Object> m=new HashMap<>();
    m.put(RenderPlayerEvent.Pre.class,new RenderPlayerEvent.Pre(null,rr,tick,0,0,0));
    m.put(RenderPlayerEvent.SetArmorModel.class,new RenderPlayerEvent.SetArmorModel(null,rr,2,tick,stack));
    m.put(RenderHandEvent.class,new RenderHandEvent(null,tick,2));
    Frame frame=new Frame(nil?null:stack);RenderItemInFrameEvent event=new RenderItemInFrameEvent(frame,null);m.put(RenderItemInFrameEvent.class,event);
    for(int i=0;i<SelectedReads.GETTERS.length;i++){
     Method read=SelectedReads.class.getDeclaredMethod("read"+i,SelectedReads.RECEIVERS[i]);Object target=m.get(SelectedReads.RECEIVERS[i]);
     Object expected=SelectedReads.GETTERS[i].equals("getRenderer")?rr:SelectedReads.GETTERS[i].equals("getItem")?(nil?null:stack):Float.valueOf(tick);
     same(read.invoke(null,target),expected,"selected "+i);
     try{read.invoke(null,new Object[]{null});throw new AssertionError("missing NPE");}catch(InvocationTargetException e){ok(e.getCause() instanceof NullPointerException,"null receiver "+i);}
    }
    frame.value=ItemStack.EMPTY;same(event.getItem(),nil?null:stack,"snapshot not live frame read");
    RenderHandEvent hand=(RenderHandEvent)m.get(RenderHandEvent.class);trace="";same(receiver(hand).getPartialTicks(),tick,"single receiver value");ok(trace.equals("R"),"one receiver evaluation");
   }
   trace="";try{fail().getPartialTicks();throw new AssertionError();}catch(IllegalStateException expected){ok(trace.equals("X"),"receiver exception before getter");}
   Custom custom=new Custom();Field f=RenderHandEvent.class.getDeclaredField("partialTicks");f.setAccessible(true);same(f.get(custom),1F,"native original field");same(custom.getPartialTicks(),2F,"external override differs, explicitly excluded");
   Frame empty=new Frame(ItemStack.EMPTY);same(new RenderItemInFrameEvent(empty,null).getItem(),ItemStack.EMPTY,"EMPTY identity not normalized");
  }
  System.out.println("PASS "+checks+" actual native event/accessor assertions; no complete handlers/event bus/game claim");
 }
}
