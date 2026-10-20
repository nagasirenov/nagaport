import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Bootstrap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.math.BlockPos;
import com.hbm.blocks.*;
import com.hbm.blocks.generic.*;
import com.hbm.blocks.generic.BlockHazard.ExtDisplayEffect;

public final class HazardSoundNativeTest {
 static int checks;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static final List<String> order=new ArrayList<>();
 static String name(BlockHazard b)throws Exception{check((boolean)field(BlockHazard.class,"beaconable").get(b),"beacon mutation before name argument");order.add("name");return "order";}
 static CreativeTabs tab(Block b){check(b.getTranslationKey().equals("tile.order"),"name before tab");order.add("tab");return CreativeTabs.BUILDING_BLOCKS;}
 static SoundType sound(Block b){check(b.getCreativeTab()==CreativeTabs.BUILDING_BLOCKS&&b.getSoundType()==SoundType.STONE,"preceding tab complete; sound not moved earlier");order.add("sound");return ModSoundTypes.legacyMetal;}
 static float hardness(Block b){check(b.getSoundType()==ModSoundTypes.legacyMetal,"sound before next argument");order.add("hardness");return 5F;}
 static void original(File jar)throws Exception{
  try(URLClassLoader loader=new URLClassLoader(new URL[]{jar.toURI().toURL()},HazardSoundNativeTest.class.getClassLoader())){
   Class<?> b=Class.forName("aji",true,loader),m=Class.forName("awt",true,loader),s=Class.forName("ajm",true,loader);
   Constructor<?> ctor=b.getDeclaredConstructor(m);ctor.setAccessible(true);Object owner=ctor.newInstance(m.getField("e").get(null));
   Method setter=b.getDeclaredMethod("a",s);setter.setAccessible(true);
   for(String f:new String[]{"j","m","g"}){Object v=b.getField(f).get(null);check(setter.invoke(owner,v)==owner,"original setter exact receiver");check(field(b,"H").get(owner)==v,"original sound field exact reference");}
   check(setter.invoke(owner,new Object[]{null})==owner&&field(b,"H").get(owner)==null,"original null stored and returned");
   check(b.getMethod("c",String.class).invoke(owner,"identity")==owner,"original name identity");
   check(b.getMethod("a",Class.forName("abt",true,loader)).invoke(owner,new Object[]{null})==owner,"original tab/null identity");
  }
 }
 public static void main(String[] args)throws Exception{
  Bootstrap.register();net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  net.minecraftforge.fml.common.ModMetadata metadata=new net.minecraftforge.fml.common.ModMetadata();metadata.modId="hbm";metadata.name="Hazard sound probe";metadata.version="1";net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(metadata);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
  Block[] blocks=HazardConstruction.create();check(blocks.length==26,"26 selected construction slices");
  for(int i=0;i<blocks.length;i++){
   Block b=blocks[i];SoundType expected=HazardConstruction.expected[i];
   check(b.getTranslationKey().equals("tile."+HazardConstruction.names[i]),"exact existing name");
   check(b.getSoundType()==expected,"exact original sound argument identity");
   check(b.getSoundType(b.getDefaultState(),null,BlockPos.ORIGIN,null)==expected,"native world getter identity");
   check(b.getSoundType().getBreakSound()==expected.getBreakSound(),"break identity");
   check(b.getSoundType().getPlaceSound()==expected.getPlaceSound(),"place identity");
   check(b.getSoundType().getStepSound()==expected.getStepSound(),"step identity");
   check(b.getSoundType().getHitSound()==expected.getHitSound(),"hit identity");
   check(b.getSoundType().getFallSound()==expected.getFallSound(),"fall identity");
   check(b.getSoundType().getVolume()==expected.getVolume()&&b.getSoundType().getPitch()==expected.getPitch(),"volume/pitch unchanged");
   Method setter=b.getClass().getMethod("withSoundType",SoundType.class);
   for(SoundType v:new SoundType[]{expected,SoundType.GLASS,SoundType.SAND,SoundType.GROUND,ModSoundTypes.legacyMetal,null}){
    check(setter.invoke(b,v)==b,"builder same receiver");check(b.getSoundType()==v,"native exact reference or null");
    check(b.getSoundType(b.getDefaultState(),null,BlockPos.ORIGIN,null)==v,"native contextual reference or null");
   }
   setter.invoke(b,expected);
   check(b.getDefaultState().getMaterial()==(b instanceof BlockHazardFalling?Material.SAND:Material.IRON),"unchanged native constructor material");
  }
  BlockHazard b=new BlockHotHazard();order.clear();Block result=((BlockHazard)b.makeBeaconable().setTranslationKey(name(b)).setCreativeTab(tab(b))).withSoundType(sound(b)).setHardness(hardness(b));
  check(result==b&&order.equals(Arrays.asList("name","tab","sound","hardness")),"complete preceding native virtual return closure and argument order");
  check(b.setDisplayEffect(ExtDisplayEffect.SCHRAB)==b&&field(BlockHazard.class,"extEffect").get(b)==ExtDisplayEffect.SCHRAB,"unchanged display field and return");
  check(b.makeBeaconable()==b&&(boolean)field(BlockHazard.class,"beaconable").get(b),"unchanged hazard beacon field and return");
  BlockHazardFalling falling=new BlockHazardFalling();check(falling.makeBeaconable()==falling&&(boolean)field(BlockHazardFalling.class,"beaconable").get(falling),"unchanged falling beacon field and return");
  check(falling.withSoundType(SoundType.SAND)==falling,"native BlockFalling owner setter return");
  // Exact null-receiver evaluation ordering: cast itself does not eagerly dereference.
  order.clear();try{((BlockHazard)(Block)null).withSoundType(nullSound());throw new AssertionError("missing NPE");}catch(NullPointerException expected){check(order.equals(Arrays.asList("arg")),"null receiver evaluates sound argument first");}
  check(ModSoundTypes.legacyMetal.getBreakSound()==SoundType.STONE.getBreakSound()&&ModSoundTypes.legacyMetal.getPlaceSound()==SoundType.STONE.getPlaceSound(),"existing legacy metal break/place mapping retained");
  check(ModSoundTypes.legacyMetal.getHitSound()==SoundType.STONE.getStepSound()&&ModSoundTypes.legacyMetal.getFallSound()==SoundType.STONE.getStepSound(),"existing legacy metal hit/fall mapping retained");
  check(ModSoundTypes.legacyMetal.getVolume()==1F&&ModSoundTypes.legacyMetal.getPitch()==1.5F,"original metal volume/pitch");
  original(new File(args[0]));
  System.out.println("PASS "+checks+" finite native/original assertions; 26 exact construction prefixes, two actual owner builders, native fields and all five sound getters, null/return/evaluation behavior.");
 }
 static SoundType nullSound(){order.add("arg");return null;}
}
