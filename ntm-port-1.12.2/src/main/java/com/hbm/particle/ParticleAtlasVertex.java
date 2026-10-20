package com.hbm.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Writes one atlas particle vertex into the native manager's open batch. */
@SideOnly(Side.CLIENT)
final class ParticleAtlasVertex {
	private ParticleAtlasVertex() { }

	static void append(BufferBuilder buffer, double x, double y, double z, double u, double v,
			float red, float green, float blue, float alpha, int brightness) {
		buffer.pos(x, y, z).tex(u, v)
				.color(MathHelper.clamp(red, 0F, 1F), MathHelper.clamp(green, 0F, 1F),
						MathHelper.clamp(blue, 0F, 1F), MathHelper.clamp(alpha, 0F, 1F))
				.lightmap(brightness >> 16 & 65535, brightness & 65535).endVertex();
	}
}
