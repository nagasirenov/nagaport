import java.lang.reflect.Field;
import java.util.*;
import com.google.common.base.Predicate;
import com.hbm.entity.mob.ai.EntityAINearestAttackableTargetNT;
import net.minecraft.entity.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.*;

/** Complete production AI against native World/Chunk query, EntityCreature, and AI suitability.
 * In-memory world allocation bypasses save/network startup, not entity selection logic. */
public class EntityTargetMinecraftTest {
    static class FixtureWorld extends World {
        Map<Long,Chunk> chunks;
        Scoreboard scoreboard;
        private FixtureWorld() { super(null,null,null,null,false); }
        protected IChunkProvider createChunkProvider() { throw new UnsupportedOperationException(); }
        protected boolean isChunkLoaded(int x,int z,boolean empty) { return chunks.containsKey(key(x,z)); }
        public Chunk getChunk(int x,int z) { return chunks.computeIfAbsent(key(x,z), k -> new Chunk(this,x,z)); }
        public Scoreboard getScoreboard() { return scoreboard; }
        static long key(int x,int z) { return ((long)x<<32) ^ (z&0xffffffffL); }
        static FixtureWorld make() throws Exception {
            Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
            FixtureWorld w=(FixtureWorld)((sun.misc.Unsafe)f.get(null)).allocateInstance(FixtureWorld.class);
            w.chunks=new HashMap<>(); w.scoreboard=new Scoreboard(); return w;
        }
        void add(Entity e) { getChunk(MathHelper.floor(e.posX/16),MathHelper.floor(e.posZ/16)).addEntity(e); }
    }
    static class Creature extends EntityCreature {
        Creature(FixtureWorld w,double x,double y,double z) { super(null); world=w; setPosition(x,y,z); }
        void seed(long n) { rand.setSeed(n); }
    }
    static class NonLiving extends Entity {
        NonLiving(FixtureWorld w) { super(null); world=w; setPosition(1,64,0); }
        protected void entityInit() {}
        protected void readEntityFromNBT(NBTTagCompound n) {}
        protected void writeEntityToNBT(NBTTagCompound n) {}
    }
    static void check(boolean b,String text) { if(!b) throw new AssertionError(text); }
    static EntityAINearestAttackableTargetNT ai(Creature owner,int chance,Predicate<Entity> predicate,double range) {
        return new EntityAINearestAttackableTargetNT(owner,Entity.class,chance,false,false,predicate,range);
    }
    static void chosen(EntityAINearestAttackableTargetNT ai,Creature owner,EntityLivingBase expected,String text) {
        owner.setAttackTarget(null);
        check(ai.shouldExecute(),text+" found");
        check(owner.getAttackTarget()==null,text+" shouldExecute does not assign");
        ai.startExecuting();
        check(owner.getAttackTarget()==null,text+" inherited start does not assign (legacy parity)");
        ai.resetTask();
        check(owner.getAttackTarget()==expected,text+" original resetTask assigns chosen target");
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        // The three vanilla mob consumers pass Predicate<Entity> for EntityLiving targets.
        // Compile and construct the native generic signature, without full unported mob classes.
        FixtureWorld signatureWorld=FixtureWorld.make();
        new net.minecraft.entity.ai.EntityAINearestAttackableTarget<EntityLiving>(
            new Creature(signatureWorld,0,64,0), EntityLiving.class, 0, true, true,
            (Predicate<Entity>)(e -> true));
        FixtureWorld w=FixtureWorld.make(); Creature owner=new Creature(w,0,64,0);
        Creature far=new Creature(w,8,64,0), near=new Creature(w,-2,64,0), rejected=new Creature(w,1,64,0);
        w.add(far);w.add(near);w.add(rejected);w.add(owner);w.add(new NonLiving(w));
        chosen(ai(owner,0,e -> e!=rejected,16),owner,near,"predicate rejection, nonliving/self rejection, nearest sorting");
        chosen(ai(owner,0,null,16),owner,rejected,"null predicate");
        check(!ai(owner,0,e -> false,16).shouldExecute(),"reject all");
        rejected.setHealth(0); chosen(ai(owner,0,null,16),owner,near,"native dead-target rejection");
        // Both signs on every axis, translated positive and negative chunk origins.
        for(int origin : new int[]{-33,33}) for(int axis=0;axis<3;axis++) for(int sign : new int[]{-1,1}) {
            w=FixtureWorld.make();owner=new Creature(w,origin,64,origin);
            double[] xyz={origin,64,origin};xyz[axis]+=sign*7;
            Creature candidate=new Creature(w,xyz[0],xyz[1],xyz[2]);w.add(candidate);
            chosen(ai(owner,0,null,8),owner,candidate,"symmetric growth axis="+axis+" sign="+sign+" origin="+origin);
            check(!ai(owner,0,null,4).shouldExecute(),"outside range rejected");
        }
        w=FixtureWorld.make();owner=new Creature(w,0,64,0);near=new Creature(w,1,64,0);w.add(near);
        final int[] calls={0};Predicate<Entity> counted=e->{calls[0]++;return true;};
        owner.seed(42);Random expected=new Random(42);EntityAINearestAttackableTargetNT randomAI=ai(owner,5,counted,8);
        int admitted=0;
        for(int i=0;i<1000;i++) { int previous=calls[0];boolean accept=expected.nextInt(5)==0;
            check(randomAI.shouldExecute()==accept,"targetChance decision");
            check(calls[0]-previous==(accept?1:0),"chance gates query/predicate");if(accept)admitted++;
        }
        check(admitted>0&&admitted<1000,"chance exercises both outcomes");
        check(owner.getRNG().nextLong()==expected.nextLong(),"exact chance RNG consumption");
        for(int chance:new int[]{0,-1}) { owner.seed(17);expected=new Random(17);check(ai(owner,chance,null,8).shouldExecute(),"nonpositive chance always searches");check(owner.getRNG().nextLong()==expected.nextLong(),"nonpositive chance no RNG draw"); }
        w.chunks.clear();check(!ai(owner,0,null,8).shouldExecute(),"empty world");
        System.out.println("PASS: complete production EntityAINearestAttackableTargetNT; real World/Chunk query and EntityCreature suitability; predicates, nearest sorting, six range directions across positive/negative chunks, dead/self/nonliving rejection, null/empty cases, 1000 RNG decisions, and original resetTask behavior. Not live-world lifecycle or full six-mob compilation.");
    }
}
