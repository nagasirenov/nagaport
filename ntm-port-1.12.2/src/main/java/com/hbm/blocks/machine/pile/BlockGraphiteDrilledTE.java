package com.hbm.blocks.machine.pile;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockGraphiteDrilledTE extends BlockGraphiteDrilledBase implements ITileEntityProvider {

	public BlockGraphiteDrilledTE() {
		super();
		this.hasTileEntity = true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}
}
