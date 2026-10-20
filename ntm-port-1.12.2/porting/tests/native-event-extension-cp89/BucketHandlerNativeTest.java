import java.lang.reflect.Field;
import java.util.*;
import sun.misc.Unsafe;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import com.hbm.handler.BucketHandler;
/** Full unchanged production BucketHandler; native Forge events and bus, explicit world storage fixture. */
public class BucketHandlerNativeTest {
 static int checks;static void ok(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static final class TestWorld extends World {
  IBlockState state;List<String> calls;BlockPos expected;
  private TestWorld(){super(null,(WorldInfo)null,null,new Profiler(),false);}
  protected IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return true;}
  public IBlockState getBlockState(BlockPos pos){ok(pos.equals(expected),"exact hit position read");calls.add("read");return state;}
  public boolean setBlockToAir(BlockPos pos){ok(pos.equals(expected),"exact hit position removed");calls.add("remove");return true;}
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();Field field=Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);Unsafe u=(Unsafe)field.get(null);TestWorld world=(TestWorld)u.allocateInstance(TestWorld.class);world.calls=new ArrayList<>();world.expected=new BlockPos(-5,64,37);RayTraceResult target=new RayTraceResult(new Vec3d(-4.5,64.5,37.5),EnumFacing.UP,world.expected);ItemStack empty=new ItemStack(Items.BUCKET);
  net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="bucket event proof";md.version="probe";net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(md);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
  EventBus bus=new EventBus();BucketHandler handler=BucketHandler.INSTANCE;handler.buckets.clear();handler.buckets.put(Blocks.WATER,Items.WATER_BUCKET);bus.register(handler);
  try{
   world.state=Blocks.WATER.getDefaultState();FillBucketEvent yes=new FillBucketEvent(null,empty,world,target);boolean canceled=bus.post(yes);ok(!canceled&&yes.getResult()==Event.Result.ALLOW,"actual bus success result");ok(yes.getFilledBucket().getItem()==Items.WATER_BUCKET&&yes.getFilledBucket().getCount()==1,"real stack result");ok(yes.getEmptyBucket()==empty&&yes.getTarget()==target,"input and target untouched");ok(world.calls.equals(Arrays.asList("read","read","read","remove")),"reads occur before single block removal");
   world.calls.clear();world.state=Blocks.STONE.getDefaultState();FillBucketEvent unknown=new FillBucketEvent(null,empty,world,target);ItemStack sentinel=new ItemStack(Items.APPLE);unknown.setFilledBucket(sentinel);unknown.setResult(Event.Result.DENY);bus.post(unknown);ok(unknown.getFilledBucket()==sentinel&&unknown.getResult()==Event.Result.DENY,"unknown block retains existing result/payload");ok(world.calls.equals(Arrays.asList("read")),"unknown block no removal");
   world.calls.clear();world.state=Blocks.WATER.getStateFromMeta(3);FillBucketEvent flowing=new FillBucketEvent(null,empty,world,target);bus.post(flowing);ok(flowing.getFilledBucket()==null&&flowing.getResult()==Event.Result.DEFAULT,"non-source water not filled");ok(world.calls.equals(Arrays.asList("read","read","read")),"nonzero metadata no removal");
   world.calls.clear();world.state=Blocks.WATER.getDefaultState();FillBucketEvent stopped=new FillBucketEvent(null,empty,world,target);stopped.setCanceled(true);ok(bus.post(stopped)&&world.calls.isEmpty()&&stopped.getFilledBucket()==null,"native already-canceled event skips production subscriber");
   bus.unregister(handler);world.calls.clear();FillBucketEvent removed=new FillBucketEvent(null,empty,world,target);bus.post(removed);ok(world.calls.isEmpty()&&removed.getResult()==Event.Result.DEFAULT,"subscriber unregister control");
  }finally{bus.unregister(handler);handler.buckets.clear();}
  System.out.println("PASS "+checks+" actual native event-bus and full production BucketHandler checks; explicit in-memory World fixture, no whole-game claim.");System.exit(0);
 }
}
