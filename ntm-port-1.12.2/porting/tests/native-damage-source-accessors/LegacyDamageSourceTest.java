package damageproof;
import java.lang.reflect.*;
import sun.misc.Unsafe;
/** Runs the actual pinned, obfuscated 1.7.10 client classes, not a reconstructed API. */
public final class LegacyDamageSourceTest {
 static int checks;
 static void ok(boolean condition,String label){checks++;if(!condition)throw new AssertionError(label);}
 public static void main(String[] args)throws Exception {
  Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);Unsafe u=(Unsafe)f.get(null);
  Class<?> entity=Class.forName("sa"),item=Class.forName("xk"),base=Class.forName("ro"),direct=Class.forName("rp"),indirect=Class.forName("rq");
  Object projectile=u.allocateInstance(item),shooter=u.allocateInstance(item);
  Method immediate=base.getMethod("i"),truth=base.getMethod("j");
  Constructor<?> plainConstructor=base.getDeclaredConstructor(String.class);plainConstructor.setAccessible(true);Object plain=plainConstructor.newInstance("probe");
  ok(truth.invoke(plain)==null,"base true source null");ok(immediate.invoke(plain)==null,"base immediate source null");
  for(Object attacker:new Object[]{shooter,null}){
   Object source=direct.getConstructor(String.class,entity).newInstance("probe",attacker);
   ok(truth.invoke(source)==attacker,"direct source identity");ok(immediate.invoke(source)==attacker,"base immediate virtual dispatch into direct source");
  }
  for(Object p:new Object[]{projectile,null})for(Object s:new Object[]{shooter,null}){
   Object source=indirect.getConstructor(String.class,entity,entity).newInstance("probe",p,s);
   ok(immediate.invoke(source)==p,"indirect projectile identity");ok(truth.invoke(source)==s,"indirect shooter identity, including null without projectile fallback");
  }
  System.out.println("PASS "+checks+" original-client native runtime source identity/null/virtual-dispatch assertions");
 }
}
