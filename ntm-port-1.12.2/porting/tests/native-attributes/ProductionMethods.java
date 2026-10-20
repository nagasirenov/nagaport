import java.lang.reflect.Field;
import java.util.Random;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.ai.attributes.*;
/** Constructor-free real entity fixture: only fields read/written by extracted methods are initialized. */
public class ProductionMethods {
 static void eq(double a,double b){if(Math.abs(a-b)>1e-12)throw new AssertionError(a+" != "+b);}
 public static void main(String[] args)throws Exception {
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)uf.get(null);
  EntityLivingBase e=(EntityLivingBase)u.allocateInstance(EntityArmorStand.class);
  Field rand=Entity.class.getDeclaredField("rand");rand.setAccessible(true);rand.set(e,new Random(49));
  AttributeMap map=new AttributeMap();IAttributeInstance a=map.registerAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
  Field attributes=EntityLivingBase.class.getDeclaredField("attributeMap");attributes.setAccessible(true);attributes.set(e,map);
  a.setBaseValue(1);e.motionX=2;e.motionY=.1;e.motionZ=4;
  ExtractedMethods.knockBack(e,null,3,3,4,2);eq(e.motionX,2);eq(e.motionY,.1);eq(e.motionZ,4);
  ExtractedMethods.setBeenAttacked(e);if(e.velocityChanged)throw new AssertionError("resistance");
  a.setBaseValue(0);ExtractedMethods.setBeenAttacked(e);if(!e.velocityChanged)throw new AssertionError("no resistance");
  ExtractedMethods.knockBack(e,null,3,3,4,2);eq(e.motionX,.52);eq(e.motionY,.4);eq(e.motionZ,1.36);if(!e.isAirBorne)throw new AssertionError("airborne");
  System.out.println("PASS two complete extracted production methods on real native EntityArmorStand with real attribute map (constructor bypass; no world/gameplay)");
 }
}
