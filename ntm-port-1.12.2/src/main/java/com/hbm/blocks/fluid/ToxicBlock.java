package com.hbm.blocks.fluid;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class ToxicBlock extends BlockFluidClassic {

	@SideOnly(Side.CLIENT)
	public static IIcon stillIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon flowingIcon;
	public Random rand = new Random();

	public static DamageSource damageSource;

	public ToxicBlock(Fluid fluid, Material material, DamageSource damage) {
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
		stillIcon = register.registerIcon(RefStrings.MODID + ":toxic_still");
		flowingIcon = register.registerIcon(RefStrings.MODID + ":toxic_flowing");
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

		if(entity instanceof EntityLivingBase)
			ContaminationUtil.contaminate((EntityLivingBase)entity, HazardType.RADIATION, ContaminationType.CREATIVE, 1.0F);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
    {
		super.neighborChanged(state, world, pos, block, fromPos);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if(reactToBlocks(world, x + 1, y, z))
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.sellafield_slaked);
		if(reactToBlocks(world, x - 1, y, z))
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.sellafield_slaked);
		if(reactToBlocks(world, x, y + 1, z))
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.sellafield_slaked);
		if(reactToBlocks(world, x, y - 1, z))
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.sellafield_slaked);
		if(reactToBlocks(world, x, y, z + 1))
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.sellafield_slaked);
		if(reactToBlocks(world, x, y, z - 1))
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.sellafield_slaked);
    }

	public boolean reactToBlocks(World world, int x, int y, int z) {
		if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock().getDefaultState().getMaterial() != ModBlocks.fluidtoxic) {
			if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock().getDefaultState().getMaterial().isLiquid()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int tickRate(World p_149738_1_) {
		return 15;
	}

}
