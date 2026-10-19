package com.hbm.dim.tekto;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.tekto.GenLayerTekto.GenLayerTektoRiverMix;
import com.hbm.dim.tekto.GenLayerTekto.GenlayerTektoBiomes;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerRiver;
import net.minecraft.world.gen.layer.GenLayerSmooth;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

public class WorldProviderTekto extends WorldProviderCelestial {

	@Override
	public void init() {
		super.init();
		this.biomeProvider = new WorldChunkManagerCelestial(createBiomeGenerators(worldObj.getSeed()));
	}

	@Override
	public String getDimensionName() {
		return "Tekto";
	}

	@Override
	public IChunkGenerator createChunkGenerator() {
		return new ChunkProviderTekto(this.worldObj, this.getSeed(), false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		float imsuper = super.getSunBrightness(par1);

		return imsuper * 0.7F;
	}

	@Override
	public Block getStone() {
		return ModBlocks.basalt;
	}

	@Override
	public boolean hasIce() {
		return true;
	}

	private static BiomeGenLayers createBiomeGenerators(long seed) {
		GenLayer genlayerBiomes = new GenlayerTektoBiomes(seed); // Your custom biome layer

		genlayerBiomes = new GenLayerZoom(1000L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1001L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1002L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1003L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1004L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1005L, genlayerBiomes);

		GenLayer genlayerRiverZoom = new GenLayerZoom(1000L, genlayerBiomes);
		GenLayer genlayerRiver = new GenLayerRiver(1001L, genlayerRiverZoom); // Your custom river layer
		GenLayerSmooth genlayersmooth = new GenLayerSmooth(1000L, genlayerRiver);

		GenLayerSmooth genlayersmooth1 = new GenLayerSmooth(1000L, genlayerBiomes);
		GenLayerTektoRiverMix genlayerrivermix = new GenLayerTektoRiverMix(100L, genlayersmooth1, genlayersmooth);
		GenLayerVoronoiZoom genlayervoronoizoom = new GenLayerVoronoiZoom(10L, genlayerrivermix);

		return new BiomeGenLayers(genlayerrivermix, genlayervoronoizoom, seed);
	}

}
