package com.hbm.blocks;

import java.util.Collection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IBlockSideRotation {

	public int getRotationFromSide(IBlockAccess world, int x, int y, int z, int side);

	/** Native atlas materials, with the original inventory/world selectors kept separate. */
	public ResourceLocation getModelTexture(int side, int metadata);
	public Collection<ResourceLocation> getModelTextures();
	public default ResourceLocation getModelTexture(IBlockAccess world, int x, int y, int z, int side) {
		IBlockState state = world.getBlockState(new BlockPos(x, y, z));
		return getModelTexture(side, state.getBlock().getMetaFromState(state));
	}

	// 0 1 3 2 becomes 0 2 3 1
	// I want to smoke that swedish kush because it clearly makes you fucking stupid
	public static int topToBottom(int topRotation) {
		switch(topRotation) {
		case 1: return 2;
		case 2: return 1;
		default: return topRotation;
		}
	}

	public static boolean isOpposite(int from, int to) {
		switch(from) {
			case 0: return to == 1;
			case 1: return to == 0;
			case 2: return to == 3;
			case 3: return to == 2;
			case 4: return to == 5;
			case 5: return to == 4;
			default: return false;
		}
	}

}
