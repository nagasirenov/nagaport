import java.lang.reflect.*;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.profiler.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.*;
import net.minecraft.world.storage.*;
import net.minecraft.client.multiplayer.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.fml.common.eventhandler.*;
/** Actual native WorldServer/WorldClient spawn methods, providers, chunks and callbacks. No spawn override. */
public class SpawnNativeTest {
 static sun.misc.Unsafe U;static int checks;static {try{Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(sun.misc.Unsafe)f.get(null);}catch(Exception e){throw new RuntimeException(e);}}
 static Field f(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static void set(Class<?> c,Object o,String n,Object v)throws Exception{f(c,n).set(o,v);}
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static List<String> trace=new ArrayList<>();
 static World make(boolean client,boolean loaded)throws Exception{
  World w=(World)U.allocateInstance(client?WorldClient.class:WorldServer.class);
  set(World.class,w,"isRemote",client);set(World.class,w,"provider",new WorldProviderSurface());set(World.class,w,"worldInfo",new WorldInfo(new NBTTagCompound()));set(World.class,w,"profiler",new Profiler());
  w.provider.setWorld(w);set(World.class,w,"worldBorder",new net.minecraft.world.border.WorldBorder());set(World.class,w,"loadedEntityList",new ArrayList<Entity>());set(World.class,w,"unloadedEntityList",new ArrayList<Entity>());set(World.class,w,"playerEntities",new ArrayList<EntityPlayer>());
  IWorldEventListener listener=(IWorldEventListener)Proxy.newProxyInstance(SpawnNativeTest.class.getClassLoader(),new Class[]{IWorldEventListener.class},(o,m,a)->{if(m.getName().equals("onEntityAdded"))trace.add("callback");return null;});
  set(World.class,w,"eventListeners",new ArrayList<>(Arrays.asList(listener)));
  if(client){ChunkProviderClient p=new ChunkProviderClient(w);set(World.class,w,"chunkProvider",p);set(WorldClient.class,w,"clientChunkProvider",p);set(WorldClient.class,w,"entityList",new HashSet<Entity>());set(WorldClient.class,w,"entitySpawnQueue",new HashSet<Entity>());if(loaded)p.loadChunk(-2,1);}
  else{ChunkProviderServer p=new ChunkProviderServer((WorldServer)w,null,null);set(World.class,w,"chunkProvider",p);set(WorldServer.class,w,"entitiesByUuid",new HashMap<UUID,Entity>());set(World.class,w,"entitiesById",new IntHashMap<Entity>());if(loaded)p.loadedChunks.put(ChunkPos.asLong(-2,1),new Chunk(w,-2,1));}
  return w;
 }
 public static class Events {
  boolean cancel,killConstruction;int joins;Entity last;
  @SubscribeEvent public void join(EntityJoinWorldEvent e){joins++;last=e.getEntity();trace.add("event");if(cancel)e.setCanceled(true);}
  @SubscribeEvent public void construct(EntityEvent.EntityConstructing e){if(killConstruction)e.getEntity().setDead();}
 }
 static class Player extends EntityPlayer {Player(World w){super(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"spawnprobe"));}public boolean isSpectator(){return false;}public boolean isCreative(){return false;}}
 static World selected;static World receiver(){trace.add("receiver");return selected;}
 static World argWorld(){trace.add("world");return selected;}
 static double coord(int n){trace.add("coord"+n);return n==1?-17.25:n==2?65:16.75;}
 static ItemStack stack(){trace.add("stack");return new ItemStack(Items.APPLE);}
 static EntityItem item(World w){return new EntityItem(w,-17.25,65,16.75,new ItemStack(Items.APPLE));}
 public static void main(String[] a)throws Exception{
  Bootstrap.register();
  net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  net.minecraftforge.fml.common.ModMetadata meta=new net.minecraftforge.fml.common.ModMetadata();meta.modId="spawnprobe";meta.name="Spawn probe";meta.version="1";
  net.minecraftforge.fml.common.Loader.instance().setupTestHarness(new net.minecraftforge.fml.common.DummyModContainer(meta));
  Events events=new Events();MinecraftForge.EVENT_BUS.register(events);
  try{
   for(boolean client:new boolean[]{false,true})for(boolean loaded:new boolean[]{false,true})for(boolean cancel:new boolean[]{false,true})for(boolean force:new boolean[]{false,true}){
    // Server forced unloaded path would invoke full chunk generation; exercise that separately on client.
    if(!client&&!loaded&&force)continue;
    World w=make(client,loaded);EntityItem e=item(w);check(!e.isDead&&e.getUniqueID()!=null,"standard constructor alive with identity");e.forceSpawn=force;events.cancel=cancel;events.joins=0;trace.clear();
    boolean expected=(loaded||force)&&(!cancel||force);boolean result=w.spawnEntity(e);
    check(result==expected,"native return "+client+loaded+cancel+force);
    check(events.joins==((loaded||force)?1:0),"unloaded pre-event rejection / exactly one join");
    check(w.loadedEntityList.contains(e)==expected,"loaded entity identity");
    check(trace.equals(expected?Arrays.asList("event","callback"):loaded||force?Arrays.asList("event"):Collections.emptyList()),"event/callback order");
    check(e.isAddedToWorld()==expected,"real Entity.onAddedToWorld callback");
    if(expected&&loaded)check(w.getChunk(-2,1).getEntityLists()[4].contains(e),"real native Chunk owns same entity");
    if(!client&&expected)check(((WorldServer)w).getEntityFromUuid(e.getUniqueID())==e,"real server UUID callback");
    if(client){check(((Set<?>)f(WorldClient.class,"entityList").get(w)).contains(e),"client tracks attempted entity");check(((Set<?>)f(WorldClient.class,"entitySpawnQueue").get(w)).contains(e)==!expected,"client failed spawn queue");}
   }
   events.cancel=false;WorldServer w=(WorldServer)make(false,true);EntityItem e=item(w);events.joins=0;check(w.spawnEntity(e),"fresh server success");EntityItem dup=item(w);dup.setUniqueId(e.getUniqueID());int before=events.joins;check(!w.spawnEntity(dup)&&events.joins==before,"duplicate UUID rejected before join, deliberately excluded");
   EntityItem dead=item(w);dead.setDead();check(!w.spawnEntity(dead)&&events.joins==before,"dead rejected before join, deliberately excluded");
   events.killConstruction=true;EntityItem hooked=item(w);events.killConstruction=false;check(hooked.isDead,"construction hook counterexample exists");check(!w.spawnEntity(hooked)&&events.joins==before,"external constructor mutation changes server behavior, compatibility gate");
   WorldServer dimensions=(WorldServer)make(false,true);EntityItem distant=item(dimensions);distant.dimension=97;check(dimensions.spawnEntity(distant)&&distant.dimension==97,"spawn does not transfer or rewrite dimension");
   events.cancel=true;w=(WorldServer)make(false,true);w.restoringBlockSnapshots=true;events.joins=0;check(!w.spawnEntity(item(w))&&events.joins==0,"snapshot item suppression precedes event");
   events.cancel=true;WorldServer pw=(WorldServer)make(false,true);
   Player player=new Player(pw);player.setPosition(-17.25,65,16.75);events.joins=0;
   check(pw.spawnEntity(player)&&events.joins==1&&pw.playerEntities.contains(player),"player flag bypasses join cancellation with actual player-list path");
   for(boolean cancel:new boolean[]{false,true}){World owned=make(false,true);events.cancel=cancel;events.joins=0;ItemStack payload=new ItemStack(Items.APPLE,3);ProductionSpawn.spawn(owned,-17.75,64.5,16.25,payload);check(events.joins==1&&events.last instanceof EntityItem,"actual selected production invocation posts once");EntityItem spawned=(EntityItem)events.last;check(spawned.world==owned&&spawned.getItem()==payload&&spawned.posX==-17.25&&spawned.posY==65&&spawned.posZ==16.75,"actual production expression keeps world/coordinates/stack identity");check(owned.loadedEntityList.contains(spawned)==!cancel,"actual production invocation retains cancellation behavior");}
   for(boolean cancel:new boolean[]{false,true}){selected=make(false,true);events.cancel=cancel;trace.clear();boolean result=receiver().spawnEntity(new EntityItem(argWorld(),coord(1),coord(2),coord(3),stack()));
    List<String> expected=new ArrayList<>(Arrays.asList("receiver","world","coord1","coord2","coord3","stack","event"));if(!cancel)expected.add("callback");check(trace.equals(expected)&&result==!cancel,"same left-to-right receiver/arguments and native returned boolean");}
   selected=null;trace.clear();try{receiver().spawnEntity(new EntityItem(argWorld(),coord(1),coord(2),coord(3),stack()));throw new AssertionError("null receiver");}catch(NullPointerException expected){check(trace.equals(Arrays.asList("receiver","world","coord1","coord2","coord3","stack")),"null receiver exception after argument construction");}
   WorldClient c=(WorldClient)make(true,true);c.restoringBlockSnapshots=true;events.cancel=false;check(c.spawnEntity(item(c)),"client ignores snapshot suppression");
  }finally{MinecraftForge.EVENT_BUS.unregister(events);}
  System.out.println("PASS "+checks+" native spawn checks: genuine server/client/chunk/callback/event paths; external constructor mutation explicitly excluded");
 }
}
