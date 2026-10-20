package com.hbm.render.item.nativeitem.context;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** The crucible's original equip snap, scoped to the actual native hand and exact owner. */
public final class NativeWeaponHandState {
    private NativeWeaponHandState() { }
    private static final Field MAIN = ReflectionHelper.findField(ItemRenderer.class,"equippedProgressMainHand","field_187469_f");
    private static final Field PREV_MAIN = ReflectionHelper.findField(ItemRenderer.class,"prevEquippedProgressMainHand","field_187470_g");
    private static final Field OFF = ReflectionHelper.findField(ItemRenderer.class,"equippedProgressOffHand","field_187471_h");
    private static final Field PREV_OFF = ReflectionHelper.findField(ItemRenderer.class,"prevEquippedProgressOffHand","field_187472_i");
    public static float[] snapEquip(ItemStack stack, NativeItemContext context) {
        ItemRenderer renderer = Minecraft.getMinecraft().getItemRenderer();
        if(context.holder != Minecraft.getMinecraft().player || renderer == null || renderer.getClass() != ItemRenderer.class)
            return new float[]{1,1};
        boolean left = context.transform == net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
        boolean main = left == (context.holder.getPrimaryHand() == EnumHandSide.LEFT);
        if((main ? context.holder.getHeldItemMainhand() : context.holder.getHeldItemOffhand()) != stack) return new float[]{1,1};
        Field current = main ? MAIN : OFF, previous = main ? PREV_MAIN : PREV_OFF;
        float eq = (Float)NativeItemFields.get(current,renderer), prev = (Float)NativeItemFields.get(previous,renderer);
        if(eq != prev) {
            float snapped = eq < prev ? 0 : 1;
            try { NativeItemFields.set(previous,renderer,snapped); NativeItemFields.set(current,renderer,snapped); }
            catch(RuntimeException failure) { NativeItemFields.set(previous,renderer,prev); NativeItemFields.set(current,renderer,eq); throw failure; }
        }
        // Original blade condition used the values read before mutation.
        return new float[]{prev,eq};
    }
}
