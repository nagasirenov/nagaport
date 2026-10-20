package com.hbm.world.biome;

import static net.minecraftforge.common.BiomeDictionary.Type.*;

import java.util.ArrayList;
import java.util.List;

import com.hbm.entity.mob.EntityUndeadSoldier;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;

public class BiomeGenNoMansLand extends Biome {
	
	public static final List EMPTY_LIST = new ArrayList(0);
	public static final List HOSTILE_LIST = new ArrayList(1);
	
	public static final Biome noMansLand = new BiomeGenNoMansLand();
	
	/** Explicit activation hook, only for the native biome registration phase.
	 * The original MainRegistry call remains commented out; this is not subscribed automatically. */
	public static void initDictionary() {
		if(!ForgeRegistries.BIOMES.containsKey(noMansLand.getRegistryName())) ForgeRegistries.BIOMES.register(noMansLand);
		else if(ForgeRegistries.BIOMES.getValue(noMansLand.getRegistryName()) != noMansLand)
			throw new IllegalStateException("Biome registry owner conflict: " + noMansLand.getRegistryName());
		BiomeDictionary.addTypes(noMansLand,	DEAD,	PLAINS,	WASTELAND);
		BiomeManager.addBiome(BiomeType.WARM, new BiomeEntry(noMansLand, 5));
	}

	public BiomeGenNoMansLand() {
		super(new BiomeProperties("No Man's Land").setBaseHeight(0.125F).setHeightVariation(0.05F).setWaterColor(0x6F6F12));
		setRegistryName("hbm", "no_mans_land");
		this.decorator = new BiomeDecoratorNoMansLand();
		this.spawnableCreatureList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableCaveCreatureList.clear();
		this.decorator.treesPerChunk = -999;
		this.decorator.flowersPerChunk = 0;
		this.decorator.grassPerChunk = 0;
		this.flowers.clear();
		
		this.HOSTILE_LIST.add(new Biome.SpawnListEntry(EntityUndeadSoldier.class, 1, 4, 6));
	}
	
	@Override
	public List getSpawnableList(EnumCreatureType type) {
		if(type == EnumCreatureType.MONSTER) {
			if(this.HOSTILE_LIST.size() != 1) {
				this.HOSTILE_LIST.clear();
				this.HOSTILE_LIST.add(new Biome.SpawnListEntry(EntityUndeadSoldier.class, 1, 4, 6));
			}
			return this.HOSTILE_LIST;
		}
		if(!EMPTY_LIST.isEmpty()) EMPTY_LIST.clear();
		return EMPTY_LIST;
	}
	
	@Override @SideOnly(Side.CLIENT)
	public int getGrassColorAtPos(BlockPos pos) { return 0x696F59; }

	@Override @SideOnly(Side.CLIENT)
	public int getFoliageColorAtPos(BlockPos pos) { return 0x767C67; }

	@Override @SideOnly(Side.CLIENT)
	public int getSkyColorByTemp(float temp) { return 0x949494; }
}
