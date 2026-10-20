package com.hbm.util;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

/** Narrow equipment boundaries for NTM consumers that deliberately use nullable stacks. */
public final class LivingEquipmentRead {
    private static final EntityEquipmentSlot[] EQUIPMENT = {
        EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS,
        EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD
    };

    private LivingEquipmentRead() { }

    /**
     * The audited NTM callers use only equipment indices 0..4, excluding offhand.
     * This is not a replacement for arbitrary external legacy integer overrides.
     * Keep native virtual slot
     * dispatch, and check the receiver before indexing just as an instance call did.
     * Callers pass the receiver and index separately so each is evaluated once, in
     * that order, before either the null check or the slot read occurs.
     */
    @Nullable
    public static ItemStack getEquipmentNullable(EntityLivingBase entity, int slot) {
        Objects.requireNonNull(entity);
        ItemStack stack = entity.getItemStackFromSlot(EQUIPMENT[slot]);
        return stack == ItemStack.EMPTY ? null : stack;
    }

    /**
     * Player armor reads used inventory.armorItemInSlot, not equipment dispatch.
     * Preserve that separate virtual inventory hook and its unmodified integer
     * index. Only canonical EMPTY becomes null; no other object is copied, changed,
     * or normalized, including tagged or noncanonical zero-count stacks.
     */
    @Nullable
    public static ItemStack getArmorNullable(EntityPlayer player, int slot) {
        ItemStack stack = player.inventory.armorItemInSlot(slot);
        return stack == ItemStack.EMPTY ? null : stack;
    }
}
