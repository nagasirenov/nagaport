package com.hbm.items.tool;

import java.util.List;

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

public class ItemDetonator extends Item {

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		list.add("Shift right-click to set position,");
		list.add("right-click to detonate!");
		if(itemstack.getTagCompound() == null) {
			list.add(TextFormatting.RED + "No position set!");
		} else {
			list.add(TextFormatting.YELLOW + "Linked to " + itemstack.getTagCompound().getInteger("x") + ", " + itemstack.getTagCompound().getInteger("y") + ", " + itemstack.getTagCompound().getInteger("z"));
		}
	}


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}

		if(player.isSneaking()) {
			stack.getTagCompound().setInteger("x", x);
			stack.getTagCompound().setInteger("y", y);
			stack.getTagCompound().setInteger("z", z);

			if(!world.isRemote) {
				player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
						.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
						.next("] ").color(TextFormatting.DARK_AQUA)
						.next("Position set!").color(TextFormatting.GREEN).flush());
			}

			world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.techBoop"), net.minecraft.util.SoundCategory.PLAYERS, 2.0F, 1.0F);

			return true;
		}

		return false;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(stack.getTagCompound() == null) {
			if(!world.isRemote) {
				player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
						.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
						.next("] ").color(TextFormatting.DARK_AQUA)
						.next("No position set!").color(TextFormatting.RED).flush());
			}
		} else {
			int x = stack.getTagCompound().getInteger("x");
			int y = stack.getTagCompound().getInteger("y");
			int z = stack.getTagCompound().getInteger("z");

			if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() instanceof IBomb) {
				world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.techBleep"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
				if(!world.isRemote) {
					BombReturnCode ret = ((IBomb) com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock()).explode(world, x, y, z);

					if(GeneralConfig.enableExtendedLogging)
						MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by " + player.getDisplayName() + "!");

					player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
							.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
							.next("] ").color(TextFormatting.DARK_AQUA)
							.nextTranslation(ret.getUnlocalizedMessage()).color(ret.wasSuccessful() ? TextFormatting.YELLOW : TextFormatting.RED).flush());
				}

			} else {
				if(!world.isRemote) {
					player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
							.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
							.next("] ").color(TextFormatting.DARK_AQUA)
							.nextTranslation(BombReturnCode.ERROR_NO_BOMB.getUnlocalizedMessage()).color(TextFormatting.RED).flush());
				}
			}
		}

		return stack;

	}

}
