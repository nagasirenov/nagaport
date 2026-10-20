package com.hbm.blocks;

import com.hbm.entity.item.EntityFallingBlockNT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.util.math.BlockPos;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

public class BlockFallingNT extends Block {
 public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
 @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, META); }
 @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(META, meta & 15); }
 @Override public int getMetaFromState(IBlockState state) { return state.getValue(META); }


	public BlockFallingNT() {
		super(Material.SAND);
		this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}

	public BlockFallingNT(Material mat) {
		super(mat);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		world.scheduleUpdate(pos, this, this.tickRate(world));
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		world.scheduleUpdate(pos, this, this.tickRate(world));
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if(!world.isRemote) {
			this.fall(world, pos.getX(), pos.getY(), pos.getZ());
		}
	}

	protected void fall(World world, int x, int y, int z) {

		if(canFallThrough(world, x, y - 1, z) && y >= 0) {
			byte range = 32;

			if(!BlockFalling.fallInstantly && world.isAreaLoaded(new BlockPos(x - range, y - range, z - range), new BlockPos(x + range, y + range, z + range))) {
				if(!world.isRemote) {
					EntityFallingBlockNT entityfallingblock = new EntityFallingBlockNT(world, x + 0.5D, y + 0.5D, z + 0.5D, this, getMetaFromState(world.getBlockState(new BlockPos(x, y, z))));
					this.modifyFallingBlock(entityfallingblock);
					world.spawnEntity(entityfallingblock);
				}
			} else {
				world.setBlockToAir(new BlockPos(x, y, z));

				while(canFallThrough(world, x, y - 1, z) && y > 0) {
					--y;
				}

				if(y > 0) {
					world.setBlockState(new BlockPos(x, y, z), this.getDefaultState());
				}
			}
		}
	}

	protected void modifyFallingBlock(EntityFallingBlockNT falling) { }

	@Override
	public int tickRate(World world) {
		return 2;
	}

	public static boolean canFallThrough(World world, int x, int y, int z) {
		IBlockState state = world.getBlockState(new BlockPos(x, y, z));
        Block block = state.getBlock();

		if(block.isAir(state, world, new BlockPos(x, y, z))) {
			return true;
		} else if(block == Blocks.FIRE) {
			return true;
		} else {
			Material material = state.getMaterial();
			return material == Material.WATER ? true : material == Material.LAVA;
		}
	}

	public void onLand(World world, int x, int y, int z, int meta) { }

}
