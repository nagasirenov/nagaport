package com.hbm.items.tool;

import com.hbm.tileentity.machine.TileEntityLockableBase;
import com.hbm.util.CompatExternal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemLock extends ItemKeyPin {

	public double lockMod = 0.1D;

	public ItemLock(double mod) {
		lockMod = mod;
	}


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int i, float f0, float f1, float f2) {

		if(this.getPins(stack) != 0) {
			TileEntity te = CompatExternal.getCoreFromPos(world, x, y, z);

			if(te != null && te instanceof TileEntityLockableBase) {
				TileEntityLockableBase tile = (TileEntityLockableBase) te;

				if(tile.isLocked())
					return false;

				tile.setPins(this.getPins(stack));
				tile.lock();
				tile.setMod(lockMod);

				world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:block.lockHang"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
				stack.shrink(1);

				return true;
			}
		}

		return false;
	}

}
