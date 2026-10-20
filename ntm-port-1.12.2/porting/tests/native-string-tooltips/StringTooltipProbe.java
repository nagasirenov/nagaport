import java.util.*;
import java.lang.reflect.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sun.misc.Unsafe;

/** Genuine original/native wrapper comparison and native dispatch of exact caller branches. */
public class StringTooltipProbe {
 static final Unsafe U;static{try{Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(Unsafe)f.get(null);}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 static int checks,events;static Screen screen;static String expected;static int x,y;static ItemStack context=ItemStack.EMPTY;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 public static class Screen extends GuiScreen {
  void setup(FontRenderer f)throws Exception{fontRenderer=f;width=320;height=240;zLevel=43;itemRender=(RenderItem)U.allocateInstance(RenderItem.class);itemRender.zLevel=-17;}
  FontRenderer font(){return fontRenderer;}
  void stable(){check(zLevel==43&&itemRender.zLevel==-17,"native String caller z-state preserved");}
 }
 public static class NativeCapture extends Screen {
  List<String> seen;int x,y,calls;
  public void drawHoveringText(List<String> list,int a,int b){seen=list;x=a;y=b;calls++;}
 }
 public static class FontCapture extends Screen {
  List<String> seen;FontRenderer font;int x,y;
  protected void drawHoveringText(List<String> list,int a,int b,FontRenderer f){seen=list;x=a;y=b;font=f;}
 }
 @SuppressWarnings({"unchecked","rawtypes"}) static void wrappers(FontRenderer font)throws Exception {
  OldStringCapture old=new OldStringCapture();NativeCapture nativeOne=new NativeCapture();nativeOne.setup(font);
  String[] texts={null,"",new String("original"),"\u00a7eFormatted","a\nb\r\t\u03bb"};int[][] points={{0,0},{-17,23},{Integer.MIN_VALUE,Integer.MAX_VALUE}};
  for(String text:texts)for(int[] point:points){List previousOld=old.seen;List previousNative=nativeOne.seen;old.invoke(text,point[0],point[1]);nativeOne.drawHoveringText(text,point[0],point[1]);
   check(old.seen.size()==1&&nativeOne.seen.size()==1,"one element for null/empty/formatted input");check(old.seen.get(0)==text&&nativeOne.seen.get(0)==text,"original String reference, no normalization/splitting");check(old.x==point[0]&&nativeOne.x==point[0]&&old.y==point[1]&&nativeOne.y==point[1],"original/native coordinate order and range");check(old.seen!=previousOld&&nativeOne.seen!=previousNative,"fresh Arrays.asList wrapper per call");
   for(List list:new List[]{old.seen,nativeOne.seen}){String replacement=new String("replacement");check(list.set(0,replacement)==text&&list.get(0)==replacement,"same fixed-size mutable list semantics");try{list.add("extra");throw new AssertionError("resizable wrapper");}catch(UnsupportedOperationException ok){checks++;}}
   nativeOne.stable();
  }
  check(old.calls==15&&nativeOne.calls==15,"exactly one virtual List call per String wrapper");
  FontCapture f=new FontCapture();f.setup(font);String text=new String("font bridge");f.drawHoveringText(text,11,-9);check(f.seen.size()==1&&f.seen.get(0)==text&&f.x==11&&f.y==-9&&f.font==font,"native String to List to font-aware virtual hook preserves inputs");f.stable();
 }
 public static class EventSink {
  @SubscribeEvent public void pre(RenderTooltipEvent.Pre e){events++;check(e.getLines().size()==1&&Objects.equals(e.getLines().get(0),expected),"exact caller text");check(e.getX()==x&&e.getY()==y,"exact caller coordinates");check(e.getFontRenderer()==screen.font()&&e.getScreenWidth()==320&&e.getScreenHeight()==240&&e.getMaxWidth()==-1,"native screen font/dimensions");check(e.getStack()==context,"native item cache identity retained");
   List<String> frames=new ArrayList<>();for(StackTraceElement s:Thread.currentThread().getStackTrace())frames.add(s.getClassName()+"."+s.getMethodName());check(Collections.frequency(frames,"net.minecraft.client.gui.GuiScreen.drawHoveringText")==3&&frames.contains("net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText"),"actual String/List/font/native utility route");e.setCanceled(true);
  }
 }
 static void expect(Screen s,String text,int a,int b){screen=s;expected=text;x=a;y=b;}
 static void branches(FontRenderer font)throws Exception{
  RepresentativeStringBranches r=new RepresentativeStringBranches();r.setup(font);int left=13,top=-7;
  for(String value:new String[]{null,"","\u00a7eone"}){expect(r,value,9,-12);int n=events;r.drawHoveringText(value,x,y);check(events==n+1,"String null/empty stays a one-line native tooltip");r.stable();}
  int[][] points={{79,39},{80,38},{80,39},{113,46},{114,46},{90,47},{-100,-100},{500,500}};
  for(boolean locked:new boolean[]{false,true})for(int setting:new int[]{0,1,30})for(int[] p:points){int mx=left+p[0],my=top+p[1];boolean inside=locked||(80<=p[0]&&p[0]<114&&38<p[1]&&p[1]<=46);int ex=Math.max(left+80,Math.min(left+114,mx)),ey=Math.max(top+38,Math.min(top+46,my));expect(r,((setting*2)/10D)+"mB/t",ex,ey);int n=events;r.combustion(mx,my,left,top,locked,setting);check(events==n+(inside?1:0),"original combustion lock/bounds gate");r.stable();}
  String[] text={TextFormatting.YELLOW+"Close",TextFormatting.YELLOW+"Clear search",TextFormatting.ITALIC+"Press ENTER to toggle focus"};int[][] origins={{152,90},{134,108},{8,108}};int[] offsets={-1,0,1,15,16,17};
  for(int i=0;i<origins.length;i++)for(int dx:offsets)for(int dy:offsets){int mx=left+origins[i][0]+dx,my=top+origins[i][1]+dy;boolean inside=0<=dx&&dx<16&&0<dy&&dy<=16;expect(r,text[i],mx,my);int n=events;r.selector(mx,my,left,top);check(events==n+(inside?1:0),"original selector independent bounds gate");r.stable();}
  ItemStack item=new ItemStack(net.minecraft.init.Items.STICK,7,2);context=item;expect(r,"0.2mB/t",left+80,top+38);net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(item);try{r.combustion(-500,-500,left,top,true,1);check(item.getCount()==7&&item.getMetadata()==2,"cached item state not mutated");r.stable();}finally{net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();context=ItemStack.EMPTY;}
 }
 public static void main(String[] args)throws Exception{
  net.minecraft.init.Bootstrap.register();net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());net.minecraftforge.fml.common.ModMetadata m=new net.minecraftforge.fml.common.ModMetadata();m.modId="stringtooltipaudit";m.name="String tooltip audit";m.version="1";net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(m);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
  FontRenderer font=(FontRenderer)U.allocateInstance(FontRenderer.class);wrappers(font);EventSink sink=new EventSink();MinecraftForge.EVENT_BUS.register(sink);try{branches(font);}finally{MinecraftForge.EVENT_BUS.unregister(sink);}
  System.out.println("PASS "+checks+" actual old/native wrapper and four exact caller-branch checks; "+events+" genuine native Pre events. No whole drawScreen or uncanceled GL execution.");
 }
}
