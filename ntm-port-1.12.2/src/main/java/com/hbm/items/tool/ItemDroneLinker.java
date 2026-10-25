package com.hbm.items.tool;

import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.IDroneLinkable;
import com.hbm.util.ChatBuilder;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemDroneLinker extends Item {


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fX, float fY, float fZ) {

		TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tile instanceof IDroneLinkable) {

			if(!world.isRemote) {
				if(!stack.hasTagCompound()) {
					stack.setTagCompound(new NBTTagCompound());
					stack.getTagCompound().setInteger("x", x);
					stack.getTagCompound().setInteger("y", y);
					stack.getTagCompound().setInteger("z", z);

					player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
							.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
							.next("] ").color(TextFormatting.DARK_AQUA)
							.next("Set initial position!").color(TextFormatting.AQUA).flush());

				} else {

					int tx = stack.getTagCompound().getInteger("x");
					int ty = stack.getTagCompound().getInteger("y");
					int tz = stack.getTagCompound().getInteger("z");

					TileEntity prev = world.getTileEntity(new net.minecraft.util.math.BlockPos(tx, ty, tz));

					if(prev instanceof IDroneLinkable) {

						BlockPos dest = ((IDroneLinkable) tile).getPoint();
						((IDroneLinkable) prev).setNextTarget(dest.getX(), dest.getY(), dest.getZ());

						player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
								.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
								.next("] ").color(TextFormatting.DARK_AQUA)
								.next("Link set!").color(TextFormatting.AQUA).flush());
					} else {
						player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
								.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
								.next("] ").color(TextFormatting.DARK_AQUA)
								.next("Previous link lost!").color(TextFormatting.RED).flush());
					}

					stack.getTagCompound().setInteger("x", x);
					stack.getTagCompound().setInteger("y", y);
					stack.getTagCompound().setInteger("z", z);
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inhand) {

		if(world.isRemote && inhand) {
			if(stack.hasTagCompound()) {
				int x = stack.getTagCompound().getInteger("x");
				int y = stack.getTagCompound().getInteger("y");
				int z = stack.getTagCompound().getInteger("z");
				MainRegistry.proxy.displayTooltip("Prev pos: " + x + " / " + y + " / " + z, MainRegistry.proxy.ID_DRONE);
			}
		}
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!world.isRemote && stack.hasTagCompound()) {
			stack.setTagCompound(null);

			player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
					.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
					.next("] ").color(TextFormatting.DARK_AQUA)
					.next("Position cleared!").color(TextFormatting.GREEN).flush());
		}

		return stack;
	}
}
