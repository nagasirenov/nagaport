package com.hbm.render.block.nativeblock;

import org.lwjgl.util.vector.Vector3f;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;

/** Native vanilla block/block display transforms, including distinct left/right first-person views. */
final class ThermalModelTransforms {
    private ThermalModelTransforms() { }
    private static ItemTransformVec3f transform(float x, float y, float z, float ty, float scale) {
        return new ItemTransformVec3f(new Vector3f(x, y, z), new Vector3f(0, ty / 16F, 0), new Vector3f(scale, scale, scale));
    }
    static final ItemCameraTransforms BLOCK = new ItemCameraTransforms(
            transform(75, 45, 0, 2.5F, .375F), transform(75, 45, 0, 2.5F, .375F),
            transform(0, 225, 0, 0, .4F), transform(0, 45, 0, 0, .4F),
            ItemTransformVec3f.DEFAULT, transform(30, 225, 0, 0, .625F),
            transform(0, 0, 0, 3, .25F), transform(0, 0, 0, 0, .5F));
}
