package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.items.ModItems;
import com.hbm.main.ModSoundEvents;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSupplyCrate extends BlockContainer {

	public BlockSupplyCrate(Material mat) {
		super(mat);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
		setSoundType(SoundType.WOOD);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntitySupplyCrate();
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

	@Override public Item getItemDropped(IBlockState state, Random rand, int fortune) { return Items.AIR; }
	
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		
		if(!player.capabilities.isCreativeMode && !world.isRemote && willHarvest) {
			
			ItemStack drop = new ItemStack(this);
			TileEntitySupplyCrate inv = (TileEntitySupplyCrate) world.getTileEntity(pos);
			NBTTagCompound nbt = new NBTTagCompound();
			
			if(inv != null) {
				for(int i = 0; i < inv.items.size(); i++) {
					ItemStack stack = inv.items.get(i);
					if(stack == null || stack.isEmpty()) continue;
					NBTTagCompound slot = new NBTTagCompound();
					stack.writeToNBT(slot);
					nbt.setTag("slot" + i, slot);
				}
				nbt.setInteger("amount", inv.items.size());
			}
			
			if(!nbt.isEmpty()) drop.setTagCompound(nbt);
			world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
		}
		return world.setBlockToAir(pos);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
		
		TileEntitySupplyCrate inv = (TileEntitySupplyCrate) world.getTileEntity(pos);
		
		if(inv != null && stack.hasTagCompound()) {
			int amount = stack.getTagCompound().getInteger("amount");
			for(int i = 0; i < amount; i++) {
				inv.items.add(new ItemStack(stack.getTagCompound().getCompoundTag("slot" + i)));
			}
		}

		super.onBlockPlacedBy(world, pos, state, player, stack);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack held = player.getHeldItem(hand);
		if(!held.isEmpty() && held.getItem().equals(ModItems.crowbar)) {
			if(!world.isRemote) {
				dropContents(world, pos.getX(), pos.getY(), pos.getZ());
				world.destroyBlock(pos, false);
				world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSoundEvents.BLOCK_CRATE_BREAK, SoundCategory.BLOCKS, 0.5F, 1.0F);
			}
			return true;
		}
		return false;
	}

	public void dropContents(World world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		if(tile instanceof TileEntitySupplyCrate) {
			TileEntitySupplyCrate crate = (TileEntitySupplyCrate) tile;
			
			for(ItemStack item : crate.items) {
				spawnAsEntity(world, new BlockPos(x, y, z), item);
			}
		}
	}
	
	public static class TileEntitySupplyCrate extends TileEntity {
		
		public List<ItemStack> items = new ArrayList();

		/** Metadata changes do not replace the stored inventory's block identity. */
		@Override
		public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
			return oldState.getBlock() != newState.getBlock();
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			items.clear();
			NBTTagList list = nbt.getTagList("items", 10);
			for(int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound nbt1 = list.getCompoundTagAt(i);
				items.add(new ItemStack(nbt1));
			}
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			NBTTagList list = new NBTTagList();
			for(int i = 0; i < items.size(); i++) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				items.get(i).writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
			nbt.setTag("items", list);
			return nbt;
		}
	}
}
