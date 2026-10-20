import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class NativeEntityItemReadAudit {
    private static int checks;
    private static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        checks++;
    }
    public static void main(String[] args) {
        Bootstrap.register();
        EntityItem entity = new EntityItem(null);
        check(entity.getItem() == ItemStack.EMPTY, "native newly registered data defaults to EMPTY");
        ItemStack live = new ItemStack(Items.DIAMOND, 3, 7);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("audit", 42);
        live.setTagCompound(tag);
        entity.setItem(live);
        check(entity.getItem() == live, "genuine data manager returns live alias");
        check(entity.getItem().getTagCompound() == tag, "NBT alias retained");
        entity.getItem().setCount(0);
        check(live.getCount() == 0, "mutation reaches stored stack");
        check(entity.getItem() == live, "zero count still same object, not singleton EMPTY");
        check(entity.getItem().getItem() == Items.AIR, "zero count hides item identity");
        check(entity.getItem().getTagCompound() == tag, "zero count retains NBT reference");
        live.setCount(3);
        check(entity.getItem().getItem() == Items.DIAMOND, "revival restores item identity");
        entity.setItem(ItemStack.EMPTY);
        check(entity.getItem() == ItemStack.EMPTY, "explicit EMPTY retained");
        entity.setItem(null);
        check(entity.getItem() == null, "explicit null is returned unchanged, not default EMPTY");
        System.out.println("PASS: " + checks + " genuine native EntityItem/EntityDataManager boundary checks (no world ticking)");
    }
}
