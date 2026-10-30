package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockLoot.TileEntityLoot;
import com.hbm.blocks.generic.BlockPedestal.TileEntityPedestal;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsRedRoom;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemModDoor;
import com.hbm.main.MainRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockStone;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import com.hbm.lib.WeightedRandomChestContent;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockKeyhole extends BlockStone {

	protected IIcon iconTop;
	protected IIcon blockIcon;

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		// 1.7.10 super.registerBlockIcons registered the side icon from the block texture name.
		this.blockIcon = iconRegister.registerIcon(com.hbm.blocks.LegacyBlockTextures.atlasName(this));
		this.iconTop = iconRegister.registerIcon("stone");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return side == 0 || side == 1 ? this.iconTop : this.blockIcon;
	}

	@Override
	public ItemStack getPickBlock(net.minecraft.block.state.IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Blocks.STONE);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null) {
			boolean cracked = com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.key_red_cracked;
			if((com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.key_red || cracked) && side != 0 && side != 1) {
				if(cracked) com.hbm.util.NativeHeldItem.getMainHandNullable(player).shrink(1);
				if(world.isRemote) return true;
				ForgeDirection dir = ForgeDirection.getOrientation(side);
				generateRoom(world, x - dir.offsetX * 4, y - 2, z - dir.offsetZ * 4);
				int b = 0;
				if(side == 2) b = 1;
				if(side == 5) b = 2;
				if(side == 3) b = 3;
				if(side == 4) b = 0;
				ItemModDoor.placeDoorBlock(world, x, y - 1, z, b, ModBlocks.door_red);
				player.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:block.lockOpen"), 1.0F, 1.0F);
				com.hbm.advancement.NtmAdvancements.award(player, MainRegistry.achRedRoom);
				return true;
			}
		}

		return false;
	}

	protected static void generateRoom(World world, int x, int y, int z) {

		int size = 9;
		int height = 5;
		int width = size / 2;

		//Outer Edges, top and bottom
		for(int i = -width; i <= width; i++) {
			{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + i; int nativeOwnedWriteY = y; int nativeOwnedWriteZ = z + width; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
			{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + i; int nativeOwnedWriteY = y; int nativeOwnedWriteZ = z - width; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
			{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + width; int nativeOwnedWriteY = y; int nativeOwnedWriteZ = z + i; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
			{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x - width; int nativeOwnedWriteY = y; int nativeOwnedWriteZ = z + i; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
			{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + i; int nativeOwnedWriteY = y + height - 1; int nativeOwnedWriteZ = z + width; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
			{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + i; int nativeOwnedWriteY = y + height - 1; int nativeOwnedWriteZ = z - width; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
			{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + width; int nativeOwnedWriteY = y + height - 1; int nativeOwnedWriteZ = z + i; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
			{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x - width; int nativeOwnedWriteY = y + height - 1; int nativeOwnedWriteZ = z + i; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		}

		for(int i = 1; i <= height - 2; i++) {
			//Outer edges, sides
			{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + width; int nativeOwnedWriteY = y + i; int nativeOwnedWriteZ = z + width; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
			{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + width; int nativeOwnedWriteY = y + i; int nativeOwnedWriteZ = z - width; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
			{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x - width; int nativeOwnedWriteY = y + i; int nativeOwnedWriteZ = z + width; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
			{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x - width; int nativeOwnedWriteY = y + i; int nativeOwnedWriteZ = z - width; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }

			//Walls
			for(int j = -width + 1; j <= width - 1; j++) {
				{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + width; int nativeOwnedWriteY = y + i; int nativeOwnedWriteZ = z + j; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
				{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x - width; int nativeOwnedWriteY = y + i; int nativeOwnedWriteZ = z + j; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 5; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
				{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + j; int nativeOwnedWriteY = y + i; int nativeOwnedWriteZ = z + width; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 2; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
				{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + j; int nativeOwnedWriteY = y + i; int nativeOwnedWriteZ = z - width; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 3; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
			}
		}

		if(world.rand.nextInt(1) == 0) {
			int r = world.rand.nextInt(4);
			if(r == 0) com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + width, y + 2, z, ModBlocks.stone_keyhole_meta, 4, 3);
			if(r == 1) com.hbm.world.WorldBlockState.writeLegacyBlock(world, x - width, y + 2, z, ModBlocks.stone_keyhole_meta, 5, 3);
			if(r == 2) com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y + 2, z + width, ModBlocks.stone_keyhole_meta, 2, 3);
			if(r == 3) com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y + 2, z - width, ModBlocks.stone_keyhole_meta, 3, 3);
		}

		for(int i = -width + 1; i <= width - 1; i++) {
			for(int j = -width + 1; j <= width - 1; j++) {
				//Floor and ceiling
				{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + i; int nativeOwnedWriteY = y; int nativeOwnedWriteZ = z + j; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 1; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
				{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + i; int nativeOwnedWriteY = y + height - 1; int nativeOwnedWriteZ = z + j; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 0; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }

				for(int k = 1; k <= height - 2; k++) {
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + i; int nativePlacementY = y + k; int nativePlacementZ = z + j; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}
		}

		//Torches
		int torchDist = width - 1;
		int torchOff = torchDist - 1;
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + torchDist, y + 2, z + torchOff, Blocks.TORCH);
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + torchDist, y + 2, z - torchOff, Blocks.TORCH);
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x - torchDist, y + 2, z + torchOff, Blocks.TORCH);
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x - torchDist, y + 2, z - torchOff, Blocks.TORCH);
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + torchOff, y + 2, z + torchDist, Blocks.TORCH);
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x - torchOff, y + 2, z + torchDist, Blocks.TORCH);
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + torchOff, y + 2, z - torchDist, Blocks.TORCH);
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x - torchOff, y + 2, z - torchDist, Blocks.TORCH);

		//Cobwebs
		if(world.rand.nextInt(4) == 0) {
			for(int i = -width + 1; i <= width - 1; i++) {
				for(int j = -width + 1; j <= width - 1; j++) {
					if(world.rand.nextBoolean()) { net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + i; int nativePlacementY = y + height - 2; int nativePlacementZ = z + j; net.minecraft.block.Block nativePlacementBlock = Blocks.WEB; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}
		}

		//Pillars
		if(world.rand.nextInt(4) == 0) {
			for(int i = 1; i <= height - 2; i++) {
				com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + width - 2, y + i, z + width - 2, ModBlocks.concrete_colored, 14, 3);
				com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + width - 2, y + i, z - width + 2, ModBlocks.concrete_colored, 14, 3);
				com.hbm.world.WorldBlockState.writeLegacyBlock(world, x - width + 2, y + i, z + width - 2, ModBlocks.concrete_colored, 14, 3);
				com.hbm.world.WorldBlockState.writeLegacyBlock(world, x - width + 2, y + i, z - width + 2, ModBlocks.concrete_colored, 14, 3);
			}
		}

		//Fire
		if(world.rand.nextInt(4) == 0) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + width - 1; int nativePlacementY = y; int nativePlacementZ = z + width - 1; net.minecraft.block.Block nativePlacementBlock = Blocks.NETHERRACK; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + width - 1; int nativePlacementY = y; int nativePlacementZ = z - width + 1; net.minecraft.block.Block nativePlacementBlock = Blocks.NETHERRACK; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x - width + 1; int nativePlacementY = y; int nativePlacementZ = z + width - 1; net.minecraft.block.Block nativePlacementBlock = Blocks.NETHERRACK; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x - width + 1; int nativePlacementY = y; int nativePlacementZ = z - width + 1; net.minecraft.block.Block nativePlacementBlock = Blocks.NETHERRACK; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + width - 1; int nativePlacementY = y + 1; int nativePlacementZ = z + width - 1; net.minecraft.block.Block nativePlacementBlock = Blocks.FIRE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + width - 1; int nativePlacementY = y + 1; int nativePlacementZ = z - width + 1; net.minecraft.block.Block nativePlacementBlock = Blocks.FIRE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x - width + 1; int nativePlacementY = y + 1; int nativePlacementZ = z + width - 1; net.minecraft.block.Block nativePlacementBlock = Blocks.FIRE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x - width + 1; int nativePlacementY = y + 1; int nativePlacementZ = z - width + 1; net.minecraft.block.Block nativePlacementBlock = Blocks.FIRE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}

		//Circle
		if(world.rand.nextInt(4) == 0) {
			for(int i = -1; i <= 1; i++) {
				for(int j = -1; j <= 1; j++) {
					if(i != 0 || j != 0) com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + i, y, z + j, ModBlocks.concrete_colored, 14, 3);
				}
			}
		}

		//Lava
		if(world.rand.nextInt(4) == 0) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + width - 2; int nativePlacementY = y; int nativePlacementZ = z + width - 1; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + width - 3; int nativePlacementY = y; int nativePlacementZ = z + width - 1; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x - width + 2; int nativePlacementY = y; int nativePlacementZ = z + width - 1; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x - width + 3; int nativePlacementY = y; int nativePlacementZ = z + width - 1; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + width - 2; int nativePlacementY = y; int nativePlacementZ = z - width + 1; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + width - 3; int nativePlacementY = y; int nativePlacementZ = z - width + 1; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x - width + 2; int nativePlacementY = y; int nativePlacementZ = z - width + 1; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x - width + 3; int nativePlacementY = y; int nativePlacementZ = z - width + 1; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + width - 1; int nativePlacementY = y; int nativePlacementZ = z + width - 2; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + width - 1; int nativePlacementY = y; int nativePlacementZ = z + width - 3; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + width - 1; int nativePlacementY = y; int nativePlacementZ = z - width + 2; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + width - 1; int nativePlacementY = y; int nativePlacementZ = z - width + 3; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x - width + 1; int nativePlacementY = y; int nativePlacementZ = z + width - 2; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x - width + 1; int nativePlacementY = y; int nativePlacementZ = z + width - 3; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x - width + 1; int nativePlacementY = y; int nativePlacementZ = z - width + 2; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x - width + 1; int nativePlacementY = y; int nativePlacementZ = z - width + 3; net.minecraft.block.Block nativePlacementBlock = Blocks.LAVA; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}

		int rand = world.rand.nextInt(20);

		if(rand == 0) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y + 1, z, ModBlocks.deco_loot);
			TileEntityLoot loot = (TileEntityLoot) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y + 1, z));

			if(world.rand.nextInt(5) == 0) {
				loot.addItem(new ItemStack(ModItems.ncrpa_helmet), 0, 0, 0);
				loot.addItem(new ItemStack(ModItems.ncrpa_plate), 0, 0, 0);
				loot.addItem(new ItemStack(ModItems.ncrpa_legs), 0, 0, 0);
				loot.addItem(new ItemStack(ModItems.ncrpa_boots), 0, 0, 0);
			} else {
				loot.addItem(new ItemStack(ModItems.trenchmaster_helmet), 0, 0, 0);
				loot.addItem(new ItemStack(ModItems.trenchmaster_plate), 0, 0, 0);
				loot.addItem(new ItemStack(ModItems.trenchmaster_legs), 0, 0, 0);
				loot.addItem(new ItemStack(ModItems.trenchmaster_boots), 0, 0, 0);
			}
		} else {
			spawnPedestalItem(world, x, y + 1, z);
		}

		//Clear dropped items
		List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(x + 0.5, y, z + 0.5, x + 0.5, y + height, z + 0.5).expand(size / 2D, 0, size / 2D));
		for(EntityItem item : items) item.setDead();
	}

	public static void spawnPedestalItem(World world, int x, int y, int z) {
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.pedestal);
		TileEntityPedestal pedestal = (TileEntityPedestal) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		WeightedRandomChestContent content = WeightedRandomChestContent.getRandomItem(world.rand, ItemPool.getPool(ItemPoolsRedRoom.POOL_RED_PEDESTAL));
		pedestal.item = content.theItemId.copy();
	}
}
