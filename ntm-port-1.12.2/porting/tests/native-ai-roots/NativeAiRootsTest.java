import java.lang.reflect.*;
import java.util.*;
import com.hbm.entity.mob.ai.EntityAIRangedAttackNT;
import com.hbm.entity.mob.ai.EntityAIFBIMelee;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class NativeAiRootsTest {
    static int checks;
    static void check(boolean ok,String message) { checks++;if(!ok)throw new AssertionError(message); }
    static void same(Object a,Object b,String message) { check(Objects.equals(a,b),message+": "+a+" != "+b); }
    static Field field(Object target,String name) throws Exception { Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f; }
    static int number(Object target,String name) throws Exception { return field(target,name).getInt(target); }
    static void put(Object target,String name,Object value) throws Exception { field(target,name).set(target,value); }
    static <T> T allocate(Class<T> type) throws Exception { Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return type.cast(((sun.misc.Unsafe)f.get(null)).allocateInstance(type)); }
    static class FixtureWorld extends World {
        FixtureWorld() { super(null,null,null,null,false); }
        protected IChunkProvider createChunkProvider() { throw new UnsupportedOperationException(); }
        protected boolean isChunkLoaded(int x,int z,boolean empty) { return false; }
    }
    static class TraceRandom extends Random {
        List<String> calls=new ArrayList<>();Float forced;
        TraceRandom(long seed) { super(seed); }
        public int nextInt(int n) { calls.add("i"+n);return super.nextInt(n); }
        public float nextFloat() { calls.add("f");return forced==null?super.nextFloat():forced; }
    }
    static class Senses extends EntitySenses {
        boolean visible=true;int calls;
        Senses(EntityLiving e) { super(e); }
        public boolean canSee(Entity e) { calls++;return visible; }
    }
    static class Navigator extends PathNavigateGround {
        Path result;int queries,moves,sets,clears;Entity requested;double requestedSpeed;boolean succeeds=true;
        Navigator(EntityLiving e,World w) { super(e,w); }
        public Path getPathToEntityLiving(Entity e) { queries++;requested=e;return result; }
        public boolean tryMoveToEntityLiving(Entity e,double s) { moves++;requested=e;requestedSpeed=s;return succeeds; }
        public boolean setPath(Path p,double s) { sets++;currentPath=p;requestedSpeed=s;return p!=null; }
        public void clearPath() { clears++;super.clearPath(); }
        void current(Path p) { currentPath=p; }
        String trace() { return queries+":"+moves+":"+sets+":"+clears+":"+Double.doubleToLongBits(requestedSpeed); }
    }
    static class Creature extends EntityCreature implements IRangedAttackMob {
        Navigator nav;Senses senses;TraceRandom rng;List<Integer> powers=new ArrayList<>();int attacks,swings;EntityLivingBase shotTarget;
        Creature(World w) { super(null);world=w;setPosition(0,64,0);nav=new Navigator(this,w);senses=new Senses(this);rng=new TraceRandom(31); }
        public PathNavigate getNavigator() { return nav==null?super.getNavigator():nav; }
        public EntitySenses getEntitySenses() { return senses==null?super.getEntitySenses():senses; }
        public Random getRNG() { return rng==null?super.getRNG():rng; }
        public void attackEntityWithRangedAttack(EntityLivingBase e,float p) { powers.add(Float.floatToIntBits(p));shotTarget=e; }
        public void setSwingingArms(boolean swinging) { throw new AssertionError("Original ranged AI has no swinging-arms event"); }
        public boolean attackEntityAsMob(Entity e) { attacks++;return true; }
        public void swingArm(EnumHand hand) { check(hand==EnumHand.MAIN_HAND,"original main hand");swings++; }
    }
    static class Player extends EntityPlayer {
        boolean alive=true,creative,spectator;
        Player() { super(null,new GameProfile(UUID.randomUUID(),"fixture")); }
        public float getEyeHeight() { return 1.62F; }
        public boolean isSpectator() { return spectator; }
        public boolean isCreative() { return creative; }
        public boolean isEntityAlive() { return alive; }
    }
    static Player player() throws Exception { Player p=allocate(Player.class);p.alive=true;p.width=.6F;p.height=1.8F;p.setPosition(4,64,0);return p; }
    static Path path(int x,int y,int z) { return new Path(new PathPoint[]{new PathPoint(x,y,z)}); }
    static EntityAIBase legacy(Creature owner,double speed,int min,int max,float radius) throws Exception {
        return (EntityAIBase)Class.forName("LegacyRangedOracle").getConstructor(IRangedAttackMob.class,double.class,int.class,int.class,float.class).newInstance(owner,speed,min,max,radius);
    }
    static void rangedDifferential() throws Exception {
        double[][] configs={{.5,60,80,15},{.5,5,5,50},{1,20,25,15}};
        double[] distances={0,.01,.1,1,1.5,14.999999,15,15.000001,16,49.99999,50,50.00001,100};
        for(double[] config:configs)for(int scenario=0;scenario<64;scenario++) {
            World w=allocate(FixtureWorld.class);Creature a=new Creature(w),b=new Creature(w),ta=new Creature(w),tb=new Creature(w);
            a.setAttackTarget(ta);b.setAttackTarget(tb);
            EntityAIBase nativeTask=new EntityAIRangedAttackNT(a,config[0],(int)config[1],(int)config[2],(float)config[3]);
            EntityAIBase oldTask=legacy(b,config[0],(int)config[1],(int)config[2],(float)config[3]);
            same(nativeTask.getMutexBits(),oldTask.getMutexBits(),"mutex");same(nativeTask.shouldExecute(),oldTask.shouldExecute(),"initial shouldExecute");
            Random schedule=new Random(scenario*101L+73);
            for(int tick=0;tick<320;tick++) {
                double x=distances[schedule.nextInt(distances.length)];double boxY=tick%13==0?61:64;
                ta.setPosition(x,64,0);tb.setPosition(x,64,0);
                ta.setEntityBoundingBox(new AxisAlignedBB(x-.3,boxY,-.3,x+.3,boxY+1.8,.3));tb.setEntityBoundingBox(ta.getEntityBoundingBox());
                a.senses.visible=b.senses.visible=schedule.nextInt(5)!=0;
                if(tick%37==0) { put(nativeTask,"attackTime",1);put(oldTask,"d",1); }
                if(tick%53==0) { put(nativeTask,"seeTime",19);put(oldTask,"f",19); }
                nativeTask.updateTask();oldTask.updateTask();
                same(number(nativeTask,"attackTime"),number(oldTask,"d"),"cooldown");same(number(nativeTask,"seeTime"),number(oldTask,"f"),"visibility streak");
                same(a.powers,b.powers,"exact power bits and firing history");same(a.nav.trace(),b.nav.trace(),"navigation events");same(a.senses.calls,b.senses.calls,"visibility calls");
                check(a.getLookHelper().getIsLooking()&&b.getLookHelper().getIsLooking(),"native look helper receives target");
                if(tick%71==0) {
                    a.setAttackTarget(null);b.setAttackTarget(null);a.nav.current(tick%2==0?null:path(4,64,0));b.nav.current(tick%2==0?null:path(4,64,0));
                    same(nativeTask.shouldContinueExecuting(),oldTask.shouldContinueExecuting(),"null-target continuation retains original stale-target/path rule");
                    nativeTask.resetTask();oldTask.resetTask();same(number(nativeTask,"attackTime"),number(oldTask,"d"),"reset cooldown");same(number(nativeTask,"seeTime"),number(oldTask,"f"),"reset visibility");
                    a.setAttackTarget(ta);b.setAttackTarget(tb);same(nativeTask.shouldExecute(),oldTask.shouldExecute(),"restart");
                }
            }
        }
    }
    static void rangedRegressions() throws Exception {
        World w=allocate(FixtureWorld.class);Creature a=new Creature(w),target=new Creature(w);a.setAttackTarget(target);target.setPosition(16,64,0);
        EntityAIRangedAttackNT task=new EntityAIRangedAttackNT(a,1,20,25,15);task.shouldExecute();put(task,"attackTime",1);task.updateTask();check(a.powers.isEmpty()&&number(task,"attackTime")==0,"outside-radius zero tick must not shoot or reset");task.updateTask();check(number(task,"attackTime")==25,"next negative tick restarts interval using unclamped ratio");
        EntityAIAttackRanged unsuitable=new EntityAIAttackRanged(a,1,20,25,15);unsuitable.shouldExecute();put(unsuitable,"rangedAttackTime",1);unsuitable.updateTask();check(a.powers.size()==1,"native rename demonstrably shoots beyond original radius");
        a.powers.clear();target.setPosition(15,64,0);put(task,"attackTime",1);task.updateTask();check(a.powers.size()==1&&a.powers.get(0)==Float.floatToIntBits(1),"exact radius inclusive");
        a.powers.clear();target.setPosition(0,64,0);put(task,"attackTime",1);task.updateTask();check(a.powers.get(0)==Float.floatToIntBits(.1F),"minimum power clamp");
        a.senses.visible=false;put(task,"attackTime",1);task.updateTask();check(number(task,"attackTime")==0,"hidden zero tick preserves restart delay");
        target.setHealth(0);check(task.shouldExecute(),"original ranged acquisition does not add an alive test");
        a.setAttackTarget(null);check(!task.shouldExecute(),"null acquisition");
        // Preserve the original comparison behavior even for invalid floating-point distances.
        a.setAttackTarget(target);task.shouldExecute();target.setPosition(Double.NaN,64,0);put(task,"seeTime",19);put(task,"attackTime",1);task.updateTask();check(number(task,"seeTime")==0,"hidden NaN visibility still clears streak");
    }
    static class MeleeCase {
        FixtureWorld w;Creature owner;Player target;EntityAIFBIMelee ai;
        MeleeCase() throws Exception { w=allocate(FixtureWorld.class);owner=new Creature(w);target=player();owner.setAttackTarget(target);ai=new EntityAIFBIMelee(owner); }
    }
    static void melee() throws Exception {
        MeleeCase c=new MeleeCase();check(c.ai.getMutexBits()==3,"FBI mutex");c.owner.setAttackTarget(null);check(!c.ai.shouldExecute(),"null melee target");c.owner.setAttackTarget(new Creature(c.w));check(!c.ai.shouldExecute(),"FBI rejects non-player before any path or RNG");check(c.owner.nav.queries==0&&c.owner.rng.calls.isEmpty(),"no rejected-target work");
        c.owner.setAttackTarget(c.target);c.target.alive=false;check(!c.ai.shouldExecute(),"dead melee target");c.target.alive=true;check(!c.ai.shouldExecute(),"initial null path");int timer=4+new Random(31).nextInt(7);check(number(c.ai,"pathTimer")==timer,"Forge initial start throttle");
        for(int i=1;i<timer;i++)check(c.ai.shouldExecute(),"original Forge cached true even with null path");check(c.owner.nav.queries==1,"cached path not recomputed");c.owner.nav.result=path(4,64,0);check(c.ai.shouldExecute(),"retry finds path");c.ai.startExecuting();check(c.owner.nav.getPath()==c.owner.nav.result&&number(c.ai,"pathTimer")==0,"start retains actual path and clears timer");same(c.owner.nav.requestedSpeed,1D,"FBI fixed speed");
        c.target.creative=true;c.target.spectator=true;check(c.ai.shouldContinueExecuting(),"original continuation has no new creative/spectator veto");c.ai.resetTask();check(c.owner.nav.noPath()&&c.owner.getAttackTarget()==c.target,"reset only clears path and preserves target");
        c.owner.setHomePosAndDistance(new BlockPos(0,64,0),2);c.target.setPosition(-1.01,64,0);check(!c.ai.shouldContinueExecuting(),"negative home floor excluded boundary");c.target.setPosition(-.01,64.99,.99);check(c.ai.shouldContinueExecuting(),"native block position preserves negative floor");
        Path[] paths={null,new Path(new PathPoint[0]),path(3,64,0),path(4,64,0)};
        for(int i=0;i<paths.length;i++) { c=new MeleeCase();put(c.ai,"failedPathFindingPenalty",20);c.owner.nav.current(paths[i]);int expected=20+4+new Random(31).nextInt(7);c.ai.updateTask();check(number(c.ai,"pathTimer")==expected,"apply old penalty first");check(number(c.ai,"failedPathFindingPenalty")==(i==3?0:30),"Forge endpoint penalty");check(c.owner.senses.calls==0,"long memory short-circuits visibility");same(c.owner.rng.calls.toString(),"[i7]","update RNG call sequence"); }
        for(double distance:new double[]{16,16.00001,32,32.00001})for(boolean success:new boolean[]{true,false}) { c=new MeleeCase();c.target.setPosition(distance,64,0);c.owner.nav.succeeds=success;c.ai.updateTask();int extra=distance>32?10:distance>16?5:0;check(number(c.ai,"pathTimer")==4+new Random(31).nextInt(7)+extra+(success?0:15),"distance and failure penalties"); }
        for(float chance:new float[]{.049F,.05F}) { c=new MeleeCase();put(c.ai,"lastX",4D);put(c.ai,"lastY",64D);put(c.ai,"lastZ",0D);c.owner.rng.forced=chance;c.ai.updateTask();check(c.owner.nav.moves==(chance<.05F?1:0),"strict refresh random threshold");same(c.owner.rng.calls.toString(),chance<.05F?"[f, i7]":"[f]","stationary RNG order"); }
        c=new MeleeCase();c.target.setPosition(1,64,0);c.owner.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(Items.STICK));for(int i=0;i<50;i++)c.ai.updateTask();check(c.owner.attacks==50&&c.owner.swings==50&&number(c.ai,"attackTick")==20,"retain original <=20 every-close-update cadence");
        c.owner.setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);c.ai.updateTask();check(c.owner.attacks==51&&c.owner.swings==50,"EMPTY maps old null hand only, not attack cancellation");put(c.ai,"attackTick",22);c.ai.updateTask();check(c.owner.attacks==51&&number(c.ai,"attackTick")==21,"greater-than-20 attack guard");c.ai.updateTask();check(c.owner.attacks==52,"counter reaches20 then attacks");
        c=new MeleeCase();c.target.setPosition(1,64,0);EntityAIAttackMelee unsuitable=new EntityAIAttackMelee(c.owner,1,true);check(unsuitable.shouldExecute(),"native pathless-in-range start differs from original null-path rejection");
        c=new MeleeCase();c.target.setEntityBoundingBox(new AxisAlignedBB(3.7,30,-.3,4.3,31.8,.3));c.ai.updateTask();check(field(c.ai,"lastY").getDouble(c.ai)==30,"melee caches box minY not target posY");check(number(c.ai,"pathTimer")==4+new Random(31).nextInt(7)+10,"melee distance uses box minY");
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();rangedDifferential();rangedRegressions();melee();
        System.out.println("PASS: "+checks+" actual native assertions; complete two production AI tasks, original ranged bytecode differential, original Forge melee cadence/target/penalty tests. Owner entities and real path search are not compiled/exercised here.");
    }
}
