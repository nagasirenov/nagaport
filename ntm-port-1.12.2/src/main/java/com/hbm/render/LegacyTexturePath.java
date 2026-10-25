package com.hbm.render;

import net.minecraft.util.ResourceLocation;

/** Normalizes legacy 1.7 atlas sprite names for the 1.12 block/item atlases. */
public final class LegacyTexturePath {

	private LegacyTexturePath() { }

	public static ResourceLocation blockAtlasLocation(String texture) {
		return atlasLocation(texture, "blocks/", "tile.");
	}

	public static ResourceLocation itemAtlasLocation(String texture) {
		return atlasLocation(texture, "items/", "item.");
	}

	private static ResourceLocation atlasLocation(String texture, String atlasPrefix, String legacyPrefix) {
		if(texture == null || texture.trim().isEmpty()) throw new IllegalArgumentException("Texture name cannot be empty");
		ResourceLocation source = new ResourceLocation(texture);
		String path = source.getPath();
		if(path.startsWith("blocks/") || path.startsWith("items/")) return source;
		if(path.startsWith(legacyPrefix)) path = path.substring(legacyPrefix.length());
		return new ResourceLocation(source.getNamespace(), atlasPrefix + path);
	}
}