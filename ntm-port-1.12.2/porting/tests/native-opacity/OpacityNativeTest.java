import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.block.properties.*;
import net.minecraft.init.*;
import net.minecraft.util.math.BlockPos;

public final class OpacityNativeTest {
 static int checks;static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 public static class SideEffect extends Block { static int calls;public SideEffect(){super(Material.ROCK);}public boolean isOpaqueCube(IBlockState s){calls++;return false;} }
 public static class Dynamic extends CallbackBank.StateFixture {public boolean isOpaqueCube(IBlockState s){return s.getValue(CallbackBank.FLAG);} }
 static void verify(Block b,int opacity,boolean fullCube){
  check(!b.isOpaqueCube(null),"null argument remains ignored");check(!b.isOpaqueCube(Blocks.STONE.getDefaultState()),"foreign state remains ignored");
  for(IBlockState s:b.getBlockState().getValidStates()){
   check(!s.isOpaqueCube(),"actual state virtual dispatch preserves false");check(!s.isFullBlock(),"constructor fullBlock cache false");check(s.getLightOpacity()==opacity,"native constructor lightOpacity cache");
   check(s.isFullCube()==fullCube,"no opaque/full-cube conflation");
   check(b.getCollisionBoundingBox(s,null,BlockPos.ORIGIN)==Block.FULL_BLOCK_AABB,"opacity does not silently rewrite collision behavior");
  }
 }
 static void original(String jar,String dir)throws Exception{
  try(URLClassLoader loader=new URLClassLoader(new URL[]{new File(jar).toURI().toURL(),new File(dir).toURI().toURL()},OpacityNativeTest.class.getClassLoader())){
   Class<?> old=Class.forName("oracle.OldOpacityFixture",true,loader),base=Class.forName("aji",true,loader);Object b=old.newInstance();
   check(old.getMethod("c").invoke(b).equals(false),"original exact constant callback");check(!field(base,"q").getBoolean(b),"original fullBlock cache false");check(field(base,"r").getInt(b)==0,"original lightOpacity zero");
   Class<?> side=Class.forName("oracle.OldOpacityFixture$SideEffect",true,loader);side.newInstance();int oldCalls=side.getField("calls").getInt(null);
   SideEffect.calls=0;new SideEffect();check(oldCalls==2&&SideEffect.calls==1,"stateful constructor callback counterexample: old invokes twice/native once");
  }
 }
 public static void main(String[] args)throws Exception{
  Bootstrap.register();net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  Block[] banks=CallbackBank.create(),children=CallbackBank.descendants();check(banks.length==95&&children.length==95,"exact 95 extracted callbacks and inherited dispatch controls");
  for(int i=0;i<banks.length;i++){verify(banks[i],0,true);verify(children[i],0,true);}
  // Genuine state implementations dispatch each call, not a one-time per-state opaque cache.
  Dynamic dynamic=new Dynamic();IBlockState lo=dynamic.getDefaultState().withProperty(CallbackBank.FLAG,false),hi=lo.withProperty(CallbackBank.FLAG,true);
  check(!lo.isOpaqueCube()&&hi.isOpaqueCube(),"state-dependent native counterexample retained");
  check(lo.isFullBlock()==dynamic.getDefaultState().getValue(CallbackBank.FLAG)&&hi.isFullBlock()==lo.isFullBlock(),"fullBlock remains constructor cache, distinct from state opacity");
  Block plasma=new com.hbm.blocks.generic.BlockPlasma(Material.FIRE),vacuum=new com.hbm.blocks.gas.BlockVacuum(),leaves=new com.hbm.blocks.generic.WasteLeaves(Material.LEAVES),turret=new com.hbm.blocks.turret.TurretSentry();
  verify(plasma,0,true);verify(vacuum,0,true);verify(leaves,0,true);verify(turret,0,true);
  Class<?> water=Class.forName("com.hbm.blocks.BlockWaterPlant");Constructor<?> ctor=water.getDeclaredConstructor();ctor.setAccessible(true);verify((Block)ctor.newInstance(),0,true);
  check(plasma.getTickRandomly()&&leaves.getTickRandomly(),"exact native owner constructor random ticks unchanged");
  BlockLeaves rubber=new com.hbm.blocks.BlockRubberLeaves();for(boolean fancy:new boolean[]{false,true,false}){rubber.setGraphicsLevel(fancy);verify(rubber,1,true);}
  check(rubber.getTickRandomly(),"native BlockLeaves initialization preserved");
  verify(new BlockChargeBaseStateSlice(),0,true);verify(new BlockNTMFlowerStateSlice(),0,true);
  original(args[0],args[1]);
  System.out.println("PASS "+checks+" assertions: 95 exact callback bodies, descendants/all 32 native state combinations, constructor/fullBlock/lightOpacity caching, six actual constructor slices, both actual custom state factories, null/foreign states, leaves graphics, and original stateful-call counterexample.");
 }
}
