import api.hbm.block.ICrucibleAcceptor;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDynamicSlag.TileEntitySlag;
import com.hbm.blocks.machine.FoundryChannel.FoundryNode;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.material.*;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMold;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.machine.*;
import com.hbm.lib.ForgeDirection;
import com.hbm.uninos.*;
import com.hbm.uninos.networkproviders.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.entity.Entity;
import net.minecraft.init.*;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import sun.misc.Unsafe;

/** Real native World/Chunk/tile/NBT/packet/raytrace APIs. NTM registry/recipe/transport
 * boundaries are explicit in the runner, not alternative Minecraft implementations. */
public class NativeFoundryTilesTest {
 static int checks;static final BlockPos P=new BlockPos(4,20,4);static NTMMaterial iron,copper;static ItemMold molds;
 static Host channel,tank,outlet,slagtap,basin,mold,slag;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static void same(Object a,Object b,String message){check(a==b,message);}
 static void set(Object o,Class<?> c,String name,Object value)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 static class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();final List<String> notifications=new ArrayList<>();int dirty,renders,scheduled;boolean powered;
  NativeWorld(boolean client){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),client);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  @Override protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "foundry native chunks, no generation";}public boolean isChunkGeneratedAt(int x,int z){return chunks.containsKey(key(x,z));}};}
  @Override protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return chunks.containsKey(key(x,z));}
  @Override public boolean checkLight(BlockPos pos){return true;}
  @Override public void notifyBlockUpdate(BlockPos pos,IBlockState oldState,IBlockState newState,int flags){notifications.add(pos+":"+flags);super.notifyBlockUpdate(pos,oldState,newState,flags);}
  @Override public void markBlockRangeForRenderUpdate(int x1,int y1,int z1,int x2,int y2,int z2){renders++;super.markBlockRangeForRenderUpdate(x1,y1,z1,x2,y2,z2);}
  @Override public void markChunkDirty(BlockPos pos,TileEntity tile){dirty++;super.markChunkDirty(pos,tile);}
  @Override public void scheduleUpdate(BlockPos pos,Block block,int delay){check(block==ModBlocks.slag&&delay==1,"slag preserves exact one-tick block schedule");scheduled++;}
  @Override public boolean isBlockPowered(BlockPos pos){return powered;}
  void clear(){notifications.clear();dirty=0;renders=0;scheduled=0;PacketThreading.packets.clear();PacketThreading.points.clear();}
  <T extends TileEntity>T put(BlockPos pos,Host owner){setBlockState(pos,owner.getDefaultState(),3);return (T)getTileEntity(pos);}
 }
 static class Host extends BlockContainer implements ICrucibleAcceptor {
  static final PropertyInteger META=PropertyInteger.create("metadata",0,15);final Supplier<? extends TileEntity> factory;
  Host(Supplier<? extends TileEntity> factory){super(Material.ROCK);this.factory=factory;setDefaultState(blockState.getBaseState().withProperty(META,0));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,META);}
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(META,meta);}
  @Override public int getMetaFromState(IBlockState state){return state.getValue(META);}
  @Override public TileEntity createNewTileEntity(World w,int metadata){return factory.get();}
  ICrucibleAcceptor acceptor(World w,int x,int y,int z){return (ICrucibleAcceptor)w.getTileEntity(new BlockPos(x,y,z));}
  public boolean canAcceptPartialFlow(World w,int x,int y,int z,ForgeDirection side,MaterialStack s){return this!=tank&&this!=basin&&acceptor(w,x,y,z).canAcceptPartialFlow(w,x,y,z,side,s);}
  public MaterialStack flow(World w,int x,int y,int z,ForgeDirection side,MaterialStack s){return this==tank||this==basin?s:acceptor(w,x,y,z).flow(w,x,y,z,side,s);}
  public boolean canAcceptPartialPour(World w,int x,int y,int z,double a,double b,double c,ForgeDirection side,MaterialStack s){return acceptor(w,x,y,z).canAcceptPartialPour(w,x,y,z,a,b,c,side,s);}
  public MaterialStack pour(World w,int x,int y,int z,double a,double b,double c,ForgeDirection side,MaterialStack s){return acceptor(w,x,y,z).pour(w,x,y,z,a,b,c,side,s);}
 }
 static Host host(String name,Supplier<? extends TileEntity> factory,Class<? extends TileEntity> type){Host h=new Host(factory);h.setRegistryName("hbm",name);ForgeRegistries.BLOCKS.register(h);TileEntity.register("hbm:"+name,type);return h;}
 static void setup(){
  iron=new NTMMaterial(11,new DictFrame("ProbeIron")).setMoltenColor(0xEAB167).smeltable(NTMMaterial.SmeltingBehavior.SMELTABLE);copper=new NTMMaterial(12,new DictFrame("ProbeCopper")).setMoltenColor(0xFF6030).smeltable(NTMMaterial.SmeltingBehavior.SMELTABLE);
  molds=new ItemMold();molds.setRegistryName("hbm","probe_mold");ForgeRegistries.ITEMS.register(molds);ModItems.mold=molds;
  ItemStack output=new ItemStack(Items.IRON_INGOT,3);NBTTagCompound tag=new NBTTagCompound();tag.setString("cast","original-output-copy");output.setTagCompound(tag);
  molds.molds.put(0,new ItemMold.Mold(0,72,iron,output));molds.molds.put(1,new ItemMold.Mold(1,648,iron,new ItemStack(Blocks.IRON_BLOCK)));molds.molds.put(2,new ItemMold.Mold(0,18,copper,new ItemStack(Items.GOLD_INGOT)));
  channel=host("probe_channel",TileEntityFoundryChannel::new,TileEntityFoundryChannel.class);ModBlocks.foundry_channel=channel;tank=host("probe_tank",TileEntityFoundryTank::new,TileEntityFoundryTank.class);outlet=host("probe_outlet",TileEntityFoundryOutlet::new,TileEntityFoundryOutlet.class);slagtap=host("probe_slagtap",TileEntityFoundrySlagtap::new,TileEntityFoundrySlagtap.class);basin=host("probe_basin",TileEntityFoundryBasin::new,TileEntityFoundryBasin.class);mold=host("probe_cast",TileEntityFoundryMold::new,TileEntityFoundryMold.class);slag=host("probe_slag",TileEntitySlag::new,TileEntitySlag.class);ModBlocks.slag=slag;
 }
 static MaterialStack stack(int amount){return new MaterialStack(iron,amount);}
 static void baseAndSync()throws Exception {
  NativeWorld server=new NativeWorld(false),client=new NativeWorld(true);TileEntityFoundryTank a=server.put(P,tank),b=client.put(P,tank);server.clear();client.clear();
  check(a.getCapacity()==2592,"tank exact four-block quantum capacity");
  for(int stored:new int[]{0,1,100,2591,2592})for(int input:new int[]{0,1,7,2592}){
   a.type=stored==0?copper:iron;a.amount=stored;MaterialStack in=stack(input);check(a.standardCheck(server,4,20,4,ForgeDirection.NORTH,in)==(stored<2592),"base original type/capacity predicate");
   if(stored<2592){MaterialStack result=a.standardAdd(server,4,20,4,ForgeDirection.NORTH,in);int sum=stored+input;check(a.amount==Math.min(sum,2592)&&a.type==iron,"exact base accepted accounting");check(result==(sum<=2592?null:in),"original leftover identity/null contract");check(in.amount==(sum<=2592?input:sum-2592),"full accept leaves input amount; overflow mutates only leftover");}
  }
  a.type=iron;a.amount=3;check(!a.standardCheck(server,4,20,4,ForgeDirection.UP,new MaterialStack(copper,1)),"different nonempty material rejected");
  for(ForgeDirection side:ForgeDirection.values())check(a.canAcceptPartialPour(server,4,20,4,0,0,0,side,stack(1))==(side==ForgeDirection.UP),"only UP pour accepted");
  a.amount=57;a.muffled=true;a.nextUpdate=19;server.clear();a.update();check(server.notifications.size()==1&&server.renders==0,"server changed payload queues one same-block native update");a.update();check(server.notifications.size()==1,"unchanged payload no extra update");
  SPacketUpdateTileEntity packet=a.getUpdatePacket();PacketBuffer encoded=new PacketBuffer(io.netty.buffer.Unpooled.buffer());packet.writePacketData(encoded);SPacketUpdateTileEntity decoded=new SPacketUpdateTileEntity();decoded.readPacketData(encoded);b.onDataPacket(null,decoded);same(b.type,iron,"real packet resolves material registry identity");check(b.amount==57&&b.muffled&&b.getPos().equals(P),"real serialized native packet preserves full foundry state");b.update();check(client.renders==1&&client.notifications.isEmpty(),"client tick invalidates only render range after sync");b.update();check(client.renders==1,"unchanged client suppresses repeated redraw");
  NBTTagCompound tag=a.getUpdateTag();TileEntityFoundryTank initial=new TileEntityFoundryTank();initial.handleUpdateTag(tag);check(initial.amount==57&&initial.type==iron&&initial.muffled,"initial native chunk tag has complete same payload");check(!tag.hasKey("nextUpdate")&&!tag.hasKey("lastAmount"),"original transient timer/cache fields remain unsaved");
  check(!a.shouldRefresh(server,P,tank.getStateFromMeta(0),tank.getStateFromMeta(5))&&a.shouldRefresh(server,P,tank.getDefaultState(),channel.getDefaultState()),"native same-block metadata retains tile; distinct block replaces it");
  server.setBlockState(P,tank.getStateFromMeta(5),3);same(server.getTileEntity(P),a,"actual native chunk retains filled tile across property change");
  TileEntity recreated=TileEntity.create(server,tag);check(recreated instanceof TileEntityFoundryTank&&((TileEntityFoundryTank)recreated).amount==57,"real native tile registry NBT reload constructs complete owner");
  TileEntityFoundryMold casting=client.put(P.east(2),mold);casting.type=iron;casting.amount=0;client.clear();casting.update();check(client.renders==0,"casting false client-render gate suppresses type-only change");casting.amount=1;casting.update();check(client.renders==1,"original precedence still redraws amount change despite false type gate");
  a.type=null;a.amount=0;check(a.writeToNBT(new NBTTagCompound()).getInteger("type")==-1,"empty material writes original -1 sentinel");
 }
 static void casting() {
  for(boolean large:new boolean[]{false,true}){
   NativeWorld w=new NativeWorld(false);TileEntityFoundryCastingBase cast=w.put(P,large?basin:mold);int id=large?1:0,cost=large?648:72;
   check(cast.isEmpty()&&cast.getStackInSlot(0)==ItemStack.EMPTY&&cast.getCapacity()==0,"native inventory empty boundary with nullable domain storage");cast.setInventorySlotContents(0,ItemStack.EMPTY);check(cast.slots[0]==null,"shared EMPTY ingress never contaminates nullable domain");
   cast.setInventorySlotContents(0,new ItemStack(molds,1,large?0:1));check(cast.getInstalledMold()==null&&cast.getCapacity()==0,"wrong-size original mold rejected");cast.setInventorySlotContents(0,new ItemStack(molds,1,id));check(cast.getCapacity()==cost,"installed mold exact quantum capacity");
   check(cast.standardCheck(w,4,20,4,ForgeDirection.UP,stack(cost)),"valid material has actual recipe output");check(!cast.standardCheck(w,4,20,4,ForgeDirection.UP,new MaterialStack(copper,1)),"recipe material mismatch rejected");
   cast.type=iron;cast.amount=cost;w.clear();for(int i=0;i<99;i++)cast.update();check(cast.cooloff==1&&cast.slots[1]==null&&cast.amount==cost,"first cast retains original 100 tick initial cooloff");cast.update();check(cast.cooloff==200&&cast.amount==0&&cast.slots[1]!=null&&cast.type==iron,"cast consumes exact amount and keeps original one-tick stale material");check(cast.slots[1]!=molds.molds.get(id).output,"cast output is copied, recipe prototype unchanged");check(w.dirty==1,"only successful cast retains original markDirty site");cast.update();check(cast.type==null,"following server tick clears consumed material");
   check(!cast.standardCheck(w,4,20,4,ForgeDirection.UP,stack(cost)),"occupied output blocks refill");
   ItemStack saved=cast.slots[1];NBTTagCompound nbt=cast.writeToNBT(new NBTTagCompound());check(!nbt.hasKey("cooloff")&&nbt.getTagList("items",10).tagCount()==2,"original item list and unsaved cooloff retained");
   TileEntityFoundryCastingBase copy=large?new TileEntityFoundryBasin():new TileEntityFoundryMold();copy.readFromNBT(nbt);check(copy.slots[0].getItem()==molds&&ItemStack.areItemStacksEqual(copy.slots[1],saved)&&copy.cooloff==100,"actual native item NBT reload and initial transient cooldown");
   copy.slots[0]=new ItemStack(Items.DIAMOND);copy.readFromNBT(new NBTTagCompound());check(copy.slots[0]==null&&copy.slots[1]==null,"casting original NBT reset not shared inventory overlay semantics");
   same(cast.removeStackFromSlot(1),saved,"output remove returns original nonempty stack");check(cast.removeStackFromSlot(1)==ItemStack.EMPTY&&cast.decrStackSize(1,1)==ItemStack.EMPTY,"native inventory missing removals return EMPTY");
   cast.setInventorySlotContents(1,new ItemStack(Items.IRON_INGOT,100));check(cast.slots[1].getCount()==64,"original inventory stack cap retained");ItemStack part=cast.decrStackSize(1,3);check(part.getCount()==3&&cast.slots[1].getCount()==61,"native split exact accounting");cast.decrStackSize(1,61);check(cast.slots[1]==null,"native exact extraction clears nullable slot");
   cast.type=iron;cast.amount=cost+20;cast.cooloff=2;cast.update();check(cast.amount==cost&&cast.cooloff==1,"original oversize clamp precedes cooldown");cast.update();check(cast.amount==0&&cast.cooloff==200,"following full cast after original clamp");
   for(net.minecraft.util.EnumFacing face:net.minecraft.util.EnumFacing.values())check(Arrays.equals(cast.getSlotsForFace(face),new int[]{1})&&!cast.canInsertItem(0,new ItemStack(molds),face)&&cast.canExtractItem(1,saved,face)&&!cast.canExtractItem(0,saved,face),"native sided inventory extracts output only");
   check(!cast.isUsableByPlayer(null)&&!cast.isItemValidForSlot(0,new ItemStack(molds))&&!cast.hasCustomName()&&cast.getName().equals("ntmFoundry")&&cast.getFieldCount()==0,"native IInventory retains no GUI/no insertion domain");
   IRenderFoundry rendered=(IRenderFoundry)cast;cast.amount=cost;cast.type=iron;check(rendered.shouldRender()&&rendered.getLevel()==(large?.875:.375),"basin/mold exact surface formula");check(rendered.minX()==.125&&rendered.maxX()==.875&&rendered.moldHeight()==.13&&rendered.outHeight()==(large?.875:.25),"unchanged rendering bounds and heights");
   if(large){MaterialStack in=stack(5);check(!cast.canAcceptPartialFlow(w,4,20,4,ForgeDirection.NORTH,in)&&cast.flow(w,4,20,4,ForgeDirection.NORTH,in)==in,"basin still refuses side flow without mutation");}
   NativeWorld client=new NativeWorld(true);TileEntityFoundryCastingBase remote=client.put(P,large?basin:mold);remote.slots[0]=new ItemStack(molds,1,id);remote.type=iron;remote.amount=cost+20;remote.cooloff=1;remote.update();check(remote.amount==cost+20&&remote.cooloff==1&&remote.slots[1]==null,"client never clamps/consumes/cools or produces items");
  }
 }
 static void seed(Random random,int bound,int result){for(long seed=0;;seed++){Random test=new Random(seed);if(test.nextInt(bound)==result){random.setSeed(seed);return;}}}
 static void transfers() {
  for(boolean channelCase:new boolean[]{false,true})for(boolean swap:new boolean[]{false,true}){
   NativeWorld w=new NativeWorld(false);Host host=channelCase?channel:tank;TileEntityFoundryBase a=w.put(P,host),b=w.put(P.east(),host);a.type=iron;a.amount=30;b.type=iron;b.amount=10;
   if(channelCase){seed(w.rand,5,swap?0:1);((TileEntityFoundryChannel)a).update();}else{for(long seed=0;;seed++){Random r=new Random(seed);r.nextInt(6);if((r.nextInt(5)==0)==swap){w.rand.setSeed(seed);break;}}((TileEntityFoundryTank)a).update();}
   check(a.amount==(swap?10:20)&&b.amount==(swap?30:20),"exact neighbor "+(channelCase?"channel":"tank")+" "+(swap?"swap":"integer equalize"));check(a.amount+b.amount==40,"neighbor transfer conserves original quanta");
   if(channelCase){TileEntityFoundryChannel c=(TileEntityFoundryChannel)b;check(c.lastFlow==ForgeDirection.WEST.ordinal(),"channel remembers opposite inflow side");check(UniNodespace.getNode(w,5,20,4,FoundryNetworkProvider.THE_PROVIDER)!=null,"neighbor init creates actual nodespace entry");}
  }
  NativeWorld w=new NativeWorld(false);TileEntityFoundryTank upper=w.put(P,tank),below=w.put(P.down(),tank),side=w.put(P.east(),tank);upper.type=iron;upper.amount=200;below.type=iron;below.amount=2500;upper.update();check(below.amount==2592&&upper.amount==108&&side.amount==0,"downward tank fill has priority and exact remainder");
  NativeWorld flow=new NativeWorld(false);TileEntityFoundryChannel src=flow.put(P,channel);TileEntityFoundryMold sink=flow.put(P.east(),mold);sink.slots[0]=new ItemStack(molds,1,0);src.type=iron;src.amount=30;sink.type=iron;sink.amount=60;src.update();check(sink.amount==72&&src.amount==18,"block acceptor flow precedes neighbor equalization and retains overflow");sink.amount=0;src.nextUpdate=0;src.update();check(sink.amount==18&&src.amount==0&&src.type==null&&src.lastFlow==0&&src.nextUpdate==5,"full flow clears original source fields and resets empty timer");
  TileEntityFoundryChannel one=flow.put(P.north(3),channel),other=flow.put(P.north(3).east(),channel);one.type=iron;one.amount=1;other.type=iron;other.amount=7;seed(flow.rand,5,1);one.update();check(one.amount==7&&other.amount==1,"single channel quantum forces original swap even nonzero RNG");
  TileEntityFoundryTank invalid=flow.put(P.south(3),tank);invalid.type=null;invalid.amount=8;invalid.update();check(invalid.amount==0,"server retains original null-type amount reset");
  NativeWorld client=new NativeWorld(true);TileEntityFoundryChannel remote=client.put(P,channel);remote.type=iron;remote.amount=7;remote.nextUpdate=19;remote.update();check(remote.amount==7&&remote.nextUpdate==19&&UniNodespace.getNode(client,4,20,4,FoundryNetworkProvider.THE_PROVIDER)==null,"client never runs transfer or creates nodes");
 }
 static void outletAndSlag() {
  NativeWorld w=new NativeWorld(false);TileEntityFoundryOutlet out=w.put(P,outlet);w.setBlockState(P,outlet.getStateFromMeta(5),3);TileEntityFoundryTank receiver=w.put(P.down(3),tank);
  for(boolean invert:new boolean[]{false,true})for(boolean powered:new boolean[]{false,true}){out.invertRedstone=invert;w.powered=powered;check(out.isClosed()==(invert^powered),"original invert-redstone XOR truth table");}out.invertRedstone=false;w.powered=false;
  for(NTMMaterial filter:new NTMMaterial[]{null,iron,copper})for(boolean invert:new boolean[]{false,true})for(ForgeDirection side:ForgeDirection.values()){
   out.filter=filter;out.invertFilter=invert;boolean expected=(filter==null||!((filter!=iron)^invert))&&side==ForgeDirection.WEST;
   check(out.canAcceptPartialFlow(w,4,20,4,side,stack(20))==expected,"original outlet filter/side and actual native raytrace acceptance");
  }
  out.filter=null;out.invertFilter=false;receiver.amount=2580;receiver.type=iron;MaterialStack input=stack(20);w.clear();MaterialStack left=out.flow(w,4,20,4,ForgeDirection.WEST,input);check(left==input&&left.amount==8&&receiver.amount==2592,"outlet real raycast and target pour preserve leftover identity");check(PacketThreading.packets.size()==1,"outlet retains original particle on nonnull input even partial pour");AuxParticlePacketNT particle=(AuxParticlePacketNT)PacketThreading.packets.get(0);check(particle.data.getString("type").equals("foundry")&&particle.data.getInteger("color")==iron.moltenColor&&particle.data.getByte("dir")==5&&particle.data.getFloat("off")==.375F&&particle.data.getFloat("base")==0&&particle.data.getFloat("len")==2.875F,"exact original outlet particle payload");check(PacketThreading.points.get(0).range==50,"original particle radius retained");
  out.filter=copper;out.invertFilter=true;out.invertRedstone=true;NBTTagCompound tag=out.writeToNBT(new NBTTagCompound());TileEntityFoundryOutlet restored=new TileEntityFoundryOutlet();restored.readFromNBT(tag);check(restored.filter==copper&&restored.invertFilter&&restored.invertRedstone&&tag.getTagId("filter")==2,"outlet filter short and both original boolean NBT keys");
  NBTTagCompound settings=out.getSettings(w,4,20,4);TileEntityFoundryOutlet pasted=new TileEntityFoundryOutlet();pasted.pasteSettings(settings,0,w,null,4,20,4);check(pasted.filter==copper&&pasted.invertFilter&&pasted.invertRedstone,"exact copytool settings round trip");
  NativeWorld client=new NativeWorld(true);TileEntityFoundryOutlet remote=client.put(P,outlet);client.clear();remote.filter=copper;remote.update();check(client.renders==1,"client filter changes redraw outlet");remote.update();check(client.renders==1,"unchanged outlet state suppresses redraw");client.powered=true;remote.update();check(client.renders==2,"client closed-state changes redraw outlet");
  NativeWorld slagWorld=new NativeWorld(false);TileEntityFoundrySlagtap tap=slagWorld.put(P,slagtap);slagWorld.setBlockState(P,slagtap.getStateFromMeta(5),3);slagWorld.setBlockState(P.down(4),Blocks.STONE.getDefaultState(),3);MaterialStack liquid=stack(TileEntitySlag.maxAmount+17);slagWorld.clear();check(tap.canAcceptPartialFlow(slagWorld,4,20,4,ForgeDirection.WEST,liquid),"slagtap reaches actual native solid hit within fifteen blocks");MaterialStack residual=tap.flow(slagWorld,4,20,4,ForgeDirection.WEST,liquid);TileEntitySlag made=(TileEntitySlag)slagWorld.getTileEntity(P.down(3));check(made.mat==iron&&made.amount==TileEntitySlag.maxAmount&&residual==liquid&&liquid.amount==17,"slagtap fills new above-hit slag with exact overflow");check(slagWorld.scheduled==1&&PacketThreading.packets.size()==1,"slagtap schedules one tick and particles only on positive transfer");
  made.amount-=10;MaterialStack extra=stack(15);slagWorld.clear();MaterialStack rest=tap.flow(slagWorld,4,20,4,ForgeDirection.WEST,extra);TileEntitySlag second=(TileEntitySlag)slagWorld.getTileEntity(P.down(2));check(rest==null&&made.amount==TileEntitySlag.maxAmount&&second.amount==5,"slagtap fills matching hit then above remainder with no lost quantum");check(slagWorld.scheduled==2,"both actual slag writes retain scheduled updates");
  NativeWorld empty=new NativeWorld(false);TileEntityFoundrySlagtap noHit=empty.put(P,slagtap);MaterialStack lost=stack(3);check(noHit.flow(empty,4,20,4,ForgeDirection.WEST,lost)==null&&lost.amount==3,"original no-hit slagtap returns null without input mutation, not invented recovery");check(noHit.flow(empty,4,20,4,ForgeDirection.WEST,null)==null,"original null slagtap flow stays null");
 }

 static void inventoryAndTickLifecycle()throws Exception {
  NativeWorld server=new NativeWorld(false);TileEntityFoundryMold cast=server.put(P,mold);cast.slots[0]=new ItemStack(molds,1,0);cast.amount=72;cast.type=iron;cast.cooloff=1;server.clear();
  check(server.loadedTileEntityList.contains(cast)&&server.tickableTileEntities.contains(cast),"actual native chunk/world registers the production ITickable tile");server.updateEntities();check(cast.cooloff==200&&cast.amount==0&&cast.slots[1].getCount()==3,"actual native World.updateEntities dispatches full production cast tick");
  TileEntityFoundryMold client=new NativeWorld(true).put(P,mold);PacketBuffer bytes=new PacketBuffer(io.netty.buffer.Unpooled.buffer());cast.getUpdatePacket().writePacketData(bytes);SPacketUpdateTileEntity packet=new SPacketUpdateTileEntity();packet.readPacketData(bytes);client.onDataPacket(null,packet);check(ItemStack.areItemStacksEqual(client.slots[0],cast.slots[0])&&ItemStack.areItemStacksEqual(client.slots[1],cast.slots[1]),"native incremental packet carries both full item/NBT slots");
  NBTTagCompound data=cast.writeToNBT(new NBTTagCompound());NBTTagList items=new NBTTagList();int[] slots={-1,2,1,1};Item[] itemTypes={Items.DIAMOND,Items.DIAMOND,Items.GOLD_INGOT,Items.IRON_INGOT};for(int i=0;i<slots.length;i++){NBTTagCompound entry=new ItemStack(itemTypes[i],i+1).writeToNBT(new NBTTagCompound());entry.setByte("slot",(byte)slots[i]);items.appendTag(entry);}data.setTag("items",items);client.readFromNBT(data);check(client.slots[0]==null&&client.slots[1].getItem()==Items.IRON_INGOT&&client.slots[1].getCount()==4,"original signed/out-of-range slot rejection and last duplicate wins");
  NBTTagCompound absent=new NBTTagCompound();absent.setString("id","hbm:not_registered");absent.setByte("slot",(byte)1);absent.setByte("Count",(byte)1);items.appendTag(absent);client.readFromNBT(data);check(client.slots[1]==null&&client.isEmpty(),"native unknown item reload normalizes only invalid/empty item boundary to nullable storage");
  cast.slots[1]=ItemStack.EMPTY;same(cast.getStackInSlot(1),ItemStack.EMPTY,"public native empty accessor remains shared EMPTY");same(cast.decrStackSize(1,1),ItemStack.EMPTY,"full extraction normalizes native empty boundary without fabricating an item");check(cast.slots[1]==null,"extraction clears nullable domain slot");cast.clear();check(cast.slots[0]==null&&cast.slots[1]==null&&cast.isEmpty(),"native clear retains nullable internal inventory");
  TileEntityFoundryChannel pipe=server.put(P.east(3),channel);server.updateEntities();FoundryNode node=(FoundryNode)UniNodespace.getNode(server,7,20,4,FoundryNetworkProvider.THE_PROVIDER);check(node!=null,"native world tick initializes real channel node");server.markTileEntityForRemoval(pipe);server.updateEntities();check(!pipe.isLoaded()&&!server.tickableTileEntities.contains(pipe)&&!node.expired,"actual native world unload callback preserves original channel soul while removing tick ownership");
 }
 static void nodes()throws Exception {
  NativeWorld w=new NativeWorld(false);TileEntityFoundryChannel a=w.put(P,channel);a.update();FoundryNode node=(FoundryNode)UniNodespace.getNode(w,4,20,4,FoundryNetworkProvider.THE_PROVIDER);check(node!=null&&node.positions[0].compare(4,20,4)&&node.connections.length==4,"actual node identity has native tile coordinates and four original connections");
  check(!a.canAcceptPartialPour(w,4,20,4,0,0,0,ForgeDirection.UP,stack(1)),"channel refuses pour before original topology network exists");FoundryNetwork network=new FoundryNetwork();network.joinLink(node);check(a.canAcceptPartialPour(w,4,20,4,0,0,0,ForgeDirection.UP,stack(1)),"valid original network allows same material pour");
  FoundryNode foreign=new FoundryNode(FoundryNetworkProvider.THE_PROVIDER,new com.hbm.util.fauxpointtwelve.BlockPos(9,20,4));foreign.type=copper;network.joinLink(foreign);check(!a.canAcceptPartialPour(w,4,20,4,0,0,0,ForgeDirection.UP,stack(1)),"any foreign material anywhere in actual network rejects pour");foreign.type=null;a.pour(w,4,20,4,0,0,0,ForgeDirection.UP,stack(8));same(node.type,iron,"pour updates actual node material");
  a.onChunkUnload();check(!a.isLoaded()&&!node.expired&&UniNodespace.getNode(w,4,20,4,FoundryNetworkProvider.THE_PROVIDER)==node,"original unload keeps network soul but updates loaded-tile status");a.invalidate();check(node.expired&&UniNodespace.getNode(w,4,20,4,FoundryNetworkProvider.THE_PROVIDER)==null&&!network.isValid(),"native invalidation destroys original node and network");
  Field unsafeField=Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);Unsafe unsafe=(Unsafe)unsafeField.get(null);DedicatedServer server=(DedicatedServer)unsafe.allocateInstance(DedicatedServer.class);WorldServer first=(WorldServer)unsafe.allocateInstance(WorldServer.class),second=(WorldServer)unsafe.allocateInstance(WorldServer.class);server.worlds=new WorldServer[]{first,second};set(net.minecraftforge.fml.server.FMLServerHandler.instance(),net.minecraftforge.fml.server.FMLServerHandler.class,"server",server);same(FMLCommonHandler.instance().getMinecraftServerInstance(),server,"actual Forge sided delegate resolves current native server");
  UniNodespace.worlds.clear();UniNodespace.activeNodeNets.clear();FoundryNode x=new FoundryNode(FoundryNetworkProvider.THE_PROVIDER,new com.hbm.util.fauxpointtwelve.BlockPos(1,2,3)).setConnections(new com.hbm.util.fauxpointtwelve.DirPos(2,2,3,ForgeDirection.EAST));FoundryNode y=new FoundryNode(FoundryNetworkProvider.THE_PROVIDER,new com.hbm.util.fauxpointtwelve.BlockPos(2,2,3)).setConnections(new com.hbm.util.fauxpointtwelve.DirPos(1,2,3,ForgeDirection.WEST));FoundryNode other=new FoundryNode(FoundryNetworkProvider.THE_PROVIDER,new com.hbm.util.fauxpointtwelve.BlockPos(1,2,3)).setConnections();UniNodespace.createNode(first,x);UniNodespace.createNode(first,y);UniNodespace.createNode(second,other);UniNodespace.updateNodespace();check(x.hasValidNet()&&y.hasValidNet()&&x.net==y.net&&other.hasValidNet()&&other.net!=x.net,"complete native dispatcher iterates actual server world array and connects only same-world neighbors");same(UniNodespace.getNode(first,1,2,3,FoundryNetworkProvider.THE_PROVIDER),x,"dispatcher preserves provider/coordinate identity");
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());ModMetadata meta=new ModMetadata();meta.modId="hbm";meta.name="Foundry native proof";meta.version="1";Loader.instance().setupTestHarness(new DummyModContainer(meta));setup();baseAndSync();casting();transfers();outletAndSlag();inventoryAndTickLifecycle();nodes();
  System.out.println("PASS "+checks+" actual native foundry state/transfer/overflow/cooldown/NBT/initial+incremental packet/raytrace/empty inventory/client authority/node lifecycle checks; complete shared dispatcher and eight tiles, explicit recipe/transport/block-owner boundaries, not full-game acceptance.");
 }
}
