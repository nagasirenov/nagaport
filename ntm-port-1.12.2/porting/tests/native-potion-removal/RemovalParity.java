import net.minecraft.init.*;
import net.minecraft.potion.*;
import net.minecraft.entity.item.EntityArmorStand;
/** Real original/native maps and remove methods; lifecycle callbacks are observation boundaries. */
public final class RemovalParity {
 static int checks;
 static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
 static class Old extends sv {
  int calls;rw removed;
  Old(){super(null);}
  public add be(){return null;}public add q(int i){return null;}public void c(int i,add s){}public add[] ak(){return new add[5];}
  protected void a(rw e){}protected void a(rw e,boolean b){}protected void b(rw e){calls++;removed=e;}
 }
 static class Native extends EntityArmorStand {
  int calls;PotionEffect removed;
  Native(){super(null);}
  protected void onNewPotionEffect(PotionEffect e){}protected void onChangedPotionEffect(PotionEffect e,boolean b){}
  protected void onFinishedPotionEffect(PotionEffect e){calls++;removed=e;}
 }
 public static void main(String[] args){
  Bootstrap.register();
  rv[] old={rv.f,rv.s,rv.t};Potion[] now={MobEffects.MINING_FATIGUE,MobEffects.HUNGER,MobEffects.WEAKNESS};
  String[] names={"potion.digSlowDown","potion.hunger","potion.weakness"};String[] keys={"minecraft:mining_fatigue","minecraft:hunger","minecraft:weakness"};int[] ids={4,17,18};
  for(int i=0;i<3;i++){
   check(old[i].H==ids[i]&&rv.a[ids[i]]==old[i],"old registry singleton");
   check(old[i].a().equals(names[i]),"old effect name");
   check(Potion.getIdFromPotion(now[i])==ids[i]&&Potion.getPotionById(ids[i])==now[i],"native singleton");
   check(Potion.REGISTRY.getNameForObject(now[i]).toString().equals(keys[i]),"native name");
   for(int amp:new int[]{0,2,19,200})for(int duration:new int[]{0,60,1200,Integer.MAX_VALUE}){
    Old a=new Old();Native b=new Native();rw oe=new rw(ids[i],duration,amp);PotionEffect ne=new PotionEffect(now[i],duration,amp);
    a.c(oe);b.addPotionEffect(ne);a.c(new rw(rv.m.H,120,2));b.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE,120,2));
    check(a.a(old[i])&&b.isPotionActive(now[i]),"present before removal");
    a.m(ids[i]);b.removePotionEffect(now[i]);
    check(!a.a(old[i])&&!b.isPotionActive(now[i]),"selected removed");check(a.a(rv.m)&&b.isPotionActive(MobEffects.RESISTANCE),"unrelated retained");
    check(a.calls==1&&b.calls==1&&a.removed==oe&&b.removed==ne,"callback count and identity");
    check(oe.b()==duration&&ne.getDuration()==duration&&oe.c()==amp&&ne.getAmplifier()==amp,"arguments unchanged");
    a.m(ids[i]);b.removePotionEffect(now[i]);check(a.calls==1&&b.calls==1,"absent repeated removal no callback");
   }
  }
  System.out.println("PASS "+checks+" original/native identity and actual removal-map assertions; lifecycle callbacks observed, not simulated gameplay or effect-formula parity.");
 }
}
