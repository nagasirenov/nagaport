package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemStructureSolid extends ItemStructureTool {
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		super.addInformation(stack, player, list, ext);
		list.add(TextFormatting.YELLOW + "Click to print a <fillWithMetadataBlocks> or <fillWithBlocks>");
		list.add(TextFormatting.YELLOW + "line with wildcard block and metadata.");
	}

	@Override
	protected boolean dualUse() {
		return true;
	}

	@Override
	protected void doTheThing(ItemStack stack, World world, int x, int y, int z) {
		
		BlockPos pos = this.getAnchor(stack);
		if(pos == null) return;

		int savedX = stack.getTagCompound().getInteger("x");
		int savedY = stack.getTagCompound().getInteger("y");
		int savedZ = stack.getTagCompound().getInteger("z");

		int minX = Math.min(savedX, x) - pos.getX();
		int minY = Math.min(savedY, y) - pos.getY();
		int minZ = Math.min(savedZ, z) - pos.getZ();
		int maxX = Math.max(savedX, x) - pos.getX();
		int maxY = Math.max(savedY, y) - pos.getY();
		int maxZ = Math.max(savedZ, z) - pos.getZ();
		//Assumes the last selected block is the one that all of them are.
		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		String line;
		if(meta > 0)
			line = "fillWithMetadataBlocks(world, box, " + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + ", " + b.getTranslationKey() +", " + meta +");\n";
		else
			line = "fillWithBlocks(world, box, " + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + ", " + b.getTranslationKey() +");\n";
		
		System.out.print(line);
		writeToFile(line);
	}
}
