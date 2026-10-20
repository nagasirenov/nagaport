package com.hbm.render.item.block;

import javax.vecmath.Matrix4f;
import com.hbm.render.model.ModelSatelliteReceiver;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.context.NativeItemContext;
import net.minecraft.item.ItemStack;

public class ItemRenderSatelliteReceiver extends NativeDecoItemRenderer {
    public ItemRenderSatelliteReceiver() { super(new ModelSatelliteReceiver(), "PoleSatelliteReceiver"); }
    @Override public Matrix4f localTransform(ItemStack stack, NativeItemContext context) {
        ItemTransformMatrix matrix = new ItemTransformMatrix();
        switch(context.transform) {
        case FIRST_PERSON_LEFT_HAND:
        case FIRST_PERSON_RIGHT_HAND: return matrix.rotateZ(-135).translate(-.6F, -.6F, -.1F).rotateY(90).scale(.5).build();
        case THIRD_PERSON_LEFT_HAND:
        case THIRD_PERSON_RIGHT_HAND: return matrix.scale(.5).translate(.8F, -.3F, .2F).rotateY(-90).build();
        default: return matrix.scale(.5).rotateX(180).translate(0, -1, 0).build();
        }
    }
}
