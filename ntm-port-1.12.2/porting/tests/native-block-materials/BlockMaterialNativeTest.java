import java.net.*;import java.nio.file.*;import java.lang.reflect.*;
import net.minecraft.block.Block;import net.minecraft.block.material.*;import net.minecraft.init.*;
public class BlockMaterialNativeTest {
 static int count;static Block next(Block b){count++;return b;}
 static class TestBlock extends Block{TestBlock(Material m){super(m);setResistance(5.0F);}}
 public static void main(String[]a)throws Exception{
  Bootstrap.register();URLClassLoader old=new URLClassLoader(new URL[]{Paths.get(a[0]).toUri().toURL()},BlockMaterialNativeTest.class.getClassLoader());
  Class<?> om=old.loadClass("awt"),ob=old.loadClass("aji");ob.getMethod("p").invoke(null);
  Constructor<?> ctor=ob.getDeclaredConstructor(om);ctor.setAccessible(true);
  Method resistance=ob.getDeclaredMethod("b",float.class),get=ob.getMethod("o"),nuke=new URLClassLoader(new URL[]{Paths.get(a[2]).toUri().toURL()},old).loadClass("ExtractedOldNukeMaterial").getMethod("getNukeResistance",ob);resistance.setAccessible(true);int checks=0;
  for(String row:Files.readAllLines(Paths.get(a[1]))){String[]r=row.split("\t");Object material=om.getField(r[3]).get(null),block=ctor.newInstance(material);resistance.invoke(block,5.0F);Material m=(Material)Material.class.getField(r[2]).get(null);Block b=new TestBlock(m);
   if(get.invoke(block)!=material||b.getDefaultState().getMaterial()!=m)throw new AssertionError(r[2]);checks+=2;
   count=0;if(next(b).getDefaultState().getMaterial()!=m||count!=1)throw new AssertionError("evaluation");checks++;
   float x=(Float)nuke.invoke(null,block),y=ExtractedNukeMaterial.getNukeResistance(b);if(Float.floatToRawIntBits(x)!=Float.floatToRawIntBits(y))throw new AssertionError(r[2]+" nuke "+x+" != "+y);checks++;
  }
  for(String[]r:new String[][]{{"b","STONE"},{"A","SANDSTONE"},{"Z","OBSIDIAN"}}){ // source mapping supplies sandstone field below
   Object block=old.loadClass("ajn").getField(r[0]).get(null);Block b=(Block)Blocks.class.getField(r[1]).get(null);float x=(Float)nuke.invoke(null,block),y=ExtractedNukeMaterial.getNukeResistance(b);if(x!=y)throw new AssertionError(r[1]);checks++;
  }
  for(Block b:Block.REGISTRY){if(b.getDefaultState()==null||b.getDefaultState().getBlock()!=b||b.getDefaultState().getMaterial()!=b.getMaterial(b.getDefaultState()))throw new AssertionError("registry owner "+b);checks++;for(net.minecraft.block.state.IBlockState state:b.getBlockState().getValidStates()){if(state.getBlock()!=b||state.getMaterial()!=b.getDefaultState().getMaterial())throw new AssertionError("state owner "+b);checks++;}}
  count=0;try{next(null).getDefaultState().getMaterial();throw new AssertionError("null");}catch(NullPointerException expected){if(count!=1)throw new AssertionError();checks++;}
  System.out.println("PASS actual original/native Block material reads and complete extracted NTM nuke-resistance method: "+checks);
 }
}
