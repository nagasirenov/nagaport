package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.IStepTickReceiver;
import com.hbm.blocks.BlockBase;
import com.hbm.render.icon.PlainCubeLegacyIcons;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;

public class BlockMultiSlab extends BlockSlab implements IStepTickReceiver {
	public static final PropertyInteger MATERIAL = PropertyInteger.create("variant", 0, 7);

	public static List<Object[]> recipeGen = new ArrayList();

	public Block[] slabMaterials;
	public Block single;
	private boolean doubleSlab;

	public BlockMultiSlab(Block single, Material mat, Block... slabMaterials) {
		this(single != null, mat);
		this.single = single;
		this.slabMaterials = slabMaterials;
		this.useNeighborBrightness = true;

		if(single == null) {
			for(int i = 0; i < slabMaterials.length; i++) {
				recipeGen.add(new Object[] {slabMaterials[i], 0, this, i});
			}
		}

	}

	public BlockMultiSlab(boolean b, Material mat) {
		super(mat);
		this.doubleSlab = b;
		this.fullBlock = b;
		setDefaultState(blockState.getBaseState()
				.withProperty(MATERIAL, 0)
				.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, MATERIAL, HALF);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(MATERIAL, meta & 7)
				.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(MATERIAL) | (state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP ? 8 : 0);
	}

	@Override
	public IProperty<?> getVariantProperty() {
		return MATERIAL;
	}

	@Override
	public Comparable<?> getTypeForItem(ItemStack stack) {
		return stack.getMetadata() & 7;
	}

	@Override
	public boolean isDouble() {
		return doubleSlab;
	}

	@Override
	public String getTranslationKey(int meta) {
		meta = (meta & 7) % slabMaterials.length;
		Block block = slabMaterials[meta];
		return super.getTranslationKey() + "." + block.getTranslationKey().substring(5);
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		meta = (meta & 7) % slabMaterials.length;
		Block block = slabMaterials[meta];
		return getLegacyIcon(block, side, meta);
	}

	@SideOnly(Side.CLIENT)
	protected static IIcon getLegacyIcon(Block block, int side, int meta) {
		if(block instanceof BlockBase) return ((BlockBase) block).getIcon(side, meta);
		if(block instanceof BlockMultiSlab) return ((BlockMultiSlab) block).getIcon(side, meta);
		return PlainCubeLegacyIcons.get(block);
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState meta, Random rand, int fortune) {
		return Item.getItemFromBlock(single != null ? single : this);
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		return new ItemStack(Item.getItemFromBlock(single != null ? single : this), 2, getItemVariant(state));
	}

	@SideOnly(Side.CLIENT)
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(Item.getItemFromBlock(single != null ? single : this), 1,
				getItemVariant(state));
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getItemVariant(state);
	}

	private int getItemVariant(IBlockState state) {
		return (state.getValue(MATERIAL) & 7) % slabMaterials.length;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(single == null) {
			for(int i = 0; i < slabMaterials.length; ++i) {
				list.add(new ItemStack(Item.getItemFromBlock(this), 1, i));
			}
		}
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity entity, Explosion explosion) {
		Block block = getSlabMaterial(world.getBlockState(pos));
		return block.getExplosionResistance(entity);
	}

	@Override
	public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
		Block block = getSlabMaterial(state);
		int index = getMaterialIndex(state);
		IBlockState materialState = block.getStateFromMeta(getMaterialMetadata(index));
		return block.getBlockHardness(materialState, world, pos);
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {
		if(entity instanceof EntityPlayer)
			onPlayerStep(world, pos.getX(), pos.getY(), pos.getZ(), (EntityPlayer) entity);
	}

	@Override
	public void onPlayerStep(World world, int x, int y, int z, EntityPlayer player) {
		if(!world.isRemote) return;
		Block block = getSlabMaterial(world.getBlockState(new BlockPos(x, y, z)));
		if(!(block instanceof BlockSpeedy))
			return;

		if(player.moveForward != 0 || player.moveStrafing != 0) {
			player.motionX *= 1.5;
			player.motionZ *= 1.5;
		}
	}

	private Block getSlabMaterial(IBlockState state) {
		return slabMaterials[getMaterialIndex(state)];
	}

	private int getMaterialIndex(IBlockState state) {
		return (state.getValue(MATERIAL) & 7) % slabMaterials.length;
	}

	protected int getMaterialMetadata(int index) {
		return index;
	}
}
