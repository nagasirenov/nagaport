package com.hbm.items.tool;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBedrockOreTE.TileEntityBedrockOre;
import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemSurveyScanner extends Item {

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!world.isRemote) {

			int x = (int) Math.floor(player.posX);
			int y = (int) Math.floor(player.posY);
			int z = (int) Math.floor(player.posZ);

			boolean hasOil = false;
			boolean hasGas = false;
			boolean hasBrine = false;
			boolean hasColtan = false;
			boolean hasBedrockOil = false;
			boolean hasDepth = false;
			boolean hasSchist = false;
			boolean hasAussie = false;
			TileEntityBedrockOre tile = null;

			for(int a = -5; a <= 5; a++) {
				for(int b = -5; b <= 5; b++) {
					for(int i =  y + 15; i > 1; i -= 2) {

						Block block = com.hbm.world.WorldBlockState.read(world, x + a * 5, i, z + b * 5).getBlock();

						//wow, this sucks!
						if(block == ModBlocks.ore_oil) hasOil = true;
						else if(block == ModBlocks.ore_gas) hasGas = true;
						else if(block == ModBlocks.ore_brine) hasBrine = true;
						else if(block == ModBlocks.ore_coltan) hasColtan = true;
						else if(block == ModBlocks.ore_bedrock_oil) hasBedrockOil = true;
						else if(block == ModBlocks.stone_depth) hasDepth = true;
						else if(block == ModBlocks.stone_depth_nether) hasDepth = true;
						else if(block == ModBlocks.stone_gneiss) hasSchist = true;
						else if(block == ModBlocks.ore_australium) hasAussie = true;
					}

					Block block = com.hbm.world.WorldBlockState.read(world, x + a * 2, 0, z + b * 2).getBlock();

					if(block == ModBlocks.ore_bedrock) {
						tile = (TileEntityBedrockOre) world.getTileEntity(new net.minecraft.util.math.BlockPos(x + a * 2, 0, z + b * 2));
					}
				}
			}

			if(hasOil) player.sendMessage(new TextComponentString("Found OIL!").setStyle(new Style().setColor(TextFormatting.BLACK)));
			if(hasGas) player.sendMessage(new TextComponentString("Found NATURAL GAS!").setStyle(new Style().setColor(TextFormatting.WHITE)));
			if(hasBrine) player.sendMessage(new TextComponentString("Found BRINE!").setStyle(new Style().setColor(TextFormatting.WHITE)));
			if(hasBedrockOil) player.sendMessage(new TextComponentString("Found BEDROCK OIL!").setStyle(new Style().setColor(TextFormatting.BLACK)));
			if(hasColtan) player.sendMessage(new TextComponentString("Found COLTAN!").setStyle(new Style().setColor(TextFormatting.GOLD)));
			if(hasDepth) player.sendMessage(new TextComponentString("Found DEPTH ROCK!").setStyle(new Style().setColor(TextFormatting.GRAY)));
			if(hasSchist) player.sendMessage(new TextComponentString("Found SCHIST!").setStyle(new Style().setColor(TextFormatting.DARK_AQUA)));
			if(hasAussie) player.sendMessage(new TextComponentString("Found AUSTRALIUM!").setStyle(new Style().setColor(TextFormatting.YELLOW)));
			if(tile != null && tile.resource != null) player.sendMessage(new TextComponentString("Found BEDROCK ORE for " + tile.resource.getDisplayName() + "!").setStyle(new Style().setColor(TextFormatting.RED)));
		}

		player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);

		return stack;

	}


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int i, float f0, float f1, float f2) {
		if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.block_beryllium && player.inventory.hasItemStack(new net.minecraft.item.ItemStack(ModItems.entanglement_kit))) {
			player.changeDimension(1);
			return true;
		}

		return false;
	}
}
