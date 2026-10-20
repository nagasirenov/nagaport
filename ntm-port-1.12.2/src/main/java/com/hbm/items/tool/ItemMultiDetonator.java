package com.hbm.items.tool;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;

import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.IBomb.BombReturnCode;
import com.hbm.main.MainRegistry;
import com.hbm.util.ChatBuilder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemMultiDetonator extends Item {

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add("Shift right-click block to add position,");
		list.add("right-click to detonate!");
		list.add("Shift right-click in the air to clear positions.");

		if(itemstack.getTagCompound() == null || getLocations(itemstack) == null) {
			list.add(TextFormatting.RED + "No position set!");
		} else {

			int[][] locs = getLocations(itemstack);

			for(int i = 0; i < locs[0].length; i++) {

				list.add(TextFormatting.YELLOW + "" + locs[0][i] + " / " + locs[1][i] + " / " + locs[2][i]);
			}
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}

		if(player.isSneaking()) {
			addLocation(stack, x, y, z);

			if(!world.isRemote) {
				player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
						.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
						.next("] ").color(TextFormatting.DARK_AQUA)
						.next("Position added!").color(TextFormatting.GREEN).flush());
			}

			world.playSoundAtEntity(player, "hbm:item.techBoop", 2.0F, 1.0F);

			return true;
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(stack.getTagCompound() == null || getLocations(stack) == null) {
			
			if(!world.isRemote) {
				player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
						.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
						.next("] ").color(TextFormatting.DARK_AQUA)
						.next("No position set!").color(TextFormatting.RED).flush());
			}
			
		} else {

			if(!player.isSneaking()) {
				int[][] locs = getLocations(stack);

				int succ = 0;

				for(int i = 0; i < locs[0].length; i++) {

					int x = locs[0][i];
					int y = locs[1][i];
					int z = locs[2][i];

					if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() instanceof IBomb) {
						
						if(!world.isRemote) {
							BombReturnCode ret = ((IBomb) com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock()).explode(world, x, y, z);
							
							if(ret.wasSuccessful())
								succ++;

							if(GeneralConfig.enableExtendedLogging)
								MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by " + player.getDisplayName() + "!");
						}
					}
				}
				
				world.playSoundAtEntity(player, "hbm:item.techBleep", 1.0F, 1.0F);
				
				if(!world.isRemote) {
					player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
							.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
							.next("] ").color(TextFormatting.DARK_AQUA)
							.next("Triggered " + succ + "/" + locs[0].length + "!").color(TextFormatting.YELLOW).flush());
				}
				
			} else {

				stack.getTagCompound().setIntArray("xValues", new int[0]);
				stack.getTagCompound().setIntArray("yValues", new int[0]);
				stack.getTagCompound().setIntArray("zValues", new int[0]);

				world.playSoundAtEntity(player, "hbm:item.techBoop", 2.0F, 1.0F);
				
				if(!world.isRemote) {
					player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
							.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
							.next("] ").color(TextFormatting.DARK_AQUA)
							.next("Locations cleared!").color(TextFormatting.RED).flush());
				}
			}
		}

		return stack;

	}

	private static void addLocation(ItemStack stack, int x, int y, int z) {

		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}

		int[] xs = stack.getTagCompound().getIntArray("xValues");
		int[] ys = stack.getTagCompound().getIntArray("yValues");
		int[] zs = stack.getTagCompound().getIntArray("zValues");

		stack.getTagCompound().setIntArray("xValues", ArrayUtils.add(xs, x));
		stack.getTagCompound().setIntArray("yValues", ArrayUtils.add(ys, y));
		stack.getTagCompound().setIntArray("zValues", ArrayUtils.add(zs, z));
	}

	private static int[][] getLocations(ItemStack stack) {

		int[] xs = stack.getTagCompound().getIntArray("xValues");
		int[] ys = stack.getTagCompound().getIntArray("yValues");
		int[] zs = stack.getTagCompound().getIntArray("zValues");

		if(xs == null || ys == null || zs == null || xs.length == 0 || ys.length == 0 || zs.length == 0) {
			return null;
		}

		return new int[][] { xs, ys, zs };
	}
}
