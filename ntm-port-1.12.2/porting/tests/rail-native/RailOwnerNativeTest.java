import java.lang.reflect.*;
import java.util.*;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.rail.*;
import com.hbm.blocks.rail.IRailNTM.*;
import com.hbm.blocks.machine.MachineFunnel;
import com.hbm.lib.ForgeDirection;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.property.IExtendedBlockState;
public final class RailOwnerNativeTest {
 static int checks;static sun.misc.Unsafe unsafe;
 static void ck(boolean condition,String label){checks++;if(!condition)throw new AssertionError(label);}
 static void same(double a,double b,String label){ck(Double.doubleToLongBits(a)==Double.doubleToLongBits(b)||Math.abs(a-b)<1E-9,label+": "+a+" != "+b);}
 public static final class W extends WorldServer {
  Map<BlockPos,IBlockState> states;Map<BlockPos,TileEntity> tiles;List<BlockPos> writes;int notified,renderUpdates;List<Entity> spawned;boolean loaded=true;
  W(){super(null,null,null,0,null);}
  @Override public IBlockState getBlockState(BlockPos p){return states.getOrDefault(p,net.minecraft.init.Blocks.AIR.getDefaultState());}
  @Override public boolean setBlockState(BlockPos p,IBlockState s,int flags){states.put(p.toImmutable(),s);writes.add(p.toImmutable());return true;}
  @Override public TileEntity getTileEntity(BlockPos p){return tiles.get(p);}
  @Override public void setTileEntity(BlockPos p,TileEntity t){tiles.put(p.toImmutable(),t);t.setWorld(this);t.setPos(p);}
  @Override public void removeTileEntity(BlockPos p){tiles.remove(p);}
  @Override public boolean isAreaLoaded(BlockPos a,BlockPos b){return loaded;}
  @Override public void notifyBlockUpdate(BlockPos p,IBlockState a,IBlockState b,int flags){notified++;}
  @Override public void markBlockRangeForRenderUpdate(BlockPos a,BlockPos b){renderUpdates++;}
  @Override public void updateComparatorOutputLevel(BlockPos p,Block b){}
  @Override public void markChunkDirty(BlockPos p,TileEntity t){}
  @Override public boolean spawnEntity(Entity e){spawned.add(e);return true;}
  @Override public void scheduleUpdate(BlockPos p,Block b,int ticks){}
  @Override public WorldType getWorldType(){return WorldType.DEFAULT;}
  @Override public boolean mayPlace(Block b,BlockPos p,boolean skip,EnumFacing face,Entity e){return true;}
  @Override public net.minecraft.server.MinecraftServer getMinecraftServer(){return null;}
  @Override public void playSound(EntityPlayer p,double x,double y,double z,SoundEvent e,SoundCategory c,float v,float pitch){}
 }
 public static final class P extends EntityPlayer {
  P(){super(null,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"rail"));}
  boolean sneaking;@Override public boolean isSneaking(){return sneaking;}@Override public boolean isSpectator(){return false;}@Override public boolean isCreative(){return capabilities.isCreativeMode;}
 }
 static W world()throws Exception{W w=(W)unsafe.allocateInstance(W.class);w.states=new HashMap<>();w.tiles=new HashMap<>();w.writes=new ArrayList<>();w.spawned=new ArrayList<>();w.loaded=true;Field f=World.class.getDeclaredField("rand");f.setAccessible(true);f.set(w,new Random(7));Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(w,new WorldProviderHell());return w;}
 static P player(W w)throws Exception{P p=(P)unsafe.allocateInstance(P.class);p.world=w;p.inventory=new InventoryPlayer(p);Field f=EntityPlayer.class.getDeclaredField("capabilities");f.setAccessible(true);f.set(p,new PlayerCapabilities());p.posX=100;p.posY=100;p.posZ=100;return p;}
 static BlockRailNTM[] owners(){return new BlockRailNTM[]{new RailNarrowStraight(),new RailNarrowCurve(),new RailStandardStraight(),new RailStandardStraightShort(),new RailStandardCurveBase(),new RailStandardCurveWide7(),new RailStandardCurveWide9(),new RailStandardRamp(),new RailStandardBuffer(),new RailStandardSwitch(),new RailStandardSwitchFlipped()};}
 static OriginalRailRouting.Base oracle(BlockRailNTM b)throws Exception{OriginalRailRouting.Base o=(OriginalRailRouting.Base)Class.forName("OriginalRailRouting$"+b.getClass().getSimpleName()).newInstance();o.owner=b;return o;}
 static Method method(Class<?> c,String name,Class<?>...args)throws Exception{for(Class<?> t=c;t!=null;t=t.getSuperclass())try{Method m=t.getDeclaredMethod(name,args);m.setAccessible(true);return m;}catch(NoSuchMethodException ignored){}throw new NoSuchMethodException(name);}
 static void contexts(RailContext a,RailContext b,MoveContext c,MoveContext d,String label){same(a.yaw,b.yaw,label+" yaw");same(a.overshoot,b.overshoot,label+" remainder");ck(Objects.equals(a.pos,b.pos),label+" next block");ck(c.collision==d.collision,label+" collision");same(c.overshoot,d.overshoot,label+" collision overshoot");}
 static void routing()throws Exception{
  Random random=new Random(907315);BlockPos core=new BlockPos(12,64,-23);
  for(BlockRailNTM block:owners()){
   OriginalRailRouting.Base oracle=oracle(block);
   Method travel=method(oracle.getClass(),"getTravelLocation",World.class,int.class,int.class,int.class,double.class,double.class,double.class,double.class,double.class,double.class,double.class,RailContext.class,MoveContext.class);
   Method snap=method(oracle.getClass(),"getSnappingPos",World.class,int.class,int.class,int.class,double.class,double.class,double.class);
   for(int meta=12;meta<16;meta++)for(int sign=-1;sign<=1;sign++){
    W world=world();world.states.put(core,block.getStateFromMeta(meta));
    if(sign>=0){RailStandardSwitch.TileEntityRailSwitch tile=new RailStandardSwitch.TileEntityRailSwitch();world.setTileEntity(core,tile);tile.isSwitched=sign==1;}
    for(int sample=0;sample<160;sample++){
     double x=core.getX()+random.nextDouble()*24-12,y=64+random.nextDouble(),z=core.getZ()+random.nextDouble()*24-12;
     double mx=random.nextDouble()*2-1,my=0,mz=random.nextDouble()*2-1,speed=sample%13==0?0:(random.nextDouble()*12-6);
     RailContext a=new RailContext(),b=new RailContext();MoveContext c=new MoveContext(RailCheckType.values()[sample%4],sample%7*.4),d=new MoveContext(c.type,c.collisionBogieDistance);
     Vec3d actual=((IRailNTM)block).getTravelLocation(world,12,64,-23,x,y,z,mx,my,mz,speed,a,c);
     OriginalRailRouting.V expected=(OriginalRailRouting.V)travel.invoke(oracle,world,12,64,-23,x,y,z,mx,my,mz,speed,b,d);
     String label=block.getClass().getSimpleName()+" meta="+meta+" sign="+sign+" sample="+sample;
     same(actual.x,expected.xCoord,label+" x");same(actual.y,expected.yCoord,label+" y");same(actual.z,expected.zCoord,label+" z");contexts(a,b,c,d,label);
     Vec3d actualSnap=((IRailNTM)block).getSnappingPos(world,12,64,-23,x,y,z);OriginalRailRouting.V expectedSnap=(OriginalRailRouting.V)snap.invoke(oracle,world,12,64,-23,x,y,z);
     same(actualSnap.x,expectedSnap.xCoord,label+" snap x");same(actualSnap.y,expectedSnap.yCoord,label+" snap y");same(actualSnap.z,expectedSnap.zCoord,label+" snap z");
    }
   }
   W empty=world();Vec3d unchanged=((IRailNTM)block).getSnappingPos(empty,0,0,0,3,4,5);ck(unchanged.equals(new Vec3d(3,4,5)),"missing core unchanged");
  }
 }
 static void placement()throws Exception{
  Class<?>[] signature={World.class,int.class,int.class,int.class,ForgeDirection.class,int.class};
  for(BlockRailNTM block:owners()){
   OriginalRailRouting.Base oracle=oracle(block);Method nativeFill=method(block.getClass(),"fillSpace",signature),oldFill=method(oracle.getClass(),"fillSpace",signature),nativeCheck=method(block.getClass(),"checkRequirement",signature),oldCheck=method(oracle.getClass(),"checkRequirement",signature);
   for(ForgeDirection dir:new ForgeDirection[]{ForgeDirection.NORTH,ForgeDirection.SOUTH,ForgeDirection.EAST,ForgeDirection.WEST}){
    W a=world(),b=world();int offset=-block.getOffset();BlockPos core=new BlockPos(10+dir.offsetX*offset,64,20+dir.offsetZ*offset);a.states.put(core,block.getStateFromMeta(dir.ordinal()+10));b.states.putAll(a.states);
    nativeFill.invoke(block,a,10,64,20,dir,offset);oldFill.invoke(oracle,b,10,64,20,dir,offset);
    ck(a.states.equals(b.states),"original multiblock occupancy/metadata "+block.getClass()+dir);ck(a.writes.equals(b.writes),"original ordered placement "+block.getClass()+dir);ck(!BlockDummyable.safeRem,"safeRem reset");
    for(BlockPos pos:a.writes){int[] actual=block.findCore(a,pos.getX(),pos.getY(),pos.getZ());ck(actual!=null&&actual[0]==core.getX()&&actual[1]==core.getY()&&actual[2]==core.getZ(),"every original dummy resolves core "+block.getClass()+dir+pos);}
    a=world();b=world();ck(nativeCheck.invoke(block,a,10,64,20,dir,offset).equals(oldCheck.invoke(oracle,b,10,64,20,dir,offset)),"empty placement requirement");
    for(BlockPos p:new ArrayList<>(worldOccupied(block,dir).keySet())){a=world();b=world();a.states.put(p,net.minecraft.init.Blocks.STONE.getDefaultState());b.states.putAll(a.states);ck(nativeCheck.invoke(block,a,10,64,20,dir,offset).equals(oldCheck.invoke(oracle,b,10,64,20,dir,offset)),"occupied requirement original "+block.getClass()+dir+p);}
   }
  }
 }
 static Map<BlockPos,IBlockState> worldOccupied(BlockRailNTM b,ForgeDirection d)throws Exception{W w=world();method(b.getClass(),"fillSpace",World.class,int.class,int.class,int.class,ForgeDirection.class,int.class).invoke(b,w,10,64,20,d,-b.getOffset());return w.states;}
 static void funnel()throws Exception{
  MachineFunnel funnel=new MachineFunnel();W w=world();BlockPos p=new BlockPos(3,64,5);net.minecraft.tileentity.TileEntityFurnace inventory=new net.minecraft.tileentity.TileEntityFurnace();ItemStack original=new ItemStack(net.minecraft.init.Items.DIAMOND,64,3);NBTTagCompound tag=new NBTTagCompound();tag.setString("rail_probe","preserved");original.setTagCompound(tag);inventory.setInventorySlotContents(0,original);w.setTileEntity(p,inventory);funnel.breakBlock(w,p,funnel.getDefaultState());int total=0;
  for(Entity e:w.spawned){ItemStack drop=((net.minecraft.entity.item.EntityItem)e).getItem();total+=drop.getCount();ck(drop.getItem()==net.minecraft.init.Items.DIAMOND&&drop.getItemDamage()==3,"funnel final stack item/meta retained");ck(drop.getTagCompound()!=tag&&drop.getTagCompound().equals(tag),"funnel copied NBT");}
  ck(total==64&&original.isEmpty()&&!w.tiles.containsKey(p),"funnel native break once conserves item count and removes tile");ck(funnel.createNewTileEntity(w,0) instanceof com.hbm.tileentity.machine.TileEntityMachineFunnel,"funnel actual owner tile factory boundary");P player=player(w);player.sneaking=true;ck(!funnel.onBlockActivated(w,p,funnel.getDefaultState(),player,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0),"funnel sneaking passes through");
 }
 static void boundaries()throws Exception{
  for(BlockRailNTM b:owners())for(int m=0;m<16;m++){IBlockState s=b.getStateFromMeta(m);ck(b.getMetaFromState(s)==m,"metadata roundtrip");ck(b.getRenderType(s)==EnumBlockRenderType.MODEL&&!b.isFullCube(s)&&!b.isOpaqueCube(s),"native model contract");W w=world();BlockPos p=new BlockPos(10,64,20);w.states.put(p,s);AxisAlignedBB box=b.getBoundingBox(s,w,p);ck(box.equals(new AxisAlignedBB(0,0,0,1,.125,1)),"original selection bounds");if(!(b instanceof RailStandardRamp)){List<AxisAlignedBB> boxes=new ArrayList<>();b.addCollisionBoxToList(s,w,p,new AxisAlignedBB(p).grow(2),boxes,null,false);ck(boxes.size()==1&&boxes.get(0).equals(new AxisAlignedBB(10,64,20,11,64.125,21)),"native original thin collision");}}
  RailStandardRamp ramp=new RailStandardRamp();ck(ramp.bounding.size()==5,"original five ramp collision steps");
  for(int meta=12;meta<16;meta++){W w=world();BlockPos core=new BlockPos(10,64,20);IBlockState state=ramp.getStateFromMeta(meta);w.states.put(core,state);List<AxisAlignedBB> boxes=new ArrayList<>();ramp.addCollisionBoxToList(state,w,core,new AxisAlignedBB(core).grow(20),boxes,null,false);ck(boxes.size()==5,"native ramp collision dispatch");ForgeDirection rot=ForgeDirection.getOrientation(meta-10).getRotation(ForgeDirection.UP);for(int i=0;i<5;i++){AxisAlignedBB expected=BlockDummyable.getAABBRotationOffset(new AxisAlignedBB(-2.5+i,0,-1.5,-1.5+i,.1+.2*i,.5),10.5,64,20.5,rot);AxisAlignedBB got=boxes.get(i);same(got.minX,expected.minX,"ramp step minX");same(got.maxZ,expected.maxZ,"ramp step maxZ");same(got.maxY,expected.maxY,"ramp step height");}}
  for(BlockRailNTM b:new BlockRailNTM[]{new RailStandardSwitch(),new RailStandardSwitchFlipped()}){
   W w=world();BlockPos p=new BlockPos(10,64,20);IBlockState state=b.getStateFromMeta(12);w.states.put(p,state);RailStandardSwitch.TileEntityRailSwitch tile=(RailStandardSwitch.TileEntityRailSwitch)b.createNewTileEntity(w,12);w.setTileEntity(p,tile);P player=player(w);
   for(EnumHand hand:EnumHand.values()){boolean old=tile.isSwitched;ck(b.onBlockActivated(w,p,state,player,hand,EnumFacing.UP,.5F,.5F,.5F),"native switch activation");ck(tile.isSwitched!=old&&w.notified>0,"switch state and native update");ck(BlockRailNTM.switchSign(b.getExtendedState(state,w,p))==(tile.isSwitched?2:1),"extended switch sign");player.setHeldItem(hand,new ItemStack(com.hbm.items.ModItems.train));old=tile.isSwitched;ck(!b.onBlockActivated(w,p,state,player,hand,EnumFacing.UP,.5F,.5F,.5F)&&old==tile.isSwitched,"train tool pass-through in active hand");player.setHeldItem(hand,ItemStack.EMPTY);}
   NBTTagCompound nbt=tile.writeToNBT(new NBTTagCompound());RailStandardSwitch.TileEntityRailSwitch copy=new RailStandardSwitch.TileEntityRailSwitch();copy.readFromNBT(nbt);ck(copy.isSwitched==tile.isSwitched,"switch persistent NBT");ck(tile.getUpdateTag().getBoolean("isSwitched")==tile.isSwitched,"initial chunk switch tag");copy.setWorld(w);copy.setPos(p);tile.isSwitched=!tile.isSwitched;copy.onDataPacket(null,tile.getUpdatePacket());ck(copy.isSwitched==tile.isSwitched&&w.renderUpdates==1,"switch native packet + render refresh");
   w.tiles.clear();ck(BlockRailNTM.switchSign(b.getExtendedState(state,w,p))==0,"no tile no sign");
  }
 }
 public static void main(String[] args)throws Exception{net.minecraft.init.Bootstrap.register();Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=(sun.misc.Unsafe)f.get(null);net.minecraft.tileentity.TileEntity.register("rail_switch_probe",RailStandardSwitch.TileEntityRailSwitch.class);routing();placement();boundaries();funnel();System.out.println("PASS "+checks+" native rail owner/routing/placement/lifecycle assertions against extracted pristine methods. Captured world boundary, not launched gameplay.");}
}
