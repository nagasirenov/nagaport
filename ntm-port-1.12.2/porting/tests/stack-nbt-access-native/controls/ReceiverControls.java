package nbt.controls;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
final class ReceiverControls {
    ItemStack field;
    ItemStack obtain() { return field; }
    static final class OtherStack { NBTTagCompound stackTagCompound; }
    void accepted(ItemStack stack, ItemStack[] slots, NBTTagCompound tag) {
        NBTTagCompound a = stack.stackTagCompound; // EXPECT:read
        stack.stackTagCompound = tag; // EXPECT:write
        slots[0].stackTagCompound = null; // EXPECT:write
        NBTTagCompound b = obtain().stackTagCompound; // EXPECT:read
        this.field.stackTagCompound = stack.stackTagCompound; // EXPECT:write,read
        (stack.stackTagCompound) = tag; // EXPECT:write
        NBTTagCompound c = (stack.stackTagCompound); // EXPECT:read
        String literal = "stack.stackTagCompound"; // EXPECT:none
        // stack.stackTagCompound = tag; // EXPECT:none
    }
    NBTTagCompound rejected(ItemStack stack, OtherStack other, NBTTagCompound tag) {
        other.stackTagCompound = tag; // EXPECT:reject
        NBTTagCompound a = other.stackTagCompound; // EXPECT:reject
        a = (stack.stackTagCompound = tag); // EXPECT:reject
        a = ((stack.stackTagCompound) = tag); // EXPECT:reject
        return stack.stackTagCompound = tag; // EXPECT:reject
    }
    static final class Shadow {
        static final class ItemStack { NBTTagCompound stackTagCompound; }
        void shadow(ItemStack stack, NBTTagCompound tag) {
            stack.stackTagCompound = tag; // EXPECT:reject
            NBTTagCompound a = stack.stackTagCompound; // EXPECT:reject
        }
    }
}
