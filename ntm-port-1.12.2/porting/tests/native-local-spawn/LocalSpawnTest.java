import java.util.*;import net.minecraft.world.*;import net.minecraft.entity.*;import net.minecraft.entity.item.*;import net.minecraft.item.*;import net.minecraft.init.*;import net.minecraft.util.math.*;import net.minecraftforge.common.*;
public class LocalSpawnTest {
 static int n;static void check(boolean b,String m){n++;if(!b)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  Bootstrap.register();net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());net.minecraftforge.fml.common.ModMetadata meta=new net.minecraftforge.fml.common.ModMetadata();meta.modId="localspawnprobe";meta.name="Local spawn probe";meta.version="1";net.minecraftforge.fml.common.Loader.instance().setupTestHarness(new net.minecraftforge.fml.common.DummyModContainer(meta));
  SpawnNativeTest.Events events=new SpawnNativeTest.Events();MinecraftForge.EVENT_BUS.register(events);
  try{
   for(boolean client:new boolean[]{false,true})for(boolean loaded:new boolean[]{false,true})for(boolean cancel:new boolean[]{false,true}){
    World w=SpawnNativeTest.make(client,loaded);events.cancel=cancel;events.joins=0;ItemStack stack=new ItemStack(Items.APPLE,4);
    LocalProducer local=new LocalProducer(w);local.setPosition(-17.75,64.8,16.25);local.motionX=.125;local.motionY=.1;local.motionZ=.125;local.spawn(stack);
    check(events.joins==(loaded?1:0),"actual local producer event count");
    if(loaded){EntityItem out=(EntityItem)events.last;check(out.getItem()==stack&&out.world==w,"fresh local identity/payload");check(out.posX==-17.5&&out.posY==65.0&&out.posZ==16.5&&out.motionX==.25&&out.motionY==.1&&out.motionZ==.25&&out.velocityChanged,"actual production motion/position preparation unchanged");check(w.loadedEntityList.contains(out)==!cancel,"actual local cancellation retained");}
    w=SpawnNativeTest.make(client,loaded);events.joins=0;TileProducer tile=new TileProducer();tile.setWorld(w);tile.setPos(new BlockPos(-18,63,16));tile.spawn(stack);check(events.joins==(loaded?1:0),"actual machine output event count");
    if(loaded){EntityItem out=(EntityItem)events.last;check(out.getItem()==stack&&out.posX==-17.5&&out.posY==65&&out.posZ==16.5,"actual machine coordinates and stack identity");check(w.loadedEntityList.contains(out)==!cancel,"actual machine cancellation");}
    w=SpawnNativeTest.make(client,loaded);events.joins=0;SpawnNativeTest.Player living=new SpawnNativeTest.Player(w);living.setPosition(-17.25,65,16.75);XPProducer.spawn(w,living);check(events.joins==(loaded?1:0),"actual XP event count");
    if(loaded){EntityXPOrb xp=(EntityXPOrb)events.last;check(!xp.isDead&&xp.xpValue==1&&xp.world==w&&xp.posX==living.posX&&xp.posY==living.posY&&xp.posZ==living.posZ,"actual XP constructor value/world/position");check(w.loadedEntityList.contains(xp)==!cancel,"actual XP cancellation");}
   }
   events.cancel=false;World w=SpawnNativeTest.make(false,true);ItemStack payload=new ItemStack(Items.APPLE,2);DebrisProducer.spawn(w,-17.25,65,16.75,.1,.2,-.1,0,Arrays.asList(payload),0,new Random(4));EntityItem debris=(EntityItem)events.last;
   check(debris.posX==-17.08&&debris.posY==65.34&&debris.posZ==16.58,"exact original raw-position adjustment");check(debris.getItem()!=payload&&debris.getItem().getCount()==2,"actual original copied stack");check(debris.getEntityBoundingBox().minX==-17.375,"original raw-position/AABB mismatch is retained, not silently fixed");
   for(boolean client:new boolean[]{false,true}){w=SpawnNativeTest.make(client,true);w.restoringBlockSnapshots=true;events.joins=0;EntityXPOrb xp=new EntityXPOrb(w,-17.25,65,16.75,1);check(w.spawnEntity(xp)&&events.joins==1,"XP is not snapshot-suppressed as EntityItem");}
  }finally{MinecraftForge.EVENT_BUS.unregister(events);}
  System.out.println("PASS "+n+" actual extracted local machine/motion/debris/XP producer checks; native registration/events exercised, not full consumer lifecycle");
 }
}
