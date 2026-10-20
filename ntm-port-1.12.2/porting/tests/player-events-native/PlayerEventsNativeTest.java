import java.util.*;
import com.hbm.main.*;
import com.hbm.config.GeneralConfig;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ShadyUtil;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
public class PlayerEventsNativeTest {
 static int checks;static void check(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
 static class W extends net.minecraft.world.World {
  final Map<BlockPos,IBlockState> states=new HashMap<>();final Map<BlockPos,TileEntity> tiles=new HashMap<>();final List<BlockPos> destroyed=new ArrayList<>();final List<Entity> spawned=new ArrayList<>();
  W(boolean remote,net.minecraft.world.WorldProvider provider){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),provider,new net.minecraft.profiler.Profiler(),remote);}
  W(){this(false,new net.minecraft.world.WorldProviderSurface());}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean e){return true;}
  public BlockPos getSpawnPoint(){return BlockPos.ORIGIN;}
  public IBlockState getBlockState(BlockPos p){return states.getOrDefault(p,Blocks.AIR.getDefaultState());}
  public TileEntity getTileEntity(BlockPos p){return tiles.get(p);}
  public boolean destroyBlock(BlockPos p,boolean drop){check(!drop,"original no block drops");destroyed.add(p);states.remove(p);tiles.remove(p);return true;}
  public boolean spawnEntity(Entity e){spawned.add(e);return true;}
 }
 static class P extends EntityPlayer {
  final List<DamageSource> sources=new ArrayList<>();final List<Float> amounts=new ArrayList<>();
  P(W w){super(w,new com.mojang.authlib.GameProfile(new UUID(0,1),"ProbePlayer"));}
  public net.minecraft.util.text.ITextComponent getDisplayName(){return new net.minecraft.util.text.TextComponentString("TEAM_PREFIX ProbePlayer TEAM_SUFFIX");}
  public String getDisplayNameString(){return "ProbePlayer";}
  public boolean isSpectator(){return false;}public boolean isCreative(){return false;}
  public boolean attackEntityFrom(DamageSource s,float a){sources.add(s);amounts.add(a);return false;}
 }
 static class R extends Random {int calls;public boolean nextBoolean(){return (++calls%2)==1;}}
 public static class Cancel { @SubscribeEvent(priority=EventPriority.HIGHEST) public void cancel(PlayerInteractEvent.RightClickBlock e){e.setCanceled(true);} }
 public static class Observer {int calls; @SubscribeEvent(receiveCanceled=true,priority=EventPriority.LOWEST)public void observe(PlayerInteractEvent.RightClickBlock e){if(e.isCanceled())calls++;} }
 static PlayerInteractEvent.RightClickBlock click(P p,BlockPos pos,EnumHand h){return new PlayerInteractEvent.RightClickBlock(p,h,pos,EnumFacing.UP,new Vec3d(pos));}
 static void reset(){ExplosionVNT.calls.clear();MainRegistry.logger.messages.clear();GeneralConfig.enable528ExplosiveEnergistics=false;GeneralConfig.enableExtendedLogging=false;WorldProviderCelestial.attemptingSleep=false;}
 static void interactions(EventBus bus) {
  Block foreign=new Block(net.minecraft.block.material.Material.ROCK);foreign.setRegistryName("appliedenergistics2","probe");
  Block nearPrefix=new Block(net.minecraft.block.material.Material.ROCK);nearPrefix.setRegistryName("appliedenergistics2extra","probe");
  BlockPos pos=new BlockPos(2,7,11),swapped=new BlockPos(2,11,7);
  for(EnumHand hand:EnumHand.values())for(Block block:new Block[]{foreign,nearPrefix}){
   reset();W w=new W();P p=new P(w);w.states.put(pos,block.getDefaultState());GeneralConfig.enable528ExplosiveEnergistics=true;
   PlayerInteractEvent.RightClickBlock event=click(p,pos,hand);check(bus.post(event),"native cancellation observed");check(w.destroyed.equals(Arrays.asList(pos)),"destroy actual xyz once");
   check(ExplosionVNT.calls.equals(Arrays.asList("create:2.5:7.5:11.5:5.0:null","processor:1.0:20.0","pierce:5.0:0.2","entity","player","effect:10:2.5:1.0","sfx","explode")),"original explosion arguments/order");check(ExplosionVNT.world==w,"same world");
   check(event.getCancellationResult()==EnumActionResult.PASS,"native default result retained, not silently replaced");
  }
  for(boolean remote:new boolean[]{false,true}){reset();W w=new W(remote,new net.minecraft.world.WorldProviderSurface());P p=new P(w);w.states.put(pos,foreign.getDefaultState());GeneralConfig.enable528ExplosiveEnergistics=remote;bus.post(click(p,pos,EnumHand.MAIN_HAND));check(w.destroyed.isEmpty()&&ExplosionVNT.calls.isEmpty(),"config/remote exclusion");}
  reset();W w=new W();P p=new P(w);w.states.put(pos,foreign.getDefaultState());GeneralConfig.enable528ExplosiveEnergistics=true;Cancel cancel=new Cancel();bus.register(cancel);bus.post(click(p,pos,EnumHand.MAIN_HAND));check(w.destroyed.isEmpty(),"already canceled event skipped");bus.unregister(cancel);
  bus.post(new PlayerInteractEvent.LeftClickBlock(p,pos,EnumFacing.UP,new Vec3d(pos)));bus.post(new PlayerInteractEvent.RightClickItem(p,EnumHand.MAIN_HAND));check(w.destroyed.isEmpty(),"left/item events do not trigger block listeners");
  for(EnumHand hand:EnumHand.values()){
   reset();w=new W();p=new P(w);TileEntitySign sign=new TileEntitySign();String[] words={"alpha","beta","gamma","delta"};for(int i=0;i<4;i++)sign.signText[i]=new net.minecraft.util.text.TextComponentString(words[i]);
   String hash=ShadyUtil.smoosh(words[0],words[1],words[2],words[3]);ShadyUtil.hashes.add(hash);w.tiles.put(pos,sign);bus.post(click(p,pos,hand));check(w.spawned.isEmpty(),"original swapped coordinate quirk retained");w.tiles.put(swapped,sign);PlayerInteractEvent.RightClickBlock event=click(p,pos,hand);bus.post(event);
   check(!event.isCanceled()&&w.destroyed.equals(Arrays.asList(swapped)),"sign removal does not cancel action");check(w.spawned.size()==1,"exactly one hidden item");EntityItem item=(EntityItem)w.spawned.get(0);check(item.getItem().getItem()==Items.DIAMOND&&item.getItem().getCount()==1,"original reward item/count");check(item.posX==2&&item.posY==11&&item.posZ==7,"original integer drop position");check(item.cannotPickup(),"one tick pickup delay");check(MainRegistry.logger.messages.size()==1&&MainRegistry.logger.messages.get(0).endsWith("by ProbePlayer!"),"unformatted player logging");ShadyUtil.hashes.remove(hash);
  }
  reset();w=new W();p=new P(w);w.states.put(pos,Blocks.LEVER.getDefaultState());GeneralConfig.enableExtendedLogging=true;bus.post(click(p,pos,EnumHand.MAIN_HAND));check(MainRegistry.logger.messages.equals(Arrays.asList("INFO:[DET] pulled lever at 2 / 7 / 11 by ProbePlayer!")),"lever log exact original");
 }
 static void sleeps(EventBus bus){
  for(int type=0;type<3;type++)for(boolean remote:new boolean[]{false,true})for(int dimension:new int[]{0,5})for(Block block:new Block[]{Blocks.BED,Blocks.STONE}){
   reset();net.minecraft.world.WorldProvider provider=type==0?new net.minecraft.world.WorldProviderSurface():type==1?new WorldProviderCelestial():new WorldProviderOrbit();W w=new W(remote,provider);provider.setDimension(dimension);P p=new P(w);w.states.put(BlockPos.ORIGIN,block.getDefaultState());bus.post(click(p,BlockPos.ORIGIN,EnumHand.MAIN_HAND));check(WorldProviderCelestial.attemptingSleep==(!remote&&dimension!=0&&type!=0&&block==Blocks.BED),"sleep dimension/provider/bed exact gate");
  }
 }
 static void food(EventBus bus)throws Exception {
  R random=new R();java.lang.reflect.Field f=ModEventHandler.class.getDeclaredField("rand");f.setAccessible(true);f.set(null,random);
  for(boolean remote:new boolean[]{false,true}){
   W w=new W(remote,new net.minecraft.world.WorldProviderSurface());P p=new P(w);ItemStack stack=new ItemStack(Items.APPLE);NBTTagCompound n=new NBTTagCompound();n.setBoolean("ntmCyanide",true);stack.setTagCompound(n);ItemStack result=new ItemStack(Items.BOWL);LivingEntityUseItemEvent.Finish e=new LivingEntityUseItemEvent.Finish(p,stack,32,result);int before=random.calls;bus.post(e);check(p.sources.size()==10&&random.calls==before+10,"ten attacks and RNG calls, original both sides");for(int i=0;i<10;i++){check(p.amounts.get(i)==1000F,"unchanged damage");check(p.sources.get(i)==(i%2==0?ModDamageSource.euthanizedSelf:ModDamageSource.euthanizedSelf2),"original RNG source branch");}check(e.getResultStack()==result&&e.getItem()==stack&&stack.getTagCompound()==n,"finish stack/result untouched");
   for(ItemStack rejected:new ItemStack[]{ItemStack.EMPTY,new ItemStack(Items.APPLE),new ItemStack(Items.STICK)}){rejected= rejected.copy();if(!rejected.isEmpty()&&rejected.getItem()==Items.STICK)rejected.setTagCompound(n.copy());int old=p.sources.size();bus.post(new LivingEntityUseItemEvent.Finish(p,rejected,32,ItemStack.EMPTY));check(p.sources.size()==old,"empty/nonpoison/nonfood exclusion");}
   net.minecraft.entity.passive.EntityCow cow=new net.minecraft.entity.passive.EntityCow(w);before=random.calls;bus.post(new LivingEntityUseItemEvent.Finish(cow,stack,32,ItemStack.EMPTY));check(random.calls==before,"new native nonplayer finish excluded");
  }
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="player events native proof";md.version="probe";net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(md);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
  EventBus bus=new EventBus();bus.register(new ModEventHandler());Observer observer=new Observer();bus.register(observer);interactions(bus);check(observer.calls==5,"native canceled observer dispatch");sleeps(bus);food(bus);System.out.println("PASS "+checks+" native player-event assertions; four exact methods, no whole-handler/game proof.");
 }
}
