import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
public class ScopeBoundaryTest {
 public static void main(String[]args)throws Exception {
  Bootstrap.register();Field raw=ReflectionHelper.findField(ItemStack.class,"stackSize","field_77994_a");
  // The exact constructor path used by SerializableRecipe.readItemStack.
  ItemStack configured=new ItemStack(Items.IRON_INGOT,1,65536);
  ItemStack output=configured.copy(); // BlastFurnaceRecipes.getOutput returns copy.
  if(raw.getInt(output)!=1||!output.isEmpty())throw new AssertionError("positive-invalid recipe result");
  // With valid matching inputs and fuel, CP80 reaches these two decision lines:
  // if(output == null) return false; if(slots[3] == null) return true;
  boolean cp80Accepts=output!=null;
  boolean migratedAccepts=output!=null&&!output.isEmpty();
  if(!cp80Accepts||migratedAccepts)throw new AssertionError("scope counterexample");
  NBTTagCompound nbt=new NBTTagCompound();nbt.setString("id","minecraft:iron_ingot");nbt.setByte("Count",(byte)1);nbt.setShort("Damage",(short)65536);
  ItemStack loaded=new ItemStack(nbt);
  if(loaded.isEmpty()||loaded.getItemDamage()!=0)throw new AssertionError("NBT short bounds");
  ItemStack inPlace=new ItemStack(Items.COAL,1);inPlace.setItemDamage(65536);
  System.out.println("Recipe meta65536: raw="+raw.getInt(output)+", empty="+output.isEmpty()+", CP80 vacant-output accepts="+cp80Accepts+", migrated accepts="+migratedAccepts);
  System.out.println("NBT Damage short65536 loads damage="+loaded.getItemDamage()+", empty="+loaded.isEmpty());
  System.out.println("Public in-place setItemDamage65536: raw="+raw.getInt(inPlace)+", empty="+inPlace.isEmpty()+", cached getItem retainsCoal="+(inPlace.getItem()==Items.COAL));
  ItemStack repaired=new ItemStack(Items.IRON_INGOT,64,65536);repaired.setItemDamage(0);
  if(repaired.isEmpty()||repaired.getCount()!=0||raw.getInt(repaired)!=64)throw new AssertionError("public stale repair");
  int oldSum=raw.getInt(repaired)+3,nativeSum=repaired.getCount()+3;
  boolean oldCapacity=oldSum<=repaired.getMaxStackSize(),nativeCapacity=nativeSum<=repaired.getMaxStackSize();
  repaired.grow(3);
  if(oldCapacity||!nativeCapacity||raw.getInt(repaired)!=67)throw new AssertionError("stale capacity counterexample");
  System.out.println("Public repaired output: old accepts="+oldCapacity+", native accepts="+nativeCapacity+", grown raw="+raw.getInt(repaired));
  System.out.println("PASS bounded native scope counterexample; no reflective writes, no production modifications");
 }
}
