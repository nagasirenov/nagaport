package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ISpotlight;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.Spotlight;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TritiumLamp extends Block implements ISpotlight {

	private final boolean isOn;

	public TritiumLamp(Material mat, boolean isOn) {
		super(mat);
		this.isOn = isOn;

		if(isOn) {
			this.setLightLevel(1.0F);
		}
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		
		if(!world.isRemote) {
			
			if(this.isOn && !world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
				world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, 4);
				
			} else if(!this.isOn && world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
				world.setBlock(x, y, z, getOn(), 0, 2);
			}
			
			updateBeam(world, x, y, z);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block b) {
		
		if(!world.isRemote) {
			
			if(this.isOn && !world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
				world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, 4);
				
			} else if(!this.isOn && world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
				world.setBlock(x, y, z, getOn(), 0, 2);
			}

			updateBeam(world, x, y, z);
		}
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random p_149674_5_) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
		
		if(!world.isRemote && this.isOn && !world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
			world.setBlock(x, y, z, getOff(), 0, 2);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
		super.breakBlock(world, x, y, z, block, metadata);
		if(world.isRemote) return;

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) Spotlight.unpropagateBeam(world, x, y, z, dir);
	}

	private void updateBeam(World world, int x, int y, int z) {
		if(!isOn) return;

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) Spotlight.propagateBeam(world, x, y, z, dir, getBeamLength(), getMeta());
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random r, int j) {
		return Item.getItemFromBlock(getOff());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int x, int y, int z) {
		return Item.getItemFromBlock(getOff());
	}

	@Override
	protected ItemStack createStackedBlock(int e) {
		return new ItemStack(getOff());
	}
	
	protected int getMeta() {
		if(this == ModBlocks.lamp_tritium_green_off || this == ModBlocks.lamp_tritium_green_on) return Spotlight.META_GREEN;
		if(this == ModBlocks.lamp_tritium_blue_off || this == ModBlocks.lamp_tritium_blue_on) return Spotlight.META_BLUE;
		return Spotlight.META_YELLOW;
	}
	
	protected Block getOff() {
		if(this == ModBlocks.lamp_tritium_green_on) return ModBlocks.lamp_tritium_green_off;
		if(this == ModBlocks.lamp_tritium_blue_on) return ModBlocks.lamp_tritium_blue_off;
		return this;
	}
	
	protected Block getOn() {
		if(this == ModBlocks.lamp_tritium_green_off) return ModBlocks.lamp_tritium_green_on;
		if(this == ModBlocks.lamp_tritium_blue_off) return ModBlocks.lamp_tritium_blue_on;
		return this;
	}

	@Override
	public int getBeamLength() {
		return 8;
	}
}
