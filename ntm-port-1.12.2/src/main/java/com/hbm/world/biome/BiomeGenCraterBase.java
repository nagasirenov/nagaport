package com.hbm.world.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BiomeGenCraterBase extends Biome implements CraterBiomeMarker {

	public static final BiomeGenCrater craterBiome = new BiomeGenCrater();
	public static final BiomeGenCraterInner craterInnerBiome = new BiomeGenCraterInner();
	public static final BiomeGenCraterOuter craterOuterBiome = new BiomeGenCraterOuter();

	@Override
	public int getWaterColorMultiplier() {
		return 0x505020; //0x50d030
	}

	private BiomeGenCraterBase(String name) {
		super(new Biome.BiomeProperties(name).setRainDisabled().setWaterColor(0xE0FFAE));
		this.spawnableCreatureList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableCaveCreatureList.clear();
	}

	private static int grassColor(BlockPos pos, int light, int dark) {
		double noise = Biome.GRASS_COLOR_NOISE.getValue((double) pos.getX() * 0.225D, (double) pos.getZ() * 0.225D);
		return noise < -0.1D ? dark : light;
	}

	public static class BiomeGenCrater extends BiomeGenCraterBase {

		public BiomeGenCrater() { super("Crater"); }

		@Override @SideOnly(Side.CLIENT)
		public int getGrassColorAtPos(BlockPos pos) { return grassColor(pos, 0x505050, 0x606060); }

		@Override @SideOnly(Side.CLIENT)
		public int getFoliageColorAtPos(BlockPos pos) { return 0x6A7039; }

		@Override @SideOnly(Side.CLIENT)
		public int getSkyColorByTemp(float temp) { return 0x525A52; }
	}

	public static class BiomeGenCraterOuter extends BiomeGenCraterBase {

		public BiomeGenCraterOuter() { super("Outer Crater"); }

		@Override @SideOnly(Side.CLIENT)
		public int getGrassColorAtPos(BlockPos pos) { return grassColor(pos, 0x6F6752, 0x776F59); }

		@Override @SideOnly(Side.CLIENT)
		public int getFoliageColorAtPos(BlockPos pos) { return 0x6A7039; }

		@Override @SideOnly(Side.CLIENT)
		public int getSkyColorByTemp(float temp) { return 0x6B9189; }
	}

	public static class BiomeGenCraterInner extends BiomeGenCraterBase {

		public BiomeGenCraterInner() { super("Inner Crater"); }

		@Override @SideOnly(Side.CLIENT)
		public int getGrassColorAtPos(BlockPos pos) { return grassColor(pos, 0x303030, 0x404040); }

		@Override @SideOnly(Side.CLIENT)
		public int getFoliageColorAtPos(BlockPos pos) { return 0x6A7039; }

		@Override @SideOnly(Side.CLIENT)
		public int getSkyColorByTemp(float temp) { return 0x424A42; }
	}
}
