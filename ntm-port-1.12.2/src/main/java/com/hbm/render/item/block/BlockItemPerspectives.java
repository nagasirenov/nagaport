package com.hbm.render.item.block;

import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import javax.vecmath.Matrix4f;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

/** Conveyor wand's original all-true block helpers, not the ordinary mesh-item path.
 * Native RenderItem supplies the final (-.5,-.5,-.5); inventory quads already
 * contain the conveyor's +.25 Y position compensation. */
public final class BlockItemPerspectives {
    private BlockItemPerspectives() { }
    public static Map<TransformType, Matrix4f> derived() {
        EnumMap<TransformType, Matrix4f> result = new EnumMap<>(TransformType.class);
        result.put(TransformType.GUI, new ItemTransformMatrix()
                .scale(1D / 16D, -1D / 16D, 1D / 16D).translate(-10, -5, -103)
                .scale(10).translate(1, .5, 1).scale(1, 1, -1)
                .rotateX(210).rotateY(45).rotateY(-90).build());
        // EQUIPPED_BLOCK's -.5 and ItemRenderBlock's +.5 cancel exactly.
        result.put(TransformType.FIRST_PERSON_RIGHT_HAND, new ItemTransformMatrix().rotateY(45).scale(.4).build());
        Matrix4f nativeArm = new ItemTransformMatrix().rotateX(-90).rotateY(180)
                .translate(.0625, .125, -.625).build();
        nativeArm.invert();
        result.put(TransformType.THIRD_PERSON_RIGHT_HAND, new ItemTransformMatrix().multiply(nativeArm)
                .translate(-.0625, .4375, .0625).translate(0, .1875, -.3125)
                .rotateX(20).rotateY(45).scale(-.375, -.375, .375).build());
        result.put(TransformType.GROUND, new ItemTransformMatrix().translate(0, -.25, 0).scale(.5).build());
        result.put(TransformType.FIXED, new ItemTransformMatrix().scale(2).translate(0, -.06, 0)
                .rotateY(180).scale(1.25).translate(0, .05, 0).rotateY(-90).scale(.5).build());
        // Native offhand is the same geometry mirrored by Forge's flip-X conjugation.
        result.put(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4f(result.get(TransformType.FIRST_PERSON_RIGHT_HAND)));
        result.put(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4f(result.get(TransformType.THIRD_PERSON_RIGHT_HAND)));
        // These contexts did not exist in the old API: approved centered native mesh.
        result.put(TransformType.HEAD, new ItemTransformMatrix().build());
        result.put(TransformType.NONE, new ItemTransformMatrix().build());
        return Collections.unmodifiableMap(result);
    }
}
