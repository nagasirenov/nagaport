package com.hbm.items.tool;

import com.hbm.items.machine.ItemSatChip;
import com.hbm.lib.Library;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.Satellite.Interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemSatDesignator extends ItemSatChip {

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!world.isRemote) {

			Satellite sat = SatelliteSavedData.getData(world, (int)player.posX, (int)player.posZ).getSatFromFreq(this.getFreq(stack));

			if(sat != null) {
				RayTraceResult pos = Library.rayTrace(player, 300, 1);

				net.minecraft.util.EnumFacing side = pos.sideHit;
				int x = pos.getBlockPos().getX() + side.getXOffset();
				int y = pos.getBlockPos().getY() + side.getYOffset();
				int z = pos.getBlockPos().getZ() + side.getZOffset();

				if(sat.satIface == Interfaces.SAT_COORD) {
					sat.onCoordAction(world, player, x, y, z);
				} else if(sat.satIface == Interfaces.SAT_PANEL) {
					sat.onClick(world, x, z);
				}
			}
		}

		return stack;
	}

}
