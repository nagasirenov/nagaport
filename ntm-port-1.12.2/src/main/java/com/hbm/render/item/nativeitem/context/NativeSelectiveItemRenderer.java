package com.hbm.render.item.nativeitem.context;

import javax.vecmath.Matrix4f;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

/** Opt-in original callback selection. Declined contexts use the complete native
 * base model, including its overrides and perspective, never a flattened icon. */
public interface NativeSelectiveItemRenderer {
    boolean handlesPerspective(ItemStack stack, TransformType type);
    Matrix4f composePerspective(ItemStack stack, NativeItemContext context);
}
