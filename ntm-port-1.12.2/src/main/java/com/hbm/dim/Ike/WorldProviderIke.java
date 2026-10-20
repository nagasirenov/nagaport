package com.hbm.dim.Ike;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.DimensionFishingLoot;
import com.hbm.dim.WorldProviderCelestial;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderIke extends WorldProviderCelestial {

	@Override
	public void init() {
		super.init();
		this.biomeProvider = new BiomeProviderSingle(BiomeGenIke.biome);
	}

	@Override
	public String getDimensionName() {
		return "Ike";
	}

	@Override
	public IChunkGenerator createChunkGenerator() {
		return new ChunkProviderIke(this.world, this.getSeed(), false);
	}

	@Override
	public Block getStone() {
		return ModBlocks.ike_stone;
	}

	@Override
	public boolean updateLightmap(int[] lightmap) {
		float sun = getSunBrightness(1.0F) - 0.1F;
		for(int i = 0; i < 256; i++) {
			float sky = lightBrightnessTable[i / 16];
			float duna = Math.max(sky - sun, 0);

			int[] color = unpackColor(lightmap[i]);

			color[0] += duna * 20;
			if(color[0] > 255) color[0] = 255;

			lightmap[i] = packColor(color);
		}
		return true;
	}

	private static List<DimensionFishingLoot.Entry> plushie;

	private List<DimensionFishingLoot.Entry> getPlushie() {
		if(plushie == null) {
			List<DimensionFishingLoot.Entry> loot = new ArrayList<>();
			loot.add(new DimensionFishingLoot.Entry(new ItemStack(ModBlocks.plushie, 1, 1), 100));
			plushie = Collections.unmodifiableList(loot);
		}

		return plushie;
	}

	/// FISH ///
	@Override
	public List<DimensionFishingLoot.Entry> getFishingLoot(DimensionFishingLoot.Category category) {
		return getPlushie();
	}
	/// FISH ///

}
