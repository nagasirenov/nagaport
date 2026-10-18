package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IAnalyzable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemAnalysisTool extends Item {

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fX, float fY, float fZ) {
		
		Block b = world.getBlock(x, y, z);
		
		if(b instanceof BlockDummyable) {
			int[] pos = ((BlockDummyable) b).findCore(world, x, y, z);
			
			if(pos != null) {
				x = pos[0];
				y = pos[1];
				z = pos[2];
			}
		}
		
		if(b instanceof IAnalyzable) {
			List<String> debug = ((IAnalyzable) b).getDebugInfo(world, x, y, z);
			
			if(debug != null && !world.isRemote) {
				for(String line : debug) {
					player.sendMessage(new TextComponentString(line).setStyle(new Style().setColor(TextFormatting.YELLOW)));
				}
			}
			
			return true;
		}
		
		return false;
	}
}
