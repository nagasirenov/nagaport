package com.hbm.render.icon;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Pure legacy IIcon delegation for sprites consumed through the icon contract.
 * This must not extend TextureAtlasSprite: a vanilla superclass makes Forge's
 * reobfuscation rename the IIcon contract methods to SRG names, which leaves
 * the interface unimplemented at runtime (AbstractMethodError on the first
 * IIcon call in a reobfuscated jar). LegacyTextureAtlasSprite subclasses this
 * for the same reason; every IIcon implementation in the port must stay free
 * of vanilla superclasses. */
@SideOnly(Side.CLIENT)
public class WrappedIcon implements IIcon {

	private final TextureAtlasSprite sprite;

	public WrappedIcon(TextureAtlasSprite sprite) {
		if(sprite == null) throw new IllegalArgumentException("sprite cannot be null");
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
