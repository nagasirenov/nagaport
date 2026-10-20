package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class WatzPump extends BlockDummyable {

	public WatzPump() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityWatzPump();
		
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		int meta = world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());
		return side == EnumFacing.UP && meta == 1;
	}
	
	public static class TileEntityWatzPump extends TileEntity {
		@Override @SideOnly(Side.CLIENT) public double getMaxRenderDistanceSquared() { return 65536.0D; }
		AxisAlignedBB bb = null;
		@Override public AxisAlignedBB getRenderBoundingBox() {
			if(bb == null) bb = AxisAlignedBB.getBoundingBox(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() - 1, this.pos.getX() + 2, this.pos.getY() + 2, this.pos.getZ() + 2);
			return bb;
		}
	}
}
