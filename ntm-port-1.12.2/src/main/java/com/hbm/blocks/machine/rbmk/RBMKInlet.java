package com.hbm.blocks.machine.rbmk;

import java.util.List;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKInlet;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class RBMKInlet extends BlockContainer implements ITooltipProvider {
	// 1.7.10 type 0 cube; BlockContainer would make it INVISIBLE in 1.12 (render-type parity audit).
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}


	public RBMKInlet(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRBMKInlet();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}
