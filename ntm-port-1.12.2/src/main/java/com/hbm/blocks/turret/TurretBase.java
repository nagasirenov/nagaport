package com.hbm.blocks.turret;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public abstract class TurretBase extends BlockContainer {
	
	Random rand = new Random();

	public TurretBase(Material mat) {
		super(mat);
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
	
	public abstract boolean executeHoldAction(World world, int i, double yaw, double pitch, int x, int y, int z);
	public abstract void executeReleaseAction(World world, int i, double yaw, double pitch, int x, int y, int z);

}
