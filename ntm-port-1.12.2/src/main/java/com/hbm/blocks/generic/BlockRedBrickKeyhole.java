package com.hbm.blocks.generic;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockPedestal.TileEntityPedestal;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsRedRoom;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemModDoor;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import com.hbm.lib.WeightedRandomChestContent;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockRedBrickKeyhole extends Block {

	protected IIcon iconFront;
	protected IIcon iconTop;
	protected IIcon blockIcon;

	public BlockRedBrickKeyhole(Material material) {
		super(material);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconFront = iconRegister.registerIcon(RefStrings.MODID + ":stone_keyhole_meta");
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":brick_red_top");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":brick_base");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return side == meta ? (side == 0 || side == 1 ? this.iconTop : this.iconFront) : this.blockIcon;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return null;
	}

	@Override
	public ItemStack getPickBlock(net.minecraft.block.state.IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(ModBlocks.brick_red);
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack stack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int l = net.minecraft.util.EnumFacing.getDirectionFromEntityLiving(nativePlacementPos, player).getIndex();
		com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, l, 2);
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
				generateRoom(world, x - dir.offsetX * 4, y - 2, z - dir.offsetZ * 4, dir);
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

	protected static void generateRoom(World world, int x, int y, int z, ForgeDirection dir) {

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
				if(dir != Library.POS_X) { net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + width; int nativeOwnedWriteY = y + i; int nativeOwnedWriteZ = z + j; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
				if(dir != Library.NEG_X) { net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x - width; int nativeOwnedWriteY = y + i; int nativeOwnedWriteZ = z + j; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
				if(dir != Library.POS_Z) { net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + j; int nativeOwnedWriteY = y + i; int nativeOwnedWriteZ = z + width; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
				if(dir != Library.NEG_Z) { net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + j; int nativeOwnedWriteY = y + i; int nativeOwnedWriteZ = z - width; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
			}
		}

		for(int i = -width + 1; i <= width - 1; i++) {
			for(int j = -width + 1; j <= width - 1; j++) {
				//Floor and ceiling
				{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + i; int nativeOwnedWriteY = y; int nativeOwnedWriteZ = z + j; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
				{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + i; int nativeOwnedWriteY = y + height - 1; int nativeOwnedWriteZ = z + j; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.brick_red; int nativeOwnedWriteMeta = 6; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }

				for(int k = 1; k <= height - 2; k++) {
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x + i; int nativePlacementY = y + k; int nativePlacementZ = z + j; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}
		}

		spawnPedestalItem(world, x, y + 1, z, ItemPool.getPool(ItemPoolsRedRoom.POOL_BLACK_SLAB));
		if(world.rand.nextBoolean()) spawnPedestalItem(world, x + 2, y + 1, z, ItemPool.getPool(ItemPoolsRedRoom.POOL_BLACK_PART));
		if(world.rand.nextBoolean()) spawnPedestalItem(world, x - 2, y + 1, z, ItemPool.getPool(ItemPoolsRedRoom.POOL_BLACK_PART));
		if(world.rand.nextBoolean()) spawnPedestalItem(world, x, y + 1, z + 2, ItemPool.getPool(ItemPoolsRedRoom.POOL_BLACK_PART));
		if(world.rand.nextBoolean()) spawnPedestalItem(world, x, y + 1, z - 2, ItemPool.getPool(ItemPoolsRedRoom.POOL_BLACK_PART));

		//Clear dropped items
		List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(x + 0.5, y, z + 0.5, x + 0.5, y + height, z + 0.5).grow(size / 2D, 0, size / 2D));
		for(EntityItem item : items) item.setDead();
	}

	public static void spawnPedestalItem(World world, int x, int y, int z, WeightedRandomChestContent[] pool) {
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.pedestal);
		TileEntityPedestal pedestal = (TileEntityPedestal) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		pedestal.item = ItemPool.getStack(pool, world.rand).copy();
	}
}
