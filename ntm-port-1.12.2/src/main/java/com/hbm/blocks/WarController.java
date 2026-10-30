package com.hbm.blocks;

import com.hbm.blocks.machine.BlockMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;
import com.hbm.tileentity.machine.TileEntityMachineWarController;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class WarController extends BlockMachineBase {
	// 1.7.10 type 0 cube; BlockContainer would make it INVISIBLE in 1.12 (render-type parity audit).
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}


	public WarController withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	public WarController() {
		super(Material.IRON, 0);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMachineWarController();
	}


}
