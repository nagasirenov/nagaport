package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.ITooltipProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockRailBase;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class RailGeneric extends net.minecraft.block.BlockRail implements ITooltipProvider {

	@SideOnly(Side.CLIENT)
	protected IIcon turnedIcon;

	protected static final float baseSpeed = 0.4F;
	protected float maxSpeed = 0.4F;
	protected boolean slopable = true;
	protected boolean flexible = true;

	public RailGeneric(String tool, int harvestLevel) {
		super();
		setHarvestLevel(tool, harvestLevel);
	}



	@Override
	public float getRailMaxSpeed(World world, EntityMinecart cart, net.minecraft.util.math.BlockPos pos) {
		return maxSpeed;
	}

	public RailGeneric setMaxSpeed(float speed) {
		this.maxSpeed = speed;
		return this;
	}

	@Override
	public boolean isFlexibleRail(IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		return !isPowered;
	}

	public RailGeneric setFlexible(boolean flexible) {
		this.flexible = flexible;
		return this;
	}

	@Override
	public boolean canMakeSlopes(IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		return slopable;
	}

	public RailGeneric setSlopable(boolean slopable) {
		this.slopable = slopable;
		return this;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		float speed = this.maxSpeed / this.baseSpeed;

		if(speed != 1F) {
			list.add((speed > 1 ? TextFormatting.BLUE : TextFormatting.RED) + "Speed: " + ((int) (speed * 100)) + "%");
		}

		if(!flexible) {
			list.add(TextFormatting.RED + "Cannot be used for turns!");
		}

		if(!slopable) {
			list.add(TextFormatting.RED + "Cannot be used for slopes!");
		}
	}
}
