package com.hbm.render.icon;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/** Bridges old IIcon consumers to a sprite registered in the 1.12 atlas. */
@SideOnly(Side.CLIENT)
public final class LegacyTextureAtlasSprite extends TextureAtlasSprite implements IIcon {

	private final TextureAtlasSprite sprite;

	LegacyTextureAtlasSprite(TextureAtlasSprite sprite) {
		super(sprite.getIconName());
		this.sprite = sprite;
	}

	public TextureAtlasSprite getSprite() {
		return sprite;
	}

	@Override
	public String getIconName() {
		return sprite.getIconName();
	}

	@Override
	public int getIconWidth() {
		return sprite.getIconWidth();
	}

	@Override
	public int getIconHeight() {
		return sprite.getIconHeight();
	}

	@Override
	public float getMinU() {
		return sprite.getMinU();
	}

	@Override
	public float getMaxU() {
		return sprite.getMaxU();
	}

	@Override
	public float getInterpolatedU(double u) {
		return sprite.getInterpolatedU(u);
	}

	@Override
	public float getMinV() {
		return sprite.getMinV();
	}

	@Override
	public float getMaxV() {
		return sprite.getMaxV();
	}

	@Override
	public float getInterpolatedV(double v) {
		return sprite.getInterpolatedV(v);
	}
}
