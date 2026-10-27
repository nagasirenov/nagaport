package com.hbm.blocks.generic;
import com.hbm.render.icon.IIcon;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

public class WasteLeaves extends Block {

	protected IIcon blockIcon;

	public WasteLeaves(Material mat) {
		super(mat);
		this.setTickRandomly(true);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":waste_leaves");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return this.blockIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public net.minecraft.util.BlockRenderLayer getRenderLayer() {
		return net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return null;
	}


		@Override
	public void updateTick(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, java.util.Random rand) {
		updateTick(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

public void updateTick(World world, int x, int y, int z, Random rand) {

		if(rand.nextInt(30) == 0) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);

			if(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock().getDefaultState().getMaterial() == Material.AIR) {
				EntityFallingBlock leaves = new EntityFallingBlock(world, x + 0.5, y + 0.5, z + 0.5, ModBlocks.leaves_layer.getDefaultState());
				leaves.fallTime = 2;
				leaves.shouldDropItem = false;
				world.spawnEntity(leaves);
			}
		}

		super.updateTick(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), rand);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if(rand.nextInt(7) == 0 && com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock().getDefaultState().getMaterial() == Material.AIR) {
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "deadleaf");
			data.setDouble("posX", x + rand.nextDouble());
			data.setDouble("posY", y - 0.05);
			data.setDouble("posZ", z + rand.nextDouble());
			MainRegistry.proxy.effectNT(data);
		}
	}

	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	protected boolean canSilkHarvest() {
		return false;
	}
	/** NTM construction hook for the native protected sound setter. */
	public WasteLeaves withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

}
