package com.hbm.blocks.generic;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.tileentity.deco.TileEntityGeysir;
import com.hbm.util.ParticleUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockGeysir extends BlockContainer {
	public <T extends BlockGeysir> T withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return (T) this;
	}


	@SideOnly(Side.CLIENT)
	private IIcon iconTop;



	public BlockGeysir(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityGeysir();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(net.minecraft.block.state.IBlockState stateIn, World p_149734_1_, net.minecraft.util.math.BlockPos pos, Random rand) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		int l = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(p_149734_1_, x, y, z) );

		if(this == ModBlocks.geysir_vapor && l == 1) {
			float f = x + 0.5F;
			float f1 = y + 1.0F;
			float f2 = z + 0.5F;

			p_149734_1_.spawnParticle(net.minecraft.util.EnumParticleTypes.CLOUD, f, f1, f2, 0.0D, 0.1D, 0.0D);
		}

		if(this == ModBlocks.geysir_nether) {
			p_149734_1_.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, x + 0.5F, y + 1.0625F, z + 0.5F, 0.0D, 0.0D, 0.0D);
		}

		if(this == ModBlocks.geysir_volcanic) {
			ParticleUtil.spawnGasFlame(p_149734_1_, x + p_149734_1_.rand.nextDouble(), y + 0.5 + p_149734_1_.rand.nextDouble(), z + p_149734_1_.rand.nextDouble(), p_149734_1_.rand.nextGaussian() * 0.2, 0.1, p_149734_1_.rand.nextGaussian() * 0.2);
		}


	}


}
