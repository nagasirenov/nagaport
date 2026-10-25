package com.hbm.blocks.bomb;

import org.apache.logging.log4j.Level;

import com.hbm.blocks.machine.BlockMachineBase;
import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.IBomb;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.bomb.TileEntityAntimatter;
import com.hbm.tileentity.bomb.TileEntityAntimatter;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class NukeAntimatter extends BlockMachineBase implements IBomb {

	public NukeAntimatter(Material mat) {
		super(mat, 0);
		rotatable = true;
	}


	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityAntimatter();
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
	public void neighborChanged(net.minecraft.block.state.IBlockState nativeState, World world, net.minecraft.util.math.BlockPos nativePos, Block b, net.minecraft.util.math.BlockPos fromPos) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();

		if (world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
			explode(world, x, y, z);
		}
	}
	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		if(!world.isRemote) {
			if(GeneralConfig.enableExtendedLogging) {
				MainRegistry.logger.log(Level.INFO, "[BOMBPL]" + this.getLocalizedName() + " placed at " + x + " / " + y + " / " + z + "! " + "by "+ player.getName());
		}
	}
	}

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {

		if(!world.isRemote) {
			TileEntityAntimatter bomb = (TileEntityAntimatter) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			if(bomb.isLoaded()) {
				bomb.explode();
				return BombReturnCode.DETONATED;
			}

			return BombReturnCode.ERROR_MISSING_COMPONENT;
		}

		return BombReturnCode.UNDEFINED;
	}

}

