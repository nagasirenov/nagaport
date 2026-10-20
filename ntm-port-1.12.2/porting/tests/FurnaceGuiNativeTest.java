import com.hbm.handler.GUIHandler;
import com.hbm.inventory.gui.GuiInfoContainer;
import com.hbm.inventory.gui.GUIMachineElectricFurnace;
import com.hbm.inventory.container.ContainerBase;
import com.hbm.inventory.SlotPattern;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import com.hbm.blocks.ModBlocks;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.*;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;
import com.mojang.authlib.GameProfile;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.security.MessageDigest;

/** Native types, no GL. Controlled test worlds, NTM dependencies and recording rendering sinks. */
public final class FurnaceGuiNativeTest {
 static int checks;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static sun.misc.Unsafe unsafe;
 static <T> T allocate(Class<T> c) throws Exception{return c.cast(unsafe.allocateInstance(c));}
 static void field(Object o,Class<?> c,String name,Object value)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 static Object fieldValue(Object o,Class<?> c,String name)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 static class TestWorld extends World {
  TileEntity tile; Block block=Blocks.AIR; Entity entity; int tileReads,blockReads,entityReads; BlockPos expected;
  TestWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);provider.setWorld(this);}
  protected IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allow){return false;}
  public TileEntity getTileEntity(BlockPos p){tileReads++;check(p.equals(expected),"native BlockPos tile lookup");return tile;}
  public IBlockState getBlockState(BlockPos p){blockReads++;return block.getDefaultState();}
  public Entity getEntityByID(int id){entityReads++;check(id==expected.getX(),"entity id uses x");return entity;}
 }
 static class Player extends EntityPlayer {
  Player(World w){super(w,new GameProfile(new UUID(0,1),"GuiTest"));}
  public boolean isSpectator(){return false;}public boolean isCreative(){return false;}
 }
 static final Container TOKEN=new Container(){public boolean canInteractWith(EntityPlayer p){return true;}};
 static final Object CLIENT=new Object();static final List<String> calls=new ArrayList<>();
 static Object route(String source,boolean client,int id,EntityPlayer p,World w,int x,int y,int z){calls.add(source+":"+client+":"+id+":"+x+":"+y+":"+z);return client?CLIENT:TOKEN;}
 static class ProviderTile extends TileEntity implements IGUIProvider {
  public Container provideContainer(int i,EntityPlayer p,World w,int x,int y,int z){return (Container)route("tile",false,i,p,w,x,y,z);}public Object provideGUI(int i,EntityPlayer p,World w,int x,int y,int z){return route("tile",true,i,p,w,x,y,z);}
 }
 static class ProviderBlock extends Block implements IGUIProvider {
  ProviderBlock(){super(Material.ROCK);}
  public Container provideContainer(int i,EntityPlayer p,World w,int x,int y,int z){return (Container)route("block",false,i,p,w,x,y,z);}public Object provideGUI(int i,EntityPlayer p,World w,int x,int y,int z){return route("block",true,i,p,w,x,y,z);}
 }
 static class ProviderItem extends Item implements IGUIProvider {
  public Container provideContainer(int i,EntityPlayer p,World w,int x,int y,int z){return (Container)route("item",false,i,p,w,x,y,z);}public Object provideGUI(int i,EntityPlayer p,World w,int x,int y,int z){return route("item",true,i,p,w,x,y,z);}
 }
 static class ProviderEntity extends Entity implements IGUIProvider {
  ProviderEntity(World w){super(w);}protected void entityInit(){}protected void readEntityFromNBT(NBTTagCompound n){}protected void writeEntityToNBT(NBTTagCompound n){}
  public Container provideContainer(int i,EntityPlayer p,World w,int x,int y,int z){return (Container)route("entity",false,i,p,w,x,y,z);}public Object provideGUI(int i,EntityPlayer p,World w,int x,int y,int z){return route("entity",true,i,p,w,x,y,z);}
 }
 static class RecordingGui extends GuiInfoContainer {
  List<String> tooltip;int tx,ty;ItemStack hovered;int hoverCalls;
  protected void renderToolTip(ItemStack s,int x,int y){hovered=s;hoverCalls++;tx=x;ty=y;}
  void hover(int x,int y){renderHoveredToolTip(x,y);}
  RecordingGui(){super(TOKEN);}protected void drawGuiContainerBackgroundLayer(float f,int x,int y){}
  public void drawHoveringText(List<String> t,int x,int y){tooltip=t;tx=x;ty=y;}
  void rect(int x,int y){guiLeft=x;guiTop=y;}boolean clickAt(int x,int y,int l,int t,int w,int h){return checkClick(x,y,l,t,w,h);}
 }
 static class RecordingFont extends FontRenderer {
  List<String> draws;
  RecordingFont(){super(null,null,null,false);}
  public int getStringWidth(String s){return s.length()*6;}
  public int drawString(String s,int x,int y,int color){draws.add(s+":"+x+":"+y+":"+color);return 0;}
 }
 static class RecordingSound extends SoundHandler {
  ISound sound;RecordingSound(){super(null,null);}public void playSound(ISound s){sound=s;}
 }
 static class TooltipItem extends Item {
  ITooltipFlag flag; EntityPlayer player;
  public String getItemStackDisplayName(ItemStack s){return "Fixture item";}
  public EnumRarity getRarity(ItemStack s){return EnumRarity.RARE;}
  public void addInformation(ItemStack s,World w,List<String> lines,ITooltipFlag f){flag=f;lines.add("Fixture detail");}
 }
 static class Upgrades extends TileEntity implements IUpgradeInfoProvider {
  public boolean canProvideInfo(UpgradeType t,int level,boolean extended){check(level==0&&!extended,"GUI upgrade query flags");return t!=UpgradeType.FORTUNE;}
  public void provideInfo(UpgradeType t,int l,List<String> lines,boolean e){}
  public HashMap<UpgradeType,Integer> getValidUpgrades(){HashMap<UpgradeType,Integer> r=new HashMap<>();for(UpgradeType t:UpgradeType.values())r.put(t,t.ordinal()+1);return r;}
 }
 static class FurnaceView extends GUIMachineElectricFurnace {
  FurnaceView(){super(null,null);}void foreground(){drawGuiContainerForegroundLayer(0,0);}void rect(){xSize=176;ySize=166;}
 }
 /** Verbatim original production tail AFTER GL color, texture binding and base panel. */
 static class BackgroundTail {
  TileEntityMachineElectricFurnace diFurnace;int guiLeft=10,guiTop=20;List<String> draws=new ArrayList<>();
  void drawTexturedModalRect(int x,int y,int u,int v,int w,int h){draws.add(x+","+y+","+u+","+v+","+w+","+h);}
  void drawInfoPanel(int x,int y,int w,int h,int type){draws.add("panel:"+x+","+y+","+w+","+h+","+type);}
  void draw(){
// @FURNACE_BACKGROUND_TAIL@
  }
 }
 static String hash(InputStream input)throws Exception{try(InputStream in=input){MessageDigest d=MessageDigest.getInstance("SHA-256");byte[] b=new byte[4096];for(int n;(n=in.read(b))!=-1;)d.update(b,0,n);StringBuilder s=new StringBuilder();for(byte b1:d.digest())s.append(String.format("%02x",b1&255));return s.toString();}}
 static void routing()throws Exception{
  GUIHandler h=new GUIHandler();TestWorld w=new TestWorld(),pw=new TestWorld();BlockPos pos=new BlockPos(-13,79,400);w.expected=pw.expected=pos;Player p=new Player(pw);ProviderItem item=new ProviderItem();ItemStack stack=new ItemStack(item);p.setHeldItem(EnumHand.MAIN_HAND,stack);pw.entity=new ProviderEntity(pw);w.entity=new ProviderEntity(w);w.block=new ProviderBlock();w.tile=new ProviderTile();
  for(boolean client:new boolean[]{false,true})for(int stage=0;stage<5;stage++){
   calls.clear();w.tile=stage==0?new ProviderTile():null;w.block=stage<=1?new ProviderBlock():Blocks.AIR;p.setHeldItem(EnumHand.MAIN_HAND,stage<=2?stack:ItemStack.EMPTY);pw.entity=stage<=3?new ProviderEntity(pw):null;
   Object r=client?h.getClientGuiElement(47,p,w,-13,79,400):h.getServerGuiElement(47,p,w,-13,79,400);
   if(stage<4){check(r==(client?CLIENT:TOKEN),"provider return identity");check(calls.equals(Arrays.asList(new String[]{"tile","block","item","entity"}[stage]+":"+client+":47:-13:79:400")),"routing priority and argument preservation");}else check(r==null&&calls.isEmpty(),"no provider returns null");
  }
  check(w.entityReads==0&&pw.entityReads==4,"entity lookup uses player's world, not supplied world");
  p.setHeldItem(EnumHand.OFF_HAND,stack);check(h.getServerGuiElement(0,p,w,-13,79,400)==null,"offhand does not change original mainhand routing");
  p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(item,0));check(h.getClientGuiElement(0,p,w,-13,79,400)==null,"native empty is not item provider");
 }
 static void guiLogic()throws Exception{
  RecordingGui g=new RecordingGui();g.rect(100,200);g.inventorySlots=new ContainerBase();
  for(int x=8;x<19;x++)for(int y=18;y<29;y++){
   g.tooltip=null;g.drawCustomInfoStat(x,y,10,20,6,6,77,88,"hello");boolean hit=x>=10&&x<16&&y>20&&y<=26;check((g.tooltip!=null)==hit,"custom tooltip asymmetric bounds");if(hit)check(g.tx==77&&g.ty==88&&g.tooltip.equals(Arrays.asList("hello")),"tooltip payload and target");
   check(g.clickAt(x+100,y+200,10,20,6,6)==hit,"click rectangle offsets");
  }
  g.drawInfo(new String[]{"a","b"},7,9);check(g.tooltip.equals(Arrays.asList("a","b"))&&g.tx==7&&g.ty==9,"drawInfo forwards lines");
  g.drawElectricityInfo(g,11,22,10,20,6,6,17,900);check(g.tooltip.equals(Arrays.asList("short:17/short:900HE")),"electricity formatter boundary");
  ContainerBase c=(ContainerBase)g.inventorySlots;TileEntityMachineElectricFurnace tile=new TileEntityMachineElectricFurnace();tile.setPos(new BlockPos(-33,64,203));c.tile=tile;SlotPattern slot=new SlotPattern(tile,2,10,20);c.slot(slot);
  for(int x=108;x<=127;x++)for(int y=218;y<=237;y++)check((g.getSlotAtPosition(x,y)==slot)==(x>=109&&x<127&&y>=219&&y<237),"native slot hitbox includes one-pixel border");
  ItemStack s=new ItemStack(Items.DIAMOND,7);NBTTagCompound data=new NBTTagCompound();data.setString("marker","identity");s.setTagCompound(data);
  check(g.handleDragNDrop(g,110,220,s,1),"pattern accepts positive stack");NBTControlPacket packet=PacketDispatcher.wrapper.packet;check(packet.x==-33&&packet.y==64&&packet.z==203&&packet.tag.getInteger("slot")==0,"native TE position / container slot number");ItemStack decoded=new ItemStack(packet.tag.getCompoundTag("stack"));check(decoded.getItem()==Items.DIAMOND&&decoded.getCount()==7&&decoded.getTagCompound().equals(data),"NEI payload retains item/count/NBT");check(s.getCount()==7&&s.getTagCompound()==data,"NEI does not mutate source aliases");
  int sends=PacketDispatcher.wrapper.sends;check(!g.handleDragNDrop(g,110,220,null,0)&&!g.handleDragNDrop(g,110,220,ItemStack.EMPTY,0)&&!g.handleDragNDrop(g,110,220,new ItemStack(Items.DIAMOND,0),0),"null/empty/zero drag ignored");check(PacketDispatcher.wrapper.sends==sends,"empty drag sends no packet");check(!g.handleDragNDrop(g,0,0,s,0),"out-of-slot drag ignored");
  check(g.getUpgradeInfo(new TileEntity(){}).isEmpty(),"non-provider upgrade info empty");check(g.getUpgradeInfo(new Upgrades()).equals(Arrays.asList("upgrade.gui.title[]","upgrade.gui.speed[1]","upgrade.gui.effectiveness[2]","upgrade.gui.power[3]","upgrade.gui.afterburner[5]","upgrade.gui.overdrive[6]")),"upgrade title/order/levels");
  check(!g.hideItemPanelSlot(g,0,0,0,0)&&g.getInventoryAreas(g).isEmpty()&&g.getItemSpawnSlots(g,s)==null,"NEI neutral methods retained");codechicken.nei.VisibilityData vd=new codechicken.nei.VisibilityData();check(g.modifyVisibility(g,vd)==vd,"NEI visibility alias");
  g.setZLevel(72.5F);check(g.getZLevel()==72.5F&&g.getGuiLeft()==100&&g.getGuiTop()==200,"GUI accessor fields");
  Minecraft mc=allocate(Minecraft.class);GameSettings settings=allocate(GameSettings.class);mc.gameSettings=settings;g.mc=mc;TooltipItem tooltipItem=new TooltipItem();net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(tooltipItem.setRegistryName("hbm_gui_fixture"));ItemStack tooltipStack=new ItemStack(tooltipItem);
  mc.player=allocate(net.minecraft.client.entity.EntityPlayerSP.class);field(mc.player,EntityPlayer.class,"inventory",new net.minecraft.entity.player.InventoryPlayer(mc.player));InventoryBasic hoverInventory=new InventoryBasic("hover",false,1);hoverInventory.setInventorySlotContents(0,tooltipStack);Slot hoverSlot=new Slot(hoverInventory,0,0,0);field(g,GuiContainer.class,"hoveredSlot",hoverSlot);g.hover(12,34);check(g.hovered==tooltipStack&&g.hoverCalls==1&&g.tx==12&&g.ty==34,"native hovered tooltip gate and alias");mc.player.inventory.setItemStack(new ItemStack(Items.STICK));g.hover(12,34);check(g.hoverCalls==1,"held cursor suppresses tooltip");mc.player.inventory.setItemStack(ItemStack.EMPTY);hoverInventory.clear();g.hover(12,34);check(g.hoverCalls==1,"empty slot suppresses tooltip");field(g,GuiContainer.class,"hoveredSlot",null);g.hover(12,34);check(g.hoverCalls==1,"missing slot suppresses tooltip");
  for(boolean advanced:new boolean[]{false,true}){settings.advancedItemTooltips=advanced;List<String> lines=g.getItemToolTip(tooltipStack);check(tooltipItem.flag==(advanced?ITooltipFlag.TooltipFlags.ADVANCED:ITooltipFlag.TooltipFlags.NORMAL),"native tooltip flag exact enum");check(lines.get(0).startsWith(EnumRarity.RARE.color+"Fixture item"),"native rarity first-line color");check(lines.get(1).equals(net.minecraft.util.text.TextFormatting.GRAY+"Fixture detail"),"native gray detail color");}
  RecordingSound sound=allocate(RecordingSound.class);field(mc,Minecraft.class,"soundHandler",sound);g.click();check(sound.sound.getSoundLocation().equals(SoundEvents.UI_BUTTON_CLICK.getSoundName()),"native button sound identity");check(sound.sound.getCategory()==SoundCategory.MASTER&&((Float)fieldValue(sound.sound,PositionedSound.class,"pitch"))==1F&&((Float)fieldValue(sound.sound,PositionedSound.class,"volume"))==0.25F&&!sound.sound.canRepeat(),"native button sound category/pitch/volume/repeat");
  // Real native renderer exits before GL for EMPTY; no synthetic icon or count recovery.
  field(null,Minecraft.class,"instance",mc);
  net.minecraft.client.renderer.RenderItem renderer=allocate(net.minecraft.client.renderer.RenderItem.class);
  for(ItemStack empty:new ItemStack[]{ItemStack.EMPTY,new ItemStack(Items.DIAMOND,0),new ItemStack(Items.DIAMOND,-3)}){
   check(empty.isEmpty()&&empty.getCount()==0&&empty.getItem()!=Items.DIAMOND,"native empty loses public original item/count identity");
   renderer.zLevel=37F;renderer.renderItemAndEffectIntoGUI(empty,11,12);renderer.renderItemOverlayIntoGUI(null,empty,11,12,"label");check(renderer.zLevel==37F,"native EMPTY renderer returns before drawing or changing layer");
  }

 }
 static void furnace()throws Exception{
  TestWorld w=new TestWorld();w.expected=new BlockPos(3,4,5);TileEntityMachineElectricFurnace old=new TileEntityMachineElectricFurnace(),fresh=new TileEntityMachineElectricFurnace();old.setWorld(w);old.setPos(w.expected);fresh.setWorld(w);fresh.setPos(w.expected);w.tile=fresh;fresh.powered=true;fresh.power=50;fresh.progress=13;w.block=ModBlocks.machine_electric_furnace_on;old.invalidate();BackgroundTail b=new BackgroundTail();b.diFurnace=old;b.draw();check(fresh.lastPowerScale==52&&fresh.lastProgressScale==24,"furnace scale arguments remain 52 and 24");check(b.diFurnace==fresh&&w.tileReads==2,"invalid tile refreshed with original two lookups");check(b.draws.equals(Arrays.asList("30,63,200,26,16,26","66,55,176,0,16,16","89,54,176,17,14,17","panel:161,39,8,8,8")),"power/flame/progress/info coordinates and scales");
  fresh.powered=false;fresh.progress=0;w.block=Blocks.FURNACE;b.draws.clear();b.draw();check(w.tileReads==2,"valid tile is not refreshed");check(b.draws.equals(Arrays.asList("89,54,176,17,1,17","panel:161,39,8,8,8")),"unpowered/inactive zero progress is one pixel");
  fresh.invalidate();w.tile=new TileEntity(){};b.draws.clear();b.draw();check(b.diFurnace==fresh,"invalid tile not replaced by incompatible type");
  FurnaceView g=new FurnaceView();check(g.getXSize()==176&&g.getYSize()==166,"production furnace constructor dimensions");g.rect();field(g,GUIMachineElectricFurnace.class,"diFurnace",fresh);RecordingFont font=allocate(RecordingFont.class);font.draws=new ArrayList<>();field(g,net.minecraft.client.gui.GuiScreen.class,"fontRenderer",font);fresh.name="Named furnace";fresh.custom=true;g.foreground();check(font.draws.get(0).equals("Named furnace:49:6:4210752"),"custom name centered with original color");check(font.draws.get(1).endsWith(":8:72:4210752"),"player inventory label position/color");check(g.getFontRenderer()==font,"native font accessor alias");fresh.custom=false;fresh.name="container.test";font.draws.clear();g.foreground();check(font.draws.get(0).equals("container.test:46:6:4210752"),"noncustom translated name branch");
 }
 public static void main(String[] args)throws Exception{
  Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=(sun.misc.Unsafe)f.get(null);Bootstrap.register();field(null,net.minecraft.client.resources.I18n.class,"i18nLocale",new net.minecraft.client.resources.Locale());routing();guiLogic();furnace();
  ResourceLocation location=new ResourceLocation("hbm:textures/gui/GUIElectricFurnace.png");check(location.getPath().equals("textures/gui/guielectricfurnace.png"),"native lowercases historical alias");
  for(IResourcePack pack:new IResourcePack[]{new FolderResourcePack(new File(args[0])),new FileResourcePack(new File(args[1],"native.zip"))}){check(pack.resourceExists(location),"actual resource pack lookup");check(hash(pack.getInputStream(location)).equals(args[2]),"actual package texture byte identity");}
  for(IResourcePack pack:new IResourcePack[]{new FolderResourcePack(new File(args[1],"legacy")),new FileResourcePack(new File(args[1],"legacy.zip"))})check(!pack.resourceExists(location),"historical mixed-case package fails native lookup");
  check(javax.imageio.ImageIO.read(new File(args[0],"assets/hbm/textures/gui/guielectricfurnace.png")).getWidth()==256,"PNG decodes with original atlas width");
  System.out.println("PASS "+checks+" native GUI/router/resource checks. Complete classes compile; no GL draw, real client, packet dispatch or playable machine claim.");
 }
}
