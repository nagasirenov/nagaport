package com.hbm.blocks.generic;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/** Vanilla falling-block behavior with a typed sound setter for legacy construction chains. */
public class BlockFallingLegacy extends BlockFalling {

	public BlockFallingLegacy(Material material) {
		super(material);
	}

	public BlockFallingLegacy withSoundType(SoundType sound) {
		super.setSoundType(sound);
		return this;
	}
}
