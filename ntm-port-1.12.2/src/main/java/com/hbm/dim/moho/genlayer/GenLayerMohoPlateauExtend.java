package com.hbm.dim.moho.genlayer;

import com.hbm.dim.moho.biome.BiomeGenBaseMoho;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerMohoPlateauExtend extends GenLayer {

	public GenLayerMohoPlateauExtend(long seed, GenLayer genLayer) {
		super(seed);
		this.parent = genLayer;
	}

	public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
		return this.getIntsExtend(areaX, areaY, areaWidth, areaHeight);
	}

	private int[] getIntsExtend(int areaX, int areaY, int areaWidth, int areaHeight) {
		int i = areaX - 1;
		int j = areaY - 1;
		int k = 1 + areaWidth + 1;
		int l = 1 + areaHeight + 1;
		int[] aint = this.parent.getInts(i, j, k, l);
		int[] aint1 = IntCache.getIntCache(areaWidth * areaHeight);

		for(int i1 = 0; i1 < areaHeight; ++i1) {
			for(int j1 = 0; j1 < areaWidth; ++j1) {
				this.initChunkSeed((long) (j1 + areaX), (long) (i1 + areaY));
				int k1 = aint[j1 + 1 + (i1 + 1) * k];

				if(k1 == Biome.getIdForBiome(BiomeGenBaseMoho.mohoCrag)) {
					int l1 = aint[j1 + 1 + (i1 + 1 - 1) * k];
					int i2 = aint[j1 + 1 + 1 + (i1 + 1) * k];
					int j2 = aint[j1 + 1 - 1 + (i1 + 1) * k];
					int k2 = aint[j1 + 1 + (i1 + 1 + 1) * k];
					boolean flag = ((l1 == Biome.getIdForBiome(BiomeGenBaseMoho.mohoCrag) || l1 == Biome.getIdForBiome(BiomeGenBaseMoho.mohoPlateau))
						&& (i2 == Biome.getIdForBiome(BiomeGenBaseMoho.mohoCrag) || i2 == Biome.getIdForBiome(BiomeGenBaseMoho.mohoPlateau))
						&& (j2 == Biome.getIdForBiome(BiomeGenBaseMoho.mohoCrag) || j2 == Biome.getIdForBiome(BiomeGenBaseMoho.mohoPlateau))
						&& (k2 == Biome.getIdForBiome(BiomeGenBaseMoho.mohoCrag) || k2 == Biome.getIdForBiome(BiomeGenBaseMoho.mohoPlateau)));
					if(flag) {
						k1 = Biome.getIdForBiome(BiomeGenBaseMoho.mohoPlateau);
					}
				}

				aint1[j1 + i1 * areaWidth] = k1;
			}
		}

		return aint1;
	}

}
