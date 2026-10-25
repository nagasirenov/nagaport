package com.hbm.items.tool;

import com.hbm.blocks.machine.ReactorZirnox;
import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.tileentity.machine.TileEntityReactorZirnox;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKBase;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemDyatlov extends Item {

		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int i, float f0, float f1, float f2) {

		if(!world.isRemote) {

			Block block = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

			if(block instanceof RBMKBase) {

				RBMKBase rbmk = (RBMKBase)block;

				int[] pos = rbmk.findCore(world, x, y, z);

				if(pos != null) {

					TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

					if(te instanceof TileEntityRBMKBase) {

						((TileEntityRBMKBase)te).meltdown();
						//((TileEntityRBMKBase)te).heat = 100000;
					}
				}
			}

			if(block instanceof ReactorZirnox) {

				ReactorZirnox zirnox = (ReactorZirnox)block;

				int[] pos = zirnox.findCore(world, x, y, z);

				if(pos != null) {

					TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

					if(te instanceof TileEntityReactorZirnox) {
						((TileEntityReactorZirnox)te).heat = 200000;
					}
				}
			}
		}

		return false;
	}
}
