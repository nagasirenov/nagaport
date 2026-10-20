import net.minecraft.init.*;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.DamageSource;
import net.minecraft.potion.PotionEffect;
public class EffectExecution {
 static class Target extends EntityArmorStand {
  float damage,healed; DamageSource source;
  Target(){super(null);}
  public boolean attackEntityFrom(DamageSource s,float amount){damage+=amount;source=s;return true;}
  public void heal(float amount){healed+=amount;}
 }
 public static void main(String[] args){
  Bootstrap.register();int checks=0;
  for(int amp:new int[]{0,1,4,9,19,99,120}) {
   Target t=new Target();t.setHealth(10);
   PotionEffect poison=new PotionEffect(MobEffects.POISON,1200,amp);
   if(!poison.onUpdate(t)||poison.getDuration()!=1199||t.damage!=1||t.source!=DamageSource.MAGIC)throw new AssertionError("poison native tick");checks++;
   t.damage=0;t.setHealth(1);MobEffects.POISON.performEffect(t,amp);if(t.damage!=0)throw new AssertionError("poison floor");checks++;
   MobEffects.WITHER.performEffect(t,amp);if(t.damage!=1||t.source!=DamageSource.WITHER)throw new AssertionError("wither");checks++;
   t.setHealth(10);MobEffects.REGENERATION.performEffect(t,amp);if(t.healed!=1)throw new AssertionError("regen");checks++;
   t.healed=0;t.setHealth(t.getMaxHealth());MobEffects.REGENERATION.performEffect(t,amp);if(t.healed!=0)throw new AssertionError("regen cap");checks++;
   PotionEffect same=new PotionEffect(MobEffects.RESISTANCE,60,2);same.combine(new PotionEffect(MobEffects.RESISTANCE,120,2));if(same.getDuration()!=120||same.getAmplifier()!=2)throw new AssertionError("combine duration");checks++;
   same.combine(new PotionEffect(MobEffects.RESISTANCE,10,3));if(same.getDuration()!=10||same.getAmplifier()!=3)throw new AssertionError("combine amplifier");checks++;
  }
  System.out.println("PASS "+checks+" native effect update, duration decrement, poison floor, wither damage, regeneration cap and merge assertions; damage/heal boundary overridden, no gameplay claim.");
 }
}
