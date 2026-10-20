package com.hbm.items.block;

import java.util.List;

import com.hbm.blocks.BlockRemap;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemBlockRemap extends ItemBlockBase {

	public ItemBlockRemap(Block block) {
		super(block);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {

		if(!(entity instanceof EntityPlayer) || !(this.block instanceof BlockRemap)) return;

		EntityPlayer player = (EntityPlayer) entity;
		BlockRemap remap = (BlockRemap) this.block;
		player.inventory.setInventorySlotContents(slot, new ItemStack(remap.remapBlock, stack.getCount(), remap.remapMeta));
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.RED + "Compatibility item, hold in inventory to convert!");
	}
}
