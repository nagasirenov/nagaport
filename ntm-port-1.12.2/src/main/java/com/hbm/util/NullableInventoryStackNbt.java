package com.hbm.util;

import javax.annotation.Nullable;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** NBT boundary for NTM-owned inventories whose vacant array entries are null.
 * Not a native IInventory return-value adapter or a 1.7 numeric-ID world importer.
 */
public final class NullableInventoryStackNbt {
    private NullableInventoryStackNbt() { }

    /**
     * Reject absent/unregistered items, as the original loader did, then delegate
     * count, damage, tag and Forge capability loading to the native constructor.
     * Do not test isEmpty(): a registered zero/negative-count record was non-null
     * in the original loader and is distinct from a missing registry entry.
     */
    @Nullable
    public static ItemStack read(NBTTagCompound compound) {
        if (!compound.hasKey("id", 8)) return null;
        Item item = Item.getByNameOrId(compound.getString("id"));
        if (item == null || item == Items.AIR) return null;
        return new ItemStack(compound);
    }
}
