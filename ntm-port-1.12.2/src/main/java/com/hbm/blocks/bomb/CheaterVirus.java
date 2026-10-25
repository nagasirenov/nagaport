package com.hbm.blocks.bomb;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.explosion.ExplosionChaos;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class CheaterVirus extends Block {

	public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);

	static boolean protect = true;

	public CheaterVirus(Material p_i45394_1_) {
		super(p_i45394_1_);
		this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));
		this.setTickRandomly(true);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, META);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META, meta & 15);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		super.breakBlock(world, pos, state);

		if(CheaterVirus.protect) {
			int x = pos.getX(), y = pos.getY(), z = pos.getZ();
			int i = state.getBlock().getMetaFromState(state);
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, this, i, 2);
		}
	}

    @Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();

    	if(GeneralConfig.enableVirus){
    		if(com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() != ModBlocks.cheater_virus && com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() != ModBlocks.cheater_virus) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + 1, y, z, ModBlocks.cheater_virus);
    		}

    		if(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() != ModBlocks.cheater_virus && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() != ModBlocks.cheater_virus) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y + 1, z, ModBlocks.cheater_virus);
    		}

    		if(com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() != ModBlocks.cheater_virus && com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() != ModBlocks.cheater_virus) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z + 1, ModBlocks.cheater_virus);
    		}

    		if(com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() != ModBlocks.cheater_virus && com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() != ModBlocks.cheater_virus) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x - 1, y, z, ModBlocks.cheater_virus);
    		}

    		if(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() != ModBlocks.cheater_virus && com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() != ModBlocks.cheater_virus) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y - 1, z, ModBlocks.cheater_virus);
    		}

    		if(com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() != ModBlocks.cheater_virus && com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() != ModBlocks.cheater_virus) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z - 1, ModBlocks.cheater_virus);
    		}

    		protect = false;
    		{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
    		protect = true;
    	}
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();

    	if((com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == ModBlocks.cheater_virus_seed) &&
    			(com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == ModBlocks.cheater_virus_seed) &&
    			(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == ModBlocks.cheater_virus_seed) &&
    			(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.cheater_virus_seed) &&
    			(com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == ModBlocks.cheater_virus_seed) &&
    			(com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == ModBlocks.cheater_virus_seed) &&
    			!world.isRemote) {
			protect = false;
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			ExplosionChaos.spreadVirus(world, x, y, z, 5);
			protect = true;
    	}
    }


		@Override
	public void onEntityWalk(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.entity.Entity entityIn) {
		onEntityWalking(worldIn, pos.getX(), pos.getY(), pos.getZ(), entityIn);
	}

public void onEntityWalking(World p_149724_1_, int p_149724_2_, int p_149724_3_, int p_149724_4_, Entity entity)
    {
    	if(entity instanceof EntityLivingBase) {
    		((EntityLivingBase)entity).addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.WITHER, 60 * 60 * 60, 9));
    	}
    }

}
