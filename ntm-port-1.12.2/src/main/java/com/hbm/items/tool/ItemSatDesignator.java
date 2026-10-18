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
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!world.isRemote) {

			Satellite sat = SatelliteSavedData.getData(world, (int)player.posX, (int)player.posZ).getSatFromFreq(this.getFreq(stack));

			if(sat != null) {
				RayTraceResult pos = Library.rayTrace(player, 300, 1);

				net.minecraft.util.EnumFacing side = pos.sideHit;
				int x = pos.getBlockPos().getX() + side.getFrontOffsetX();
				int y = pos.getBlockPos().getY() + side.getFrontOffsetY();
				int z = pos.getBlockPos().getZ() + side.getFrontOffsetZ();

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
