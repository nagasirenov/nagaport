import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.lib.ForgeDirection;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public class DummyableNativeTest {
 static int checks;static sun.misc.Unsafe u;
 static void ck(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 public static class W extends WorldServer {
  net.minecraft.world.chunk.Chunk chunk;
  Map<BlockPos,IBlockState> states;Map<BlockPos,TileEntity> tiles;List<Integer> scheduled;int removals;List<Entity> spawned;boolean loaded=true;
  W(){super(null,null,null,0,null);}
  public IBlockState getBlockState(BlockPos p){return chunk!=null?chunk.getBlockState(p):states.getOrDefault(p,net.minecraft.init.Blocks.AIR.getDefaultState());}
  public boolean setBlockState(BlockPos p,IBlockState s,int flags){if(chunk!=null)return chunk.setBlockState(p,s)!=null;IBlockState old=getBlockState(p);states.put(p.toImmutable(),s);if(old.getBlock()!=s.getBlock())old.getBlock().breakBlock(this,p,old);return true;}
  public TileEntity getTileEntity(BlockPos p){return tiles.get(p);}
  public void setTileEntity(BlockPos p,TileEntity tile){if(tile==null)return;tiles.put(p.toImmutable(),tile);tile.setWorld(this);tile.setPos(p);if(chunk!=null)chunk.addTileEntity(p,tile);}public void removeTileEntity(BlockPos p){removals++;tiles.remove(p);if(chunk!=null)chunk.removeTileEntity(p);}
  public boolean isAreaLoaded(BlockPos a,BlockPos b){return loaded;}
  public void scheduleUpdate(BlockPos p,Block b,int delay){scheduled.add(delay);}
  public void updateComparatorOutputLevel(BlockPos p,Block b){}
  public boolean spawnEntity(Entity e){spawned.add(e);return true;}
  public WorldType getWorldType(){return WorldType.DEFAULT;}
  public boolean mayPlace(Block b,BlockPos p,boolean skip,EnumFacing face,Entity e){return true;}
  public net.minecraft.server.MinecraftServer getMinecraftServer(){return null;}
  public void playSound(EntityPlayer p,BlockPos pos,SoundEvent event,SoundCategory cat,float volume,float pitch){}
  public void playSound(EntityPlayer p,double x,double y,double z,SoundEvent event,SoundCategory cat,float volume,float pitch){}
 }
 public static class B extends BlockDummyable {
  int neighbor,tick,placed,broken,placement,harvest,collision;boolean require=true;
  B(){super(Material.IRON);}
  public TileEntity createNewTileEntity(World w,int m){return null;}public int[] getDimensions(){return new int[]{1,0,1,1,1,1};}public int getOffset(){return 0;}
  public void onNeighborBlockChange(World w,int x,int y,int z,Block b){neighbor++;super.onNeighborBlockChange(w,x,y,z,b);}
  public void updateTick(World w,int x,int y,int z,Random r){tick++;super.updateTick(w,x,y,z,r);}
  public int onBlockPlaced(World w,int x,int y,int z,int side,float hx,float hy,float hz,int meta){placement++;return super.onBlockPlaced(w,x,y,z,side,hx,hy,hz,meta);}
  public void onBlockPlacedBy(World w,int x,int y,int z,EntityLivingBase p,ItemStack s){placed++;super.onBlockPlacedBy(w,x,y,z,p,s);}
  public void breakBlock(World w,int x,int y,int z,Block b,int m){broken++;super.breakBlock(w,x,y,z,b,m);}
  protected boolean checkRequirement(World w,int x,int y,int z,ForgeDirection d,int o){return require&&super.checkRequirement(w,x,y,z,d,o);}
 }
 public static class T extends B {public TileEntity createNewTileEntity(World w,int meta){return meta>=6?new TileEntity(){}:null;}}
 public static class Shape extends B {
  int boxCalls;public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){boxCalls++;return new AxisAlignedBB(x+.25,y,z+.25,x+.75,y+.25,z+.75);}
 }
 public static class C extends net.minecraft.world.chunk.Chunk {C(W w){super(w,0,0);}public void generateSkylightMap(){} }
 public static class P extends EntityPlayer {
  P(){super(null,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"probe"));}
  public boolean isSpectator(){return false;}public boolean isCreative(){return capabilities.isCreativeMode;}
  public boolean canPlayerEdit(BlockPos p,EnumFacing f,ItemStack s){return true;}
 }
 static P player(W w)throws Exception{P p=(P)u.allocateInstance(P.class);p.world=w;p.inventory=new net.minecraft.entity.player.InventoryPlayer(p);java.lang.reflect.Field f=EntityPlayer.class.getDeclaredField("capabilities");f.setAccessible(true);f.set(p,new PlayerCapabilities());p.posX=100;p.posY=100;p.posZ=100;return p;}
 static void chunkLifecycle()throws Exception {
  W w=world();java.lang.reflect.Field pf=World.class.getDeclaredField("provider");pf.setAccessible(true);pf.set(w,new net.minecraft.world.WorldProviderHell());w.chunk=new C(w);B b=new B();b.setLightOpacity(0);BlockPos p=new BlockPos(5,64,5);
  w.setBlockState(p,b.getStateFromMeta(12),3);ck(w.getBlockState(p).getBlock()==b,"actual Chunk stores base state");
  w.setBlockState(p,b.getStateFromMeta(13),3);ck(b.broken==0,"actual Chunk metadata-only change no break");
  net.minecraft.tileentity.TileEntityFurnace tile=new net.minecraft.tileentity.TileEntityFurnace();ItemStack input=new ItemStack(net.minecraft.init.Items.DIAMOND,57);net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();tag.setInteger("probe",42);input.setTagCompound(tag);tile.setInventorySlotContents(0,input);w.tiles.put(p,tile);
  w.setBlockState(p,net.minecraft.init.Blocks.AIR.getDefaultState(),3);ck(b.broken==1,"actual Chunk dispatch break override once");ck(w.removals==1,"actual Chunk base removes tile once");
  int dropped=0;for(Entity e:w.spawned){ItemStack out=((net.minecraft.entity.item.EntityItem)e).getItem();dropped+=out.getCount();ck(out.getItem()==net.minecraft.init.Items.DIAMOND&&out.getTagCompound().getInteger("probe")==42,"native drop preserves item and copied NBT");ck(out.getTagCompound()!=tag,"drop tag independent");}ck(dropped==57&&input.isEmpty(),"one native Chunk removal conserves inventory once");
 }
 static void extraLifecycle()throws Exception{
  W w=world();java.lang.reflect.Field pf=World.class.getDeclaredField("provider");pf.setAccessible(true);pf.set(w,new net.minecraft.world.WorldProviderHell());w.chunk=new C(w);T b=new T();b.setLightOpacity(0);BlockPos p=new BlockPos(6,64,6);w.setBlockState(p,b.getStateFromMeta(5),3);ck(w.getTileEntity(p)==null,"ordinary dummy no tile");b.makeExtra(w,6,64,6);TileEntity tile=w.getTileEntity(p);ck(tile!=null,"native Chunk creates extra connector tile on metadata upgrade");b.removeExtra(w,6,64,6);ck(w.getTileEntity(p)==null,"native mod tile refresh removes extra connector on metadata downgrade");
 }
 static void placement()throws Exception{
  B b=new B();b.setRegistryName("dummy_probe","base");net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(b);ItemBlock item=new ItemBlock(b);item.setRegistryName(b.getRegistryName());net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item);java.lang.reflect.Field f=Item.class.getDeclaredField("BLOCK_TO_ITEM");f.setAccessible(true);((Map<Block,Item>)f.get(null)).put(b,item);
  for(EnumHand hand:EnumHand.values())for(boolean success:new boolean[]{true,false})for(int count:new int[]{1,10,64}){
   W w=world();P p=player(w);ItemStack stack=new ItemStack(item,count);p.setHeldItem(hand,stack);b.require=success;int before=b.placed;
   ck(item.onItemUse(p,w,new BlockPos(5,64,5),hand,EnumFacing.UP,.5f,.5f,.5f)==EnumActionResult.SUCCESS,"native ItemBlock result");
   ck(b.placed==before+1,"native ItemBlock virtual placement exactly once");int total=0;for(ItemStack v:p.inventory.mainInventory)total+=v.getCount();for(ItemStack v:p.inventory.offHandInventory)total+=v.getCount();
   ck(total==count-(success?1:0),"placement conservation hand="+hand+" success="+success+" count="+count+" total="+total);
   ck((w.getBlockState(new BlockPos(5,64,5)).getBlock()==b)==success,"placed core or failure removed");ck(!BlockDummyable.safeRem,"safe removal reset");
  }
 }
 static W world()throws Exception{W w=(W)u.allocateInstance(W.class);w.states=new HashMap<>();w.tiles=new HashMap<>();w.scheduled=new ArrayList<>();w.loaded=true;w.spawned=new ArrayList<>();java.lang.reflect.Field rf=World.class.getDeclaredField("rand");rf.setAccessible(true);rf.set(w,new Random(123));return w;}
 public static void main(String[] args)throws Exception{
  net.minecraft.init.Bootstrap.register();java.lang.reflect.Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);u=(sun.misc.Unsafe)f.get(null);
  placement();chunkLifecycle();extraLifecycle();
  RealBoilerHooks boiler=new RealBoilerHooks();W bw=world();BlockPos core=new BlockPos(10,60,10);bw.states.put(core,boiler.getStateFromMeta(12));boiler.fillSpace(bw,10,60,9,ForgeDirection.NORTH,-1);int extras=0;for(IBlockState st:bw.states.values())if(boiler.hasExtra(boiler.getMetaFromState(st)))extras++;ck(extras==3,"exact original boiler fill hook calls base and makes three extra connectors");
  B b=new B();W w=world();BlockPos p=new BlockPos(5,64,-9);
  for(int i=0;i<16;i++)ck(b.getMetaFromState(b.getStateFromMeta(i))==i,"metadata "+i);
  for(EnumFacing face:EnumFacing.values())ck(b.getMetaFromState(b.getStateForPlacement(w,p,face,.2f,.3f,.4f,0,null,EnumHand.MAIN_HAND))==face.getIndex(),"placement side");
  ck(b.placement==6,"placement virtual hook once");
  w.states.put(p,b.getStateFromMeta(12));b.neighborChanged(w.getBlockState(p),w,p,net.minecraft.init.Blocks.AIR,p.up());ck(b.neighbor==1&&w.getBlockState(p).getBlock()==b,"core neighbor once remains");
  b.updateTick(w,p,w.getBlockState(p),new Random(1));ck(b.tick==1,"tick hook once");
  w.states.clear();w.states.put(p,b.getStateFromMeta(5));w.loaded=false;b.neighborChanged(w.getBlockState(p),w,p,b,p);ck(w.getBlockState(p).getBlock()==b,"unloaded orphan retained");w.loaded=true;b.neighborChanged(w.getBlockState(p),w,p,b,p);ck(w.getBlockState(p).getBlock()==net.minecraft.init.Blocks.AIR,"loaded orphan removed");ck(b.broken==1&&w.removals==1,"break dispatch and tile removal once");
  w.states.put(p,b.getStateFromMeta(12));MultiblockHandlerXR.fillSpace(w,p.getX(),p.getY(),p.getZ(),b.getDimensions(),b,ForgeDirection.NORTH);
  for(BlockPos q:new ArrayList<>(w.states.keySet())){int[] c=b.findCore(w,q.getX(),q.getY(),q.getZ());ck(c!=null&&c[0]==p.getX()&&c[1]==p.getY()&&c[2]==p.getZ(),"dummy chain resolves core "+q);}
  BlockPos extra=p.east();b.makeExtra(w,extra.getX(),extra.getY(),extra.getZ());ck(b.getMetaFromState(w.getBlockState(extra))==11,"extra metadata");b.removeExtra(w,extra.getX(),extra.getY(),extra.getZ());ck(b.getMetaFromState(w.getBlockState(extra))==5,"remove extra");
  b.setBlockBounds(0,0,0,1,.5f,1);ck(b.getBoundingBox(b.getDefaultState(),w,p).maxY==.5,"owned bounds retained");List<AxisAlignedBB> boxes=new ArrayList<>();b.addCollisionBoxToList(b.getDefaultState(),w,p,new AxisAlignedBB(p).grow(2),boxes,null,false);ck(boxes.size()==1&&boxes.get(0).maxY==64.5,"native collision uses owned bounds");
  AxisAlignedBB local=new AxisAlignedBB(-1,0,-2,3,4,5);for(ForgeDirection d:new ForgeDirection[]{ForgeDirection.NORTH,ForgeDirection.SOUTH,ForgeDirection.EAST,ForgeDirection.WEST}){AxisAlignedBB out=BlockDummyable.getAABBRotationOffset(local,100,64,-100,d);ck(out.minY==64&&out.maxY==68&&out.minX>90&&out.maxZ< -90,"immutable native AABB offset "+d);}
  Shape shape=new Shape();boxes.clear();shape.addCollisionBoxToList(shape.getDefaultState(),w,p,new AxisAlignedBB(p),boxes,null,false);ck(shape.boxCalls==1&&boxes.size()==1&&boxes.get(0).maxY==64.25,"custom world-coordinate collision hook once");
  ck(b.getRenderType(b.getDefaultState())==EnumBlockRenderType.INVISIBLE,"default TESR owner invisible");
  System.out.println("PASS "+checks+" native whole-base assertions; captured world storage boundary, not launched game.");
 }
}
