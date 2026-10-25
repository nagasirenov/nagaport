package com.hbm.blocks.generic;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class BlockOrrery extends BlockContainer {

	public BlockOrrery(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityOrrery();
	}

	@Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}
	@Override public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) { return false; }
	@Override public boolean isFullCube(net.minecraft.block.state.IBlockState state) { return false; }

	public class TileEntityOrrery extends TileEntity {

		AxisAlignedBB bb = null;

		@Override
		public AxisAlignedBB getRenderBoundingBox() {
			if(bb == null) {
				bb = new AxisAlignedBB(
					this.pos.getX() - 49,
					this.pos.getY() - 19,
					this.pos.getZ() - 49,
					this.pos.getX() + 50,
					this.pos.getY() + 20,
					this.pos.getZ() + 50
				);
			}

			return bb;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public double getMaxRenderDistanceSquared() {
			return 65536.0D;
		}

	}

}
