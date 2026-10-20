package com.hbm.blocks.machine;

import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import com.hbm.tileentity.machine.TileEntityFoundrySlagtap;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class FoundrySlagtap extends FoundryOutlet {
	


	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFoundrySlagtap();
	}

}
