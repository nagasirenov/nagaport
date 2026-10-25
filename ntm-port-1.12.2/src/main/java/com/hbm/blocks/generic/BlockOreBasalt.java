package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.util.EnumUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class BlockOreBasalt extends BlockEnumMulti {

	protected IIcon[] topIcons;

	public BlockOreBasalt() {
		super(Material.ROCK, EnumBasaltOreType.class, true, true);
	}

	public static enum EnumBasaltOreType {
		SULFUR(ModItems.sulfur),
		FLUORITE(ModItems.fluorite),
		ASBESTOS(ModItems.ingot_asbestos),
		GEM(ModItems.gem_volcanic),
		MOLYSITE(ModItems.powder_molysite);

		public Item drop;

		private EnumBasaltOreType(Item drop) {
			this.drop = drop;
			if(drop == null) throw new IllegalStateException("EnumBasaltOreType initialized before ModItems!");
		}
	}

	public String getTextureMultiName(Enum num) {
		return this.getRegistryName().toString() + "_" + num.name().toLowerCase(Locale.US);
	}

	public String getUnlocalizedMultiName(Enum num) {
		return super.getTranslationKey() + "_" + num.name().toLowerCase(Locale.US);
	}

	@Override
	public net.minecraft.item.Item getItemDropped(net.minecraft.block.state.IBlockState state, Random rand, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		EnumBasaltOreType type = EnumUtil.grabEnumSafely(EnumBasaltOreType.class, meta);
		return type.drop;
	}


		@Override
	public void onEntityWalk(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.entity.Entity entityIn) {
		onEntityWalking(worldIn, pos.getX(), pos.getY(), pos.getZ(), entityIn);
	}

public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		if(meta == EnumBasaltOreType.ASBESTOS.ordinal() && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == Blocks.AIR) {
			if(world.rand.nextInt(10) == 0) com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y + 1, z, ModBlocks.gas_asbestos);
			for(int i = 0; i < 5; i++) world.spawnParticle(net.minecraft.util.EnumParticleTypes.TOWN_AURA, x + world.rand.nextFloat(), y + 1.1, z + world.rand.nextFloat(), 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public void neighborChanged(net.minecraft.block.state.IBlockState nativeState, World world, net.minecraft.util.math.BlockPos nativePos, Block block, net.minecraft.util.math.BlockPos fromPos) { } //no more BUD outgassing for you, mister

	@Override
	public void dropBlockAsItemWithChance(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, float chance, int fortune) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ(); int meta = com.hbm.world.WorldBlockState.metadata(state);
		if(meta == EnumBasaltOreType.ASBESTOS.ordinal()) com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.gas_asbestos);
		super.dropBlockAsItemWithChance(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), chance, fortune);
	}


		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		return ModBlocks.getDropsWithoutDamage(world, this, metadata, fortune);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		super.registerBlockIcons(reg);

		Enum[] enums = theEnum.getEnumConstants();
		this.topIcons = new IIcon[enums.length];

		for(int i = 0; i < topIcons.length; i++) {
			Enum num = enums[i];
			this.topIcons[i] = reg.registerIcon(this.getTextureMultiName(num) + "_top");
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		if(side <= 1) return this.topIcons[meta % this.topIcons.length];
		return super.getIcon(side, meta);
	}

}
