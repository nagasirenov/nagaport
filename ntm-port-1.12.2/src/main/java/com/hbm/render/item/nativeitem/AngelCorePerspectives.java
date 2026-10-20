package com.hbm.render.item.nativeitem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import javax.vecmath.Matrix4f;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

/** Caller-compensated original core geometry. See porting/native-item-render-assessment.md.
 * Not a replacement enum or a universal legacy renderer adapter.
 */
public final class AngelCorePerspectives {
    private AngelCorePerspectives() { }

    /** Only independently derived original contexts are published. Missing contexts gate activation. */
    public static Map<TransformType, Matrix4f> derived() {
        EnumMap<TransformType, Matrix4f> result = new EnumMap<>(TransformType.class);
        result.put(TransformType.GUI, new ItemTransformMatrix()
                .scale(1D / 16D, -1D / 16D, 1D / 16D).translate(0, 2, -103)
                .rotateX(-30).rotateY(45).scale(-1).translate(0, -3, 0).scale(16)
                .translate(.5, .5, .5).build());
        result.put(TransformType.GROUND, new ItemTransformMatrix()
                .translate(0, -.25, 0).scale(.75).rotateY(90).translate(.5, .5, .5).build());
        result.put(TransformType.FIRST_PERSON_RIGHT_HAND, new ItemTransformMatrix()
                .rotateY(45).scale(.4).multiply(equippedHelper())
                .translate(.5, .25, 0).rotateY(90).translate(.5, .5, .5).build());
        Matrix4f nativeArmSuffix = new ItemTransformMatrix().rotateX(-90).rotateY(180)
                .translate(.0625, .125, -.625).build();
        nativeArmSuffix.invert();
        result.put(TransformType.THIRD_PERSON_RIGHT_HAND, new ItemTransformMatrix()
                .multiply(nativeArmSuffix).translate(-.0625, .4375, .0625)
                .translate(.25, .1875, -.1875).scale(.375).rotateZ(60).rotateX(-90).rotateZ(20)
                .multiply(equippedHelper()).translate(.5, .25, 0).translate(.5, .5, .5).build());
        result.put(TransformType.FIXED, new ItemTransformMatrix().scale(2).translate(0, -.06, 0)
                .scale(.75).rotateY(270).translate(.5, .5, .5).build());
        return Collections.unmodifiableMap(result);
    }
    /** User-approved 1.12 extensions, kept distinct from the old-context derivations. */
    public static Map<TransformType, Matrix4f> complete() {
        EnumMap<TransformType, Matrix4f> result = new EnumMap<>(TransformType.class);
        result.putAll(derived());
        // Forge handleCameraTransforms applies flip-X conjugation for left hands.
        result.put(TransformType.FIRST_PERSON_LEFT_HAND,
                new Matrix4f(result.get(TransformType.FIRST_PERSON_RIGHT_HAND)));
        result.put(TransformType.THIRD_PERSON_LEFT_HAND,
                new Matrix4f(result.get(TransformType.THIRD_PERSON_RIGHT_HAND)));
        Matrix4f centered = new ItemTransformMatrix().scale(.75).rotateY(90)
                .translate(.5, .5, .5).build();
        result.put(TransformType.HEAD, new Matrix4f(centered));
        result.put(TransformType.NONE, new Matrix4f(centered));
        return Collections.unmodifiableMap(result);
    }
    private static Matrix4f equippedHelper() {
        return new ItemTransformMatrix().translate(0, -.3, 0).scale(1.5)
                .rotateY(50).rotateZ(335).translate(-.9375, -.0625, 0).build();
    }
}
