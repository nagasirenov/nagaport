package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemWand extends Item {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		list.add("Creative-only item");
		list.add("\"Destruction brings creation\"");
		list.add("(Set positions with right click,");
		list.add("set block with shift-right click!)");

		if(stack.getTagCompound() != null && !(stack.getTagCompound().getInteger("x") == 0 && stack.getTagCompound().getInteger("y") == 0 && stack.getTagCompound().getInteger("z") == 0)) {
			list.add("Pos: " + stack.getTagCompound().getInteger("x") + ", " + stack.getTagCompound().getInteger("y") + ", " + stack.getTagCompound().getInteger("z"));
		} else {
			list.add("Positions not set!");
		}
		if(stack.getTagCompound() != null)
			list.add("Block saved: " + Block.getBlockById(stack.getTagCompound().getInteger("block")).getTranslationKey());
	}


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}

		if(player.isSneaking()) {
			stack.getTagCompound().setInteger("block", Block.getIdFromBlock(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock()));
			stack.getTagCompound().setInteger("meta", com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ));
			if(world.isRemote)
				player.sendMessage(new TextComponentString("Set block " + Block.getBlockById(stack.getTagCompound().getInteger("block")).getTranslationKey()));
		} else {
			if(stack.getTagCompound().getInteger("x") == 0 && stack.getTagCompound().getInteger("y") == 0 && stack.getTagCompound().getInteger("z") == 0) {
				stack.getTagCompound().setInteger("x", x);
				stack.getTagCompound().setInteger("y", y);
				stack.getTagCompound().setInteger("z", z);
				if(world.isRemote)
					player.sendMessage(new TextComponentString("Position set!"));
			} else {

				int ox = stack.getTagCompound().getInteger("x");
				int oy = stack.getTagCompound().getInteger("y");
				int oz = stack.getTagCompound().getInteger("z");

				stack.getTagCompound().setInteger("x", 0);
				stack.getTagCompound().setInteger("y", 0);
				stack.getTagCompound().setInteger("z", 0);

				if(!world.isRemote) {
					Block block = Block.getBlockById(stack.getTagCompound().getInteger("block"));
					int meta = stack.getTagCompound().getInteger("meta");
					boolean replaceAir = block == ModBlocks.wand_air;

					for(int i = Math.min(ox, x); i <= Math.max(ox, x); i++) {
						for(int j = Math.min(oy, y); j <= Math.max(oy, y); j++) {
							for(int k = Math.min(oz, z); k <= Math.max(oz, z); k++) {
								if(replaceAir && com.hbm.world.WorldBlockState.read(world, i, j, k).getBlock() != Blocks.AIR) continue;
								com.hbm.world.WorldBlockState.writeLegacyBlock(world, i, j, k, block, meta, 3);
							}
						}
					}
				}
				if(world.isRemote)
					player.sendMessage(new TextComponentString("Selection filled!"));
			}
		}

		return true;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		if(player.isSneaking()) {
			stack.getTagCompound().setInteger("block", 0);
			stack.getTagCompound().setInteger("meta", 0);
			if(world.isRemote)
				player.sendMessage(new TextComponentString("Set block " + Block.getBlockById(stack.getTagCompound().getInteger("block")).getTranslationKey()));
		}

		return stack;
	}

}
