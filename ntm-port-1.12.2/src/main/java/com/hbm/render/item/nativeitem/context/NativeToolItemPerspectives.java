package com.hbm.render.item.nativeitem.context;

import javax.vecmath.Matrix4f;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.direct.PlainItemPerspectives;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

/** Original ordinary/full-3D equipped caller selection, using the actual holder. */
public final class NativeToolItemPerspectives {
    private NativeToolItemPerspectives() { }
    public static boolean isBlocking(ItemStack stack, NativeItemContext context) {
        return context.holder != null && context.holder.isHandActive() && context.holder.getActiveItemStack() == stack
                && stack.getItemUseAction() == net.minecraft.item.EnumAction.BLOCK;
    }
    public static Matrix4f compose(ItemStack stack, NativeItemContext context, Matrix4f local) {
        TransformType type = context.transform;
        if((type == TransformType.THIRD_PERSON_RIGHT_HAND || type == TransformType.THIRD_PERSON_LEFT_HAND)
                && !stack.isEmpty() && stack.getItem().isFull3D()) {
            Matrix4f inverse = new ItemTransformMatrix().rotateX(-90).rotateY(180).translate(.0625,.125,-.625).build();
            inverse.invert();
            ItemTransformMatrix m = new ItemTransformMatrix().multiply(inverse).translate(-.0625,.4375,.0625);
            if(stack.getItem().shouldRotateAroundWhenRendering()) m.rotateZ(180).translate(0,-.125,0);
            if(isBlocking(stack, context)) m.translate(.05F,0,-.1F).rotateY(-50).rotateX(-10).rotateZ(-60);
            return m.translate(0,.1875,0).scale(.625,-.625,.625).rotateX(-100).rotateY(45)
                    .translate(0,-.3F,0).scale(1.5).rotateY(50).rotateZ(335).translate(-.9375,-.0625,0)
                    .multiply(local).translate(.5,.5,.5).build();
        }
        return PlainItemPerspectives.compose(type, local);
    }
}
