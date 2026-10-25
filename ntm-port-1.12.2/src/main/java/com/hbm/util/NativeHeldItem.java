package com.hbm.util;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/** Main-hand boundary for NTM call sites that still deliberately use nullable stacks. */
public final class NativeHeldItem {
    private NativeHeldItem() { }

    /**
     * Keeps the old main-hand-only nullable contract without bypassing native virtual
     * equipment dispatch. Only the engine's canonical empty sentinel becomes null:
     * noncanonical zero-count or tagged stacks retain their identity and payload.
     * This does not restore legacy ItemStack.getItem semantics for zero-count stacks.
     */
    @Nullable
    public static ItemStack getMainHandNullable(EntityLivingBase entity) {
        ItemStack stack = entity.getHeldItemMainhand();
        return (stack == null || stack.isEmpty()) ? null : stack;
    }
}
