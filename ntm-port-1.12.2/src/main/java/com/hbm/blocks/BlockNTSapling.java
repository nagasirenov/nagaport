package com.hbm.blocks;

import net.minecraft.block.state.IBlockState;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.hbm.dim.tekto.TTree;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.handler.atmosphere.IPlantableBreathing;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BlockNTSapling extends BlockSapling implements IBlockMulti, IPlantableBreathing {

	public static enum EnumSapling {
		VINYL,
		PVC,
	}

	private IIcon[] textures;

	public BlockNTSapling() {
		super();
	}

	@Override
	public boolean canBreathe(CBT_Atmosphere atmosphere) {
		return atmosphere != null && (atmosphere.hasFluid(Fluids.TEKTOAIR, 0.1) || atmosphere.hasFluid(Fluids.CHLORINE, 0.1));
	}

	protected boolean canPlaceBlockOn(Block block) {
		return block == ModBlocks.rubber_silt || block == ModBlocks.rubber_grass || block == ModBlocks.rubber_farmland;
	}

	public void registerBlockIcons(IIconRegister iconRegister) {
		textures = new IIcon[EnumSapling.values().length];
		for(int i = 0; i < EnumSapling.values().length; i++) {
			EnumSapling sapling = EnumSapling.values()[i];
			textures[i] = iconRegister.registerIcon(RefStrings.MODID + ":sapling_" + sapling.name().toLowerCase(Locale.US));
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return textures[meta];
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, net.minecraft.util.NonNullList<ItemStack> list) {
		for(int i = 0; i < EnumSapling.values().length; i++) {
			list.add(new ItemStack(this, 1, i));
		}
	}

	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for(int i = 0; i < EnumSapling.values().length; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		Block soil = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();
		return soil == ModBlocks.rubber_grass || soil == ModBlocks.rubber_silt || soil.canSustainPlant(soil.getDefaultState(), world, new BlockPos(x, y - 1, z), EnumFacing.UP, this);
	}


		@Override
	public boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		return canBlockStay(worldIn, pos.getX(), pos.getY(), pos.getZ());
	}

public boolean canBlockStay(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		Block soil = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();
		return (world.getLight(pos) >= 8 || world.canBlockSeeSky(pos)) && soil != null && soil.canSustainPlant(soil.getDefaultState(), world, pos.down(), EnumFacing.UP, this);
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random random) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
		if(!world.isRemote) {
			if(world.getLight(new net.minecraft.util.math.BlockPos(x, y + 1, z)) >= 9 && random.nextInt(7) == 0) {
				this.func_149878_d(world, x, y, z, random);
			}
		}
	}

	public void func_149878_d(World world, int x, int y, int z, Random rand) {
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		WorldGenAbstractTree treeGen = new TTree(true, 3, 4, 6, 3, 2, false, ModBlocks.vinyl_log, ModBlocks.pet_leaves);

		switch(meta) {
		case 0:
			treeGen = new TTree(false, 2, 4, 5, 3, 2, false, ModBlocks.vinyl_log, ModBlocks.pet_leaves);
			break;
		case 1:
			treeGen = new TTree(true, 2, 5, 7, 4, 3, false, ModBlocks.pvc_log, ModBlocks.rubber_leaves);
			break;
		}

		if(treeGen != null) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			if (!treeGen.generate(world, rand, new net.minecraft.util.math.BlockPos(x, y, z))) {
				com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, this, meta, 2);
			}
		}
	}

	public int damageDropped(int meta) {
		return meta;
	}

	public int getDamageValue(World world, int x, int y, int z) {
		return com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase entity, ItemStack stack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, this, stack.getItemDamage(), 2);
	}

	@Override
	public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
		return true;
	}

	@Override
	public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
		return (double)worldIn.rand.nextFloat() < 0.45D;
	}

	@Override
	public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
		this.func_149878_d(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

	public void func_149853_b(World world, Random random, int x, int y, int z) {
		this.func_149878_d(world, x, y, z, random);
	}

	@Override
	public int getSubCount() {
		return EnumSapling.values().length;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		EnumSapling sapling = EnumSapling.values()[stack.getItemDamage()];
		return super.getTranslationKey() + "_" + sapling.name().toLowerCase(Locale.US);
	}

}
