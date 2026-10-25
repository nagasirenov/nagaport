package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.Watz;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityWatzStruct extends TileEntity implements net.minecraft.util.ITickable {

	@Override
	public void update() {

		if(this.world.isRemote) return;
		if(this.world.getTotalWorldTime() % 20 != 0) return;

		/*
		 * skeptics may say that his is shit. i don't necessarily disagree, but it was both easy and quick to do
		 * and it remains readable and not terribly long, so who the fuck cares.
		 */
		if(!cbr(ModBlocks.watz_cooler, 0, 1, 0)) return;
		if(!cbr(ModBlocks.watz_cooler, 0, 2, 0)) return;

		for(int i = 0; i < 3; i++) {
			if(!cbr(ModBlocks.watz_element, 1, i, 0)) return;
			if(!cbr(ModBlocks.watz_element, 2, i, 0)) return;
			if(!cbr(ModBlocks.watz_element, 0, i, 1)) return;
			if(!cbr(ModBlocks.watz_element, 0, i, 2)) return;
			if(!cbr(ModBlocks.watz_element, -1, i, 0)) return;
			if(!cbr(ModBlocks.watz_element, -2, i, 0)) return;
			if(!cbr(ModBlocks.watz_element, 0, i, -1)) return;
			if(!cbr(ModBlocks.watz_element, 0, i, -2)) return;
			if(!cbr(ModBlocks.watz_element, 1, i, 1)) return;
			if(!cbr(ModBlocks.watz_element, 1, i, -1)) return;
			if(!cbr(ModBlocks.watz_element, -1, i, 1)) return;
			if(!cbr(ModBlocks.watz_element, -1, i, -1)) return;
			if(!cbr(ModBlocks.watz_cooler, 2, i, 1)) return;
			if(!cbr(ModBlocks.watz_cooler, 2, i, -1)) return;
			if(!cbr(ModBlocks.watz_cooler, 1, i, 2)) return;
			if(!cbr(ModBlocks.watz_cooler, -1, i, 2)) return;
			if(!cbr(ModBlocks.watz_cooler, -2, i, 1)) return;
			if(!cbr(ModBlocks.watz_cooler, -2, i, -1)) return;
			if(!cbr(ModBlocks.watz_cooler, 1, i, -2)) return;
			if(!cbr(ModBlocks.watz_cooler, -1, i, -2)) return;

			for(int j = -1; j < 2; j++) {
				if(!cbr(ModBlocks.watz_end, 1, 3, i, j)) return;
				if(!cbr(ModBlocks.watz_end, 1, j, i, 3)) return;
				if(!cbr(ModBlocks.watz_end, 1, -3, i, j)) return;
				if(!cbr(ModBlocks.watz_end, 1, j, i, -3)) return;
			}
			if(!cbr(ModBlocks.watz_end, 1, 2, i, 2)) return;
			if(!cbr(ModBlocks.watz_end, 1, 2, i, -2)) return;
			if(!cbr(ModBlocks.watz_end, 1, -2, i, 2)) return;
			if(!cbr(ModBlocks.watz_end, 1, -2, i, -2)) return;
		}

		Watz watz = (Watz)ModBlocks.watz;
		BlockDummyable.safeRem = true;
		com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), ModBlocks.watz, 12, 3);
		watz.fillSpace(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), ForgeDirection.NORTH, 0);
		BlockDummyable.safeRem = false;
	}

	/** [G]et [B]lock at [R]elative position */
	private Block gbr(int x, int y, int z) {
		return com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + x, this.pos.getY() + y, this.pos.getZ() + z).getBlock();
	}

	/** [G]et [M]eta at [R]elative position */
	private int gmr(int x, int y, int z) {
		return com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + x, this.pos.getY() + y, this.pos.getZ() + z) );
	}

	/** [C]heck [B]lock at [R]elative position */
	private boolean cbr(Block b, int x, int y, int z) {
		return b == gbr(x, y, z);
	}
	private boolean cbr(Block b, int meta, int x, int y, int z) {
		return b == gbr(x, y, z) && meta == gmr(x, y, z);
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 3,
					this.pos.getY(),
					this.pos.getZ() - 3,
					this.pos.getX() + 4,
					this.pos.getY() + 3,
					this.pos.getZ() + 4
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
