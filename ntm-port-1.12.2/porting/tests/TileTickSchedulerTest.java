import java.lang.reflect.*;
import java.util.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.*;

/** Native World scheduler; only world storage/bootstrap services are controlled fixtures. */
public final class TileTickSchedulerTest {
    static final List<String> events = new ArrayList<>();
    static void check(boolean value,String label) { if(!value) throw new AssertionError(label); }
    static class FixtureWorld extends World {
        Map<Long,Chunk> chunks;
        private FixtureWorld() { super(null,null,null,null,false); }
        protected IChunkProvider createChunkProvider() { throw new UnsupportedOperationException(); }
        protected boolean isChunkLoaded(int x,int z,boolean empty) { return chunks.containsKey(key(x,z)); }
        public Chunk getChunk(int x,int z) { return chunks.computeIfAbsent(key(x,z), k -> new Chunk(this,x,z)); }
        static long key(int x,int z) { return ((long)x<<32) ^ (z&0xffffffffL); }
        static FixtureWorld make() throws Exception {
            Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
            FixtureWorld w=(FixtureWorld)((sun.misc.Unsafe)f.get(null)).allocateInstance(FixtureWorld.class);
            for(Field field:World.class.getDeclaredFields()) if(List.class.isAssignableFrom(field.getType()) && !Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true); field.set(w,new ArrayList<>());
            }
            set(w,"profiler",new Profiler()); set(w,"worldBorder",new WorldBorder());
            set(w,"worldInfo",new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
            w.chunks=new HashMap<>(); w.getChunk(0,0); return w;
        }
        static void set(World w,String name,Object value) throws Exception { Field f=World.class.getDeclaredField(name);f.setAccessible(true);f.set(w,value); }
    }
    static class StaticTile extends TileEntity {
        final String name; int loads;
        StaticTile(String name,int x) { this.name=name;setPos(new BlockPos(x,64,0)); }
        public void onLoad() { loads++;events.add(name+":load"); }
    }
    static class TickRoot extends StaticTile implements ITickable {
        int ticks;
        TickRoot(String name,int x) { super(name,x); }
        public void update() { ticks++;events.add(name+":base"); }
    }
    static class InheritedTile extends TickRoot { InheritedTile(String name,int x) { super(name,x); } }
    static class OverrideTile extends TickRoot {
        boolean stop;
        OverrideTile(String name,int x) { super(name,x); }
        public void update() { events.add(name+":before"); if(stop)return;super.update();events.add(name+":after"); }
    }
    static class SpawnerTile extends TickRoot {
        TickRoot pending; boolean added;
        SpawnerTile(TickRoot pending) { super("spawner",4);this.pending=pending; }
        public void update() { super.update();if(!added) {
            added=true;check(world.addTileEntity(pending),"deferred add accepted");
            check(!world.loadedTileEntityList.contains(pending) && !world.tickableTileEntities.contains(pending),"deferred tile not admitted mid-loop");
            check(pending.loads==0 && pending.ticks==0,"deferred tile neither loaded nor ticked mid-loop");
        } }
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register(); FixtureWorld w=FixtureWorld.make();
        StaticTile fixed=new StaticTile("static",0); InheritedTile inherited=new InheritedTile("inherited",1);
        OverrideTile child=new OverrideTile("child",2);
        w.addTileEntity(fixed);w.addTileEntity(inherited);w.addTileEntity(child);
        check(w.loadedTileEntityList.size()==3 && w.tickableTileEntities.size()==2,"native admission separates static/ticking");
        check(fixed.loads==1 && inherited.loads==1 && child.loads==1,"onLoad includes static tiles");
        check(fixed.getWorld()==w && inherited.getWorld()==w,"native world binding");
        events.clear();w.updateEntities();
        check(events.equals(Arrays.asList("inherited:base","child:before","child:base","child:after")),"inherited dispatch and exact parent-child ordering");
        child.stop=true;events.clear();w.updateEntities();
        check(events.equals(Arrays.asList("inherited:base","child:before")) && child.ticks==1,"early return does not call parent");
        TickRoot pending=new TickRoot("pending",5);SpawnerTile spawner=new SpawnerTile(pending);w.addTileEntity(spawner);
        events.clear();w.updateEntities();check(pending.loads==1 && pending.ticks==0,"deferred admission/onLoad after current pass");
        check(w.tickableTileEntities.contains(pending),"deferred ticking admission");w.updateEntities();check(pending.ticks==1,"deferred tile ticks next pass");
        TickRoot unloaded=new TickRoot("unloaded",32);w.addTileEntity(unloaded);w.updateEntities();check(unloaded.ticks==0,"unloaded chunk suppresses tick");
        w.getChunk(2,0);w.updateEntities();check(unloaded.ticks==1,"loaded chunk resumes ticking");
        int prior=inherited.ticks;inherited.invalidate();w.updateEntities();
        check(inherited.ticks==prior && !w.tickableTileEntities.contains(inherited) && !w.loadedTileEntityList.contains(inherited),"invalid tile skipped and removed natively");
        TickRoot worldless=new TickRoot("worldless",6);w.addTileEntity(worldless);worldless.setWorld(null);w.updateEntities();check(worldless.ticks==0,"missing world suppresses tick");
        TickRoot outside=new TickRoot("outside",80);w.getChunk(5,0);w.addTileEntity(outside);w.getWorldBorder().setTransition(32);w.updateEntities();check(outside.ticks==0,"world border suppresses tick");
        System.out.println("PASS: native World.addTileEntity/updateEntities admission, static exclusion/onLoad, world binding, inherited dispatch, parent call order, early return, deferred addition/next-pass ticking, invalid removal, unloaded chunk suppression/resumption, missing-world and border suppression.");
        System.out.println("LIMIT: controlled native World/Chunk fixtures, not production tile compilation or live gameplay/save lifecycle.");
    }
}
