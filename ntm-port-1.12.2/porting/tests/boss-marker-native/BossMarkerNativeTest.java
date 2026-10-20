import com.hbm.entity.boss.INtmBossDisplay;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.boss.*;
import net.minecraft.init.Bootstrap;
import java.lang.reflect.Field;
public final class BossMarkerNativeTest {
    static int n;
    static void check(boolean pass,String what){n++;if(!pass)throw new AssertionError(what);}
    static class Marked extends EntityMob implements INtmBossDisplay {Marked(){super(null);} protected void initEntityAI(){} public net.minecraft.scoreboard.Team getTeam(){return null;}}
    static class Child extends Marked {}
    static class Unmarked extends EntityMob {Unmarked(){super(null);}protected void initEntityAI(){}public boolean isNonBoss(){return false;}}
    static Object allocate(Class<?> cls)throws Exception{Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return ((sun.misc.Unsafe)f.get(null)).allocateInstance(cls);}
    public static void main(String[] args)throws Exception{
        Bootstrap.register();Marked mob=new Marked();mob.setCustomNameTag("Original display");mob.setHealth(7.5F);
        INtmBossDisplay display=mob;
        check(display.getHealth()==7.5F,"immediate health");check(display.getMaxHealth()==mob.getMaxHealth(),"native max health");
        check(display.getDisplayName().getUnformattedText().equals("Original display"),"native display name");
        check(INtmBossDisplay.isDisplayBoss(mob),"original NTM classification");check(INtmBossDisplay.isDisplayBoss(new Child()),"inherited marker");
        check(mob.isNonBoss(),"marker must not alter native portal predicate");check(Marked.class.getMethod("isNonBoss").getDeclaringClass()==Entity.class,"no portal override");
        check(INtmBossDisplay.isDisplayBoss((Entity)allocate(EntityDragon.class)),"original vanilla dragon");
        check(INtmBossDisplay.isDisplayBoss((Entity)allocate(EntityWither.class)),"original vanilla wither");
        check(!INtmBossDisplay.isDisplayBoss(new Unmarked()),"do not infer display class from native gameplay flag");
        check(!INtmBossDisplay.isDisplayBoss(null),"null remains unclassified");
        System.out.println("PASS "+n+" native display marker/value/thermal-classification checks; no HUD activation, full boss AI, portal travel or gameplay certification.");
    }
}
