import com.hbm.advancement.client.LegacyInventoryActionHandler;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.init.Bootstrap;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.*;
import org.lwjgl.input.Keyboard;

/** Actual native processKeyBinds/displayGuiScreen, real transformed Forge events.
 * No window/game loop: GUI events are canceled; Minecraft/Player constructor state
 * is selectively allocated, and controller/horse-send boundaries are controlled.
 */
public final class InventoryActionNativeTest {
 static int checks, awards, guiEvents, nativeAttempts;
 static Minecraft mc;static Player player;static Controller controller;static Method process;
 static boolean cancelEarly, replaceEarly, nested;static int nestedDepth;
 static void check(boolean condition,String label){checks++;if(!condition)throw new AssertionError(label);}
 static void set(Class<?> cls,Object owner,String field,Object value)throws Exception {Field f=cls.getDeclaredField(field);f.setAccessible(true);f.set(owner,value);}
 static <T>T allocate(Class<T> cls)throws Exception {Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return cls.cast(((sun.misc.Unsafe)f.get(null)).allocateInstance(cls));}
 public static final class Controller extends PlayerControllerMP {
  boolean horse,creative;int resets;
  Controller(Minecraft mc){super(mc,null);}
  @Override public boolean isRidingHorse(){return horse;}
  @Override public boolean isInCreativeMode(){return creative;}
  @Override public void resetBlockRemoving(){resets++;}
 }
 public static final class Player extends EntityPlayerSP {
  int horseRequests;
  Player(){super(null,null,null,null,null);}
  @Override public boolean isHandActive(){return false;}
  @Override public void sendHorseInventory(){horseRequests++;}
 }
 public static final class Earlier {
  @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true)
  public void before(GuiOpenEvent e){if(cancelEarly)e.setCanceled(true);if(replaceEarly)e.setGui(new GuiChat());}
 }
 public static final class Nested {
  @SubscribeEvent(priority=EventPriority.NORMAL,receiveCanceled=true)
  public void open(GuiOpenEvent e){if(nested&&nestedDepth==0){nestedDepth++;try{mc.displayGuiScreen(new GuiInventory(player));}finally{nestedDepth--;}}}
 }
 public static final class CancelAndObserve {
  @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
  public void cancel(GuiOpenEvent e){guiEvents++;if(LegacyInventoryActionHandler.isNativeInventoryAttempt(Thread.currentThread().getStackTrace()))nativeAttempts++;e.setCanceled(true);}
 }
 static void run()throws Exception {try{process.invoke(mc);}catch(InvocationTargetException e){throw new AssertionError("native processKeyBinds failed",e.getCause());}}
 static void queue(int code,int count){for(int i=0;i<count;i++)KeyBinding.onTick(code);}
 static void rebind(int code){KeyBinding.unPressAllKeys();mc.gameSettings.keyBindInventory.setKeyCode(code);KeyBinding.resetKeyBindingArrayAndHash();}
 static StackTraceElement frame(String owner,String method){return new StackTraceElement(owner,method,"Minecraft.java",1);}
 static void predicates(){
  String owner=Minecraft.class.getName();
  check(LegacyInventoryActionHandler.isNativeInventoryAttempt(new StackTraceElement[]{frame(owner,"displayGuiScreen"),frame(owner,"processKeyBinds")}),"MCP native adjacent origin accepted");
  check(LegacyInventoryActionHandler.isNativeInventoryAttempt(new StackTraceElement[]{frame(owner,"func_147108_a"),frame(owner,"func_184117_aA")}),"SRG native adjacent origin accepted");
  check(!LegacyInventoryActionHandler.isNativeInventoryAttempt(new StackTraceElement[]{frame(owner,"displayGuiScreen"),frame("mod.Listener","open"),frame(owner,"displayGuiScreen"),frame(owner,"processKeyBinds")}),"nearest display frame rejects nested origin despite outer native pair");
  check(!LegacyInventoryActionHandler.isNativeInventoryAttempt(new StackTraceElement[]{frame("mod.Minecraft","displayGuiScreen"),frame(owner,"processKeyBinds")}),"lookalike owner rejected");
  check(!LegacyInventoryActionHandler.isNativeInventoryAttempt(new StackTraceElement[]{frame(owner,"displayGuiScreen")}),"truncated origin rejected");
 }
 public static void main(String[]args)throws Exception {
  check(InventoryActionNativeTest.class.getClassLoader()==Launch.classLoader,"genuine Forge transforming classloader");
  Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  ModMetadata metadata=new ModMetadata();metadata.modId="hbm";metadata.name="Inventory native action probe";metadata.version="1";
  DummyModContainer owner=new DummyModContainer(metadata);Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);
  mc=allocate(Minecraft.class);set(Minecraft.class,null,"instance",mc);mc.gameSettings=new GameSettings();
  player=allocate(Player.class);mc.player=player;controller=new Controller(mc);mc.playerController=controller;set(Minecraft.class,mc,"tutorial",new Tutorial(mc));
  // Controlled physical-input boundary only: the unchanged native Keyboard getter
  // reads an all-up buffer, so Forge modifier lookup can execute without a window.
  Field keys=Keyboard.class.getDeclaredField("keyDownBuffer");keys.setAccessible(true);ByteBuffer state=(ByteBuffer)keys.get(null);for(int i=0;i<state.capacity();i++)state.put(i,(byte)0);set(Keyboard.class,null,"created",true);
  process=Minecraft.class.getDeclaredMethod("processKeyBinds");process.setAccessible(true);
  MinecraftForge.EVENT_BUS.register(new Earlier());MinecraftForge.EVENT_BUS.register(new LegacyInventoryActionHandler(()->awards++));
  MinecraftForge.EVENT_BUS.register(new Nested());MinecraftForge.EVENT_BUS.register(new CancelAndObserve());
  predicates();
  int original=mc.gameSettings.keyBindInventory.getKeyCode();queue(original,1);run();
  check(awards==1&&guiEvents==1&&nativeAttempts==1,"native inventory key consumed and actual GUI event awards exactly once");
  check(mc.currentScreen==null&&!mc.gameSettings.keyBindInventory.isPressed(),"canceled GUI still awards attempt, native queue consumed");
  run();check(awards==1,"no queued action means no repeated award");
  KeyBinding.setKeyBindState(original,true);run();check(awards==1,"held key alone without isPressed queue does not award");KeyBinding.setKeyBindState(original,false);
  queue(original,3);run();check(awards==4&&guiEvents==4,"multiple native isPressed queue entries preserve every attempt");
  rebind(Keyboard.KEY_R);queue(original,1);run();check(awards==4,"old inventory key stops awarding after native rebinding/hash reset");
  queue(Keyboard.KEY_R,2);run();check(awards==6,"rebound keyboard queues dispatch actual native attempts");
  rebind(-97);queue(-97,2);run();check(awards==8,"rebound mouse button queues dispatch actual native attempts");
  cancelEarly=true;queue(-97,1);run();cancelEarly=false;check(awards==9,"earlier HIGHEST cancellation retained by receiveCanceled");
  controller.creative=true;queue(-97,1);run();controller.creative=false;check(awards==10,"creative native inventory attempt precedes any screen redirection");
  controller.horse=true;int eventsBefore=guiEvents;queue(-97,2);run();controller.horse=false;
  check(awards==10&&guiEvents==eventsBefore&&player.horseRequests==2,"horse branch sends horse inventory without normal inventory award");
  mc.displayGuiScreen(new GuiInventory(player));check(awards==10&&guiEvents==eventsBefore+1,"programmatic native inventory opening excluded");
  nested=true;eventsBefore=guiEvents;queue(-97,1);run();nested=false;
  check(awards==11&&guiEvents==eventsBefore+2,"subscriber nested inventory opening excluded while original key attempt remains");
  int nativeBefore=nativeAttempts;queue(mc.gameSettings.keyBindChat.getKeyCode(),1);run();check(awards==11&&nativeAttempts==nativeBefore+1,"native chat key shares caller frames but exact GUI class prevents inventory award");
  rebind(0);queue(0,1);run();check(awards==11,"unbound inventory key does not award");
  // Deliberately prove the external-mod boundary: equal-priority earlier GUI
  // replacement hides the original screen type. No current NTM subscriber does this.
  rebind(Keyboard.KEY_R);replaceEarly=true;queue(Keyboard.KEY_R,1);run();replaceEarly=false;
  check(awards==11,"documented earlier equal-priority GUI replacement compatibility limit reproduced");
  check(controller.resets>0,"native processKeyBinds reaches unchanged end-of-method controller path");
  System.out.println("PASS "+checks+" real native inventory action/queue/event checks. No full client, physical-input or external-mod compatibility claim.");
  System.out.println("LIMIT: earlier same-priority GuiOpenEvent replacement can hide the original inventory screen; native cancelled and nested cases are proven separately.");
 }
}
