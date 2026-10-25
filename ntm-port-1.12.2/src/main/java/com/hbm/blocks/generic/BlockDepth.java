package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.item.IDepthRockTool;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockDepth extends Block implements ITooltipProvider {

	public BlockDepth() {
		super(Material.ROCK);
		this.setBlockUnbreakable();
		this.setResistance(10.0F);
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(net.minecraft.block.state.IBlockState state, EntityPlayer player, World world, net.minecraft.util.math.BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		
		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof IDepthRockTool) {
			
			if(((IDepthRockTool)com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem()).canBreakRock(world, player, com.hbm.util.NativeHeldItem.getMainHandNullable(player), this, x, y, z))
			return (float) (1D / 50D);
		}
		
		return super.getPlayerRelativeBlockHardness(com.hbm.world.WorldBlockState.read(world, x, y, z), player, world, pos);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.tile.depth"));
	}
}