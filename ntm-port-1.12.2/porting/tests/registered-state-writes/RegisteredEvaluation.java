import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.block.state.IBlockState;import net.minecraft.init.*;import net.minecraft.util.math.BlockPos;import net.minecraft.world.World;
public class RegisteredEvaluation extends WorldWriteTest {
 static StringBuilder seen=new StringBuilder();static World selectedWorld;static Block selectedBlock;static int failure;static int x=Integer.MAX_VALUE;static int writes;static boolean shortMode;
 static World receiver(){seen.append('r');return selectedWorld;}static int arg(int i){seen.append(i);return i==1?x:i==2?64:0;}
 static Block block(){seen.append('b');return selectedBlock;}static int metadata(){seen.append('m');if(failure==1)throw new IllegalStateException();return 6;}static int flags(){seen.append('f');if(failure==2)throw new IllegalStateException();return 2;}
 static class Converted extends com.hbm.blocks.BlockEnumMulti{Converted(){super(Material.ROCK, RegisteredWriteTest.Kind.class,true,true);}public IBlockState getStateFromMeta(int m){seen.append('c');return super.getStateFromMeta(m);}}
 static class Recorder extends NativeWorld{Recorder(){super(true);}public boolean setBlockState(BlockPos p,IBlockState s,int f){seen.append('w');writes++;check(p.equals(new BlockPos(1,64,0))&&s.getBlock()==selectedBlock&&s.getBlock().getMetaFromState(s)==(shortMode?0:6)&&f==(shortMode?3:2),"captured arguments to native virtual write");return false;}}
 static void trace(String expected){check(seen.toString().equals(expected),"owned capture order: "+seen);}
 public static void main(String[]a){Bootstrap.register();selectedWorld=new Recorder();
  selectedBlock=null;seen.setLength(0);CapturedRegisteredWrite.invoke();trace("r123bmf");
  selectedBlock=new Converted();seen.setLength(0);CapturedRegisteredWrite.invoke();trace("r123bmf");
  selectedWorld=null;seen.setLength(0);try{CapturedRegisteredWrite.invoke();throw new AssertionError();}catch(NullPointerException e){trace("r123bmf");}
  for(int fail:new int[]{1,2}){failure=fail;seen.setLength(0);try{CapturedRegisteredWrite.invoke();throw new AssertionError();}catch(IllegalStateException e){trace(fail==1?"r123bm":"r123bmf");}}failure=0;
  selectedWorld=new Recorder();x=1;seen.setLength(0);CapturedRegisteredWrite.invoke();trace("r123bmfcw");check(writes==1,"one native dispatch despite ignored false return");
  selectedBlock=null;seen.setLength(0);try{CapturedRegisteredWrite.invoke();throw new AssertionError();}catch(NullPointerException e){trace("r123bmf");}check(writes==1,"null valid owner never reaches native write");
  shortMode=true;selectedBlock=new Converted();selectedWorld=new Recorder();seen.setLength(0);CapturedRegisteredShort.invoke();trace("r123bcw");check(writes==2,"implicit zero/three short overload invokes once");
  x=Integer.MAX_VALUE;selectedBlock=null;seen.setLength(0);CapturedRegisteredShort.invoke();trace("r123b");
  selectedWorld=null;seen.setLength(0);try{CapturedRegisteredShort.invoke();throw new AssertionError();}catch(NullPointerException e){trace("r123b");}
  System.out.println("PASS: "+checks+" generated production-template order/bounds/null/conversion/subclass-dispatch assertions");
 }
}
