package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityMultiblock extends TileEntity implements net.minecraft.util.ITickable {

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(this.getBlockType() == ModBlocks.struct_launcher_core && isCompact()) {
				buildCompact();
			}

			if(this.getBlockType() == ModBlocks.struct_launcher_core_large) {

				int meta = isTable();

				if(meta != -1)
					buildTable(meta);
			}
		}
	}

	private boolean isCompact() {

		for(int i = -1; i <= 1; i++)
			for(int j = -1; j <= 1; j++)
				if(!(i == 0 && j == 0))
					if(com.hbm.world.WorldBlockState.read(this.world, this.getPos().getX() + i, this.getPos().getY(), this.getPos().getZ() + j).getBlock() != ModBlocks.struct_launcher)
						return false;

		return true;
	}

	private int isTable() {

		for(int i = -4; i <= 4; i++)
			for(int j = -4; j <= 4; j++)
				if(!(i == 0 && j == 0))
					if(com.hbm.world.WorldBlockState.read(this.world, this.getPos().getX() + i, this.getPos().getY(), this.getPos().getZ() + j).getBlock() != ModBlocks.struct_launcher)
						return -1;

		boolean flag = true;

		for(int k = 1; k < 12; k++) {
			if(com.hbm.world.WorldBlockState.read(this.world, this.getPos().getX() + 3, this.getPos().getY() + k, this.getPos().getZ()).getBlock() != ModBlocks.struct_scaffold)
				flag = false;
		}

		if(flag)
			return 0;
		flag = true;

		for(int k = 1; k < 12; k++) {
			if(com.hbm.world.WorldBlockState.read(this.world, this.getPos().getX() - 3, this.getPos().getY() + k, this.getPos().getZ()).getBlock() != ModBlocks.struct_scaffold)
				flag = false;
		}

		if(flag)
			return 1;
		flag = true;

		for(int k = 1; k < 12; k++) {
			if(com.hbm.world.WorldBlockState.read(this.world, this.getPos().getX(), this.getPos().getY() + k, this.getPos().getZ() + 3).getBlock() != ModBlocks.struct_scaffold)
				flag = false;
		}

		if(flag)
			return 2;
		flag = true;

		for(int k = 1; k < 12; k++) {
			if(com.hbm.world.WorldBlockState.read(this.world, this.getPos().getX(), this.getPos().getY() + k, this.getPos().getZ() - 3).getBlock() != ModBlocks.struct_scaffold)
				flag = false;
		}

		if(flag)
			return 3;

		return -1;

	}

	private void buildCompact() {

		com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.compact_launcher);

		placeDummy(this.getPos().getX() + 1, this.getPos().getY(), this.getPos().getZ() + 1, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_port_compact_launcher);
		placeDummy(this.getPos().getX() + 1, this.getPos().getY(), this.getPos().getZ(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_plate_compact_launcher);
		placeDummy(this.getPos().getX() + 1, this.getPos().getY(), this.getPos().getZ() - 1, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_port_compact_launcher);
		placeDummy(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ() - 1, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_plate_compact_launcher);
		placeDummy(this.getPos().getX() - 1, this.getPos().getY(), this.getPos().getZ() - 1, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_port_compact_launcher);
		placeDummy(this.getPos().getX() - 1, this.getPos().getY(), this.getPos().getZ(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_plate_compact_launcher);
		placeDummy(this.getPos().getX() - 1, this.getPos().getY(), this.getPos().getZ() + 1, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_port_compact_launcher);
		placeDummy(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ() + 1, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_plate_compact_launcher);
	}

	private void buildTable(int meta) {

		com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.launch_table, meta, 2);

		switch(meta) {
		case 0:
			for(int i = 1; i < 12; i++)
				com.hbm.world.WorldBlockState.write(this.world, this.getPos().getX() + 3, this.getPos().getY() + i, this.getPos().getZ(), Blocks.AIR.getStateFromMeta(0), 3);

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(this.getPos().getX() + i, this.getPos().getY(), this.getPos().getZ(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_port_launch_table);

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ() + i, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_plate_launch_table);

			break;

		case 1:
			for(int i = 1; i < 12; i++)
				com.hbm.world.WorldBlockState.write(this.world, this.getPos().getX() - 3, this.getPos().getY() + i, this.getPos().getZ(), Blocks.AIR.getStateFromMeta(0), 3);

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(this.getPos().getX() + i, this.getPos().getY(), this.getPos().getZ(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_port_launch_table);

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ() + i, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_plate_launch_table);

			break;

		case 2:
			for(int i = 1; i < 12; i++)
				com.hbm.world.WorldBlockState.write(this.world, this.getPos().getX(), this.getPos().getY() + i, this.getPos().getZ() + 3, Blocks.AIR.getStateFromMeta(0), 3);

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(this.getPos().getX() + i, this.getPos().getY(), this.getPos().getZ(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_plate_launch_table);

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ() + i, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_port_launch_table);

			break;

		case 3:
			for(int i = 1; i < 12; i++)
				com.hbm.world.WorldBlockState.write(this.world, this.getPos().getX(), this.getPos().getY() + i, this.getPos().getZ() - 3, Blocks.AIR.getStateFromMeta(0), 3);

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(this.getPos().getX() + i, this.getPos().getY(), this.getPos().getZ(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_plate_launch_table);

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ() + i, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_port_launch_table);

			break;

		}

		for(int i = -4; i <= 4; i++)
			for(int j = -4; j <= 4; j++)
				if(i != 0 && j != 0)
					placeDummy(this.getPos().getX() + i, this.getPos().getY(), this.getPos().getZ() + j, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), ModBlocks.dummy_port_launch_table);


	}

	private void placeDummy(int x, int y, int z, int xCoord, int yCoord, int zCoord, Block block) {

		com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, x, y, z, block);

		TileEntity te = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(te instanceof TileEntityDummy) {
			TileEntityDummy dummy = (TileEntityDummy)te;
			dummy.targetX = xCoord;
			dummy.targetY = yCoord;
			dummy.targetZ = zCoord;
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

}
