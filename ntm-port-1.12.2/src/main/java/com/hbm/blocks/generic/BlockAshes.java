package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ArmorRegistry.HazardClass;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class BlockAshes extends BlockFalling {
	public BlockAshes withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}


	public BlockAshes(Material mat) {
		super(mat);
	}

	public static int ashes = 0;

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World p_149734_1_, BlockPos pos, Random p_149734_5_) {
		super.randomDisplayTick(state, p_149734_1_, pos, p_149734_5_);
		int p_149734_2_ = pos.getX(), p_149734_3_ = pos.getY(), p_149734_4_ = pos.getZ();

		if(p_149734_5_.nextInt(25) == 0) {

			if(ArmorUtil.checkArmorPiece(MainRegistry.proxy.me(), ModItems.ashglasses, 3)) {
				if(ashes < 256 * 0.25) {
					ashes++;
				}
			} else if(ArmorRegistry.hasAnyProtection(MainRegistry.proxy.me(), 3, HazardClass.SAND, HazardClass.LIGHT)) {
				if(ashes < 256 * 0.75) {
					ashes++;
				}
			} else {
				if(ashes < 256 * 0.95) {
					ashes++;
				}
			}
		}
	}
}
