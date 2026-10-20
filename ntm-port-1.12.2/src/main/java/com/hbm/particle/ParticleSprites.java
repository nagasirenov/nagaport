package com.hbm.particle;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Native atlas entries refreshed by the real texture-stitch event. */
@SideOnly(Side.CLIENT)
public final class ParticleSprites {
	public static TextureAtlasSprite base, leaf, swen, len, splash, ashes, flare;

	private ParticleSprites() { }

	public static void register(TextureMap map) {
		base = map.registerSprite(new ResourceLocation("hbm", "blocks/particle/particle_base"));
		leaf = map.registerSprite(new ResourceLocation("hbm", "blocks/particle/dead_leaf"));
		swen = map.registerSprite(new ResourceLocation("hbm", "blocks/particle/particlenote2"));
		len = map.registerSprite(new ResourceLocation("hbm", "blocks/particle/particlenote1"));
		splash = map.registerSprite(new ResourceLocation("hbm", "blocks/particle/particle_splash"));
		ashes = map.registerSprite(new ResourceLocation("hbm", "blocks/particle/particle_ashes"));
		flare = map.registerSprite(new ResourceLocation("hbm", "blocks/particle/yelflare"));
	}
}
