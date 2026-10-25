package com.hbm.blocks;

import java.util.Random;

import com.hbm.items.ModItems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockLeaves;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRubberLeaves extends BlockLeaves {

	@Override
	public net.minecraft.block.BlockPlanks.EnumType getWoodType(int meta) {
		return net.minecraft.block.BlockPlanks.EnumType.OAK;
	}

	@Override
	public java.util.List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		return java.util.Collections.singletonList(new ItemStack(this));
	}
	@SideOnly(Side.CLIENT)
	private IIcon legacyBlockIcon;

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		String texture = LegacyBlockTextures.atlasName(this);
		if(texture == null) {
			net.minecraft.util.ResourceLocation name = getRegistryName();
			if(name == null) throw new IllegalStateException("Block must be registered before its icon callback: " + this);
			texture = new net.minecraft.util.ResourceLocation(name.getNamespace(), "blocks/" + name.getPath()).toString();
		}
		legacyBlockIcon = register.registerIcon(texture);
	}

	public BlockRubberLeaves withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	public BlockRubberLeaves() {
		super();
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		if(this == ModBlocks.pet_leaves) {
			return ModItems.leaf_pet;
		}
		return ModItems.leaf_rubber;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	protected boolean canSilkHarvest() {
		return false;
	}

	@Override
	public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
		super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);

		if(!world.isRemote) {
			Random rand = world.rand;
			if(this == ModBlocks.rubber_leaves && rand.nextFloat() < 0.3F) {
				net.minecraft.block.Block.spawnAsEntity(world, pos, new net.minecraft.item.ItemStack(ModItems.leaf_rubber));

				if(rand.nextFloat() < 0.5F) {
					net.minecraft.block.Block.spawnAsEntity(world, pos, new net.minecraft.item.ItemStack(ModBlocks.sapling_pvc, 1, 1));
				}
			}
			if(this == ModBlocks.pet_leaves && rand.nextFloat() < 0.3F) {
				net.minecraft.block.Block.spawnAsEntity(world, pos, new net.minecraft.item.ItemStack(ModItems.leaf_pet));

				if(rand.nextFloat() < 0.5F) {
					net.minecraft.block.Block.spawnAsEntity(world, pos, new net.minecraft.item.ItemStack(ModBlocks.sapling_pvc, 1, 0));
				}
			}
		}
	}

	// Resetting some leaf stuff back to `Block` implementation, as we don't use biome colours, and we like our leaves dense
	@SideOnly(Side.CLIENT)
	public int getBlockColor() {
		return 0xFFFFFF;
	}

	@SideOnly(Side.CLIENT)
	public int getRenderColor(int meta) {
		return 0xFFFFFF;
	}

	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		return 0xFFFFFF;
	}

	public IIcon getIcon(int side, int meta) {
		return this.legacyBlockIcon;
	}

	public String[] func_150125_e() {
		// we don't use `ItemLeaves` so just give nothing
		return null;
	}


}
