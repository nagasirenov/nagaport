import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.init.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import com.hbm.blocks.generic.BlockMetalFence;

public final class FullCubeNativeTest {
 static int checks;static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 public static class SideEffect extends Block { static int calls;public SideEffect(){super(Material.ROCK);}public boolean isFullCube(IBlockState s){calls++;return false;} }
 public static class Dynamic extends CallbackBank.StateFixture {public boolean isFullCube(IBlockState s){return s.getValue(CallbackBank.FLAG);} }
 public static class FullControl extends Block { public FullControl(){super(Material.ROCK);} public boolean isOpaqueCube(IBlockState s){return false;} }
 public static class OpaqueNotFull extends Block {public OpaqueNotFull(){super(Material.ROCK);} public boolean isFullCube(IBlockState s){return false;} }
 static class Access implements IBlockAccess {
  IBlockState state;Access(IBlockState s){state=s;}
  public IBlockState getBlockState(BlockPos p){return state;}
  public net.minecraft.tileentity.TileEntity getTileEntity(BlockPos p){throw new AssertionError("unexercised tile access");}
  public int getCombinedLight(BlockPos p,int i){throw new AssertionError("unexercised light access");}
  public boolean isAirBlock(BlockPos p){throw new AssertionError("unexercised air access");}
  public net.minecraft.world.biome.Biome getBiome(BlockPos p){throw new AssertionError("unexercised biome access");}
  public int getStrongPower(BlockPos p,EnumFacing f){throw new AssertionError("unexercised power access");}
  public WorldType getWorldType(){throw new AssertionError("unexercised world type");}
  public boolean isSideSolid(BlockPos p,EnumFacing f,boolean d){return state.isSideSolid(this,p,f);}
 }
 static BlockMetalFence fence;
 static void verify(Block b){
  check(!b.isFullCube(null),"null argument remains ignored");check(!b.isFullCube(Blocks.STONE.getDefaultState()),"foreign state remains ignored");
  for(IBlockState s:b.getBlockState().getValidStates()){
   check(!s.isFullCube(),"actual state full-cube virtual dispatch");check(!s.isOpaqueCube(),"independent actual opacity callback unchanged");
   check(!s.isFullBlock()&&s.getLightOpacity()==0,"constructor caches opacity rather than full-cube");
   check(!s.isBlockNormalCube()&&!s.isNormalCube()&&!s.causesSuffocation()&&!s.isTopSolid(),"genuine native full-cube consumers retain false");
   check(b.getCollisionBoundingBox(s,null,BlockPos.ORIGIN)==Block.FULL_BLOCK_AABB,"full-cube callback does not rewrite default collision");
   Access w=new Access(s);
   check(!fence.canConnectFenceTo(w,BlockPos.ORIGIN),"actual NTM fence rejects constant false target");
   for(EnumFacing face:EnumFacing.values())check(s.getBlockFaceShape(w,BlockPos.ORIGIN,face)==BlockFaceShape.SOLID,"no guessed face-shape policy added");
  }
 }
 static void original(String jar,String dir)throws Exception{
  try(URLClassLoader loader=new URLClassLoader(new URL[]{new File(jar).toURI().toURL(),new File(dir).toURI().toURL()},FullCubeNativeTest.class.getClassLoader())){
   Class<?> old=Class.forName("oracle.OldFullCubeFixture",true,loader),base=Class.forName("aji",true,loader);Object b=old.newInstance();
   check(old.getMethod("d").invoke(b).equals(false),"original exact render-normal callback");
   check(base.getMethod("q").invoke(b).equals(false)&&base.getMethod("r").invoke(b).equals(false),"actual original normal-cube consumers retain false");
   Method silk=base.getDeclaredMethod("E");silk.setAccessible(true);check(silk.invoke(b).equals(false),"actual original silk eligibility rejects nonnormal block");
   Class<?> access=Class.forName("ahl",true,loader),oldFence=Class.forName("akz",true,loader),material=Class.forName("awt",true,loader);
   Object w=java.lang.reflect.Proxy.newProxyInstance(loader,new Class[]{access},(proxy,method,values)->{if(method.getName().equals("a")&&method.getParameterCount()==3)return b;throw new AssertionError("unexercised original world query "+method);});
   Object fence=oldFence.getConstructor(String.class,material).newInstance("test",material.getField("e").get(null));
   check(oldFence.getMethod("e",access,int.class,int.class,int.class).invoke(fence,w,0,0,0).equals(false),"actual original fence full-cube branch rejects same false target as NTM native fence");
   check(!field(base,"q").getBoolean(b)&&field(base,"r").getInt(b)==0,"original independent opacity constructor cache");
   Class<?> side=Class.forName("oracle.OldFullCubeFixture$SideEffect",true,loader);Object count=side.newInstance();
   check(side.getField("calls").getInt(null)==0,"original constructor does not call render-normal");
   side.getMethod("d").invoke(count);side.getMethod("d").invoke(count);check(side.getField("calls").getInt(null)==2,"original callback remains live");
  }
 }
 public static void main(String[] args)throws Exception{
  Bootstrap.register();net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  fence=new BlockMetalFence(Material.IRON);
  Block[] banks=CallbackBank.create(),children=CallbackBank.descendants();check(banks.length==85&&children.length==85,"exact 85 callbacks and inherited dispatch controls");
  for(int i=0;i<banks.length;i++){verify(banks[i]);verify(children[i]);}
  Dynamic dynamic=new Dynamic();IBlockState lo=dynamic.getDefaultState().withProperty(CallbackBank.FLAG,false),hi=lo.withProperty(CallbackBank.FLAG,true);
  check(!lo.isFullCube()&&hi.isFullCube(),"state-dependent native callback is live, not cached");
  check(lo.isFullBlock()&&hi.isFullBlock(),"fullBlock cache is opacity, independent from full-cube");
  SideEffect.calls=0;SideEffect side=new SideEffect();check(SideEffect.calls==0,"native constructor does not call full-cube");side.getDefaultState().isFullCube();side.getDefaultState().isFullCube();check(SideEffect.calls==2,"state delegates each full-cube query");
  OpaqueNotFull opaque=new OpaqueNotFull();check(opaque.getDefaultState().isOpaqueCube()&&opaque.getDefaultState().isFullBlock()&&!opaque.getDefaultState().isFullCube(),"opacity is not conflated with full-cube");
  check(!opaque.getDefaultState().isNormalCube()&&!opaque.getDefaultState().causesSuffocation()&&!opaque.getDefaultState().isTopSolid(),"full-cube false alone is relevant to actual native consumers");
  Block plasma=new com.hbm.blocks.generic.BlockPlasma(Material.FIRE),vacuum=new com.hbm.blocks.gas.BlockVacuum(),leaves=new com.hbm.blocks.generic.WasteLeaves(Material.LEAVES),turret=new com.hbm.blocks.turret.TurretSentry();
  verify(plasma);verify(vacuum);verify(leaves);verify(turret);
  Constructor<?> ctor=Class.forName("com.hbm.blocks.BlockWaterPlant").getDeclaredConstructor();ctor.setAccessible(true);verify((Block)ctor.newInstance());
  check(plasma.getTickRandomly()&&leaves.getTickRandomly(),"actual constructor ticks retained");verify(new BlockChargeBaseStateSlice());verify(new BlockNTMFlowerStateSlice());
  // Native default face shape/collision are separate contracts. Do not claim old connectivity parity.
  Access full=new Access(new FullControl().getDefaultState()),nonfull=new Access(banks[0].getDefaultState());
  for(EnumFacing face:EnumFacing.HORIZONTALS){
   check(((BlockFence)Blocks.OAK_FENCE).canConnectTo(full,BlockPos.ORIGIN,face)==((BlockFence)Blocks.OAK_FENCE).canConnectTo(nonfull,BlockPos.ORIGIN,face),"native vanilla fence result unaffected by full-cube migration");
   check(((BlockPane)Blocks.GLASS_PANE).canPaneConnectTo(full,BlockPos.ORIGIN,face)==((BlockPane)Blocks.GLASS_PANE).canPaneConnectTo(nonfull,BlockPos.ORIGIN,face),"native vanilla pane result unaffected by full-cube migration");
   check(Blocks.STONE.getDefaultState().shouldSideBeRendered(full,BlockPos.ORIGIN,face)==Blocks.STONE.getDefaultState().shouldSideBeRendered(nonfull,BlockPos.ORIGIN,face),"native side culling opacity path unaffected");
  }
  check(fence.canConnectFenceTo(full,BlockPos.ORIGIN)&&!fence.canConnectFenceTo(nonfull,BlockPos.ORIGIN),"NTM fence consumes actual full-cube, unlike native vanilla fence");
  com.hbm.blocks.ModBlocks.fence_gate=banks[0];check(fence.canConnectFenceTo(nonfull,BlockPos.ORIGIN),"custom gate identity exception remains intact");com.hbm.blocks.ModBlocks.fence_gate=null;
  check(fence.canConnectFenceTo(new Access(fence.getDefaultState()),BlockPos.ORIGIN),"same-owner exception remains intact");check(fence.canConnectFenceTo(new Access(Blocks.OAK_FENCE_GATE.getDefaultState()),BlockPos.ORIGIN),"oak-gate exception remains intact");
  Method nativeSilk=Block.class.getDeclaredMethod("canSilkHarvest");nativeSilk.setAccessible(true);check(nativeSilk.invoke(banks[0]).equals(false),"genuine native default-state silk callback retains false");
  original(args[0],args[1]);
  System.out.println("PASS "+checks+" assertions: 85 exact callbacks/104-owner source closure, state and inherited dispatch, five actual constructor slices, both custom state factories, genuine NTM fence consumer, native face-shape/collision separation, and original live callback/cache checks.");
 }
}
