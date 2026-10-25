package com.hbm.items.tool;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemModDoor extends Item {

	public ItemModDoor() {
		this.maxStackSize = 1;
		this.setCreativeTab(CreativeTabs.REDSTONE);
	}

		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if(side != 1) {
			return false;
		} else {
			++y;

			Block block = Blocks.AIR;

			if(this == ModItems.door_metal) block = ModBlocks.door_metal;
			if(this == ModItems.door_office) block = ModBlocks.door_office;
			if(this == ModItems.door_bunker) block = ModBlocks.door_bunker;
			if(this == ModItems.door_red) block = ModBlocks.door_red;
			if(this == ModItems.fence_gate) block = ModBlocks.fence_gate;

			if(player.canPlayerEdit(new net.minecraft.util.math.BlockPos(x, y, z), net.minecraft.util.EnumFacing.byIndex(side), stack) && player.canPlayerEdit(new net.minecraft.util.math.BlockPos(x, y + 1, z), net.minecraft.util.EnumFacing.byIndex(side), stack)) {
				if(!block.canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x, y, z))) {
					return false;
				} else {
					int i1 = MathHelper.floor((double) ((player.rotationYaw + 180.0F) * 4.0F / 360.0F) - 0.5D) & 3;
					placeDoorBlock(world, x, y, z, i1, block);
					stack.shrink(1);
					return true;
				}
			} else {
				return false;
			}
		}
	}

	public static void placeDoorBlock(World world, int x, int y, int z, int meta, Block door) {
		byte offsetX = 0;
		byte offsetZ = 0;

		if(meta == 0) {
			offsetZ = 1;
		}

		if(meta == 1) {
			offsetX = -1;
		}

		if(meta == 2) {
			offsetZ = -1;
		}

		if(meta == 3) {
			offsetX = 1;
		}

		int i1 = (com.hbm.world.WorldBlockState.read(world, x - offsetX, y, z - offsetZ).isNormalCube() ? 1 : 0) + (com.hbm.world.WorldBlockState.read(world, x - offsetX, y + 1, z - offsetZ).isNormalCube() ? 1 : 0);
		int j1 = (com.hbm.world.WorldBlockState.read(world, x + offsetX, y, z + offsetZ).isNormalCube() ? 1 : 0) + (com.hbm.world.WorldBlockState.read(world, x + offsetX, y + 1, z + offsetZ).isNormalCube() ? 1 : 0);
		boolean flag = com.hbm.world.WorldBlockState.read(world, x - offsetX, y, z - offsetZ).getBlock() == door || com.hbm.world.WorldBlockState.read(world, x - offsetX, y + 1, z - offsetZ).getBlock() == door;
		boolean flag1 = com.hbm.world.WorldBlockState.read(world, x + offsetX, y, z + offsetZ).getBlock() == door || com.hbm.world.WorldBlockState.read(world, x + offsetX, y + 1, z + offsetZ).getBlock() == door;
		boolean flag2 = false;

		if(flag && !flag1) {
			flag2 = true;
		} else if(j1 > i1) {
			flag2 = true;
		}

		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, door, meta, 2);
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y + 1, z, door, 8 | (flag2 ? 1 : 0), 2);
		world.notifyNeighborsOfStateChange(new net.minecraft.util.math.BlockPos(x, y, z), door, true);
		world.notifyNeighborsOfStateChange(new net.minecraft.util.math.BlockPos(x, y + 1, z), door, true);
	}
}
