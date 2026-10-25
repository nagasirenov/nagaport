package com.hbm.blocks.fluid;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class AcidBlock extends BlockFluidClassic {

	@SideOnly(Side.CLIENT)
	public static IIcon stillIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon flowingIcon;
	public Random rand = new Random();

	public static DamageSource damageSource;

	public AcidBlock(Fluid fluid, Material material, DamageSource damage) {
		super(fluid, material);
		damageSource = damage;
		setQuantaPerBlock(4);
		setCreativeTab(null);
		displacements.put(this, false);
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return (side == 0 || side == 1) ? stillIcon : flowingIcon;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		stillIcon = register.registerIcon(RefStrings.MODID + ":acid_still");
		flowingIcon = register.registerIcon(RefStrings.MODID + ":acid_flowing");
	}

	public boolean canDisplace(IBlockAccess world, int x, int y, int z) {

		if (com.hbm.world.WorldBlockState.read(world, x, y, z).getMaterial().isLiquid()) {
			return false;
		}
		return super.canDisplace(world, new net.minecraft.util.math.BlockPos(x, y, z));
	}

	public boolean displaceIfPossible(World world, int x, int y, int z) {

		if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock().getDefaultState().getMaterial().isLiquid()) {
			return false;
		}
		return super.displaceIfPossible(world, new net.minecraft.util.math.BlockPos(x, y, z));
	}


		@Override
	public void onEntityCollision(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.entity.Entity entityIn) {
		onEntityCollidedWithBlock(worldIn, pos.getX(), pos.getY(), pos.getZ(), entityIn);
	}

public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		entity.setInWeb();
		entity.attackEntityFrom(ModDamageSource.acid, 10000F);
	}


		@Override
	public void updateTick(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, java.util.Random rand) {
		updateTick(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

public void updateTick(World world, int x, int y, int z, Random rand) {
		super.updateTick(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), rand);

		reactToBlocks(world, x + 1, y, z);
		reactToBlocks(world, x - 1, y, z);
		reactToBlocks(world, x, y + 1, z);
		reactToBlocks(world, x, y - 1, z);
		reactToBlocks(world, x, y, z + 1);
		reactToBlocks(world, x, y, z - 1);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
    {
		super.neighborChanged(state, world, pos, block, fromPos);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		reactToBlocks(world, x + 1, y, z);
		reactToBlocks(world, x - 1, y, z);
		reactToBlocks(world, x, y + 1, z);
		reactToBlocks(world, x, y - 1, z);
		reactToBlocks(world, x, y, z + 1);
		reactToBlocks(world, x, y, z - 1);
    }

	public void reactToBlocks(World world, int x, int y, int z) {
		if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() != ModBlocks.acid_block) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	}

	@Override
	public int tickRate(World p_149738_1_) {
		return 5;
	}

}
