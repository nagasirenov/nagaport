package com.hbm.blocks.bomb;

import java.util.Random;

import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class DigammaFlame extends Block {

	public DigammaFlame() {
		super(Material.FIRE);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		
		if(entity instanceof EntityLivingBase) {
			ContaminationUtil.contaminate((EntityLivingBase) entity, HazardType.DIGAMMA, ContaminationType.DIGAMMA, 0.05F);
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
		return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return 1;
	}

	@Override
	public boolean isCollidable() {
		return false;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return null;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return World.doesBlockHaveSolidTopSurface(world, x, y - 1, z);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if(!canPlaceBlockAt(world, x, y, z)) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
		}
	}
}
