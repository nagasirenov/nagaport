package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.fusion.MachineFusionTorus;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityFusionTorusStruct extends TileEntity implements net.minecraft.util.ITickable {

	@Override
	public void update() {

		if(this.world.isRemote) return;
		if(this.world.getTotalWorldTime() % 20 != 0) return;

		for(int y = 0; y < 5; y++) {
			for(int x = 0; x < MachineFusionTorus.layout[0].length; x++) {
				for(int z = 0; z < MachineFusionTorus.layout[0][0].length; z++) {

					int ly = y > 2 ? 4 - y : y;
					int i = MachineFusionTorus.layout[ly][x][z];

					if(i == 0) continue; // ignore air
					if(x == 7 && y == 0 && z == 7) continue; // ignore core component position
					if(!cbr(ModBlocks.fusion_component, i, x - 7, y, z - 7)) return;
				}
			}
		}

		MachineFusionTorus block = (MachineFusionTorus) ModBlocks.fusion_torus;
		BlockDummyable.safeRem = true;
		com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), ModBlocks.fusion_torus, 12, 3);
		block.fillSpace(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), ForgeDirection.NORTH, 0);
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
	private boolean cbr(Block b, int meta, int x, int y, int z) {
		return b == gbr(x, y, z) && meta == gmr(x, y, z);
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 7,
					this.pos.getY(),
					this.pos.getZ() - 7,
					this.pos.getX() + 8,
					this.pos.getY() + 5,
					this.pos.getZ() + 8
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
