package com.hbm.render.item.nativeitem.direct;

import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import javax.vecmath.Matrix4f;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

/** Caller compensation for ordinary, non-full3D items whose old helper used neither
 * BLOCK_3D nor EQUIPPED_BLOCK. Local mesh matrices are supplied separately.
 * See porting/native-item-render-assessment.md; not valid for tool or block branches.
 */
public final class PlainItemPerspectives {
    private PlainItemPerspectives() { }

    public static Matrix4f compose(TransformType type, Matrix4f local) {
        ItemTransformMatrix result = new ItemTransformMatrix();
        switch(type) {
        case GUI:
            result.scale(1D / 16D, -1D / 16D, 1D / 16D).translate(-8, -8, -103);
            break;
        case GROUND:
            result.translate(0, -.25, 0).scale(.5);
            break;
        case FIRST_PERSON_LEFT_HAND:
        case FIRST_PERSON_RIGHT_HAND:
            result.rotateY(45).scale(.4).multiply(equippedHelper());
            break;
        case THIRD_PERSON_LEFT_HAND:
        case THIRD_PERSON_RIGHT_HAND:
            Matrix4f nativeSuffix = new ItemTransformMatrix().rotateX(-90).rotateY(180)
                    .translate(.0625, .125, -.625).build();
            nativeSuffix.invert();
            result.multiply(nativeSuffix).translate(-.0625, .4375, .0625)
                    .translate(.25, .1875, -.1875).scale(.375).rotateZ(60).rotateX(-90).rotateZ(20)
                    .multiply(equippedHelper());
            break;
        case FIXED:
            // The frame event supplies the same-pivot XY correction; this is not GROUND.
            result.scale(2).translate(0, -.06, 0).scale(.5).rotateY(180);
            break;
        case HEAD:
        case NONE:
            // Approved centered-mesh policy for contexts absent in the original renderer.
            result.scale(.5);
            break;
        default: throw new IllegalArgumentException("Unhandled native item perspective: " + type);
        }
        // Native RenderItem translates the built-in mesh by -.5 after perspective.
        return result.multiply(local).translate(.5, .5, .5).build();
    }

    private static Matrix4f equippedHelper() {
        return new ItemTransformMatrix().translate(0, -(double).3F, 0).scale(1.5)
                .rotateY(50).rotateZ(335).translate(-.9375, -.0625, 0).build();
    }
}
