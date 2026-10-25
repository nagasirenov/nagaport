package com.hbm.blocks.generic;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.world.feature.HugeMush;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class BlockMush extends Block implements IGrowable, IPlantable {

	public BlockMush withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}


	private final net.minecraft.util.math.AxisAlignedBB shape;

	public BlockMush(Material p_i45394_1_) {
		super(p_i45394_1_);
		float f = 0.2F;
		this.shape = new net.minecraft.util.math.AxisAlignedBB(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
		this.setTickRandomly(true);
	}

	protected boolean canPlaceBlockOn(Block block) {
		return block.getDefaultState().isFullBlock();
	}

	@Override
	public boolean canPlaceBlockAt(World world, net.minecraft.util.math.BlockPos nativePlacementPos) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		return super.canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x, y, z)) && this.canBlockStay(world, x, y, z);
	}


	public boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		return canBlockStay(worldIn, pos.getX(), pos.getY(), pos.getZ());
	}

public boolean canBlockStay(World world, int x, int y, int z) {

		if(y >= 0 && y < 256) {
			Block block = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();
			return block.canSustainPlant(block.getDefaultState(), world, new BlockPos(x, y - 1, z), EnumFacing.UP, this) || canMushGrowHere(world, x, y, z);
		} else {
			return false;
		}
	}

	private static final Set<Block> canGrowOn = new HashSet();

	public boolean canMushGrowHere(World world, int x, int y, int z) {
		if(canGrowOn.isEmpty()) {
			canGrowOn.add(ModBlocks.waste_earth);
			canGrowOn.add(ModBlocks.waste_mycelium);
			canGrowOn.add(ModBlocks.waste_trinitite);
			canGrowOn.add(ModBlocks.waste_trinitite_red);
			canGrowOn.add(ModBlocks.block_waste);
			canGrowOn.add(ModBlocks.block_waste_painted);
			canGrowOn.add(ModBlocks.block_waste_vitrified);
		}

		Block block = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();
		return canGrowOn.contains(block);
	}

	public boolean growHuge(World world, int x, int y, int z, Random rand) {
		com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
		(new HugeMush()).generate(world, rand, x, y, z);

		return true;
	}

	@Override
	public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
		return canBlockStay(worldIn, pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
		return rand.nextFloat() < 0.4D;
	}

	@Override
	public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
		this.growHuge(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

	public boolean func_149851_a(World world, int x, int y, int z, boolean b) {
		return canBlockStay(world, x, y, z);
	}

	public boolean func_149852_a(World p_149852_1_, Random p_149852_2_, int p_149852_3_, int p_149852_4_, int p_149852_5_) {
		return p_149852_2_.nextFloat() < 0.4D;
	}

	public void func_149853_b(World world, Random rand, int x, int y, int z) {
		this.growHuge(world, x, y, z, rand);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(net.minecraft.block.state.IBlockState state, IBlockAccess world, BlockPos pos) {
		return null;
	}

	/**
	 * Is this block (a) opaque and (b) a full 1m cube? This determines whether
	 * or not to render the shared face of two adjacent blocks and also whether
	 * the player can attach torches, redstone wire, etc to this block.
	 */
	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	/**
	 * If this block doesn't render as an ordinary block it will return False
	 * (examples: signs, buttons, stairs, etc)
	 */
	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	/**
	 * The type of render function that is called for this block
	 */
	public int getRenderType() {
		return 1;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block b, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, b, fromPos);
		this.checkAndDropBlock(world, pos.getX(), pos.getY(), pos.getZ());
	}

	/**
	 * checks if the block can stay, if not drop as item
	 */
	protected void checkAndDropBlock(World world, int x, int y, int z) {

		if(!this.canBlockStay(world, x, y, z)) {
			BlockPos dpos = new BlockPos(x, y, z); this.dropBlockAsItem(world, dpos, world.getBlockState(dpos), 0);
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
		}
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
		this.checkAndDropBlock(world, x, y, z);

		if(GeneralConfig.enableMycelium && com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.waste_earth && rand.nextInt(5) == 0) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y - 1, z, ModBlocks.waste_mycelium);
		}

		if(rand.nextInt(25) == 0) {
			byte range = 4;
			int maxShroom = 3;
			int ix;
			int iy;
			int iz;

			for(ix = x - range; ix <= x + range; ++ix) {
				for(iy = y - range; iy <= y + range; ++iy) {
					for(iz = z - 1; iz <= z + 1; ++iz) {
						if(com.hbm.world.WorldBlockState.read(world, ix, iz, iy).getBlock() == this) {
							--maxShroom;

							if(maxShroom <= 0) {
								return;
							}
						}
					}
				}
			}

			ix = x + rand.nextInt(5) - 2;
			iy = z + rand.nextInt(2) - rand.nextInt(2);
			iz = y + rand.nextInt(5) - 2;

			for(int l1 = 0; l1 < 4; ++l1) {
				if(world.isAirBlock(new net.minecraft.util.math.BlockPos(ix, iy, iz)) && this.canMushGrowHere(world, ix, iy, iz)) {
					x = ix;
					z = iy;
					y = iz;
				}

				ix = x + rand.nextInt(5) - 2;
				iy = z + rand.nextInt(2) - rand.nextInt(2);
				iz = y + rand.nextInt(5) - 2;
			}

			if(world.isAirBlock(new net.minecraft.util.math.BlockPos(ix, iy, iz)) && this.canMushGrowHere(world, ix, iy, iz)) {
				com.hbm.world.WorldBlockState.writeLegacyBlock(world, ix, iy, iz, this, 0, 2);
			}
		}
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Cave;
	}

	@Override
	public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		return world.getBlockState(pos);
	}

	public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
		return EnumPlantType.Cave;
	}

	public Block getPlant(IBlockAccess world, int x, int y, int z) {
		return this;
	}

	public int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
		return com.hbm.world.WorldBlockState.readMetadata(world, x, y, z);
	}



	public net.minecraft.util.math.AxisAlignedBB getBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		return shape;
	}
}
