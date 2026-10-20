import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.*;
import sun.misc.Unsafe;

/** Actual native widget/Forge path, stopped by a genuine Pre listener before GL. */
public class TooltipProbe {
 static final Unsafe U; static {try {Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(Unsafe)f.get(null);}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 static int checks, events; static ProbeScreen expectedScreen; static List<String> expected; static int x,y; static boolean mutate;
 static FontRenderer alternate; static ItemStack expectedStack=ItemStack.EMPTY; static ItemStack itemInput; static int itemCalls;
 static void check(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
 static Object fresh(Class<?> c)throws Exception{return U.allocateInstance(c);}
 public static class ProbeScreen extends GuiScreen {
  void setup(FontRenderer font,float z)throws Exception{fontRenderer=font;width=320;height=240;zLevel=z;itemRender=(RenderItem)fresh(RenderItem.class);itemRender.zLevel=z+17;}
  float z(){return zLevel;} float itemZ(){return itemRender.zLevel;}
  FontRenderer font(){return fontRenderer;}
  protected void renderToolTip(ItemStack s,int a,int b){check(s==itemInput&&a==x&&b==y,"unchanged item branch arguments");itemCalls++;}
 }
 public static class HookScreen extends ProbeScreen {
  List<String> seen; int a,b;FontRenderer font;
  protected void drawHoveringText(List<String> lines,int x,int y,FontRenderer f){seen=lines;a=x;b=y;font=f;}
 }
 public static final class First {
  @SubscribeEvent(priority=EventPriority.HIGHEST) public void pre(RenderTooltipEvent.Pre e){
   events++;check(e.getLines().equals(expected),"exact tooltip lines/content/order");check(e.getX()==x&&e.getY()==y,"original coordinates order");
   check(e.getScreenWidth()==320&&e.getScreenHeight()==240&&e.getMaxWidth()==-1,"native dimension/default wrap inputs");
   check(e.getFontRenderer()==expectedScreen.font(),"actual enclosing screen font identity");check(e.getStack()==expectedStack,"native tooltip cached item context identity");
   try{e.getLines().add("forbidden");throw new AssertionError("event lines mutable");}catch(UnsupportedOperationException ok){checks++;}
   Set<String> stack=new HashSet<>();for(StackTraceElement f:Thread.currentThread().getStackTrace())stack.add(f.getClassName()+"."+f.getMethodName());
   check(stack.contains("net.minecraft.client.gui.GuiScreen.drawHoveringText")&&stack.contains("net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText"),"actual native Screen/GuiUtils dispatch");
   if(mutate){e.setX(x+3);e.setY(y-4);e.setScreenWidth(640);e.setScreenHeight(480);e.setMaxWidth(73);e.setFontRenderer(alternate);}
  }
 }
 public static final class Last {
  @SubscribeEvent(priority=EventPriority.LOWEST) public void pre(RenderTooltipEvent.Pre e){
   check(e.getX()==x+(mutate?3:0)&&e.getY()==y-(mutate?4:0),"earlier listener coordinate changes preserved");
   check(e.getFontRenderer()==(mutate?alternate:expectedScreen.font()),"earlier font change preserved");
   check(e.getScreenWidth()==(mutate?640:320)&&e.getScreenHeight()==(mutate?480:240)&&e.getMaxWidth()==(mutate?73:-1),"earlier wrapping changes preserved");
   e.setCanceled(true);check(e.isCanceled(),"genuine cancellable native Pre event");
  }
 }
 static void expect(ProbeScreen screen,List<String> lines,int a,int b){expectedScreen=screen;expected=lines;x=a;y=b;}
 static void stable(ProbeScreen s,float z){check(s.z()==z&&s.itemZ()==z+17,"caller GUI/item z-state preserved");}
 static void direct(FontRenderer font)throws Exception{
  List<String> lines=new ArrayList<>(Arrays.asList("\u00a7eTitle","line two","\u03bb"));
  HookScreen hook=new HookScreen();hook.setup(font,300);hook.drawHoveringText(lines,-8,41);check(hook.seen==lines&&hook.a==-8&&hook.b==41&&hook.font==font,"native 3-arg forwards exact alias/x/y/font to virtual 4-arg hook");stable(hook,300);
  for(float z:new float[]{-9,0,300}){ProbeScreen s=new ProbeScreen();s.setup(font,z);for(boolean m:new boolean[]{false,true}){mutate=m;expect(s,lines,-11,209);int n=events;s.drawHoveringText(lines,x,y);check(events==n+1,"one Pre per nonempty native call");check(lines.equals(Arrays.asList("\u00a7eTitle","line two","\u03bb")),"no input content mutation");stable(s,z);}
   int n=events;s.drawHoveringText(Collections.<String>emptyList(),4,8);check(events==n,"empty list bypasses native event/drawing");stable(s,z);
   try{s.drawHoveringText((List<String>)null,4,8);throw new AssertionError("null accepted");}catch(NullPointerException ok){checks++;}stable(s,z);
  }
  ProbeScreen cached=new ProbeScreen();cached.setup(font,73);ItemStack context=new ItemStack(net.minecraft.init.Items.STICK,7,2);expectedStack=context;expect(cached,lines,44,98);mutate=false;
  net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(context);try{cached.drawHoveringText(lines,x,y);check(context.getCount()==7&&context.getMetadata()==2,"cached item context not mutated");stable(cached,73);}finally{net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();expectedStack=ItemStack.EMPTY;}
  cached.drawHoveringText(lines,x,y);stable(cached,73);
  RenderTooltipEvent.Pre view=new RenderTooltipEvent.Pre(ItemStack.EMPTY,lines,1,2,320,240,-1,font);lines.set(1,"changed");check(view.getLines().get(1).equals("changed"),"event unmodifiable view remains backed by original list");lines.set(1,"line two");
 }
 static void methods(FontRenderer font)throws Exception{
  mutate=false;
  GUIScreenBobmazon b=new GUIScreenBobmazon();b.setup(font,300);GUIScreenBobmazon.FolderButton bb=b.new FolderButton();
  GUIScreenDesignator d=new GUIScreenDesignator();d.setup(font,-9);GUIScreenDesignator.FolderButton db=d.new FolderButton();
  GUIScreenTemplateFolder t=new GUIScreenTemplateFolder();t.setup(font,0);GUIScreenTemplateFolder.FolderButton tb=t.new FolderButton();
  for(String s:new String[]{null,""}){bb.info=s;db.info=s;tb.info=s;int n=events;bb.drawString(1,2);db.drawString(1,2);tb.drawString(1,2);check(events==n,"three original null/empty guards");}
  for(String s:new String[]{"folder","\u00a7aOriginal\ncontent"}){bb.info=s;db.info=s;tb.info=s;expect(b,Arrays.asList(s),-3,89);int n=events;bb.drawString(x,y);check(events==n+1,"Bobmazon outer native dispatch");stable(b,300);
   expect(d,Arrays.asList(s),77,13);n=events;db.drawString(x,y);check(events==n+1,"Designator outer native dispatch");stable(d,-9);
   expect(t,Arrays.asList(s),201,-4);n=events;tb.drawString(x,y);check(events==n+1,"TemplateFolder outer native dispatch");stable(t,0);
  }
  itemInput=ItemStack.EMPTY;tb.stack=itemInput;int n=events;tb.drawString(x,y);check(events==n&&itemCalls==1,"existing item branch bypasses List tooltip");check(tb.stack==itemInput&&itemInput.isEmpty(),"item branch does not mutate stack");stable(t,0);
  GUIRBMKConsole r=new GUIRBMKConsole();r.setup(font,300);String[] lines={"first","\u00a7ctwo"};
  for(int mx=9;mx<=15;mx++)for(int my=19;my<=25;my++){boolean inside=10<=mx&&14>mx&&20<my&&24>=my;expect(r,Arrays.asList(lines),199,-21);n=events;r.drawCustomInfoStat(mx,my,10,20,4,4,x,y,lines);check(events==n+(inside?1:0),"exact asymmetric original hover bounds");stable(r,300);}
 }
 public static void main(String[] args)throws Exception{
  net.minecraft.init.Bootstrap.register();net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());net.minecraftforge.fml.common.ModMetadata m=new net.minecraftforge.fml.common.ModMetadata();m.modId="tooltipaudit";m.name="Tooltip audit";m.version="1";net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(m);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
  FontRenderer font=(FontRenderer)fresh(FontRenderer.class);alternate=(FontRenderer)fresh(FontRenderer.class);First first=new First();Last last=new Last();MinecraftForge.EVENT_BUS.register(first);MinecraftForge.EVENT_BUS.register(last);try{direct(font);methods(font);}finally{MinecraftForge.EVENT_BUS.unregister(first);MinecraftForge.EVENT_BUS.unregister(last);}
  System.out.println("PASS "+checks+" actual native forwarding/event/cancellation/z-state and four exact extracted production-method checks; "+events+" native Pre events. Uncancelled GL/layout not run.");
 }
}
