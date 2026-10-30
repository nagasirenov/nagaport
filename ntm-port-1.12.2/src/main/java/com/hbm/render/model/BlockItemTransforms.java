package com.hbm.render.model;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Display transforms of vanilla models/block/block.json. Code-baked cube models need these;
 * with ItemCameraTransforms.DEFAULT a cube shows only its front face in the GUI.
 */
@SideOnly(Side.CLIENT)
public final class BlockItemTransforms {

	private BlockItemTransforms() { }

	private static ItemTransformVec3f transform(float x, float y, float z, float ty, float scale) {
		return new ItemTransformVec3f(new Vector3f(x, y, z), new Vector3f(0, ty / 16F, 0), new Vector3f(scale, scale, scale));
	}

	public static final ItemCameraTransforms BLOCK = new ItemCameraTransforms(
			transform(75, 45, 0, 2.5F, .375F), transform(75, 45, 0, 2.5F, .375F),
			transform(0, 225, 0, 0, .4F), transform(0, 45, 0, 0, .4F),
			ItemTransformVec3f.DEFAULT, transform(30, 225, 0, 0, .625F),
			transform(0, 0, 0, 3, .25F), transform(0, 0, 0, 0, .5F));
}
