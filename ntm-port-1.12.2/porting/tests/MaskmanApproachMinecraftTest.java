import java.lang.reflect.Field;
import java.util.*;
import com.hbm.entity.mob.ai.EntityAIMaskmanCasualApproach;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntitySenses;
import net.minecraft.init.Bootstrap;
import net.minecraft.pathfinding.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/** Whole production AI on native Minecraft classes; only environment outcomes are controlled.
 * Does not simulate vanilla path search, entity ticking, or a loaded game. */
public class MaskmanApproachMinecraftTest {
    static int checks;
    static void check(boolean ok,String msg) { checks++; if(!ok) throw new AssertionError(msg); }
    static void close(double a,double b,String msg) { check(Math.abs(a-b)<1e-10,msg+": "+a+" != "+b); }
    static Field field(String name) throws Exception { Field f=EntityAIMaskmanCasualApproach.class.getDeclaredField(name);f.setAccessible(true);return f; }
    static int value(EntityAIMaskmanCasualApproach ai,String name) throws Exception { return field(name).getInt(ai); }
    static void put(EntityAIMaskmanCasualApproach ai,String name,Object v) throws Exception { field(name).set(ai,v); }
    static class FixtureWorld extends World {
        private FixtureWorld() { super(null,null,null,null,false); }
        protected IChunkProvider createChunkProvider() { throw new UnsupportedOperationException(); }
        protected boolean isChunkLoaded(int x,int z,boolean empty) { return false; }
        static FixtureWorld make() throws Exception {
            Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);
            return (FixtureWorld)((sun.misc.Unsafe)f.get(null)).allocateInstance(FixtureWorld.class);
        }
    }
    static class TraceRandom extends Random {
        List<String> calls=new ArrayList<>(); Float forcedFloat;
        TraceRandom(long seed) { super(seed); }
        public int nextInt(int n) { calls.add("i"+n);return super.nextInt(n); }
        public float nextFloat() { calls.add("f");return forcedFloat==null?super.nextFloat():forcedFloat; }
        public double nextGaussian() { calls.add("g");return super.nextGaussian(); }
        String trace() { return String.join(",",calls); }
    }
    static class Senses extends EntitySenses {
        int calls;boolean visible=true;
        Senses(EntityLiving e) { super(e); }
        public boolean canSee(Entity e) { calls++;return visible; }
    }
    static class Navigator extends PathNavigateGround {
        Path result;int queries,moves,sets;BlockPos requested;double[] move;double requestedSpeed;boolean succeeds=true;
        Navigator(EntityLiving e,World w) { super(e,w); }
        public Path getPathToPos(BlockPos p) { queries++;requested=p;return result; }
        public boolean setPath(Path p,double s) { sets++;currentPath=p;speed=s;requestedSpeed=s;return p!=null; }
        public boolean tryMoveToXYZ(double x,double y,double z,double s) { moves++;move=new double[]{x,y,z};requestedSpeed=s;return succeeds; }
        void current(Path p) { currentPath=p; }
    }
    static class Creature extends EntityCreature {
        Navigator nav;Senses senses;TraceRandom rng;int attacks;
        Creature(World w,double x,double y,double z) { super(null);world=w;setPosition(x,y,z);nav=new Navigator(this,w);senses=new Senses(this);rng=new TraceRandom(31); }
        public PathNavigate getNavigator() { return nav==null?super.getNavigator():nav; }
        public EntitySenses getEntitySenses() { return senses==null?super.getEntitySenses():senses; }
        public Random getRNG() { return rng==null?super.getRNG():rng; }
        public boolean attackEntityAsMob(Entity e) { attacks++;return true; }
    }
    static class OtherCreature extends Creature { OtherCreature(World w) { super(w,1,64,0); } }
    static class Case {
        FixtureWorld w;Creature owner,target;EntityAIMaskmanCasualApproach ai;
        Case(boolean memory) throws Exception { w=FixtureWorld.make();owner=new Creature(w,0,64,0);target=new Creature(w,4,64,0);owner.setAttackTarget(target);ai=new EntityAIMaskmanCasualApproach(owner,1.25,memory); }
    }
    static Path path(int x,int y,int z) { return new Path(new PathPoint[]{new PathPoint(x,y,z)}); }
    static void cache(Case c,double x,double y,double z) throws Exception { put(c.ai,"lastX",x);put(c.ai,"lastY",y);put(c.ai,"lastZ",z); }
    static double[] approach(Creature o,Creature t,Random r) {
        double x=o.posX-t.posX,y=o.posY-t.posY,z=o.posZ-t.posZ;
        // Target Vec3d uses a float-rounded sqrt, then promotes to double (verified bytecode).
        double length=(float)Math.sqrt(x*x+y*y+z*z),range=Math.min(length,20)-10;
        if(length<1.0e-4) { x=0;y=0;z=0; } else { x/=length;y/=length;z/=length; }
        return new double[]{o.posX+x*range+r.nextGaussian()*2,o.posY+y-5+r.nextInt(11),o.posZ+z*range+r.nextGaussian()*2};
    }
    static void vector(double[] actual,double[] expected,String msg) { for(int i=0;i<3;i++) close(actual[i],expected[i],msg+" axis "+i); }
    static void eligibilityAndLifecycle() throws Exception {
        Case c=new Case(false);check(c.ai.getMutexBits()==3,"mutex bits");check(field("worldObj").get(c.ai)==c.w,"custom world field from native owner");
        c.owner.setAttackTarget(null);check(!c.ai.shouldExecute(),"null target");c.owner.setAttackTarget(c.target);c.target.setHealth(0);check(!c.ai.shouldExecute(),"dead target");c.target.setHealth(20);
        EntityAIMaskmanCasualApproach filtered=new EntityAIMaskmanCasualApproach(c.owner,OtherCreature.class,1,false);check(!filtered.shouldExecute(),"class filter rejection");
        check(c.owner.rng.calls.isEmpty()&&c.owner.nav.queries==0,"eligibility consumes no RNG/path");
        c.owner.setAttackTarget(new OtherCreature(c.w));c.owner.nav.result=path(1,64,0);check(new EntityAIMaskmanCasualApproach(c.owner,Creature.class,1,false).shouldExecute(),"assignable subclass");
        c=new Case(false);Random expected=new Random(31);double[] pos=approach(c.owner,c.target,expected);int timer=4+expected.nextInt(7);
        check(!c.ai.shouldExecute(),"missing initial path");check(c.owner.nav.requested.equals(new BlockPos(pos[0],pos[1],pos[2])),"native final XYZ floors coordinates");check(value(c.ai,"pathTimer")==timer,"initial timer");
        check(c.owner.rng.trace().equals("g,i11,g,i7"),"initial RNG ordering");check(c.owner.rng.nextLong()==expected.nextLong(),"initial RNG state");
        for(int i=1;i<timer;i++) check(c.ai.shouldExecute(),"cached timer retains legacy true even with null path");check(c.owner.nav.queries==1,"no cached path queries");
        c.owner.nav.result=path(4,64,0);check(c.ai.shouldExecute(),"path retry succeeds");c.ai.startExecuting();check(c.owner.nav.getPath()==c.owner.nav.result&&value(c.ai,"pathTimer")==0,"start path and reset timer");close(c.owner.nav.requestedSpeed,1.25,"start speed");
        check(c.ai.shouldContinueExecuting(),"short memory active path");c.owner.nav.getPath().incrementPathIndex();check(!c.ai.shouldContinueExecuting(),"finished native path");c.owner.nav.current(path(4,64,0));c.ai.resetTask();check(c.owner.nav.noPath(),"native reset clears path");check(!c.ai.shouldContinueExecuting(),"no path continuation");
        c=new Case(true);c.owner.setHomePosAndDistance(new BlockPos(0,64,0),2);
        c.target.setPosition(-1.01,64,0);check(!c.ai.shouldContinueExecuting(),"negative floor lands exact excluded radius");c.target.setPosition(-0.01,64.99,0.99);check(c.ai.shouldContinueExecuting(),"negative fraction floors within home");
        c.target.setPosition(2,64,0);check(!c.ai.shouldContinueExecuting(),"exact home boundary excluded");c.owner.detachHome();check(c.ai.shouldContinueExecuting(),"no home unrestricted with no path");
        c.target.setHealth(0);check(!c.ai.shouldContinueExecuting(),"dead continuation");c.owner.setAttackTarget(null);check(!c.ai.shouldContinueExecuting(),"null continuation");
        for(int axis=0;axis<3;axis++) { c=new Case(true);c.owner.setHomePosAndDistance(BlockPos.ORIGIN,2);double[] xyz={0,0,0};xyz[axis]=-1.01;c.target.setPosition(xyz[0],xyz[1],xyz[2]);check(!c.ai.shouldContinueExecuting(),"negative home floor axis "+axis);xyz[axis]=-0.01;c.target.setPosition(xyz[0],xyz[1],xyz[2]);check(c.ai.shouldContinueExecuting(),"inside negative home floor axis "+axis); }

    }
    static void gatingAndRng() throws Exception {
        Case c=new Case(false);c.owner.senses.visible=false;put(c.ai,"attackTick",2);c.ai.updateTask();check(c.owner.nav.moves==0&&value(c.ai,"pathTimer")==-1&&value(c.ai,"attackTick")==1,"hidden gates path but ticks counters");check(c.owner.rng.calls.isEmpty(),"hidden no RNG");check(c.owner.getLookHelper().getIsLooking(),"native look helper set");
        c.ai.updateTask();c.ai.updateTask();check(value(c.ai,"attackTick")==0&&c.owner.attacks==0,"attack tick saturates and disabled attack remains disabled");
        c=new Case(true);c.owner.senses.visible=false;c.ai.updateTask();check(c.owner.senses.calls==0&&c.owner.nav.moves==1,"long memory skips visibility");check(c.owner.rng.trace().equals("i7,g,i11,g"),"sentinel avoids random float");
        c=new Case(false);put(c.ai,"pathTimer",2);c.ai.updateTask();check(c.owner.nav.moves==0&&value(c.ai,"pathTimer")==1&&c.owner.rng.calls.isEmpty(),"positive timer gates RNG");
        for(float draw:new float[]{0.049f,0.05f}) { c=new Case(false);cache(c,4,64,0);c.owner.rng.forcedFloat=draw;c.ai.updateTask();boolean refresh=draw<0.05f;check(c.owner.nav.moves==(refresh?1:0),"5 percent refresh strict boundary");check(c.owner.rng.trace().equals(refresh?"f,i7,g,i11,g":"f"),"stationary RNG short circuit"); }
        for(double movement:new double[]{0.999,1}) { c=new Case(false);cache(c,4-movement,64,0);c.owner.rng.forcedFloat=0.9f;c.ai.updateTask();check(c.owner.nav.moves==(movement==1?1:0),"movement threshold");check(c.owner.rng.trace().startsWith(movement==1?"i7":"f"),"movement skips float only at threshold"); }
        c=new Case(false);c.target.setEntityBoundingBox(new AxisAlignedBB(3.5,60,-0.5,4.5,62,0.5));c.ai.updateTask();close(field("lastY").getDouble(c.ai),60,"cached bounding box minY, not posY");
    }
    static void penalties() throws Exception {
        // null path, empty path/final point, endpoint exactly one away, and close endpoint.
        Path[] paths={null,new Path(new PathPoint[0]),path(3,64,0),path(4,64,0)};
        for(int i=0;i<paths.length;i++) { Case c=new Case(false);put(c.ai,"failedPathFindingPenalty",20);c.owner.nav.current(paths[i]);Random r=new Random(31);int timer=20+4+r.nextInt(7);double[] pos=approach(c.owner,c.target,r);c.ai.updateTask();check(value(c.ai,"pathTimer")==timer,"old penalty applies before update "+i);check(value(c.ai,"failedPathFindingPenalty")== (i==3?0:30),"endpoint penalty "+i);vector(c.owner.nav.move,pos,"movement formula");check(c.owner.rng.nextLong()==r.nextLong(),"update RNG state"); }
        for(double distance:new double[]{16,16.01,32,32.01}) for(boolean success:new boolean[]{true,false}) { Case c=new Case(false);c.target.setPosition(distance,64,0);c.owner.nav.succeeds=success;int base=4+new Random(31).nextInt(7);c.ai.updateTask();int extra=distance>32?10:distance>16?5:0;check(value(c.ai,"pathTimer")==base+extra+(success?0:15),"distance/failed move penalty "+distance+" "+success); }
        Case c=new Case(false);c.target.setPosition(0,64,0);c.target.setEntityBoundingBox(new AxisAlignedBB(-.5,31,-.5,.5,33,.5));c.ai.updateTask();check(value(c.ai,"pathTimer")==4+new Random(31).nextInt(7)+10,"distance uses bounding box minY");
        // A second refresh uses the accumulated penalty, but a good final endpoint clears only future penalties.
        c=new Case(false);c.ai.updateTask();check(value(c.ai,"failedPathFindingPenalty")==10,"first failed path penalty");put(c.ai,"pathTimer",0);c.target.setPosition(6,64,0);c.owner.nav.current(path(6,64,0));Random r=new Random(31);r.nextInt(7);approach(c.owner,new Creature(c.w,4,64,0),r);int expected=10+4+r.nextInt(7);c.ai.updateTask();check(value(c.ai,"pathTimer")==expected&&value(c.ai,"failedPathFindingPenalty")==0,"second refresh applies then clears penalty");
    }
    static void formula() throws Exception {
        double[][] offsets={{0,0,0},{0.00001,0,0},{1,2,3},{10,0,0},{20,0,0},{40,0,0},{0,30,0},{-3,-4,-5}};
        for(double[] v:offsets) { Case c=new Case(false);c.owner.setPosition(-8.25,9.5,3.125);c.target.setPosition(c.owner.posX+v[0],c.owner.posY+v[1],c.owner.posZ+v[2]);Random r=new Random(31);vector(c.ai.getApproachPos(),approach(c.owner,c.target,r),"approach "+Arrays.toString(v));check(c.owner.rng.trace().equals("g,i11,g"),"approach exact call order");check(c.owner.rng.nextLong()==r.nextLong(),"approach RNG state"); }
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();eligibilityAndLifecycle();gatingAndRng();penalties();formula();
        System.out.println("PASS: "+checks+" assertions; complete production Maskman approach AI against native World/EntityCreature/Path/PathNavigateGround and look/home APIs. Timers, RNG order/state, visibility, home floors, path penalties, bounding boxes, approach math and disabled attacks. Controlled navigation/visibility outcomes, not live path search or gameplay.");
    }
}
