package com.hbm.items.tool;

import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.BonemealEvent;

public class ItemFertilizer extends Item {


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fX, float fY, float fZ) {

		if(!player.canPlayerEdit(new net.minecraft.util.math.BlockPos(x, y, z), net.minecraft.util.EnumFacing.byIndex(side), stack)) {
			return false;
		}

		world.captureBlockSnapshots = false; //what the fuck is the point of this?!

		boolean didSomething = false;

		for(int i = x - 1; i <= x + 1; i++) {
			for(int j = y - 1; j <= y + 1; j++) {
				for(int k = z - 1; k <= z + 1; k++) {
					boolean success = fertilize(world, i, j, k, player, i == x && j == y && k == z);
					didSomething = didSomething || success;
					if(success && !world.isRemote) {
						world.playEvent(2005, new net.minecraft.util.math.BlockPos(i, j, k), 0);
					}
				}
			}
		}

		if(didSomething && !player.capabilities.isCreativeMode) {
			stack.shrink(1);
		}

		return false;
	}

	public static boolean useFertillizer(ItemStack stack, World world, int x, int y, int z) {

		if(!(world instanceof WorldServer)) return false;
		EntityPlayer player = FakePlayerFactory.getMinecraft((WorldServer)world);

		boolean didSomething = false;

		for(int i = x - 1; i <= x + 1; i++) {
			for(int j = y - 1; j <= y + 1; j++) {
				for(int k = z - 1; k <= z + 1; k++) {
					boolean success = fertilize(world, i, j, k, player, i == x && j == y && k == z);
					didSomething = didSomething || success;
					if(success && !world.isRemote) {
						world.playEvent(2005, new net.minecraft.util.math.BlockPos(i, j, k), 0);
					}
				}
			}
		}

		if(didSomething) stack.shrink(1);

		return didSomething;
	}

	public static boolean fertilize(World world, int x, int y, int z, EntityPlayer player, boolean force) {
		net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);
		net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
		Block b = state.getBlock();

		BonemealEvent event = new BonemealEvent(player, world, pos, state, net.minecraft.util.EnumHand.MAIN_HAND, player != null ? player.getHeldItemMainhand() : ItemStack.EMPTY);
		if(MinecraftForge.EVENT_BUS.post(event)) {
			return false;
		}

		if(event.getResult() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
			return true;
		}

		if(b instanceof IGrowable) {
			IGrowable growable = (IGrowable) b;

			if(growable.canGrow(world, pos, state, world.isRemote)) {
				if(!world.isRemote) {
					if(force || growable.canUseBonemeal(world, world.rand, pos, state)) {
						growable.grow(world, world.rand, pos, state);
					}
				}
				return true;
			}
		}

		return false;
	}
}
