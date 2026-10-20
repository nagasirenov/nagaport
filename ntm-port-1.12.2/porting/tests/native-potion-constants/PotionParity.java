import java.nio.file.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.init.*;
import net.minecraft.potion.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.ai.attributes.*;
public class PotionParity {
 static int checks;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception {
  Bootstrap.register();
  URLClassLoader loader=new URLClassLoader(new URL[]{new java.io.File(args[1]).toURI().toURL()},PotionParity.class.getClassLoader());
  Class<?> old=loader.loadClass("rv"), effect=loader.loadClass("rw"),mod=loader.loadClass("tj");
  for(String line:Files.readAllLines(Paths.get(args[0]))) {
   String[] r=line.split("\t"); int id=Integer.parseInt(r[3]);Object before=old.getField(r[4]).get(null);Potion now=(Potion)MobEffects.class.getField(r[2]).get(null);
   check(old.getMethod("a").invoke(before).equals(r[5]),"old translation identity "+line); check(Potion.REGISTRY.getNameForObject(now).toString().equals(r[6]),"native registry identity "+line);
   check(old.getField("H").getInt(before)==id,"old id "+line); check(Potion.getIdFromPotion(now)==id,"native id "+line);
   check(Potion.getPotionById(id)==now,"native singleton");check(((Object[])old.getField("a").get(null))[id]==before,"old singleton");
   check(old.getMethod("b").invoke(before).equals(now.isInstant()),"instant");check(old.getMethod("f").invoke(before).equals(now.isBadEffect()),"bad effect");
   check(old.getMethod("j").invoke(before).equals(now.getLiquidColor()),"color "+line);
   Map<?,?> om=(Map<?,?>)old.getMethod("k").invoke(before);Map<IAttribute,AttributeModifier> nm=now.getAttributeModifierMap();check(om.size()==nm.size(),"modifier count "+line);
   for(Object ov:om.values()) {
    UUID uuid=(UUID)mod.getMethod("a").invoke(ov); AttributeModifier nv=null;for(AttributeModifier candidate:nm.values())if(candidate.getID().equals(uuid))nv=candidate;
    check(nv!=null,"uuid "+line);check(mod.getMethod("c").invoke(ov).equals(nv.getOperation()),"operation");check(mod.getMethod("d").invoke(ov).equals(nv.getAmount()),"amount");
    for(int amp:new int[]{0,1,2,4,9,19,99,120})check(old.getMethod("a",int.class,mod).invoke(before,amp,ov).equals(now.getAttributeModifierAmount(amp,nv)),"scaled modifier");
   }
   for(int amp:new int[]{0,1,2,4,9,19,99,120})for(int duration:new int[]{0,1,19,20,49,50,99,100,600,3600,216000,Integer.MAX_VALUE}) {
    check(old.getMethod("a",int.class,int.class).invoke(before,duration,amp).equals(now.isReady(duration,amp)),"tick readiness "+line);
    Object oe=effect.getConstructor(int.class,int.class,int.class,boolean.class).newInstance(id,duration,amp,false);
    PotionEffect ne=new PotionEffect(now,duration,amp,false,true);
    check(effect.getMethod("a").invoke(oe).equals(Potion.getIdFromPotion(ne.getPotion())),"effect id");check(effect.getMethod("b").invoke(oe).equals(ne.getDuration()),"duration");check(effect.getMethod("c").invoke(oe).equals(ne.getAmplifier()),"amplifier");check(effect.getMethod("e").invoke(oe).equals(ne.getIsAmbient()),"ambient");
    PotionEffect restored=PotionEffect.readCustomPotionEffectFromNBT(ne.writeCustomPotionEffectToNBT(new NBTTagCompound()));
    check(restored.getPotion()==now && restored.getDuration()==duration && restored.getAmplifier()==amp,"actual native NBT roundtrip");
   }
  }
  Object strength=old.getField("g").get(null),weakness=old.getField("t").get(null);
  check(((Double)old.getMethod("a",int.class,mod).invoke(strength,0,null))==1.3,"old strength");check(MobEffects.STRENGTH.getAttributeModifierAmount(0,null)==3,"native strength differs");
  check(((Double)old.getMethod("a",int.class,mod).invoke(weakness,0,null))==-0.5,"old weakness");check(MobEffects.WEAKNESS.getAttributeModifierAmount(0,null)==-4,"native weakness differs");
  System.out.println("PASS "+checks+" real original/native registry, attribute, readiness, constructor and native NBT assertions; strength and weakness mismatch guards.");
 }
}
