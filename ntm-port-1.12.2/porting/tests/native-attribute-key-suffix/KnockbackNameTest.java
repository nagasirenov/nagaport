import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import java.util.UUID;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
public class KnockbackNameTest {
 public static void main(String[] args){
  String old=yj.c.a(),now=SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName();int checks=0;
  if(!old.equals("generic.knockbackResistance")||!old.equals(now)||old.hashCode()!=now.hashCode())throw new AssertionError("exact original/native key");checks++;
  for(double amount:new double[]{100D,.25D,Double.POSITIVE_INFINITY,Double.NaN}){
   Multimap<String,AttributeModifier> map=HashMultimap.create();UUID id=new UUID(3,7);AttributeModifier modifier=new AttributeModifier(id,"Armor modifier",amount,0);map.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(),modifier);
   if(map.get(old).iterator().next()!=modifier||!modifier.getID().equals(id)||modifier.getOperation()!=0||Double.doubleToLongBits(modifier.getAmount())!=Double.doubleToLongBits(amount))throw new AssertionError("unchanged map lookup/UUID/op/amount");checks++;
  }
  System.out.println("PASS "+checks+" actual original/native knockback-name and map-key identity controls; existing 100D preserved, no modifier application or clamping");
 }
}
