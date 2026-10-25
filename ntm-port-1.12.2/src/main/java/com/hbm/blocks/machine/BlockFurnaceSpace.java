package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.atmosphere.IBlockSealable;
import com.hbm.tileentity.machine.TileEntityFurnaceSpace;

import net.minecraft.block.BlockFurnace;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockFurnaceSpace extends BlockFurnace implements IBlockSealable {
	public BlockFurnaceSpace withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}


	public BlockFurnaceSpace(boolean lit) {
		super(lit);
	}

	public static void updateFurnaceBlockState(boolean lit, World world, int x, int y, int z) {
		int l = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		TileEntity tileentity = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.block.BlockFurnace.class, null, true, "field_149934_M", "keepInventory");

		if(lit) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.lit_furnace);
		} else {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.furnace);
		}

		net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.block.BlockFurnace.class, null, false, "field_149934_M", "keepInventory");
		com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, l, 2);

		if(tileentity != null) {
			tileentity.validate();
			world.setTileEntity(new net.minecraft.util.math.BlockPos(x, y, z), tileentity);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFurnaceSpace();
	}

	@Override
	public boolean isSealed(World world, int x, int y, int z) {
		return false;
	}

}
