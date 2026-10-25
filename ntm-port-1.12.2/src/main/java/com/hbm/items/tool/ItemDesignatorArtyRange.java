package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.Library;
import com.hbm.tileentity.turret.TileEntityTurretBaseArtillery;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemDesignatorArtyRange extends Item {

	public ItemDesignatorArtyRange() {
		this.setFull3D();
		this.setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		if(itemstack.getTagCompound() == null) {
			list.add(TextFormatting.RED + "No turret linked!");
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

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

		if(b instanceof BlockDummyable) {
			int pos[] = ((BlockDummyable) b).findCore(world, x, y, z);

			if(pos == null)
				return false;

			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

			if(te instanceof TileEntityTurretBaseArtillery) {

				if(world.isRemote)
					return true;

				if(!stack.hasTagCompound())
					stack.setTagCompound(new NBTTagCompound());

				stack.getTagCompound().setInteger("x", pos[0]);
				stack.getTagCompound().setInteger("y", pos[1]);
				stack.getTagCompound().setInteger("z", pos[2]);
				world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.techBleep"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
				return true;
			}
		}

		return false;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!stack.hasTagCompound())
			return stack;

		RayTraceResult pos = Library.rayTrace(player, 500, 1);
		int x = pos.getBlockPos().getX();
		int y = pos.getBlockPos().getY();
		int z = pos.getBlockPos().getZ();

		if(!world.isRemote) {
			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(stack.getTagCompound().getInteger("x"), stack.getTagCompound().getInteger("y"), stack.getTagCompound().getInteger("z")));

			if(te instanceof TileEntityTurretBaseArtillery) {
				TileEntityTurretBaseArtillery arty = (TileEntityTurretBaseArtillery) te;
				arty.enqueueTarget(x + 0.5, y + 0.5, z + 0.5);
				world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.techBoop"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
		}

		return stack;
	}
}
