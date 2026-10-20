package com.hbm.util;

import net.minecraft.inventory.EntityEquipmentSlot;

/** NTM constructors, model arrays and armor-mod UUIDs use head/chest/legs/feet order. */
public final class ArmorSlotOrder {
    private ArmorSlotOrder() { }

    /** Not the reversed native armor-inventory index used by ISpecialArmor. */
    public static EntityEquipmentSlot fromIndex(int index) {
        switch(index) {
        case 0: return EntityEquipmentSlot.HEAD;
        case 1: return EntityEquipmentSlot.CHEST;
        case 2: return EntityEquipmentSlot.LEGS;
        case 3: return EntityEquipmentSlot.FEET;
        default: throw new IllegalArgumentException("Not an NTM armor slot: " + index);
        }
    }

    public static int index(EntityEquipmentSlot slot) {
        switch(slot) {
        case HEAD: return 0;
        case CHEST: return 1;
        case LEGS: return 2;
        case FEET: return 3;
        default: throw new IllegalArgumentException("Not an armor slot: " + slot);
        }
    }
}
