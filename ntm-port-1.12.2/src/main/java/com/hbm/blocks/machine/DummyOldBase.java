package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.interfaces.IDummy;
import com.hbm.interfaces.IMultiblock;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.tileentity.machine.TileEntityDummy;

import api.hbm.energymk2.IEnergyConnectorBlock;
import api.hbm.fluid.IFluidConnectorBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public abstract class DummyOldBase extends BlockContainer implements IDummy, IEnergyConnectorBlock, IFluidConnectorBlock {

	public static boolean safeBreak = false;
	private boolean port = false;

	public DummyOldBase(Material mat, boolean port) {
		super(mat);
		this.port = port;
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityDummy();
	}

	@Override
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int i = block.getMetaFromState(state);
		if(!safeBreak) {
			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(te != null && te instanceof TileEntityDummy) {
				int a = ((TileEntityDummy) te).targetX;
				int b = ((TileEntityDummy) te).targetY;
				int c = ((TileEntityDummy) te).targetZ;

				if(!world.isRemote && com.hbm.world.WorldBlockState.read(world, a, b, c).getBlock() instanceof IMultiblock)
					com.hbm.world.WorldBlockDestruction.destroy(world, a, b, c, true);
			}
		}
		world.removeTileEntity(pos);
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return null;
	}

	@Override public boolean canConnect(IBlockAccess world, int x, int y, int z, ForgeDirection dir) { return port; }
	@Override public boolean canConnect(FluidType type, IBlockAccess world, int x, int y, int z, ForgeDirection dir) { return port; }
}
