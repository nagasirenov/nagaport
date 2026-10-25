package com.hbm.blocks.bomb;

import org.apache.logging.log4j.Level;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.main.MainRegistry;
import com.hbm.world.biome.CraterBiomeMarker;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFissureBomb extends BlockTNTBase {

	@Override
	public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
		ExplosionNukeSmall.explode(world, x, y, z, ExplosionNukeSmall.PARAMS_MEDIUM);

		int range = 5;

		boolean crater = world.getBiome(new BlockPos((int) Math.floor(x), 0, (int) Math.floor(z))) instanceof CraterBiomeMarker;

		for(int i = -range; i <= range; i++) {
			for(int j = -range; j <= range; j++) {
				for(int k = -range; k <= range; k++) {

					int a = (int) Math.floor(x + i);
					int b = (int) Math.floor(y + j);
					int c = (int) Math.floor(z + k);

					Block block = com.hbm.world.WorldBlockState.read(world, a, b, c).getBlock();

					if(block == ModBlocks.ore_bedrock) {
						com.hbm.world.WorldBlockState.writeLegacyBlock(world, a, b, c, ModBlocks.ore_volcano, crater ? 1 : 0, 3);
					} else if(block == ModBlocks.ore_bedrock_oil) {
						{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = a; int nativePlacementY = b; int nativePlacementZ = c; net.minecraft.block.Block nativePlacementBlock = Blocks.BEDROCK; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
					}
				}
			}
		}
	}
	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
	if(!world.isRemote) {
			if(GeneralConfig.enableExtendedLogging) {
			MainRegistry.logger.log(Level.INFO, "[BOMBPL]" + this.getLocalizedName() + " placed at " + x + " / " + y + " / " + z + "! " + "by "+ player.getName());
		}
	}
	}
}
