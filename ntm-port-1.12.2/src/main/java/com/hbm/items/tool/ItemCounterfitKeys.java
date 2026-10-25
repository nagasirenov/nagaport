package com.hbm.items.tool;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntityLockableBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemCounterfitKeys extends Item {


        @Override
    public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
        net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
        int side = facing != null ? facing.getIndex() : 0;
        return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
    }

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int b, float f0, float f1, float f2)
    {
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(te instanceof TileEntityLockableBase) {

			TileEntityLockableBase locked = (TileEntityLockableBase) te;

			if(locked.isLocked()) {
				ItemStack st = new ItemStack(ModItems.key_fake);
				ItemKeyPin.setPins(st, locked.getPins());

				player.inventory.setInventorySlotContents(player.inventory.currentItem, st.copy());

				if(!player.inventory.addItemStackToInventory(st.copy())) {
					player.dropItem(st.copy(), false);
				}

				player.inventoryContainer.detectAndSendChanges();

				player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);

				return true;
			}
		}

		return false;
    }

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag)
	{
		list.add("Use on a locked container to create two counterfeit keys!");
	}

}
