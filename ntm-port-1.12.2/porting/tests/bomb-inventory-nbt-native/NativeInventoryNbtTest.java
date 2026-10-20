import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
public class NativeInventoryNbtTest {
    static int checks;
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); checks++; }
    static NBTTagCompound data(String id, int count, int damage) {
        NBTTagCompound n = new NBTTagCompound(); n.setString("id", id);
        n.setByte("Count", (byte)count); n.setShort("Damage", (short)damage); return n;
    }
    static class HookItem extends Item {
        int hooks, inits; NBTTagCompound received; Provider provider;
        public boolean updateItemStackNBT(NBTTagCompound n) { hooks++; n.getCompoundTag("tag").setBoolean("hook", true); return true; }
        public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound caps) { inits++; received=caps; provider=new Provider(); return provider; }
    }
    static class Provider implements net.minecraftforge.common.capabilities.ICapabilitySerializable<NBTTagCompound> {
        NBTTagCompound state = new NBTTagCompound(); int reads;
        public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> cap, net.minecraft.util.EnumFacing facing) { return false; }
        public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, net.minecraft.util.EnumFacing facing) { return null; }
        public NBTTagCompound serializeNBT() { return state; }
        public void deserializeNBT(NBTTagCompound n) { state=n; reads++; }
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        check(NullableInventoryStackNbt.read(new NBTTagCompound())==null,"missing id vacancy");
        check(NullableInventoryStackNbt.read(data("nonexistent:absent",1,0))==null,"invalid registry vacancy");
        check(NullableInventoryStackNbt.read(data("minecraft:air",1,0))==null,"air vacancy");
        NBTTagCompound numeric=data("minecraft:iron_ingot",1,0); numeric.setShort("id",(short)265);
        check(NullableInventoryStackNbt.read(numeric)==null,"numeric old world ID not imported");
        java.lang.reflect.Field countField=ItemStack.class.getDeclaredField("stackSize"); countField.setAccessible(true);
        java.lang.reflect.Field damageField=ItemStack.class.getDeclaredField("itemDamage"); damageField.setAccessible(true);
        for (int count : new int[]{-128,-1,0,1,64,127}) for (int damage : new int[]{-32768,-1,0,12,32767}) {
            NBTTagCompound n=data("minecraft:iron_ingot",count,damage);
            NBTTagCompound tag=new NBTTagCompound(); tag.setString("unknown","retained"); n.setTag("tag",tag);
            ItemStack result=NullableInventoryStackNbt.read(n);
            check(result!=null && result!=ItemStack.EMPTY,"registered count remains separate non-null stack");
            check(countField.getInt(result)==count,"signed raw count preserved");
            check(damageField.getInt(result)==Math.max(0,damage),"damage clamp matches old loader");
            check(result.getTagCompound()==tag,"tag alias retained");
            check(result.isEmpty()==(count<=0),"native empty state remains native");
            if (count>0) {
                check(result.getItem()==Items.IRON_INGOT && result.getCount()==count,"public native populated stack");
                ItemStack again=NullableInventoryStackNbt.read(result.writeToNBT(new NBTTagCompound()));
                check(again.getCount()==count && again.getTagCompound().equals(tag),"native serialization roundtrip");
            }
        }
        HookItem item=new HookItem(); item.setRegistryName("nbtprobe","hook");
        net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item);
        NBTTagCompound n=data("nbtprobe:hook",3,4),tag=new NBTTagCompound(),caps=new NBTTagCompound();
        NBTTagCompound parent=new NBTTagCompound(); parent.setInteger("test",123); caps.setTag("Parent",parent); n.setTag("tag",tag); n.setTag("ForgeCaps",caps);
        ItemStack loaded=NullableInventoryStackNbt.read(n);
        check(loaded.getItem()==item,"registered custom item");
        check(item.hooks==1 && loaded.getTagCompound().getBoolean("hook"),"native item NBT hook executed once");
        check(item.inits==1 && item.received==caps,"native Forge capability NBT forwarded unchanged once");
        check(item.provider.reads==1 && item.provider.state.getInteger("test")==123,"native dispatcher deserializes provider state");
        NBTTagCompound saved=loaded.writeToNBT(new NBTTagCompound());
        check(saved.getCompoundTag("ForgeCaps").getCompoundTag("Parent").getInteger("test")==123,"native provider state serializes");
        ItemStack reloaded=NullableInventoryStackNbt.read(saved);
        check(item.provider.reads==1 && item.provider.state.getInteger("test")==123 && reloaded.getCount()==3,"capability roundtrip through production loader");
        // This is the selected production loading assignment, not a pretend Minecraft inventory.
        ItemStack[] slots=new ItemStack[2];
        slots[0]=NullableInventoryStackNbt.read(data("nonexistent:absent",1,0));
        slots[1]=NullableInventoryStackNbt.read(data("minecraft:iron_ingot",1,0));
        check(slots[0]==null && slots[1]!=null,"owned-array null branch remains distinguishable");
        System.out.println("PASS native production nullable-inventory loader: "+checks+" checks");
    }
}
