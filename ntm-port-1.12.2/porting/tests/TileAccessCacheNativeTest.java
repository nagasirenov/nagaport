import api.hbm.tile.ILoadedTile;
import api.hbm.tile.ILoadedTile.TileAccessCache;
import com.hbm.util.Compat;
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

/** Actual cache + extracted production lookup, with native World and provider implementations.
 * Unsafe only bypasses unrelated server/client bootstrap constructors; no Minecraft APIs replaced.
 * Cache-specific world fixture controls clock and tile identity, independently of native lookup tests.
 */
public final class TileAccessCacheNativeTest {
    static int checks;
    static final sun.misc.Unsafe UNSAFE;
    static { try { Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true); UNSAFE=(sun.misc.Unsafe)f.get(null); } catch(Exception e) { throw new RuntimeException(e); } }
    static void check(boolean b,String s) { checks++; if(!b) throw new AssertionError(s); }
    static void field(Object o,Class<?> type,String name,Object value) throws Exception { Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(o,value); }
    static class Tile extends TileEntity implements ILoadedTile { boolean loaded=true; public boolean isLoaded(){ return loaded; } }
    static class CacheWorld extends World {
        TileEntity current; long time; boolean loaded=true; int lookups; int lastX,lastZ; boolean lastAllow;
        CacheWorld(int dim) { super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false); provider.setWorld(this);provider.setDimension(dim); }
        protected IChunkProvider createChunkProvider(){return null;}
        protected boolean isChunkLoaded(int x,int z,boolean allow){lastX=x;lastZ=z;lastAllow=allow;return loaded;}
        public TileEntity getTileEntity(BlockPos pos){lookups++;return current;}
        public long getTotalWorldTime(){return time;}
    }
    static <T extends World> T nativeWorld(Class<T> cls) throws Exception {
        T w=(T)UNSAFE.allocateInstance(cls);
        field(w,World.class,"worldInfo",new WorldInfo(new NBTTagCompound()));
        field(w,World.class,"provider",new WorldProviderSurface());w.provider.setWorld(w);
        field(w,World.class,"profiler",new Profiler());
        for(Field f:World.class.getDeclaredFields()) if(List.class.isAssignableFrom(f.getType())&&!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,new ArrayList<>());}
        return w;
    }
    static void nativeLookup() throws Exception {
        WorldServer server=nativeWorld(WorldServer.class);
        ChunkProviderServer sp=new ChunkProviderServer(server,null,null);
        field(server,World.class,"chunkProvider",sp);
        check(Compat.getTileStandard(server,-1,64,-17)==null,"server missing returns null without loader/generator");
        check(sp.loadedChunks.isEmpty(),"missing lookup does not load server chunk");
        Chunk sc=new Chunk(server,-1,-2);sp.loadedChunks.put(ChunkPos.asLong(-1,-2),sc);
        BlockPos p=new BlockPos(-1,64,-17);Tile tile=new Tile();tile.setPos(p);tile.setWorld(server);sc.getTileEntityMap().put(p,tile);
        sc.unloadQueued=true;
        check(server.isBlockLoaded(p,false)&&sc.unloadQueued,"server loaded guard does not cancel pending unload");
        check(Compat.getTileStandard(server,-1,64,-17)==tile,"native server tile retrieved at negative chunk boundary");
        check(!sc.unloadQueued,"native getTileEntity downstream provideChunk cancels queued unload");
        check(Compat.getTileStandard(server,-1,-1,-17)==null&&Compat.getTileStandard(server,-1,256,-17)==null,"native build-height rejection");
        Tile pending=new Tile();pending.setPos(p);pending.setWorld(server);
        field(server,World.class,"processingLoadedTiles",true);
        Field added=World.class.getDeclaredField("addedTileEntityList");added.setAccessible(true);((List)added.get(server)).add(pending);
        check(Compat.getTileStandard(server,-1,64,-17)==pending,"native pending tile wins during processing");
        field(server,World.class,"processingLoadedTiles",false);
        WorldClient client=nativeWorld(WorldClient.class);ChunkProviderClient cp=new ChunkProviderClient(client);
        field(client,World.class,"chunkProvider",cp);
        // WorldClient overrides getChunkProvider via its clientChunkProvider field.
        field(client,WorldClient.class,"clientChunkProvider",cp);
        check(client.isBlockLoaded(p),"one-argument native client guard admits absent chunk");
        check(!client.isBlockLoaded(p,false),"explicit false rejects native empty fallback");
        check(Compat.getTileStandard(client,-1,64,-17)==null&&cp.getLoadedChunk(-1,-2)==null,"client lookup does not create missing chunk");
        Chunk cc=new Chunk(client,-1,-2);
        Field loaded=ChunkProviderClient.class.getDeclaredField("loadedChunks");loaded.setAccessible(true);
        ((it.unimi.dsi.fastutil.longs.Long2ObjectMap<Chunk>)loaded.get(cp)).put(ChunkPos.asLong(-1,-2),cc);
        Tile ct=new Tile();ct.setWorld(client);ct.setPos(p);cc.getTileEntityMap().put(p,ct);
        check(!cc.isEmpty()&&client.isBlockLoaded(p,false),"ordinary loaded native chunk passes guard");
        check(Compat.getTileStandard(client,-1,64,-17)==ct,"native client tile lookup");
        ((it.unimi.dsi.fastutil.longs.Long2ObjectMap<Chunk>)loaded.get(cp)).remove(ChunkPos.asLong(-1,-2));
        check(Compat.getTileStandard(client,-1,64,-17)==null,"client chunk removed before lookup drops tile");
    }
    static TileEntity get(CacheWorld w){return TileAccessCache.getTileOrCache(w,15,64,-17);}
    static void cache() {
        TileAccessCache.cache.clear();CacheWorld w=new CacheWorld(7);w.time=100;
        check(get(w)==null&&w.lookups==1,"initial null lookup");
        Tile first=new Tile();w.current=first;w.time=119;check(get(w)==null&&w.lookups==1,"negative hit until tick19");
        w.time=120;check(get(w)==first&&w.lookups==2,"negative refresh at tick20");
        Tile second=new Tile();w.current=second;w.time=179;check(get(w)==first&&w.lookups==2,"nonnull hit until tick59 hides replacement");
        w.time=180;check(get(w)==second&&w.lookups==3,"nonnull refresh at tick60");
        second.invalidate();w.current=first;check(get(w)==first&&w.lookups==4,"invalid expires immediately");
        first.loaded=false;w.current=second;second.validate();check(get(w)==second&&w.lookups==5,"unloaded expires immediately");
        w.loaded=false;second.loaded=false;check(get(w)==null&&w.lookups==5,"unloaded chunk refresh does not call tile lookup");
        check(w.lastX==0&&w.lastZ==-2&&!w.lastAllow,"native coordinate shift and explicit false guard");
        w.loaded=true;w.time=200;first.loaded=true;w.current=first;check(get(w)==first,"negative expiry after unload");
        TileAccessCache.getTileOrCache(w,16,64,-16);check(w.lastX==1&&w.lastZ==-1,"positive and negative chunk boundaries");
        w.current=second;check(get(w)==first,"scratch key mutation does not change prior cloned key");
        CacheWorld otherDim=new CacheWorld(8);otherDim.current=second;otherDim.time=200;check(get(otherDim)==second,"dimension key separation");
        CacheWorld sameDim=new CacheWorld(7);sameDim.current=second;sameDim.time=200;check(get(sameDim)==first&&sameDim.lookups==0,"documented same-dimension different-world cache collision retained");
        w.time=0;check(get(w)==first,"backward clock preserves cached entry");
        TileAccessCache c=new TileAccessCache(first,10);check(!c.hasExpired(9)&&c.hasExpired(10),"exact >= expiry boundary");
        TileAccessCache.cache.clear();w.time=Long.MAX_VALUE-5;w.current=first;int before=w.lookups;get(w);get(w);check(w.lookups==before+2,"long overflow causes immediate relookup, retained");
        int old=TileAccessCache.NONNULL_CACHE;try {TileAccessCache.NONNULL_CACHE=0;TileAccessCache.cache.clear();w.time=30;before=w.lookups;get(w);get(w);check(w.lookups==before+2,"public configurable zero TTL retained");}finally{TileAccessCache.NONNULL_CACHE=old;}
        TileAccessCache.cache.clear();
    }
    public static void main(String[] args) throws Exception {Bootstrap.register();nativeLookup();cache();System.out.println("PASS: "+checks+" cache/native client/server lookup checks; constructor bootstrap controlled, full Compat not compiled");}
}
