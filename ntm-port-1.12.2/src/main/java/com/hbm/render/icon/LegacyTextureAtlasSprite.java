package com.hbm.render.icon;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/** Bridges old IIcon consumers to a sprite registered in the 1.12 atlas.
 * Pure WrappedIcon delegation: the full legacy IIcon contract (getIconName,
 * getIconWidth/Height, getMinU/MaxU, getMinV/MaxV, getInterpolatedU/V) is
 * inherited with dev-named methods and no vanilla superclass, so it survives
 * reobfuscation. Extending TextureAtlasSprite here previously renamed every
 * contract method to SRG and crashed the first IIcon call in a reobf jar
 * (AbstractMethodError, entity_rubble render). */
@SideOnly(Side.CLIENT)
public final class LegacyTextureAtlasSprite extends WrappedIcon {

	LegacyTextureAtlasSprite(TextureAtlasSprite sprite) {
		super(sprite);
	}
}
