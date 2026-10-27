package com.hbm.blocks;

import java.util.Random;

import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class BlockKelp extends Block {
	public BlockKelp withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}


	private IIcon[] icons = new IIcon[2];

	public BlockKelp() {
		super(Material.WATER);
	}

	public IIcon getIcon(int side, int meta) {
		return (meta == 0 && side == 0) ? this.icons[0] : this.icons[1];
	}

	public void registerBlockIcons(IIconRegister iconRegister) {
		this.icons[0] = iconRegister.registerIcon(RefStrings.MODID + ":laythe_kelp");
		this.icons[1] = iconRegister.registerIcon(RefStrings.MODID + ":laythe_kelp_top");
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}
	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	private static final AxisAlignedBB KELP_AABB = new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 1.0D, 0.9D);

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, net.minecraft.world.IBlockAccess source, BlockPos pos) {
		return KELP_AABB;
	}

	@Override
	public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(net.minecraft.world.IBlockAccess worldIn, IBlockState state, BlockPos pos, net.minecraft.util.EnumFacing face) {
		return net.minecraft.block.state.BlockFaceShape.UNDEFINED;
	}

	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public net.minecraft.util.BlockRenderLayer getRenderLayer() {
		return net.minecraft.util.BlockRenderLayer.CUTOUT;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(net.minecraft.block.state.IBlockState blockState, net.minecraft.world.IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}


	public boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		return canBlockStay(worldIn, pos.getX(), pos.getY(), pos.getZ());
	}

public boolean canBlockStay(World world, int x, int y, int z) {
		Block below = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();
		return below == this || below == ModBlocks.laythe_silt;
	}

	@Override
	public boolean canPlaceBlockAt(World world, net.minecraft.util.math.BlockPos nativePlacementPos) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		Block below = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();
		return below == this || below == ModBlocks.laythe_silt;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		this.updateBlockMeta(world, pos.getX(), pos.getY(), pos.getZ());
	}

	private void updateBlockMeta(World world, int x, int y, int z) {
		Block above = com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock();
		if (above == this) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 0, 2);
		} else {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 8, 2);
		}
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState parMetadata, Random parRand, int parFortune) {
	   return ModItems.saltleaf;
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
		return random.nextInt(4);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if (!this.canBlockStay(world, x, y, z)) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.WATER; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		} else {
			updateBlockMeta(world, x, y, z);
		}
	}

}
