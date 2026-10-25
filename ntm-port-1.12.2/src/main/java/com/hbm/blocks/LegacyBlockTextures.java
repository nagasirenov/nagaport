package com.hbm.blocks;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import com.hbm.blocks.machine.BlockPillar;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

/** Per-instance bridge for legacy Block#setBlockTextureName assignments. */
public final class LegacyBlockTextures {

	private static final Map<Block, String> TEXTURES = new IdentityHashMap<>();

	private LegacyBlockTextures() { }

	public static <T extends Block> T bind(T block, String texture) {
		if(block == null) throw new NullPointerException("block");
		if(texture == null || texture.trim().isEmpty()) throw new IllegalArgumentException("texture cannot be empty");
		TEXTURES.put(block, texture);
		if(block instanceof BlockBase) ((BlockBase) block).setBlockTextureName(texture);
		if(block instanceof BlockPillar) ((BlockPillar) block).setBlockTextureName(texture);
		return block;
	}

	public static String texture(Block block) {
		return TEXTURES.get(block);
	}

	public static Map<Block, String> bindings() {
		return Collections.unmodifiableMap(TEXTURES);
	}

	/** 1.7's block-only atlas path maps to the shared 1.12 atlas under blocks/. */
	public static String atlasName(Block block) {
		String texture = texture(block);
		if(texture == null) return null;
		ResourceLocation legacy = new ResourceLocation(texture);
		String path = legacy.getPath();
		if(!path.startsWith("blocks/")) path = "blocks/" + path;
		return new ResourceLocation(legacy.getNamespace(), path).toString();
	}
}
