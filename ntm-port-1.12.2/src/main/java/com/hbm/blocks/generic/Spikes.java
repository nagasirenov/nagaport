package com.hbm.blocks.generic;

import com.hbm.lib.ModDamageSource;
import com.hbm.main.ModSoundEvents;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Spikes extends Block {

    public Spikes(Material mat) {
        super(mat);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return null;
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if(entity instanceof EntityLivingBase && entity.motionY < -0.1D &&
                entity.attackEntityFrom(ModDamageSource.spikes, 100)) {
            world.playSound(null, pos, ModSoundEvents.SLICER, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        }
    }

}
