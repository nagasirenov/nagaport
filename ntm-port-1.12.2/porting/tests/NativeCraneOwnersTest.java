import java.lang.reflect.Field;
import java.util.*;
import api.hbm.block.IToolable.ToolType;
import api.hbm.conveyor.*;
import com.hbm.blocks.network.*;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.network.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.Container;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.storage.*;

/** Whole production crane block owners/base tile; no substitute Minecraft classes or extracted owner bodies. */
public final class NativeCraneOwnersTest {
 static int checks;static final BlockPos P=new BlockPos(8,64,8);
 static BlockCraneBase[] owners;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 static int meta(World w){IBlockState s=w.getBlockState(P);return s.getBlock().getMetaFromState(s);}
 static final class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();final List<EntityItem> drops=new ArrayList<>();final List<String> events=new ArrayList<>();int updates,neighbors,removes,comparators;boolean rejectDrops;
  NativeWorld(boolean client){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),client);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  @Override protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "native crane fixture";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}
  @Override protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return chunks.containsKey(key(x,z));}
  @Override public boolean checkLight(BlockPos p){return true;}
  @Override public boolean setBlockState(BlockPos p,IBlockState s,int flags){if(p.equals(P))events.add("set:"+s.getBlock().getMetaFromState(s)+":"+flags);return super.setBlockState(p,s,flags);}
  @Override public void notifyBlockUpdate(BlockPos p,IBlockState a,IBlockState b,int flags){updates++;events.add("update:"+flags);super.notifyBlockUpdate(p,a,b,flags);}
  @Override public void notifyNeighborsOfStateChange(BlockPos p,Block b,boolean observers){neighbors++;events.add("neighbors");super.notifyNeighborsOfStateChange(p,b,observers);}
  @Override public boolean spawnEntity(Entity e){drops.add((EntityItem)e);events.add("drop");return !rejectDrops;}
  @Override public void removeTileEntity(BlockPos p){removes++;events.add("remove");super.removeTileEntity(p);}
  @Override public void updateComparatorOutputLevel(BlockPos p,Block b){comparators++;events.add("comparator");super.updateComparatorOutputLevel(p,b);}
  void clear(){drops.clear();events.clear();updates=neighbors=removes=comparators=0;}
  TileEntityCraneBase crane(){return (TileEntityCraneBase)getTileEntity(P);}
 }
 static final class Player extends EntityPlayer {
  Player(World w){super(w,new GameProfile(new UUID(11,12),"crane-probe"));}
  public boolean isCreative(){return false;}public boolean isSpectator(){return false;}
 }
 static ItemStack stack(int count){ItemStack s=new ItemStack(Items.STICK,count,3);NBTTagCompound n=new NBTTagCompound();n.setString("marker","crane-original");NBTTagCompound child=new NBTTagCompound();child.setInteger("nested",29);n.setTag("child",child);s.setTagCompound(n);return s;}
 static void override(TileEntityCraneBase tile,ForgeDirection d){NBTTagCompound n=tile.writeToNBT(new NBTTagCompound());n.setByte("CraneOutputOverride",(byte)d.ordinal());tile.readFromNBT(n);}
 static void register(Block b,String n){net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(b.setRegistryName("crane_probe",n));net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));}
 static LegacyCraneFaceOracle.BlockCraneBase oracle(int i){switch(i){case 0:return new LegacyCraneFaceOracle.CraneBoxer();case 1:return new LegacyCraneFaceOracle.CraneExtractor();case 2:return new LegacyCraneFaceOracle.CraneGrabber();case 3:return new LegacyCraneFaceOracle.CraneInserter();default:return new LegacyCraneFaceOracle.CraneUnboxer();}}
 static void stateAndTextures(){
  NativeWorld w=new NativeWorld(false);LegacyCraneFaceOracle.World old=new LegacyCraneFaceOracle.World();
  for(int i=0;i<owners.length;i++){BlockCraneBase b=owners[i];LegacyCraneFaceOracle.BlockCraneBase o=oracle(i);w.setBlockState(P,b.getDefaultState(),2);
   Collection<ResourceLocation> catalog=b.getModelTextures();check(catalog.size()==19,"all 19 nonnull declared texture fields cataloged "+i);try{catalog.clear();throw new AssertionError("mutable catalog");}catch(UnsupportedOperationException expected){checks++;}
   for(int m=0;m<16;m++){IBlockState s=b.getStateFromMeta(m);w.setBlockState(P,s,2);old.meta=m;check(meta(w)==m&&b.getMetaFromState(s)==m,"native all16 metadata "+i+":"+m);check(s.getMaterial()==Material.IRON&&b.getSoundType()==SoundType.STONE,"iron/default STONE");check(s.isFullCube()&&s.isOpaqueCube()&&s.getRenderType()==EnumBlockRenderType.MODEL,"native full opaque MODEL");
    check(b.getItemDropped(s,new Random(1),0)==Item.getItemFromBlock(b)&&b.damageDropped(s)==0,"original block drop/meta0");
    for(int face=0;face<6;face++){ResourceLocation tex=b.getModelTexture(face,m);check(Objects.equals(tex==null?null:tex.toString(),o.getIcon(face,m)),"pristine inventory texture "+i+":"+m+":"+face);check(tex==null||catalog.contains(tex),"inventory texture is cataloged");}
    for(ForgeDirection out:ForgeDirection.values()){override(w.crane(),out);old.outputOverride=out;for(int face=0;face<6;face++){ResourceLocation tex=b.getModelTexture(w,8,64,8,face);check(Objects.equals(tex==null?null:tex.toString(),o.getIcon(old,8,64,8,face)),"pristine world texture "+i+":"+m+":"+out+":"+face);check(tex==null||catalog.contains(tex),"world texture is cataloged");check(b.getRotationFromSide(w,8,64,8,face)==o.getRotationFromSide(old,8,64,8,face),"pristine world rotation");}
     check(b.getExtendedState(s,w,P)!=null,"actual native extended state");
    }
   }
  }
  System.out.println("PASS all16 native states/default iron-STONE/full-cube/drop semantics and pristine exhaustive inventory/world texture/rotation selectors across all seven overrides.");
 }
 static void transferAlgorithm(){
  net.minecraft.inventory.InventoryBasic plain=new net.minecraft.inventory.InventoryBasic("plain",false,2);plain.setInventorySlotContents(0,stack(62));ItemStack plainInput=stack(7);check(CraneInserter.addToInventory(plain,null,plainInput,5)==null&&plainInput.isEmpty()&&plain.getStackInSlot(0).getCount()==64&&plain.getStackInSlot(1).getCount()==5,"actual unsided native InventoryBasic follows merge-before-empty path");
  for(boolean nullable:new boolean[]{false,true}){TileEntityCraneInserter inv=new TileEntityCraneInserter();Arrays.fill(inv.slots,nullable?null:ItemStack.EMPTY);inv.limit=16;inv.slots[2]=stack(14);inv.slots[4]=stack(15);ItemStack input=stack(9);ItemStack ret=CraneInserter.addToInventory(inv,new int[]{4,2,0},input,4);check(ret==null&&input.getCount()==0,"all transferred return null");check(inv.slots[4].getCount()==16&&inv.slots[2].getCount()==16&&inv.slots[0].getCount()==6,"merge-before-empty access order/limit");check(inv.dirty==3&&inv.lastFace==EnumFacing.WEST,"dirty per mutation/native side");check(inv.slots[0]!=input&&inv.slots[0].getTagCompound()!=input.getTagCompound(),"new slot deep stack/tag copy");}
  TileEntityCraneInserter inv=new TileEntityCraneInserter();inv.limit=3;ItemStack input=stack(9);ItemStack ret=CraneInserter.addToInventory(inv,new int[]{5},input,3);check(ret==input&&input.getCount()==6&&inv.slots[5].getCount()==3,"remainder identity and restricted access");
  inv=new TileEntityCraneInserter();inv.insert=false;input=stack(6);ret=CraneInserter.addToInventory(inv,null,input,2);check(ret==input&&input.getCount()==6&&inv.isEmpty()&&inv.dirty==0&&inv.validCalls.isEmpty(),"sided insertion rejection short-circuits validity");
  inv=new TileEntityCraneInserter();inv.valid=false;input=stack(6);CraneInserter.addToInventory(inv,null,input,1);check(input.getCount()==6&&inv.isEmpty()&&inv.dirty==0,"slot validity rejection");
  inv=new TileEntityCraneInserter();inv.slots[0]=stack(2);input=stack(4);input.getTagCompound().setString("marker","different");CraneInserter.addToInventory(inv,new int[]{0},input,0);check(input.getCount()==4&&inv.slots[0].getCount()==2,"NBT mismatch not merged");input=new ItemStack(Items.STICK,4,4);CraneInserter.addToInventory(inv,new int[]{0},input,0);check(input.getCount()==4,"damage mismatch not merged");
  inv=new TileEntityCraneInserter();input=new ItemStack(Items.ENDER_PEARL,30);CraneInserter.addToInventory(inv,new int[]{0},input,0);check(inv.slots[0].getCount()==30,"legacy empty-slot path caps inventory not item stack maximum");
  System.out.println("PASS native ItemStack two-pass insertion, EMPTY/null absence, limits/access/order, side predicates, validity, NBT/damage matching, aliases and dirty calls.");
 }
 static void workflows(){
  CraneInserter b=(CraneInserter)owners[3];NativeWorld w=new NativeWorld(false);w.setBlockState(P,b.getStateFromMeta(2),2);TileEntityCraneInserter own=(TileEntityCraneInserter)w.crane();w.setBlockState(P.south(),owners[0].getDefaultState(),2);TileEntityCraneBoxer target=(TileEntityCraneBoxer)w.getTileEntity(P.south());
  ItemStack input=stack(8);b.onItemEnter(w,8,64,8,ForgeDirection.NORTH,()->input);check(input.getCount()==8&&target.slots[0].getCount()==8&&own.isEmpty()&&w.drops.isEmpty(),"item copies input and inserts neighbor first");check(target.lastFace==EnumFacing.NORTH&&com.hbm.util.InventoryUtil.lastSide==2,"output opposite native face");
  target.clear();w.setBlockState(P.west(),Blocks.REDSTONE_BLOCK.getDefaultState(),3);check(w.isBlockPowered(P),"genuine native redstone power");b.onItemEnter(w,8,64,8,ForgeDirection.NORTH,()->input);check(target.isEmpty()&&own.slots[0].getCount()==8,"redstone suppresses neighbor but retains local buffer");
  own.clear();own.insert=false;w.clear();b.onItemEnter(w,8,64,8,ForgeDirection.NORTH,()->input);check(w.drops.size()==1&&w.drops.get(0).getItem().getCount()==8&&w.drops.get(0).posX==8.5&&input.getCount()==8,"buffer refusal drops copied centered remainder");own.destroyer=true;w.clear();b.onItemEnter(w,8,64,8,ForgeDirection.NORTH,()->input);check(w.drops.isEmpty(),"destroyer suppresses leftover drop");
  own.destroyer=false;own.insert=true;w.setBlockToAir(P.west());w.clear();target.clear();ItemStack packaged=stack(7);b.onPackageEnter(w,8,64,8,ForgeDirection.EAST,()->new ItemStack[]{packaged});check(packaged.isEmpty()&&target.slots[0].getCount()==7,"package mutates supplied stacks unlike single item");
  b.onItemEnter(w,8,64,8,ForgeDirection.NORTH,null);b.onItemEnter(w,8,64,8,ForgeDirection.NORTH,()->ItemStack.EMPTY);b.onPackageEnter(w,8,64,8,ForgeDirection.NORTH,null);b.onPackageEnter(w,8,64,8,ForgeDirection.NORTH,()->new ItemStack[0]);check(w.drops.isEmpty(),"null/empty ingress guard");
  for(BlockCraneBase owner:new BlockCraneBase[]{owners[0],owners[3],owners[4]}){w.setBlockState(P,owner.getStateFromMeta(2),2);TileEntityCraneBase tile=w.crane();override(tile,ForgeDirection.EAST);IEnterableBlock enter=(IEnterableBlock)owner;for(ForgeDirection dir:ForgeDirection.values()){check(enter.canItemEnter(w,8,64,8,dir,()->input)==(owner!=owners[4]&&dir==ForgeDirection.NORTH),"item gate distinct input direction");check(enter.canPackageEnter(w,8,64,8,dir,()->new ItemStack[]{input})==(owner!=owners[4]||dir==ForgeDirection.EAST),"package gate output vs unconditional");}
   tile.insert=false;w.clear();ItemStack payload=stack(4);enter.onPackageEnter(w,8,64,8,ForgeDirection.WEST,()->new ItemStack[]{payload});check(w.drops.size()==1&&w.drops.get(0).getItem().getCount()==4,"box/unbox/inserter native package remainder drop");check(tile.lastFace==EnumFacing.WEST,"overridden output opposite accesses package inventory");
   check(owner.hasComparatorInputOverride(w.getBlockState(P))&&owner.getComparatorInputOverride(w.getBlockState(P),w,P)==Container.calcRedstoneFromInventory(tile),"native comparator exact Container result");
   tile.slots[0]=stack(64);check(owner.getComparatorInputOverride(w.getBlockState(P),w,P)==Container.calcRedstoneFromInventory(tile)&&owner.getComparatorInputOverride(w.getBlockState(P),w,P)>0,"nonempty comparator native fullness signal");tile.clear();
   if(owner==owners[0]){tile.insert=true;tile.clear();ItemStack item=stack(5);enter.onItemEnter(w,8,64,8,ForgeDirection.NORTH,()->item);check(item.isEmpty()&&tile.slots[0].getCount()==5&&tile.lastFace==EnumFacing.NORTH,"boxer direct item identity/native input access");}
  }
  CraneExtractor extractor=(CraneExtractor)owners[1];for(EnumFacing face:EnumFacing.values())check(extractor.canConnectRedstone(extractor.getDefaultState(),w,P,face),"extractor redstone connection every face");check(extractor.canConnectRedstone(extractor.getDefaultState(),w,P,null),"extractor null redstone side");
  System.out.println("PASS native world item/package transfer, redstone gating, buffer/destroyer/remainder drops, input/output predicates, comparator and extractor redstone faces.");
 }
 static void lifecycleAndScrew(){
  for(BlockCraneBase b:owners){NativeWorld w=new NativeWorld(false);w.setBlockState(P,b.getStateFromMeta(2),2);TileEntityCraneBase tile=w.crane();tile.slots[0]=stack(5);override(tile,ForgeDirection.EAST);
   for(int m=0;m<16;m++){w.setBlockState(P,b.getStateFromMeta(m),2);check(w.crane()==tile&&!tile.isInvalid()&&tile.slots[0].getCount()==5&&tile.getOutputOverride()==ForgeDirection.EAST,"all16 native same-block Chunk lifecycle retains tile/inventory/override");check(tile.getInputSide()==ForgeDirection.getOrientation(m),"native tile metadata cache invalidated");}
   w.setBlockState(P,b.getStateFromMeta(2),2);override(tile,ForgeDirection.UNKNOWN);w.clear();tile.setInput(ForgeDirection.EAST);check(meta(w)==5&&tile.getOutputOverride()==ForgeDirection.SOUTH&&w.crane()==tile,"input turn preserves prior default output");check(w.events.get(0).equals("set:5:3"),"input ordinary flags3");w.clear();tile.setInput(ForgeDirection.SOUTH);check(meta(w)==3&&tile.getOutputOverride()==ForgeDirection.EAST,"input-output collision swaps sides");check(w.events.get(0).equals("set:3:4"),"swap input flags4 before output notification");
   w.clear();tile.setOutputOverride(ForgeDirection.EAST);check(tile.getOutputOverride()==ForgeDirection.WEST&&w.updates==1&&w.neighbors==1,"same output click flips and synchronizes");
   Player player=new Player(w);player.setSneaking(false);check(!b.onScrew(w,player,8,64,8,0,0,0,0,ToolType.WRENCH),"wrong tool false");check(b.onScrew(w,player,8,64,8,1,0,0,0,ToolType.SCREWDRIVER)&&tile.getInputSide()==ForgeDirection.UP,"screw nonsneak changes input");player.setSneaking(true);check(b.onScrew(w,player,8,64,8,4,0,0,0,ToolType.SCREWDRIVER)&&tile.getOutputOverride()==ForgeDirection.EAST,"sneak same output flips");
   NBTTagCompound n=tile.writeToNBT(new NBTTagCompound());check(n.getByte("CraneOutputOverride")==5,"output override NBT byte");NativeWorld client=new NativeWorld(true);client.setBlockState(P,b.getStateFromMeta(meta(w)),2);TileEntityCraneBase remote=client.crane();remote.onDataPacket(null,tile.getUpdatePacket());check(remote.getOutputOverride()==tile.getOutputOverride()&&remote.slots[0].getCount()==5,"actual packet NBT accessor roundtrip");client.clear();remote.update();check(client.updates==1,"client changed override redraw");remote.update();check(client.updates==1,"cached override prevents duplicate redraw");
   NBTTagCompound settings=new NBTTagCompound();settings.setInteger("outputSide",4);settings.setInteger("inputSide",0);w.clear();tile.pasteSettings(settings,1,w,player,8,64,8);check(tile.getInputSide()==ForgeDirection.DOWN&&tile.getOutputSide()==ForgeDirection.WEST,"orientation settings preserved");check(w.events.indexOf("update:3")<w.events.indexOf("set:0:3"),"paste notifies output before input metadata");check(tile.getSettings(w,8,64,8).getInteger("inputSide")==0&&tile.getSettings(w,8,64,8).getInteger("outputSide")==4,"copy orientation");
   w.setBlockState(P,Blocks.STONE.getDefaultState(),3);check(tile.isInvalid()&&w.getTileEntity(P)==null,"different block invalidates/removes tile");check(!b.onScrew(w,player,8,64,8,0,0,0,0,ToolType.SCREWDRIVER),"missing tile screwdriver false");
  }
  System.out.println("PASS actual World/Chunk tile-preserving all16 metadata lifecycle, screwdriver direction/swap/flags, NBT/packet/client redraw and settings order; block replacement removes tile.");
 }
 static void placementAndActivation(){
  BlockCraneBase b=owners[3];NativeWorld w=new NativeWorld(true);w.setBlockState(P,b.getDefaultState(),2);Player p=new Player(w);
  for(double dx:new double[]{0,1.9999999,2,-2,-1.9999999})for(double dy:new double[]{-2,-1.82,-1.819,0,.18,.181,3})for(float yaw:new float[]{-405,-315,-225,-135,-45,0,44.999f,45,135,225,315,405})for(boolean sneak:new boolean[]{false,true}){
   p.setPosition(8+dx,64+dy,8);p.rotationYaw=yaw;p.setSneaking(sneak);double eye=64+dy+1.82;int expected;boolean close=MathHelper.abs((float)p.posX-8F)<2F&&MathHelper.abs((float)p.posZ-8F)<2F;if(close&&eye-64>2)expected=1;else if(close&&64-eye>0)expected=0;else expected=new int[]{2,5,3,4}[MathHelper.floor((double)(yaw*4F/360F)+.5D)&3];w.clear();b.onBlockPlacedBy(w,P,w.getBlockState(P),p,ItemStack.EMPTY);check(meta(w)==expected&&w.events.get(0).equals("set:"+expected+":2"),"legacy piston fixed1.82 float corner/strict thresholds/yaw/sneak "+dx+":"+dy+":"+yaw+":"+sneak);
  }
  p.setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);p.setSneaking(true);check(b.onBlockActivated(w,P,w.getBlockState(P),p,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0),"client activation before sneak");p.setHeldItem(EnumHand.OFF_HAND,new ItemStack(new com.hbm.items.tool.ItemTooling()));check(!b.onBlockActivated(w,P,w.getBlockState(P),p,EnumHand.OFF_HAND,EnumFacing.UP,0,0,0),"native activation selected hand tooling refusal");p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(new com.hbm.items.tool.ItemConveyorWand()));check(!b.onBlockActivated(w,P,w.getBlockState(P),p,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0),"conveyor wand refusal");NativeWorld server=new NativeWorld(false);server.setBlockState(P,b.getDefaultState(),2);Player q=new Player(server);q.setSneaking(true);check(!b.onBlockActivated(server,P,server.getBlockState(P),q,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0),"server sneaking bypasses GUI");
  System.out.println("PASS exact legacy piston fixed1.82/corner float/strict threshold/yaw placement across 840 cases and native hand/client/sneak activation boundaries (GUI transport excluded).");
 }
 public static final class FilterTile extends TileEntityCraneBase implements com.hbm.tileentity.IControlReceiverFilter {
  final List<Integer> modes=new ArrayList<>();public FilterTile(){super(8);}public int[] getFilterSlots(){return new int[]{2,5};}public void nextMode(int slot){modes.add(slot);}public void notifyChange(){onBlockChanged();}
 }
 static void settingsFilterAndPacket()throws Exception {
  NativeWorld w=new NativeWorld(false);BlockCraneBase b=owners[1];w.setBlockState(P,b.getStateFromMeta(2),2);FilterTile f=new FilterTile();w.setTileEntity(P,f);f.slots[2]=stack(7);f.slots[4]=stack(9);override(f,ForgeDirection.EAST);
  NBTTagCompound settings=f.getSettings(w,8,64,8);NBTTagList tags=settings.getTagList("items",10);check(tags.tagCount()==2&&tags.getCompoundTagAt(0).getByte("slot")==0&&tags.getCompoundTagAt(1).getByte("slot")==2,"filter settings include nonempty stacks with relative slots and counts");check(new ItemStack(tags.getCompoundTagAt(0)).getCount()==7&&new ItemStack(tags.getCompoundTagAt(1)).getCount()==9,"filter copy does not normalize count");
  NBTTagList source=new NBTTagList();for(int i=0;i<3;i++){NBTTagCompound tag=new NBTTagCompound();stack(3+i).writeToNBT(tag);tag.setByte("slot",(byte)i);source.appendTag(tag);}NBTTagCompound paste=new NBTTagCompound();paste.setTag("items",source);f.clear();f.pasteSettings(paste,0,w,null,8,64,8);check(f.slots[2].getCount()==3&&f.slots[3].isEmpty()&&f.slots[4].isEmpty()&&f.modes.equals(Arrays.asList(0)),"pristine filter absolute-index/list-size predicate and mode ordering retained (not silently corrected)");
  net.minecraft.network.play.server.SPacketUpdateTileEntity packet=f.getUpdatePacket();net.minecraft.network.PacketBuffer buf=new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());packet.writePacketData(buf);net.minecraft.network.play.server.SPacketUpdateTileEntity decoded=new net.minecraft.network.play.server.SPacketUpdateTileEntity();decoded.readPacketData(buf);buf.release();FilterTile receiver=new FilterTile();receiver.onDataPacket(null,decoded);check(decoded.getPos().equals(P)&&decoded.getTileEntityType()==0&&receiver.getOutputOverride()==ForgeDirection.EAST&&receiver.slots[2].getCount()==3,"genuine native SPacketUpdateTileEntity bytes and NBT roundtrip");
  NBTTagCompound noOverride=receiver.writeToNBT(new NBTTagCompound());noOverride.removeTag("CraneOutputOverride");receiver.readFromNBT(noOverride);check(receiver.getOutputOverride()==ForgeDirection.EAST,"missing byte key preserves existing override as original");receiver.update();receiver.notifyChange();check(receiver.getOutputOverride()==ForgeDirection.EAST,"detached update/change notification returns without world access");
  check(Arrays.equals(f.infoForDisplay(w,8,64,8),new String[]{"copytool.filter","copytool.orientation"}),"copy display ordering unchanged");
  System.out.println("PASS filter-settings relative slots/counts and original indexing/mode order, missing-key behavior, detached base lifecycle and native packet byte roundtrip.");
 }
 static void initialChunkSync()throws Exception {
  for(BlockCraneBase b:owners)for(int sections:new int[]{65535,1 << (P.getY() >> 4)}) {
   NativeWorld server=new NativeWorld(false);server.setBlockState(P,b.getStateFromMeta(2),2);TileEntityCraneBase source=server.crane();source.slots[0]=stack(17);override(source,ForgeDirection.EAST);
   net.minecraft.network.play.server.SPacketChunkData chunkPacket=new net.minecraft.network.play.server.SPacketChunkData(server.getChunk(P),sections);
   check(chunkPacket.getTileEntityTags().size()==1,"initial native chunk includes one crane update tag");
   NBTTagCompound initial=chunkPacket.getTileEntityTags().get(0);check(initial.equals(source.writeToNBT(new NBTTagCompound())),"initial chunk update tag dispatches complete crane NBT including override/inventory");
   net.minecraft.network.PacketBuffer bytes=new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());chunkPacket.writePacketData(bytes);net.minecraft.network.play.server.SPacketChunkData decoded=new net.minecraft.network.play.server.SPacketChunkData();decoded.readPacketData(bytes);bytes.release();
   check(decoded.isFullChunk()==(sections==65535)&&decoded.getChunkX()==0&&decoded.getChunkZ()==0&&decoded.getTileEntityTags().size()==1,"real native full/partial chunk packet byte roundtrip");
   NativeWorld client=new NativeWorld(true);client.setBlockState(P,b.getStateFromMeta(2),2);TileEntityCraneBase destination=client.crane();check(destination.getOutputOverride()==ForgeDirection.UNKNOWN&&destination.slots[0].isEmpty(),"fresh client has no server orientation/inventory before initial tag");
   destination.handleUpdateTag(decoded.getTileEntityTags().get(0));check(destination.getOutputOverride()==ForgeDirection.EAST&&destination.slots[0].getCount()==17&&destination.slots[0].getTagCompound().getCompoundTag("child").getInteger("nested")==29,"native handleUpdateTag restores override and full inventory NBT on initial client load");
   check(destination.slots[0]!=source.slots[0]&&destination.slots[0].getTagCompound()!=source.slots[0].getTagCompound(),"chunk packet inventory is an independent native NBT copy");
   for(int face=0;face<6;face++)check(Objects.equals(b.getModelTexture(client,8,64,8,face),b.getModelTexture(server,8,64,8,face)),"initial chunk sync produces the correct world texture immediately");
  }
  System.out.println("PASS initial native full/partial SPacketChunkData tag and byte roundtrip plus native client handleUpdateTag restore override/inventory/world textures for all five crane owners.");
 }
 static void drops()throws Exception {
  int[][] bounds={{0,21},{9,20},{9,11},{0,21},{0,23}};Field field=BlockCraneBase.class.getDeclaredField("rand");field.setAccessible(true);
  for(int b=0;b<owners.length;b++)for(boolean reject:new boolean[]{false,true}){BlockCraneBase owner=owners[b];NativeWorld w=new NativeWorld(false);w.setBlockState(P,owner.getStateFromMeta(4),2);TileEntityCraneBase tile=w.crane();for(int i=0;i<tile.slots.length;i++)tile.slots[i]=stack(45+i);Random actual=(Random)field.get(owner);long seed=900+b;actual.setSeed(seed);Random old=new Random(seed);w.rejectDrops=reject;w.clear();
   class Expected {double x,y,z,vx,vy,vz;int count;Expected(Random r,float x,float y,float z,int n){this.x=8+x;this.y=64+y;this.z=8+z;count=n;float motion=.05F;vx=(float)r.nextGaussian()*motion;vy=(float)r.nextGaussian()*motion+.2F;vz=(float)r.nextGaussian()*motion;}}
   List<Expected> expected=new ArrayList<>();for(int i=bounds[b][0];i<bounds[b][1];i++){float x=old.nextFloat()*.8F+.1F,y=old.nextFloat()*.8F+.1F,z=old.nextFloat()*.8F+.1F;int left=45+i;while(left>0){int n=old.nextInt(21)+10;if(n>left)n=left;left-=n;expected.add(new Expected(old,x,y,z,n));}}
   owner.breakBlock(w,P,w.getBlockState(P));check(w.drops.size()==expected.size(),"drop count range "+b);for(int i=0;i<expected.size();i++){Expected e=expected.get(i);EntityItem d=w.drops.get(i);check(d.posX==e.x&&d.posY==e.y&&d.posZ==e.z&&d.motionX==e.vx&&d.motionY==e.vy&&d.motionZ==e.vz,"seeded exact legacy drop positions/motions");check(d.getItem().getItem()==Items.STICK&&d.getItem().getItemDamage()==3&&d.getItem().getCount()==e.count,"depletion retains actual item/damage/count");check(d.getItem().getTagCompound().getCompoundTag("child").getInteger("nested")==29,"drop deep NBT preserved");if(i>0)check(d.getItem().getTagCompound()!=w.drops.get(i-1).getItem().getTagCompound(),"split drops separate NBT copies");}
   for(int i=0;i<tile.slots.length;i++)check(tile.slots[i].getCount()==(i>=bounds[b][0]&&i<bounds[b][1]?0:45+i),"exact half-open dropped slot bounds");check(w.events.indexOf("comparator")>w.events.lastIndexOf("drop")&&w.events.indexOf("remove")>w.events.indexOf("comparator"),"drop then comparator then native tile removal");check(w.removes==2,"original helper and owner both call superclass removal");check(actual.nextLong()==old.nextLong(),"exact random consumption");
  }
  System.out.println("PASS exact seeded drop slot ranges, count splitting, RNG/float motion order, retained item/damage/deep NBT, rejected-spawn behavior and comparator/removal order.");
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();owners=new BlockCraneBase[]{new CraneBoxer(),new CraneExtractor(),new CraneGrabber(),new CraneInserter(),new CraneUnboxer()};for(int i=0;i<owners.length;i++)register(owners[i],"crane_"+i);
  TileEntity.register("crane_probe_filter",FilterTile.class);TileEntity.register("crane_probe_boxer",TileEntityCraneBoxer.class);TileEntity.register("crane_probe_extractor",TileEntityCraneExtractor.class);TileEntity.register("crane_probe_grabber",TileEntityCraneGrabber.class);TileEntity.register("crane_probe_inserter",TileEntityCraneInserter.class);TileEntity.register("crane_probe_unboxer",TileEntityCraneUnboxer.class);
  stateAndTextures();transferAlgorithm();workflows();lifecycleAndScrew();placementAndActivation();settingsFilterAndPacket();initialChunkSync();drops();System.out.println("PASS NativeCraneOwnersTest: "+checks+" native assertions.");
 }
}
