import java.lang.reflect.*;
import net.minecraft.init.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.storage.*;
import net.minecraft.world.gen.ChunkProviderServer;
public class ProductionMethodsTest {
 public static void main(String[] a)throws Exception{
 Bootstrap.register();WorldServer w=WorldReadNativeTest.world();ChunkProviderServer cp=new ChunkProviderServer(w,null,null);WorldReadNativeTest.field(World.class,"chunkProvider").set(w,cp);
 Chunk c=new Chunk(w,-1,-2);cp.loadedChunks.put(ChunkPos.asLong(-1,-2),c);ExtendedBlockStorage s=new ExtendedBlockStorage(64,true);c.getBlockStorageArray()[4]=s;
 ProductionTorus t=new ProductionTorus();t.setWorld(w);t.setPos(new BlockPos(-16,60,-32));Method block=ProductionTorus.class.getDeclaredMethod("gbr",int.class,int.class,int.class),meta=ProductionTorus.class.getDeclaredMethod("gmr",int.class,int.class,int.class);block.setAccessible(true);meta.setAccessible(true);
 for(int m=0;m<16;m++){
 s.set(15,0,15,Blocks.WOOL.getStateFromMeta(m));WorldReadNativeTest.check(block.invoke(t,15,4,15)==Blocks.WOOL,"complete torus relative block body");WorldReadNativeTest.check((Integer)meta.invoke(t,15,4,15)==m,"complete torus relative metadata body");
 WorldReadNativeTest.check(new ProductionSapling().getDamageValue(w,-1,64,-17)==m,"complete sapling metadata body");WorldReadNativeTest.check(new ProductionTallPlant().getDamageValue(w,-1,64,-17)==m%8,"complete tall plant remainder body");WorldReadNativeTest.check(new ProductionCrop().func_149851_a(w,-1,64,-17,false)==(m!=7),"complete crop growth predicate");
 }
 WorldReadNativeTest.check(new ProductionSapling().getDamageValue(w,30000000,64,0)==0,"production bound metadata");t.setPos(new BlockPos(Integer.MAX_VALUE,64,0));WorldReadNativeTest.check(block.invoke(t,1,0,0)==Blocks.AIR,"actual relative overflow before bounds");
 System.out.println("PASS: "+WorldReadNativeTest.checks+" assertions executing five complete actual production methods");
 }
}
