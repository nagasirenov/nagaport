package com.hbm.world.gen.nbt.selector;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;

public class StoneBrickSelector extends BlockSelector {

	@Override
	public void selectBlocks(Random rand, int x, int y, int z, boolean notInterior) {
		int selectedMetadata;
		float f = rand.nextFloat();

		if (f < 0.2F) {
			selectedMetadata = 2;
		} else if (f < 0.5F) {
			selectedMetadata = 1;
		} else {
			selectedMetadata = 0;
		}
		this.blockstate = Blocks.STONEBRICK.getStateFromMeta(selectedMetadata);
	}

}
