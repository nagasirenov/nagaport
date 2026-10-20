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

	public BlockRedBrickKeyhole(Material material) {
		super(material);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconFront = iconRegister.registerIcon(RefStrings.MODID + ":stone_keyhole_meta");
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":brick_red_top");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":brick_base");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return side == meta ? (side == 0 || side == 1 ? this.iconTop : this.iconFront) : this.blockIcon;
	}
	
	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return null;
	}

	@Override
	public ItemStack getPickBlock(RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(ModBlocks.brick_red);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		int l = BlockPistonBase.determineOrientation(world, x, y, z, player);
		world.setBlockMetadataWithNotify(x, y, z, l, 2);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		
		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null) {
			boolean cracked = com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.key_red_cracked;
			if((com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.key_red || cracked) && side != 0 && side != 1) {
				if(cracked) com.hbm.util.NativeHeldItem.getMainHandNullable(player).stackSize--;
				if(world.isRemote) return true;
				ForgeDirection dir = ForgeDirection.getOrientation(side);
				generateRoom(world, x - dir.offsetX * 4, y - 2, z - dir.offsetZ * 4, dir);
				int b = 0;
				if(side == 2) b = 1;
				if(side == 5) b = 2;
				if(side == 3) b = 3;
				if(side == 4) b = 0;
				ItemModDoor.placeDoorBlock(world, x, y - 1, z, b, ModBlocks.door_red);
				world.playSoundAtEntity(player, "hbm:block.lockOpen", 1.0F, 1.0F);
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
			world.setBlock(x + i, y, z + width, ModBlocks.brick_red, 6, 3);
			world.setBlock(x + i, y, z - width, ModBlocks.brick_red, 6, 3);
			world.setBlock(x + width, y, z + i, ModBlocks.brick_red, 6, 3);
			world.setBlock(x - width, y, z + i, ModBlocks.brick_red, 6, 3);
			world.setBlock(x + i, y + height - 1, z + width, ModBlocks.brick_red, 6, 3);
			world.setBlock(x + i, y + height - 1, z - width, ModBlocks.brick_red, 6, 3);
			world.setBlock(x + width, y + height - 1, z + i, ModBlocks.brick_red, 6, 3);
			world.setBlock(x - width, y + height - 1, z + i, ModBlocks.brick_red, 6, 3);
		}
		
		for(int i = 1; i <= height - 2; i++) {
			//Outer edges, sides
			world.setBlock(x + width, y + i, z + width, ModBlocks.brick_red, 6, 3);
			world.setBlock(x + width, y + i, z - width, ModBlocks.brick_red, 6, 3);
			world.setBlock(x - width, y + i, z + width, ModBlocks.brick_red, 6, 3);
			world.setBlock(x - width, y + i, z - width, ModBlocks.brick_red, 6, 3);
			
			//Walls
			for(int j = -width + 1; j <= width - 1; j++) {
				if(dir != Library.POS_X) world.setBlock(x + width, y + i, z + j, ModBlocks.brick_red, 6, 3);
				if(dir != Library.NEG_X) world.setBlock(x - width, y + i, z + j, ModBlocks.brick_red, 6, 3);
				if(dir != Library.POS_Z) world.setBlock(x + j, y + i, z + width, ModBlocks.brick_red, 6, 3);
				if(dir != Library.NEG_Z) world.setBlock(x + j, y + i, z - width, ModBlocks.brick_red, 6, 3);
			}
		}
		
		for(int i = -width + 1; i <= width - 1; i++) {
			for(int j = -width + 1; j <= width - 1; j++) {
				//Floor and ceiling
				world.setBlock(x + i, y, z + j, ModBlocks.brick_red, 6, 3);
				world.setBlock(x + i, y + height - 1, z + j, ModBlocks.brick_red, 6, 3);
				
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
		List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(x + 0.5, y, z + 0.5, x + 0.5, y + height, z + 0.5).expand(size / 2D, 0, size / 2D));
		for(EntityItem item : items) item.setDead();
	}
	
	public static void spawnPedestalItem(World world, int x, int y, int z, WeightedRandomChestContent[] pool) {
		world.setBlock(x, y, z, ModBlocks.pedestal);
		TileEntityPedestal pedestal = (TileEntityPedestal) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		pedestal.item = ItemPool.getStack(pool, world.rand).copy();
	}
}
