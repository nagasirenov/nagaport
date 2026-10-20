package com.hbm.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Appends one native atlas quad to ParticleManager's already-open particle buffer. */
@SideOnly(Side.CLIENT)
public final class ParticleQuadRenderer {
	private ParticleQuadRenderer() { }

	public static void append(BufferBuilder buffer, TextureAtlasSprite sprite, float x, float y, float z,
			float scale, float fX, float fY, float fZ, float sX, float sZ,
			float red, float green, float blue, float alpha, int brightness) {
		int sky = brightness >> 16 & 65535;
		int block = brightness & 65535;
		buffer.pos(x - fX * scale - sX * scale, y - fY * scale, z - fZ * scale - sZ * scale)
				.tex(sprite.getMaxU(), sprite.getMaxV()).color(red, green, blue, alpha).lightmap(sky, block).endVertex();
		buffer.pos(x - fX * scale + sX * scale, y + fY * scale, z - fZ * scale + sZ * scale)
				.tex(sprite.getMaxU(), sprite.getMinV()).color(red, green, blue, alpha).lightmap(sky, block).endVertex();
		buffer.pos(x + fX * scale + sX * scale, y + fY * scale, z + fZ * scale + sZ * scale)
				.tex(sprite.getMinU(), sprite.getMinV()).color(red, green, blue, alpha).lightmap(sky, block).endVertex();
		buffer.pos(x + fX * scale - sX * scale, y - fY * scale, z + fZ * scale - sZ * scale)
				.tex(sprite.getMinU(), sprite.getMaxV()).color(red, green, blue, alpha).lightmap(sky, block).endVertex();
	}
}
