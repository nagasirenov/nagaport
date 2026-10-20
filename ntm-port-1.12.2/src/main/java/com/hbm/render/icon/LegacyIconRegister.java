package com.hbm.render.icon;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

/**
 * NTM-owned adapter for legacy icon callbacks. Every ordinary icon is
 * registered through TextureMap.registerSprite; generated sprites are inserted
 * as their actual TextureAtlasSprite instances into the same atlas.
 */
@SideOnly(Side.CLIENT)
public final class LegacyIconRegister implements IIconRegister {

	private final TextureMap textureMap;
	private final boolean itemTextures;
	private final Map<TextureAtlasSprite, LegacyTextureAtlasSprite> wrappers = new IdentityHashMap<>();

	public LegacyIconRegister(TextureMap textureMap) {
		this(textureMap, false);
	}

	/** Item callbacks use the old item-atlas-relative resource convention. */
	public LegacyIconRegister(TextureMap textureMap, boolean itemTextures) {
		if(textureMap == null) throw new IllegalArgumentException("textureMap cannot be null");
		this.textureMap = textureMap;
		this.itemTextures = itemTextures;
	}

	@Override
	public IIcon registerIcon(String name) {
		if(name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Icon name cannot be empty");
		return wrap(registerSprite(itemTextures
				? com.hbm.render.item.NativeItemTextureRegistry.atlasLocation(name) : new ResourceLocation(name)));
	}

	/** Registers a normal resource in the 1.12 texture atlas. */
	public TextureAtlasSprite registerSprite(ResourceLocation location) {
		if(location == null) throw new IllegalArgumentException("location cannot be null");
		return textureMap.registerSprite(location);
	}

	/** Adds a custom sprite (for example an ItemAutogen color-mutated sprite). */
	public IIcon registerGeneratedSprite(TextureAtlasSprite sprite) {
		if(sprite == null) throw new IllegalArgumentException("sprite cannot be null");
		if(!textureMap.setTextureEntry(sprite)) {
			TextureAtlasSprite existing = textureMap.getTextureExtry(sprite.getIconName());
			if(existing != sprite) {
				throw new IllegalStateException("Texture atlas already contains a different sprite named " + sprite.getIconName());
			}
		}
		return wrap(sprite);
	}

	private LegacyTextureAtlasSprite wrap(TextureAtlasSprite sprite) {
		LegacyTextureAtlasSprite wrapper = wrappers.get(sprite);
		if(wrapper == null) {
			wrapper = new LegacyTextureAtlasSprite(sprite);
			wrappers.put(sprite, wrapper);
		}
		return wrapper;
	}
}
