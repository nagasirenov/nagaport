package com.hbm.world.feature;

import java.util.Random;

import com.hbm.blocks.BlockEnums.EnumBiomeType;
import com.hbm.blocks.ModBlocks;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.MinecraftForge;
import com.hbm.lib.ForgeDirection;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;

public class BiomeCave {
	
	private NoiseGeneratorPerlin noise;
	/** The number that is being deducted flat from the result of the perlin noise before all other processing. Increase this to make strata rarer. */
	private double threshold = 2D;
	/** The mulitplier for the remaining bit after the threshold has been deducted. Increase to make strata wavier. */
	private int rangeMult = 3;
	/** The maximum range after multiplying - anything above this will be subtracted from (maxRange * 2) to yield the proper range. Increase this to make strata thicker. */
	private int maxRange = 4;
	/** The y-level around which the stratum is centered. */
	private int yLevel = 30;
	
	public BiomeCave() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public BiomeCave setThreshold(double threshold) {
		this.threshold = threshold;
		return this;
	}
	
	public BiomeCave setRangeMult(int rangeMult) {
		this.rangeMult = rangeMult;
		return this;
	}
	
	public BiomeCave setMaxRange(int maxRange) {
		this.maxRange = maxRange;
		return this;
	}
	
	public BiomeCave setYLevel(int yLevel) {
		this.yLevel = yLevel;
		return this;
	}

	@SubscribeEvent
	public void onDecorate(DecorateBiomeEvent.Pre event) {
		
		World world = event.getWorld();
		
		if(world.provider == null || world.provider.getDimension() != 0) return;
		
		if(this.noise == null) {
			this.noise = new NoiseGeneratorPerlin(new Random(event.getWorld().getSeed() - 1916169 + yLevel), 2);
		}
		
		int cX = event.chunkX;
		int cZ = event.chunkZ;
		
		double scale = 0.01D;
		
		for(int x = cX + 8; x < cX + 24; x++) {
			for(int z = cZ + 8; z < cZ + 24; z++) {
				
				Biome biome = world.getBiome(new BlockPos(x, 0, z));
				EnumBiomeType type = getTypeFromBiome(biome);
				
				double n = noise.func_151601_a(x * scale, z * scale);
				
				if(type != null && n > threshold) {
					int range = (int)((n - threshold) * rangeMult);
					
					if(range > maxRange)
						range = (maxRange * 2) - range;
					
					if(range < 0)
						continue;
					
					for(int y = yLevel - range; y <= yLevel + range; y++) {
						handleBiome(world, x, y, z, type);
					}
				}
			}
		}
	}
	
	private static void handleBiome(World world, int x, int y, int z, EnumBiomeType type) {
		net.minecraft.block.state.IBlockState nativeCubeState_target = com.hbm.world.WorldBlockState.read(world, x, y, z);
		Block target = nativeCubeState_target.getBlock();
		
		if(target.isNormalCube(nativeCubeState_target) && target.isReplaceableOreGen(world, x, y, z, Blocks.STONE)) {
			
			boolean shouldGen = false;
			
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if(com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock().isAir(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)) {
					shouldGen = true; break;
				}
				if(com.hbm.world.WorldBlockState.read(world, x + dir.offsetX * 2, y + dir.offsetY * 2, z + dir.offsetZ * 2).getBlock().isAir(world, x + dir.offsetX * 2, y + dir.offsetY * 2, z + dir.offsetZ * 2)) {
					shouldGen = true; break;
				}
			}
			
			if(shouldGen) {
				{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.stone_biome; int nativeRegisteredWriteMeta = type.ordinal(); int nativeRegisteredWriteFlags = 2; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
			}
		}
	}
	
	private static EnumBiomeType getTypeFromBiome(Biome biome) {

		if(biome.getDefaultTemperature() >= 1 && biome.getRainfall() < 0.25) return EnumBiomeType.DESERT;
		if(biome.getDefaultTemperature() >= 0.5 && biome.getRainfall() > 0.25 && biome.getTempCategory() != TempCategory.OCEAN) return EnumBiomeType.WOODLAND;
		
		return null;
	}
}
