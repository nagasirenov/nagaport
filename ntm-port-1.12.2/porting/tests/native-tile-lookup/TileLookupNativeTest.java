import java.lang.reflect.*;
import java.util.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.storage.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.client.multiplayer.*;
import net.minecraft.nbt.NBTTagCompound;

/** Direct native World and provider implementations; production methods tested separately.
 * Unsafe only bypasses unrelated server/client bootstrap constructors; no Minecraft APIs replaced.
 * ObservedWorld controls dispatch only for expression-order checks, not native lookup tests.
 */
public final class TileLookupNativeTest {
    static int checks;
    static final sun.misc.Unsafe UNSAFE;
    static { try { Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true); UNSAFE=(sun.misc.Unsafe)f.get(null); } catch(Exception e) { throw new RuntimeException(e); } }
    static void check(boolean b,String s) { checks++; if(!b) throw new AssertionError(s); }
    static void field(Object o,Class<?> type,String name,Object value) throws Exception { Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(o,value); }
    static class Tile extends TileEntity {}
    static <T extends World> T nativeWorld(Class<T> cls) throws Exception {
        T w=(T)UNSAFE.allocateInstance(cls);
        field(w,World.class,"worldInfo",new WorldInfo(new NBTTagCompound()));
        field(w,World.class,"provider",new WorldProviderSurface());w.provider.setWorld(w);
        field(w,World.class,"profiler",new Profiler());
        for(Field f:World.class.getDeclaredFields()) if(List.class.isAssignableFrom(f.getType())&&!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,new ArrayList<>());}
        return w;
    }

    static TileEntity lookup(World w,int x,int y,int z) {return w.getTileEntity(new BlockPos(x,y,z));}
    static Chunk install(WorldServer w,ChunkProviderServer cp,int x,int z) {Chunk c=new Chunk(w,x>>4,z>>4);cp.loadedChunks.put(ChunkPos.asLong(x>>4,z>>4),c);return c;}
    static Tile tile(World w,BlockPos p){Tile t=new Tile();t.setPos(p);t.setWorld(w);return t;}
    static void nativeLookup() throws Exception {
        WorldServer w=nativeWorld(WorldServer.class);ChunkProviderServer cp=new ChunkProviderServer(w,null,null);field(w,World.class,"chunkProvider",cp);
        for(int x:new int[]{-30000001,-30000000,-17,-16,-1,0,15,16,29999999,30000000,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
            int z=~x;Chunk c=install(w,cp,x,z);
            for(int y:new int[]{0,64,255}) {BlockPos p=new BlockPos(x,y,z);Tile t=tile(w,p);c.getTileEntityMap().put(p,t);c.unloadQueued=true;
                check(lookup(w,x,y,z)==t,"native exact identity, boundaries and horizontal world border");check(!c.unloadQueued,"direct lookup retains provideChunk unload effect");}
            check(lookup(w,x,-1,z)==null&&lookup(w,x,256,z)==null,"height rejection");
        }
        BlockPos p=new BlockPos(-1,64,-17);Chunk c=install(w,cp,p.getX(),p.getZ());Tile normal=tile(w,p),pending=tile(w,p),invalid=tile(w,p);invalid.invalidate();c.getTileEntityMap().put(p,normal);
        Field f=World.class.getDeclaredField("addedTileEntityList");f.setAccessible(true);List list=(List)f.get(w);list.add(invalid);list.add(pending);
        field(w,World.class,"processingLoadedTiles",true);check(lookup(w,-1,64,-17)==pending,"valid pending first while processing");
        field(w,World.class,"processingLoadedTiles",false);check(lookup(w,-1,64,-17)==normal,"normal chunk tile wins outside processing");c.getTileEntityMap().clear();check(lookup(w,-1,64,-17)==pending,"pending fallback after chunk miss");list.clear();check(lookup(w,-1,64,-17)==null,"absent tile in loaded chunk");
        // Install real native chest state without unrelated lighting/notification callbacks.
        net.minecraft.world.chunk.storage.ExtendedBlockStorage storage=new net.minecraft.world.chunk.storage.ExtendedBlockStorage(64,true);
        storage.set(15,0,15,net.minecraft.init.Blocks.CHEST.getDefaultState());c.getBlockStorageArray()[4]=storage;
        check(lookup(w,-1,64,-17) instanceof net.minecraft.tileentity.TileEntityChest,"World IMMEDIATE creates native tile");
        check(c.getTileEntityMap().containsKey(p),"immediate creation installs tile in native chunk");
        WorldClient client=nativeWorld(WorldClient.class);ChunkProviderClient clientCP=new ChunkProviderClient(client);field(client,World.class,"chunkProvider",clientCP);field(client,WorldClient.class,"clientChunkProvider",clientCP);
        check(lookup(client,-1,64,-17)==null&&clientCP.getLoadedChunk(-1,-2)==null,"direct native client missing chunk fallback");
        Chunk cc=new Chunk(client,-1,-2);Field loaded=ChunkProviderClient.class.getDeclaredField("loadedChunks");loaded.setAccessible(true);((it.unimi.dsi.fastutil.longs.Long2ObjectMap<Chunk>)loaded.get(clientCP)).put(ChunkPos.asLong(-1,-2),cc);
        Tile ct=tile(client,p);cc.getTileEntityMap().put(p,ct);check(lookup(client,-1,64,-17)==ct,"native client loaded identity");
        // This is characterization of an excluded receiver, never a migration claim.
        ChunkCache cache=(ChunkCache)UNSAFE.allocateInstance(ChunkCache.class);field(cache,ChunkCache.class,"chunkX",-1);field(cache,ChunkCache.class,"chunkZ",-2);field(cache,ChunkCache.class,"chunkArray",new Chunk[][]{{c}});
        c.getTileEntityMap().clear();check(cache.getTileEntity(p)==null,"excluded ChunkCache CHECK does not create missing chest tile");check(cache.getTileEntity(new BlockPos(32,64,32))==null,"native cache out-of-bounds null");check(lookup(w,-1,64,-17)!=null,"World still creates after cache CHECK");
    }
    static StringBuilder trace=new StringBuilder();static World selected;static int coordinate(int i){trace.append(i);return i==1?Integer.MAX_VALUE:i==2?64:-17;}static World receiver(){trace.append('r');return selected;}
    static class ObservedWorld extends World {
        BlockPos got;int calls;
        ObservedWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);}
        protected IChunkProvider createChunkProvider(){return null;}protected boolean isChunkLoaded(int x,int z,boolean allow){return false;}
        public TileEntity getTileEntity(BlockPos p){trace.append('c');got=p;calls++;return null;}
    }
    static void order() {
        ObservedWorld w=new ObservedWorld();selected=w;trace.setLength(0);receiver().getTileEntity(new BlockPos(coordinate(1)+1,coordinate(2),coordinate(3)));check(trace.toString().equals("r123c")&&w.calls==1&&w.got.getX()==Integer.MIN_VALUE,"receiver then x y z, once, integer overflow, virtual dispatch");
        selected=null;trace.setLength(0);try{receiver().getTileEntity(new BlockPos(coordinate(1),coordinate(2),coordinate(3)));throw new AssertionError();}catch(NullPointerException expected){check(trace.toString().equals("r123"),"null receiver checked after coordinates");}
        selected=w;trace.setLength(0);try{receiver().getTileEntity(new BlockPos(coordinate(1),(new int[0])[coordinate(2)],coordinate(3)));throw new AssertionError();}catch(ArrayIndexOutOfBoundsException expected){check(trace.toString().equals("r12")&&w.calls==1,"coordinate exception prevents later argument and invocation");}
    }
    public static void main(String[] args)throws Exception {Bootstrap.register();nativeLookup();order();System.out.println("PASS: "+checks+" real native World/client/server/ChunkCache and evaluation checks");}
}
