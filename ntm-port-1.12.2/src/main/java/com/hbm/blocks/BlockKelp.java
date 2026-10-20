package com.hbm.blocks;

import java.util.Random;

import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class BlockKelp extends Block {

	private IIcon[] icons = new IIcon[2];
	
	public BlockKelp() {
		super(Material.WATER);
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return (meta == 0 && side == 0) ? this.icons[0] : this.icons[1];
	}

	@Override
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
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public int getRenderType() {
		return 1; 
	}
	
	@Override
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
	public void onBlockAdded(World world, int x, int y, int z) {
		this.updateBlockMeta(world, x, y, z);
	}

	private void updateBlockMeta(World world, int x, int y, int z) {
		Block above = com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock();
		if (above == this) {
			world.setBlockMetadataWithNotify(x, y, z, 0, 2);
		} else {
			world.setBlockMetadataWithNotify(x, y, z, 8, 2);
		}
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState parMetadata, Random parRand, int parFortune) {
	   return ModItems.saltleaf;
	}

	@Override
	public int quantityDropped(int meta, int fortune, java.util.Random random) {
		return random.nextInt(4); 
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if (!this.canBlockStay(world, x, y, z)) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.WATER; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		} else {
			updateBlockMeta(world, x, y, z); 
		}
	}

}
