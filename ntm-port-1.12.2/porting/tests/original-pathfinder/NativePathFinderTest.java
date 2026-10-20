import java.lang.reflect.*;
import java.util.*;
import com.hbm.entity.pathfinder.*;
import com.hbm.blocks.generic.BlockMetalFence;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.*;

/** Both engines execute against their actual version's blocks, points, heap,
 * world superclass and entity superclass. Only world storage/entity callbacks
 * are controlled. No net.minecraft or Forge test replacements exist. */
public final class NativePathFinderTest {
    static long checks;
    static void check(boolean condition,String message) { ++checks;if(!condition)throw new AssertionError(message); }
    static void same(Object a,Object b,String message) { check(Objects.equals(a,b),message+": "+a+" != "+b); }
    static final sun.misc.Unsafe U;
    static { try { Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(sun.misc.Unsafe)f.get(null); } catch(Exception e) { throw new RuntimeException(e); } }
    static <T>T allocate(Class<T> c) { try { return c.cast(U.allocateInstance(c)); } catch(Exception e) { throw new RuntimeException(e); } }
    static void set(Object object,Class<?> type,String name,Object value) throws Exception { Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(object,value); }
    static final class Cell { final int id,meta;Cell(int id,int meta) { this.id=id;this.meta=meta; } }
    static String key(int x,int y,int z) { return x+","+y+","+z; }
    static class Grid {
        Map<String,Cell> cells=new HashMap<>();int ground=63;
        List<String> chunks=new ArrayList<>(),cache=new ArrayList<>(),profiles=new ArrayList<>(),events=new ArrayList<>();
        long reads;boolean recordReads;long failAtRead=-1;
        Cell cell(int x,int y,int z) { reads++;if(reads==failAtRead)throw new IllegalStateException("controlled world read failure");if(recordReads)events.add("read:"+key(x,y,z));Cell c=cells.get(key(x,y,z));return c==null?new Cell(y<=ground?1:0,0):c; }
        Grid put(int x,int y,int z,int id,int meta) { cells.put(key(x,y,z),new Cell(id,meta));return this; }
        Grid copy() { Grid g=new Grid();g.cells.putAll(cells);g.ground=ground;g.recordReads=recordReads;g.failAtRead=failAtRead;return g; }
        void clearTrace() { chunks.clear();cache.clear();profiles.clear();events.clear();reads=0; }
    }
    static BlockMetalFence metal;
    static aji oldMetal;
    static class OldMetal extends akz {
        OldMetal() { super("fixture",awt.f); }
        public int b() { return 1000; } // Original NTM custom render ID, never vanilla 11.
    }
    static IBlockState state(Cell c) { Block b=c.id==3000?metal:Block.getBlockById(c.id);return b.getStateFromMeta(c.meta); }
    static aji oldBlock(Cell c) { return c.id==3000?oldMetal:aji.e(c.id); }
    static class NativeProfile extends Profiler {
        Grid grid;NativeProfile(Grid g) { grid=g; }
        public void startSection(String name) { grid.profiles.add("start:"+name); }
        public void endSection() { grid.profiles.add("end"); }
    }
    static class OldProfile extends qi {
        Grid grid;OldProfile(Grid g) { grid=g; }
        public void a(String name) { grid.profiles.add("start:"+name); }
        public void b() { grid.profiles.add("end"); }
    }
    static class NativeWorld extends World {
        Grid grid;
        NativeWorld() { super(null,null,null,null,false); }
        protected IChunkProvider createChunkProvider() { throw new UnsupportedOperationException(); }
        protected boolean isChunkLoaded(int x,int z,boolean empty) { return true; }
        public IBlockState getBlockState(BlockPos p) { return state(grid.cell(p.getX(),p.getY(),p.getZ())); }
        public Chunk getChunk(int x,int z) { grid.chunks.add(x+","+z);NativeChunk c=allocate(NativeChunk.class);c.grid=grid;c.cx=x;c.cz=z;return c; }
    }
    static class NativeChunk extends Chunk {
        Grid grid;int cx,cz;
        NativeChunk() { super(null,0,0); }
        public boolean isEmptyBetween(int a,int b) { grid.cache.add("bounds:"+a+":"+b);return false; }
        public IBlockState getBlockState(BlockPos p) { grid.cache.add("read:"+p.getX()+","+p.getY()+","+p.getZ());return state(grid.cell(p.getX(),p.getY(),p.getZ())); }
    }
    static class OldWorld extends ahb {
        Grid grid;
        OldWorld() { super(null,"fixture",(aqo)null,(ahj)null,null); }
        protected apu j() { throw new UnsupportedOperationException(); }
        protected int p() { return 0; }
        public sa a(int id) { return null; }
        public aji a(int x,int y,int z) { return oldBlock(grid.cell(x,y,z)); }
        public int e(int x,int y,int z) { return grid.cell(x,y,z).meta; }
        public apx e(int x,int z) { grid.chunks.add(x+","+z);OldChunk c=allocate(OldChunk.class);c.grid=grid;c.cx=x;c.cz=z;return c; }
    }
    static class OldChunk extends apx {
        Grid grid;int cx,cz;
        OldChunk() { super(null,0,0); }
        public boolean c(int a,int b) { grid.cache.add("bounds:"+a+":"+b);return false; }
        public aji a(int x,int y,int z) { x+=cx*16;z+=cz*16;grid.cache.add("read:"+x+","+y+","+z);return oldBlock(grid.cell(x,y,z)); }
    }
    static class NativeEntity extends Entity {
        boolean water,lava;int fall=3;List<String> callbacks=new ArrayList<>();
        NativeEntity(World w) { super(w); }
        protected void entityInit() { }
        protected void readEntityFromNBT(NBTTagCompound n) { }
        protected void writeEntityToNBT(NBTTagCompound n) { }
        public boolean isInWater() { callbacks.add("water");return water; }
        public boolean isInLava() { callbacks.add("lava");return lava; }
        public int getMaxFallHeight() { callbacks.add("fall");return fall; }
    }
    static class OldEntity extends sa {
        boolean water,lava;int fall=3;List<String> callbacks=new ArrayList<>();
        OldEntity(ahb w) { super(w); }
        protected void c() { }
        protected void a(dh n) { }
        protected void b(dh n) { }
        public boolean M() { callbacks.add("water");return water; }
        public boolean P() { callbacks.add("lava");return lava; }
        public int ax() { callbacks.add("fall");return fall; }
    }
    static class Pair {
        Grid ng,og;NativeWorld nw;OldWorld ow;NativeEntity ne;OldEntity oe;
        Pair(Grid g,double x,double y,double z,float width,float height) throws Exception {
            ng=g.copy();og=g.copy();nw=allocate(NativeWorld.class);ow=allocate(OldWorld.class);nw.grid=ng;ow.grid=og;
            set(nw,World.class,"profiler",new NativeProfile(ng));set(ow,ahb.class,"C",new OldProfile(og));
            ne=allocate(NativeEntity.class);oe=allocate(OldEntity.class);ne.world=nw;oe.o=ow;ne.callbacks=new ArrayList<>();oe.callbacks=new ArrayList<>();ne.fall=oe.fall=3;
            ne.posX=oe.s=x;ne.posY=oe.t=y;ne.posZ=oe.u=z;ne.width=oe.M=width;ne.height=oe.N=height;
            ne.setEntityBoundingBox(new AxisAlignedBB(x-width/2.0F,y,z-width/2.0F,x+width/2.0F,y+height,z+width/2.0F));
            set(oe,sa.class,"C",azt.a(ne.getEntityBoundingBox().minX,y,ne.getEntityBoundingBox().minZ,ne.getEntityBoundingBox().maxX,y+height,ne.getEntityBoundingBox().maxZ));
        }
        void flags(boolean water,boolean lava,int fall) { ne.water=oe.water=water;ne.lava=oe.lava=lava;ne.fall=oe.fall=fall; }
        void clear() { ng.clearTrace();og.clearTrace();ne.callbacks.clear();oe.callbacks.clear(); }
    }
    static void compare(Path n,ayf o,Pair p,String label) {
        check((n==null)==(o==null),label+" null path");
        if(n!=null) {
            same(n.getCurrentPathLength(),o.d(),label+" length");same(n.getCurrentPathIndex(),o.e(),label+" index");
            for(int i=0;i<o.d();i++) {
                PathPoint np=n.getPathPointFromIndex(i);aye op=o.a(i);
                same(key(np.x,np.y,np.z),key(op.a,op.b,op.c),label+" vertex"+i);
                same(Float.floatToIntBits(np.totalPathDistance),Float.floatToIntBits(op.e),label+" cost"+i);
                same(Float.floatToIntBits(np.distanceToNext),Float.floatToIntBits(op.f),label+" heuristic"+i);
                same(Float.floatToIntBits(np.distanceToTarget),Float.floatToIntBits(op.g),label+" heap cost"+i);
                same(np.visited,op.i,label+" visited"+i);
                Vec3d nv=n.getVectorFromIndex(p.ne,i);azw ov=o.a(p.oe,i);
                same(Double.doubleToLongBits(nv.x),Double.doubleToLongBits(ov.a),label+" position x");
                same(Double.doubleToLongBits(nv.y),Double.doubleToLongBits(ov.b),label+" position y");
                same(Double.doubleToLongBits(nv.z),Double.doubleToLongBits(ov.c),label+" position z");
            }
        }
        same(p.ne.callbacks,p.oe.callbacks,label+" entity callback order");
    }
    static Path direct(Pair p,int x,int y,int z,float distance,int flags,String label) {
        NTMPathFinder finder=new NTMPathFinder(p.nw,(flags&1)!=0,(flags&2)!=0,(flags&4)!=0,(flags&8)!=0);
        ayg old=new ayg(p.ow,(flags&1)!=0,(flags&2)!=0,(flags&4)!=0,(flags&8)!=0);
        Path n=finder.createEntityPathTo(p.ne,x,y,z,distance);ayf o=old.a(p.oe,x,y,z,distance);
        compare(n,o,p,label);return n;
    }
    static void pathAndHeap() {
        Random r=new Random(908127);
        for(int i=0;i<10000;i++) {
            int x=r.nextInt(),y=r.nextInt(),z=r.nextInt();
            PathPoint n=new PathPoint(x,y,z);aye o=new aye(x,y,z);
            same(n.hashCode(),o.hashCode(),"packed hash");same(PathPoint.makeHash(x,y,z),aye.a(x,y,z),"static packed hash");
            PathPoint nn=new PathPoint(r.nextInt(),r.nextInt(),r.nextInt());aye oo=new aye(nn.x,nn.y,nn.z);
            same(Float.floatToIntBits(n.distanceTo(nn)),Float.floatToIntBits(o.a(oo)),"point distance bits");
            same(Float.floatToIntBits(n.distanceToSquared(nn)),Float.floatToIntBits(o.b(oo)),"point squared distance bits");
        }
        for(int trial=0;trial<32;trial++) {
            PathHeap heap=new PathHeap();ayd old=new ayd();List<PathPoint> np=new ArrayList<>();List<aye> op=new ArrayList<>();
            for(int i=0;i<1300;i++) { PathPoint n=new PathPoint(i,0,0);aye o=new aye(i,0,0);n.distanceToTarget=o.g=r.nextInt(40);heap.addPoint(n);old.a(o);np.add(n);op.add(o); }
            for(int i=0;i<1000;i++) { int j=r.nextInt(np.size());float d=r.nextInt(40);heap.changeDistance(np.get(j),d);old.a(op.get(j),d); }
            while(!heap.isPathEmpty()) { same(heap.dequeue().x,old.c().a,"heap equal-cost/decrease/increase order");same(heap.isPathEmpty(),old.e(),"heap empty"); }
            heap.clearPath();old.a();check(heap.isPathEmpty()&&old.e(),"heap clear");
        }
    }
    static void obstacleMatrix() throws Exception {
        int[] ids={0,1,8,9,10,11,64,71,96,85,113,107,139,66,27,28,157,51,81,65,30,78,118,120,3000};
        float[] widths={.6F,1F,1.75F,2.1F};
        for(int id:ids)for(int meta:new int[]{0,4,8,12})for(int flags=0;flags<16;flags++)for(float width:widths) {
            Grid g=new Grid();
            for(int z=-4;z<=4;z++) { g.put(2,64,z,id,(id==64||id==71)?meta&7:id==118?meta&3:meta);if(id==64||id==71)g.put(2,65,z,id,8|(meta>>>3)); }
            Pair p=new Pair(g,.5,64,.5,width,1F);p.flags((flags&8)!=0&&(id==8||id==9),(flags&2)!=0,3);
            direct(p,6,64,0,12,flags,"obstacle="+id+" meta="+meta+" flags="+flags+" width="+width);
        }
    }
    static void randomWorlds() throws Exception {
        Random random=new Random(9179);int[] ids={0,0,0,1,1,8,10,85,96,66,51,3000};
        for(int trial=0;trial<640;trial++) {
            Grid grid=new Grid();
            for(int x=-5;x<11;x++)for(int z=-7;z<=7;z++)if(random.nextInt(4)==0) {
                int id=ids[random.nextInt(ids.length)],height=1+random.nextInt(3);
                for(int y=64;y<64+height;y++)grid.put(x,y,z,id,random.nextBoolean()?0:4);
            }
            if(trial%5==0)for(int z=-2;z<=2;z++)for(int x=1;x<=7;x++)for(int y=60;y<=63;y++)grid.put(x,y,z,0,0);
            Pair p=new Pair(grid,random.nextBoolean()?.5:-.5,64+(trial%4)*.24,.5,new float[]{.6F,1F,1.75F,2.5F}[trial%4],new float[]{1F,1.8F,2F}[trial%3]);
            p.flags(trial%7==0,trial%3==0,trial%6);direct(p,8,64,trial%9-4,16,trial%16,"random="+trial);
        }
    }
    static void boundaries() throws Exception {
        // No native 200-iteration truncation, diagonal insertion or origin-distance bound.
        Grid corridor=new Grid();for(int x=-1;x<=250;x++)for(int y=64;y<=67;y++) { corridor.put(x,y,-1,1,0);corridor.put(x,y,1,1,0); }
        Pair p=new Pair(corridor,.5,64,.5,.6F,1F);Path route=direct(p,240,64,0,241,9,"long corridor");
        check(route!=null&&route.getFinalPathPoint().x==240&&route.getCurrentPathLength()==241,"search continues past 200 expanded nodes");
        for(int startX:new int[]{-32770,-257,-2,0,255,32767,16777215,16777216})for(float width:new float[]{.6F,1.75F}) {
            p=new Pair(new Grid(),startX+.5,64,.5,width,1F);direct(p,startX+8,64,0,16,9,"float integer target boundary");
        }
        for(double y:new double[]{-1.6,-.5,0,.49,.5,1,63.49,63.5,64})for(int fall=0;fall<5;fall++) {
            Grid g=new Grid();g.ground=-2;p=new Pair(g,.5,y,.5,.6F,1F);p.flags(false,false,fall);direct(p,5,(int)y,0,12,9,"Y/fall boundary");
        }
        for(float distance:new float[]{0,1,1.00001F,4,Float.NaN}) { p=new Pair(new Grid(),.5,64,.5,.6F,1F);direct(p,4,64,0,distance,9,"strict maxDistance="+distance); }
        Grid g=new Grid();for(int x=-2;x<=2;x++)for(int y=64;y<=67;y++)for(int z=-2;z<=2;z++)g.put(x,y,z,9,0);
        p=new Pair(g,.5,64,.5,.6F,1F);p.flags(true,false,3);
        NTMPathFinder n=new NTMPathFinder(p.nw,true,false,true,true);ayg o=new ayg(p.ow,true,false,true,true);
        for(int i=0;i<3;i++) { p.clear();p.flags(i==0,false,3);compare(n.createEntityPathTo(p.ne,7,64,0,12),o.a(p.oe,7,64,0,12),p,"reuse/water avoidance restoration"+i); }
        // Rails are rejected unless the entity is on a rail now or immediately above one.
        for(int sourceY:new int[]{63,64,65}) { g=new Grid();for(int x=1;x<8;x++)g.put(x,64,0,66,0);g.put(0,sourceY,0,66,0);p=new Pair(g,.5,64,.5,.6F,1);direct(p,7,64,0,12,9,"rail source height"+sourceY); }
        // An impenetrable wall yields the original closest partial route, not null.
        g=new Grid();for(int z=-20;z<=20;z++)for(int y=64;y<70;y++)g.put(4,y,z,1,0);
        p=new Pair(g,.5,64,.5,.6F,1);route=direct(p,8,64,0,12,9,"closest partial");check(route!=null&&route.getFinalPathPoint().x==3,"closest partial retained");
    }
    static void wrappers() throws Exception {
        Random random=new Random(781);
        for(int trial=0;trial<240;trial++) {
            Grid g=new Grid();int tx=trial%13-6,ty=trial%21+55,tz=trial%11-5;
            if(trial%3==0)g.ground=trial%21+55;
            if(trial%7==0)g.ground=-10;
            if(trial%5==0)for(int x=-14;x<=14;x++)for(int z=-14;z<=14;z++)g.put(x,74,z,1,0);
            Pair p=new Pair(g,random.nextDouble()*2-1,64.25,random.nextDouble()*2-1,trial%2==0?.6F:1.75F,1F);
            float distance=trial%6==0?0:trial%3==0?4:16;int flags=trial%16;
            Path n=PathFinderUtils.getPathEntityToCoordPartial(p.nw,p.ne,tx,ty,tz,distance,(flags&1)!=0,(flags&2)!=0,(flags&4)!=0,(flags&8)!=0);
            ayf o=LegacyPathFinderUtils.getPathEntityToCoordPartial(p.ow,p.oe,tx,ty,tz,distance,(flags&1)!=0,(flags&2)!=0,(flags&4)!=0,(flags&8)!=0);
            compare(n,o,p,"coordinate wrapper="+trial);same(p.ng.chunks,p.og.chunks,"cache X/Z footprint");same(p.ng.cache,p.og.cache,"cache Y bounds");same(p.ng.profiles,p.og.profiles,"profiler order");same(p.ng.profiles,Arrays.asList("start:pathfind","end"),"profile start/end");
            p.clear();Pair target=new Pair(g,tx+.125,ty+.25,tz+.875,.6F,1);
            n=PathFinderUtils.getPathEntityToEntityPartial(p.nw,p.ne,target.ne,distance,(flags&1)!=0,(flags&2)!=0,(flags&4)!=0,(flags&8)!=0);
            o=LegacyPathFinderUtils.getPathEntityToEntityPartial(p.ow,p.oe,target.oe,distance,(flags&1)!=0,(flags&2)!=0,(flags&4)!=0,(flags&8)!=0);
            compare(n,o,p,"entity wrapper="+trial);same(p.ng.chunks,p.og.chunks,"entity cache footprint");same(p.ng.cache,p.og.cache,"entity cache Y bounds");same(p.ng.profiles,p.og.profiles,"entity profiler order");
        }
        // A nearby target is deliberately extended to maxDist, not clamped.
        Pair p=new Pair(new Grid(),.5,64,.5,.6F,1);Path n=PathFinderUtils.getPathEntityToCoordPartial(p.nw,p.ne,1,64,0,16,true,false,false,true);
        check(n!=null&&n.getFinalPathPoint().x>1,"nearby target remains extended");
    }
    static void callbackBoundaries() throws Exception {
        // In worlds whose blocks need no separate metadata lookup, the complete
        // live read sequence must also match, not only the returned route.
        for(int id:new int[]{0,1,8,9,10,11,85,113,139,66,27,51,81,30,78,118,3000})for(int flags=0;flags<16;flags++) {
            Grid grid=new Grid();grid.recordReads=true;
            for(int z=-2;z<=2;z++)grid.put(2,64,z,id,id==78?7:0);
            Pair pair=new Pair(grid,.5,64,.5,.6F,1);pair.flags(false,true,3);
            direct(pair,5,64,0,10,flags,"read sequence id="+id+" flags="+flags);
            same(pair.ng.events,pair.og.events,"ordered live block observations");
        }
        // Surface scan uses the supplied cache, but collisions use entity.world.
        Grid scan=new Grid();for(int y=64;y<68;y++)scan.put(0,y,0,9,0);
        Pair live=new Pair(new Grid(),.5,64,.5,.6F,1),cache=new Pair(scan,.5,64,.5,.6F,1);live.flags(true,false,3);
        NTMPathFinder n=new NTMPathFinder(cache.nw,true,false,true,true);ayg o=new ayg(cache.ow,true,false,true,true);
        compare(n.createEntityPathTo(live.ne,5,64,0,12),o.a(live.oe,5,64,0,12),live,"distinct scan cache and live world");
        same(cache.ng.reads,5L,"exact native surface-only cache reads");same(cache.og.reads,5L,"exact original surface-only cache reads");
        check(live.ng.reads>cache.ng.reads&&live.og.reads>cache.og.reads,"collisions do not use cache");
        for(long fail:new long[]{1,2,11,27}) {
            Grid grid=new Grid();grid.failAtRead=fail;Pair pair=new Pair(grid,.5,64,.5,.6F,1);
            boolean nf=false,of=false;
            try { PathFinderUtils.getPathEntityToCoordPartial(pair.nw,pair.ne,5,64,0,12,true,false,false,true); } catch(IllegalStateException ex) { nf=true; }
            try { LegacyPathFinderUtils.getPathEntityToCoordPartial(pair.ow,pair.oe,5,64,0,12,true,false,false,true); } catch(IllegalStateException ex) { of=true; }
            check(nf&&of,"original failure propagates");same(pair.ng.profiles,pair.og.profiles,"failure profiler order");same(pair.ng.profiles,Arrays.asList("start:pathfind"),"no added endSection on original failure path");
        }
    }
    static void vanillaAudit() throws Exception {
        Method collision=NTMPathFinder.class.getDeclaredMethod("collision",Entity.class,int.class,int.class,int.class,PathPoint.class);collision.setAccessible(true);
        int cases=0;
        for(int id=0;id<=175;id++)for(int meta=0;meta<16;meta++) {
            Block block=Block.getBlockById(id);aji old=aji.e(id);if(block==null||old==null||Block.getIdFromBlock(block)!=id||aji.b(old)!=id)continue;
            IBlockState state;try { state=block.getStateFromMeta(meta); } catch(RuntimeException e) { continue; }
            if(block.getMetaFromState(state)!=meta)continue;cases++;
            same(NTMPathFinder.isOriginalNormalCube(state),old.r(),"all original canonical vanilla support id="+id+" meta="+meta);
            for(int flags=0;flags<8;flags++) {
                Pair pair=new Pair(new Grid().put(5,64,0,id,meta),.5,64,.5,.6F,1);
                int nc=(Integer)collision.invoke(new NTMPathFinder(pair.nw,(flags&1)!=0,(flags&2)!=0,(flags&4)!=0,false),pair.ne,5,64,0,new PathPoint(1,1,1));
                int oc=ayg.a(pair.oe,5,64,0,new aye(1,1,1),(flags&4)!=0,(flags&2)!=0,(flags&1)!=0);
                same(nc,oc,"all original canonical vanilla collision id="+id+" meta="+meta+" flags="+flags);
            }
        }
        System.out.println("PASS "+cases+" canonical original vanilla block states: support and all eight collision flags");
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();kl.b();metal=new BlockMetalFence(Material.IRON);oldMetal=new OldMetal();
        vanillaAudit();pathAndHeap();System.out.println("PASS actual original/native PathPoint and PathHeap differential");
        obstacleMatrix();System.out.println("PASS full flag/metadata/width obstacle matrix");
        randomWorlds();boundaries();wrappers();callbackBoundaries();
        OwnerCallsTest.run();
        System.out.println("PASS "+checks+" original/native path assertions; whole production helpers and native metal fence compile. Original ayg/aye/ayd/ayf bytecode executes unchanged. Whole FBI/Glyphid entities and live movement are not certified.");
    }
}
