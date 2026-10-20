package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.items.ModItems;
import com.hbm.main.ModSoundEvents;
import com.hbm.items.food.ItemConserve.EnumFoodType;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockCanCrate extends Block {

	public BlockCanCrate(Material p_i45394_1_) {
		super(p_i45394_1_);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
		setSoundType(SoundType.WOOD);
	}

	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);

	@Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
	@Override public IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(METADATA, metadata & 15); }
	@Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
	@Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
	@Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
	@Override public boolean isOpaqueCube(IBlockState state) { return false; }
	@Override public boolean isFullCube(IBlockState state) { return false; }
	@Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack held = player.getHeldItem(hand);
		if(!held.isEmpty() && held.getItem().equals(ModItems.crowbar)) {
			if(!world.isRemote) {
				dropContents(world, pos.getX(), pos.getY(), pos.getZ());
				world.setBlockToAir(pos);
				world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSoundEvents.BLOCK_CRATE_BREAK, SoundCategory.BLOCKS, 0.5F, 1.0F);
			}
			return true;
		}
		return false;
	}

	Random rand = new Random();

	public void dropContents(World world, int x, int y, int z) {
		ArrayList<ItemStack> items = getContents(world, x, y, z);

		for(ItemStack item : items) {
			spawnAsEntity(world, new BlockPos(x, y, z), item);
		}
	}

	public ArrayList<ItemStack> getContents(World world, int x, int y, int z) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

		int count = getContentAmount(world.rand);
		for(int i = 0; i < count; i++) {
			ret.add(getRandomItem(world.rand));
		}

		return ret;
	}

	public ItemStack getRandomItem(Random rand) {

		List<ItemStack> items = new ArrayList();
		for(int a = 0; a < EnumFoodType.values().length; a++)
			items.add(new ItemStack(ModItems.canned_conserve, 1, a));
		items.add(new ItemStack(ModItems.can_smart));
		items.add(new ItemStack(ModItems.can_creature));
		items.add(new ItemStack(ModItems.can_redbomb));
		items.add(new ItemStack(ModItems.can_mrsugar));
		items.add(new ItemStack(ModItems.can_overcharge));
		items.add(new ItemStack(ModItems.can_luna));
		items.add(new ItemStack(ModItems.can_breen));
		items.add(new ItemStack(ModItems.can_bepis));
		items.add(new ItemStack(ModItems.pudding));

		return items.get(rand.nextInt(items.size()));
	}

	public int getContentAmount(Random rand) {
		return 5 + rand.nextInt(4);
	}
}
