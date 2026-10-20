import java.util.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.*;
/** Uses real 1.7.10 obfuscated and native 1.12.2 classes together; no substitutes. */
public class AttributeParity {
 static int checks;
 static void ok(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void eq(double a,double b,String s){ok(Double.doubleToLongBits(a)==Double.doubleToLongBits(b),s+": "+a+" / "+b);}
 public static void main(String[] args)throws Exception {
  String[] oldFields={"a","b","c","d","e"};
  IAttribute[] current={SharedMonsterAttributes.MAX_HEALTH,SharedMonsterAttributes.FOLLOW_RANGE,SharedMonsterAttributes.KNOCKBACK_RESISTANCE,SharedMonsterAttributes.MOVEMENT_SPEED,SharedMonsterAttributes.ATTACK_DAMAGE};
  double[] values={Double.NEGATIVE_INFINITY,-100,-1,-0.0,0,Float.MIN_VALUE,.25,.5,1,2,10,32,40,50,60,100,1024,2048,15000,Double.MAX_VALUE,Double.POSITIVE_INFINITY,Double.NaN};
  for(int k=0;k<5;k++) {
   th old=(th)yj.class.getField(oldFields[k]).get(null); IAttribute n=current[k];
   ok(old.a().equals(n.getName()),"name"); eq(old.b(),n.getDefaultValue(),"default"); ok(old.c()==n.getShouldWatch(),"watch");
   System.out.println(oldFields[k]+" "+n.getName()+" default="+n.getDefaultValue()+" watch="+n.getShouldWatch()+" oldClamp=["+old.a(Double.NEGATIVE_INFINITY)+","+old.a(Double.POSITIVE_INFINITY)+"] nativeClamp=["+n.clampValue(Double.NEGATIVE_INFINITY)+","+n.clampValue(Double.POSITIVE_INFINITY)+"]");
   if(k!=1 && k!=2){ok(old.a(Double.POSITIVE_INFINITY)!=n.clampValue(Double.POSITIVE_INFINITY),"excluded cap mismatch");continue;}
   for(double v:values)eq(old.a(v),n.clampValue(v),"clamp");
   tq om=new tq(); AttributeMap nm=new AttributeMap(); ti oi=om.b(old); IAttributeInstance ni=nm.registerAttribute(n);
   ok(om.a(old)==oi && om.a(old.a())==oi,"old lookup");ok(nm.getAttributeInstance(n)==ni && nm.getAttributeInstanceByName(n.getName())==ni,"native lookup");
   eq(oi.b(),ni.getBaseValue(),"initial base");
   for(double v:values){oi.a(v);ni.setBaseValue(v);eq(oi.b(),ni.getBaseValue(),"stored base");eq(oi.e(),ni.getAttributeValue(),"computed");}
   for(double base:new double[]{0,.5,1,10,32,100,3000})for(int op=0;op<3;op++)for(double amount:new double[]{-.1,.25,.5,1,100}){
    UUID id=new UUID(17,31);tj a=new tj(id,"NTM probe",amount,op);AttributeModifier b=new AttributeModifier(id,"NTM probe",amount,op);
    oi.a(base);ni.setBaseValue(base);oi.a(a);ni.applyModifier(b);ok(oi.a(id)==a && ni.getModifier(id)==b,"modifier lookup");eq(oi.e(),ni.getAttributeValue(),"modifier result");
    oi.b(a);ni.removeModifier(b);ok(oi.a(id)==null && ni.getModifier(id)==null,"modifier removal");eq(oi.e(),ni.getAttributeValue(),"restore");
   }
  }
  System.out.println("PASS "+checks+" real attribute checks");
 }
}
