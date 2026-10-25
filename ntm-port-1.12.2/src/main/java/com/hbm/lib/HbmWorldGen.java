package com.hbm.lib;

import com.hbm.blocks.BlockEnums.EnumStoneType;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockNTMFlower.EnumFlowerType;
import com.hbm.config.GeneralConfig;
import com.hbm.config.MobConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsSingle;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.TomSaveData;
import com.hbm.tileentity.bomb.TileEntityLandmine;
import com.hbm.tileentity.deco.TileEntityLanternBehemoth;
import com.hbm.tileentity.machine.storage.TileEntitySafe;
import com.hbm.tileentity.machine.storage.TileEntitySoyuzCapsule;
import com.hbm.util.Compat;
import com.hbm.util.LootGenerator;
import com.hbm.util.WeightedRandomGeneric;
import com.hbm.world.dungeon.*;
import com.hbm.world.feature.*;
import com.hbm.world.feature.BedrockOre.BedrockOreDefinition;
import com.hbm.world.generator.CellularDungeonFactory;
import com.hbm.world.generator.DungeonToolbox;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.WeightedRandom;
import com.hbm.lib.WeightedRandomChestContent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import com.hbm.lib.ForgeDirection;

import java.util.Random;

public class HbmWorldGen implements IWorldGenerator {

	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

		if(world.provider instanceof WorldProviderCelestial) {
			generateSurface(world, rand, chunkX * 16, chunkZ * 16);
			return;
		}

		switch (world.provider.getDimension()) {
		case -1:
			generateNether(world, rand, chunkX * 16, chunkZ * 16); break;
		case 0:
			generateSurface(world, rand, chunkX * 16, chunkZ * 16); break;
		case 1:
			generateEnd(world, rand, chunkX * 16, chunkZ * 16); break;
		default:
			if(GeneralConfig.enableMDOres)
				generateSurface(world, rand, chunkX * 16, chunkZ * 16); break;
		}
	}

	private void generateSurface(World world, Random rand, int i, int j) {

		// Don't generate anything for non-surface dimensions
		if(world.provider instanceof WorldProviderHell || world.provider instanceof WorldProviderEnd)
			return;

		Biome biome = world.getBiome(new BlockPos(i, 0, j));

		/// CELESTIAL AND EARTH EVENTS
		if(WorldConfig.meteoriteSpawn > 0 && rand.nextInt(WorldConfig.meteoriteSpawn) == 0) {
			int x = i + rand.nextInt(16) + 8;
			int z = j + rand.nextInt(16) + 8;
			int y = world.getHeight(x, z) - rand.nextInt(10);
			if(y > 1) (new Meteorite()).generate(world, rand, x, y, z, false, false, false);
		}

		if(WorldConfig.spaceshipStructure > 0 && rand.nextInt(WorldConfig.spaceshipStructure) == 0) {
			int x = i + rand.nextInt(16);
			int z = j + rand.nextInt(16);
			int y = world.getHeight(x, z);

			new Spaceship().generate(world, rand, x, y, z);
		}
		/// CELESTIAL EVENTS

		// Only generate our ores for the overworld and for custom dimensions
		// We'll handle ore generation on planets separately
		if(world.provider instanceof WorldProviderCelestial && world.provider.getDimension() != 0)
			return;

		if(!TomSaveData.forWorld(world).impact) {

			if((biome == Biomes.FOREST || biome == Biomes.FOREST_HILLS || biome == Biomes.BIRCH_FOREST ||
					biome == Biomes.BIRCH_FOREST_HILLS || biome == Biomes.MUTATED_FOREST || biome == Biomes.ROOFED_FOREST) && rand.nextInt(16) == 0) {
				DungeonToolbox.generateFlowers(world, rand, i, j, ModBlocks.plant_flower, EnumFlowerType.FOXGLOVE.ordinal());
			}
			if(biome == Biomes.ROOFED_FOREST && rand.nextInt(8) == 0) {
				DungeonToolbox.generateFlowers(world, rand, i, j, ModBlocks.plant_flower, EnumFlowerType.NIGHTSHADE.ordinal());
			}
			if((biome == Biomes.JUNGLE || biome == Biomes.JUNGLE_HILLS || biome == Biomes.JUNGLE_EDGE) && rand.nextInt(8) == 0) {
				DungeonToolbox.generateFlowers(world, rand, i, j, ModBlocks.plant_flower, EnumFlowerType.TOBACCO.ordinal());
			}
			if(rand.nextInt(64) == 0) {
				DungeonToolbox.generateFlowers(world, rand, i, j, ModBlocks.plant_flower, EnumFlowerType.WEED.ordinal());
			}
			if(biome == Biomes.PLAINS && rand.nextInt(32) == 0) {
				DungeonToolbox.generateFlowers(world, rand, i, j, ModBlocks.plant_flower, EnumFlowerType.STRAWBERRY.ordinal());
			}
			if(biome == Biomes.PLAINS && rand.nextInt(32) == 0) {
				DungeonToolbox.generateFlowers(world, rand, i, j, ModBlocks.plant_flower, EnumFlowerType.MINT.ordinal());
			}
			if((biome == Biomes.RIVER || biome == Biomes.FROZEN_RIVER) && rand.nextInt(4) == 0) {
				DungeonToolbox.generateFlowers(world, rand, i, j, ModBlocks.reeds, 0);
			}
			if((biome == Biomes.BEACH || biome == Biomes.COLD_BEACH) && rand.nextInt(8) == 0) {
				DungeonToolbox.generateFlowers(world, rand, i, j, ModBlocks.reeds, 0);
			}
		}

		if(WorldConfig.gasbubbleSpawn > 0 && rand.nextInt(WorldConfig.gasbubbleSpawn) == 0)
			DungeonToolbox.generateOre(world, rand, i, j, 1, 32, 30, 10, ModBlocks.gas_flammable, 1);

		if(WorldConfig.explosivebubbleSpawn > 0 && rand.nextInt(WorldConfig.explosivebubbleSpawn) == 0)
			DungeonToolbox.generateOre(world, rand, i, j, 1, 32, 30, 10, ModBlocks.gas_explosive, 1);

		if(WorldConfig.alexandriteSpawn > 0 && rand.nextInt(WorldConfig.alexandriteSpawn) == 0)
			DungeonToolbox.generateOre(world, rand, i, j, 1, 3, 10, 5, ModBlocks.ore_alexandrite);

		if(WorldConfig.overworldOre) {

			DepthDeposit.generateConditionOverworld(world, i, 0, 3, j, 5, 0.6D, ModBlocks.cluster_depth_iron, rand, 24);
			DepthDeposit.generateConditionOverworld(world, i, 0, 3, j, 5, 0.6D, ModBlocks.cluster_depth_titanium, rand, 32);
			DepthDeposit.generateConditionOverworld(world, i, 0, 3, j, 5, 0.6D, ModBlocks.cluster_depth_tungsten, rand, 32);
			DepthDeposit.generateConditionOverworld(world, i, 0, 3, j, 5, 0.8D, ModBlocks.ore_depth_cinnebar, rand, 16);
			DepthDeposit.generateConditionOverworld(world, i, 0, 3, j, 5, 0.8D, ModBlocks.ore_depth_zirconium, rand, 16);
			DepthDeposit.generateConditionOverworld(world, i, 0, 3, j, 5, 0.8D, ModBlocks.ore_depth_borax, rand, 16);

			DungeonToolbox.generateOre(world, rand, i, j, 25, 6, 30, 10, ModBlocks.ore_gneiss_iron, ModBlocks.stone_gneiss);
			DungeonToolbox.generateOre(world, rand, i, j, 10, 6, 30, 10, ModBlocks.ore_gneiss_gold, ModBlocks.stone_gneiss);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.uraniumSpawn * 3, 6, 30, 10, ModBlocks.ore_gneiss_uranium, ModBlocks.stone_gneiss);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.copperSpawn * 3, 6, 30, 10, ModBlocks.ore_gneiss_copper, ModBlocks.stone_gneiss);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.asbestosSpawn * 3, 6, 30, 10, ModBlocks.ore_gneiss_asbestos, ModBlocks.stone_gneiss);
			// DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.lithiumSpawn, 6, 30, 10, ModBlocks.ore_gneiss_lithium, ModBlocks.stone_gneiss);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.rareSpawn, 6, 30, 10, ModBlocks.ore_gneiss_rare, ModBlocks.stone_gneiss);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.gassshaleSpawn * 3, 10, 30, 10, ModBlocks.ore_gneiss_gas, ModBlocks.stone_gneiss);

			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.uraniumSpawn, 5, 5, 20, ModBlocks.ore_uranium);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.thoriumSpawn, 5, 5, 25, ModBlocks.ore_thorium);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.titaniumSpawn, 6, 5, 30, ModBlocks.ore_titanium);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.sulfurSpawn, 8, 5, 30, ModBlocks.ore_sulfur);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.aluminiumSpawn, 6, 5, 40, ModBlocks.ore_aluminium);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.copperSpawn, 6, 5, 45, ModBlocks.ore_copper);
			// DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.nickelSpawn, 6, 5, 10, ModBlocks.ore_nickel);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.zincSpawn, 6, 5, 32, ModBlocks.ore_zinc);
			// DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.mineralSpawn, 10, 12, 32, ModBlocks.ore_mineral);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.fluoriteSpawn, 4, 5, 45, ModBlocks.ore_fluorite);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.niterSpawn, 6, 5, 30, ModBlocks.ore_niter);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.tungstenSpawn, 8, 5, 30, ModBlocks.ore_tungsten);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.leadSpawn, 9, 5, 30, ModBlocks.ore_lead);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.berylliumSpawn, 4, 5, 30, ModBlocks.ore_beryllium);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.rareSpawn, 5, 5, 20, ModBlocks.ore_rare);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.ligniteSpawn, 24, 35, 25, ModBlocks.ore_lignite);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.asbestosSpawn, 4, 16, 16, ModBlocks.ore_asbestos);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.cinnebarSpawn, 4, 8, 16, ModBlocks.ore_cinnebar);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.cobaltSpawn, 4, 4, 8, ModBlocks.ore_cobalt);

			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.ironClusterSpawn, 6, 15, 45, ModBlocks.cluster_iron);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.titaniumClusterSpawn, 6, 15, 30, ModBlocks.cluster_titanium);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.aluminiumClusterSpawn, 6, 15, 35, ModBlocks.cluster_aluminium);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.copperClusterSpawn, 6, 15, 20, ModBlocks.cluster_copper);

			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.limestoneSpawn, 16, 25, 30, ModBlocks.stone_resource, EnumStoneType.LIMESTONE.ordinal());

			if(WorldConfig.newBedrockOres) {

				if(rand.nextInt(10) == 0) {
					int randPosX = i + rand.nextInt(2) + 8;
					int randPosZ = j + rand.nextInt(2) + 8;

					BedrockOre.generate(world, randPosX, randPosZ, new ItemStack(ModItems.bedrock_ore_base), null, 0xD78A16, 1);
				}

			} else {

				if(rand.nextInt(3) == 0) {
					@SuppressWarnings("unchecked")
					WeightedRandomGeneric<BedrockOreDefinition> item = (WeightedRandomGeneric<BedrockOreDefinition>) WeightedRandom.getRandomItem(rand, BedrockOre.weightedOres);
					BedrockOreDefinition def = item.get();

					int randPosX = i + rand.nextInt(2) + 8;
					int randPosZ = j + rand.nextInt(2) + 8;
					BedrockOre.generate(world, randPosX, randPosZ, def.stack, def.acid, def.color, def.tier);
				}
			}

			if(GeneralConfig.enable528ColtanSpawn) {
				DungeonToolbox.generateOre(world, rand, i, j, GeneralConfig.coltanRate, 4, 15, 40, ModBlocks.ore_coltan);
			}

			Random colRand = new Random(world.getSeed() + 5);
			int colX = (int) (colRand.nextGaussian() * 1500);
			int colZ = (int) (colRand.nextGaussian() * 1500);
			int colRange = 750;

			if(GeneralConfig.enable528ColtanDeposit) {
				for(int k = 0; k < 2; k++) {

					for(int r = 1; r <= 5; r++) {
						int randPosX = i + rand.nextInt(16);
						int randPosY = rand.nextInt(25) + 15;
						int randPosZ = j + rand.nextInt(16);

						int range = colRange / r;

						if(randPosX <= colX + range && randPosX >= colX - range && randPosZ <= colZ + range && randPosZ >= colZ - range) {
							(new WorldGenMinable(ModBlocks.ore_coltan.getStateFromMeta(0), 4, BlockMatcher.forBlock(Blocks.STONE))).generate(world, rand, new BlockPos(randPosX, randPosY, randPosZ));
						}
					}
				}
			}
		}

		boolean enableDungeons = world.getWorldInfo().isMapFeaturesEnabled();
		if(GeneralConfig.enableDungeons == 1) enableDungeons = true;
		if(GeneralConfig.enableDungeons == 0) enableDungeons = false;

		if(enableDungeons && world.provider.getDimension() == 0) {

			if(MobConfig.enableHives && rand.nextInt(MobConfig.hiveSpawn) == 0) {
				int x = i + rand.nextInt(16) + 8;
				int z = j + rand.nextInt(16) + 8;
				int y = world.getHeight(x, z);

				for(int k = 3; k >= -1; k--) {
					if(world.getBlockState(new BlockPos(x, y - 1 + k, z)).isNormalCube()) {
						GlyphidHive.generateSmall(world, x, y + k, z, rand, rand.nextInt(10) == 0, true);
						break;
					}
				}
			}

			if(biome.getDefaultTemperature() >= 0.4F && biome.getRainfall() <= 0.6F) {
				if(WorldConfig.antennaStructure > 0 && rand.nextInt(WorldConfig.antennaStructure) == 0) {
					for(int a = 0; a < 1; a++) {
						int x = i + rand.nextInt(16);
						int z = j + rand.nextInt(16);
						int y = world.getHeight(x, z);

						new Antenna().generate(world, rand, x, y, z);
					}
				}
			}

			if(!biome.canRain() && biome.getDefaultTemperature() >= 1.5F) {
				if(WorldConfig.atomStructure > 0 && rand.nextInt(WorldConfig.atomStructure) == 0) {
					for(int a = 0; a < 1; a++) {
						int x = i + rand.nextInt(16);
						int z = j + rand.nextInt(16);
						int y = world.getHeight(x, z);

						new DesertAtom001().generate(world, rand, x, y, z);
					}
				}
			}

			if(WorldConfig.dungeonStructure > 0 && rand.nextInt(WorldConfig.dungeonStructure) == 0) {
				int x = i + rand.nextInt(16);
				int y = rand.nextInt(256);
				int z = j + rand.nextInt(16);
				new LibraryDungeon().generate(world, rand, x, y, z);
			}

			if(WorldConfig.dudStructure > 0 && rand.nextInt(WorldConfig.dudStructure) == 0) {
				int x = i + 8 + rand.nextInt(16);
				int z = j + 8 + rand.nextInt(16);
				int y = world.getHeight(x, z);

				new Dud().generate(world, rand, x, y, z);
			}


			if(WorldConfig.barrelStructure > 0 && biome.getDefaultTemperature() >= 1.5F && !biome.canRain() && rand.nextInt(WorldConfig.barrelStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				new Barrel().generate(world, rand, x, y, z);
			}

			if(WorldConfig.broadcaster > 0 && rand.nextInt(WorldConfig.broadcaster) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				if(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock().canPlaceTorchOnTop(world.getBlockState(new BlockPos(x, y - 1, z)), world, new BlockPos(x, y - 1, z))) {
					world.setBlockState(new BlockPos(x, y, z), ModBlocks.broadcaster_pc.getStateFromMeta(rand.nextInt(4) + 2), 2);

					if(GeneralConfig.enableDebugMode)
						MainRegistry.logger.info("[Debug] Successfully spawned corrupted broadcaster at " + x + " " + (y) +" " + z);
				}
			}

			if(WorldConfig.minefreq > 0 && GeneralConfig.enableMines && rand.nextInt(WorldConfig.minefreq) == 0) {
				int x = i + rand.nextInt(16) + 8;
				int z = j + rand.nextInt(16) + 8;
				int y = world.getHeight(x, z);

				for(int g = y + 2; g >= y; g--) {

					if(world.getBlockState(new BlockPos(x, g - 1, z)).getBlock().canPlaceTorchOnTop(world.getBlockState(new BlockPos(x, g - 1, z)), world, new BlockPos(x, g - 1, z))) {
						world.setBlockState(new BlockPos(x, g, z), ModBlocks.mine_ap.getStateFromMeta(0), 3);
						TileEntity tile = world.getTileEntity(new BlockPos(x, g, z));
						if(tile instanceof TileEntityLandmine) {
							TileEntityLandmine landmine = (TileEntityLandmine) tile;
							landmine.waitingForPlayer = true;
						}
						if(GeneralConfig.enableDebugMode) MainRegistry.logger.info("[Debug] Successfully spawned landmine at " + x + " " + g + " " + z);
						break;
					}
				}
			}

			if(rand.nextInt(2000) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				if(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock().canPlaceTorchOnTop(world.getBlockState(new BlockPos(x, y - 1, z)), world, new BlockPos(x, y - 1, z)) && world.getBlockState(new BlockPos(x, y, z)).getBlock().isReplaceable(world, new BlockPos(x, y, z))) {

					world.setBlockState(new BlockPos(x, y, z), ModBlocks.lantern_behemoth.getStateFromMeta(12), 3);
					MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {4, 0, 0, 0, 0, 0}, ModBlocks.lantern_behemoth, ForgeDirection.NORTH);

					TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
					if(tile instanceof TileEntityLanternBehemoth) {
						TileEntityLanternBehemoth lantern = (TileEntityLanternBehemoth) tile;
						lantern.isBroken = true;
					}

					if(rand.nextInt(2) == 0) {
						LootGenerator.setBlock(world, x, y, z - 2);
						LootGenerator.lootBooklet(world, x, y, z - 2);
					}

					if(GeneralConfig.enableDebugMode)
						MainRegistry.logger.info("[Debug] Successfully spawned lantern at " + x + " " + (y) + " " + z);
				}
			}

			if(GeneralConfig.enable528BosniaSimulator && rand.nextInt(16) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);
				if(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock().canPlaceTorchOnTop(world.getBlockState(new BlockPos(x, y - 1, z)), world, new BlockPos(x, y - 1, z))) {
					world.setBlockState(new BlockPos(x, y, z), ModBlocks.mine_he.getStateFromMeta(0), 3);
					TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
					if(tile instanceof TileEntityLandmine) {
						TileEntityLandmine landmine = (TileEntityLandmine) tile;
						landmine.waitingForPlayer = true;
					}
				}
			}

			if (WorldConfig.geyserChlorine > 0 && biome == Biomes.PLAINS && rand.nextInt(WorldConfig.geyserWater) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				if(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() == Blocks.GRASS)
					new Geyser().generate(world, rand, x, y, z);
			}

			if (WorldConfig.geyserWater > 0 && biome == Biomes.DESERT && rand.nextInt(WorldConfig.geyserChlorine) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				if(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() == Blocks.SAND)
					new GeyserLarge().generate(world, rand, x, y, z);
			}

			if (WorldConfig.capsuleStructure > 0 && biome == Biomes.BEACH && rand.nextInt(WorldConfig.capsuleStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z) - 4;

				if(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock().canPlaceTorchOnTop(world.getBlockState(new BlockPos(x, y + 1, z)), world, new BlockPos(x, y + 1, z))) {

					world.setBlockState(new BlockPos(x, y, z), ModBlocks.soyuz_capsule.getStateFromMeta(3), 2);
					TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));

					if(tile instanceof TileEntitySoyuzCapsule) {
						TileEntitySoyuzCapsule cap = (TileEntitySoyuzCapsule) tile;
						cap.setInventorySlotContents(rand.nextInt(cap.getSizeInventory()), new ItemStack(ModItems.record_glass));
					}

					if(GeneralConfig.enableDebugMode)
						MainRegistry.logger.info("[Debug] Successfully spawned capsule at " + x + " " + z);
				}
			}

			if (WorldConfig.geyserVapor > 0 && rand.nextInt(WorldConfig.geyserVapor) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);
				
				for(int k = 1; k >= -1; k--) {
					if(world.getBlockState(new BlockPos(x, y + k, z)).getBlock() == Blocks.STONE) {
						world.setBlockState(new BlockPos(x, y + k, z), ModBlocks.geysir_vapor.getStateFromMeta(0), 3);
						MainRegistry.logger.info("[Debug] Successfully spawned vapor geyser at " + x + " " + z);
						break;
					}
				}
			}

			if (rand.nextInt(1000) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);

				boolean done = false;

				for(int k = 0; k < 256; k++) {
					if(world.getBlockState(new BlockPos(x, k, z)).getBlock() == Blocks.LOG && Blocks.LOG.getMetaFromState(world.getBlockState(new BlockPos(x, k, z))) == 0) {
						world.setBlockState(new BlockPos(x, k, z), ModBlocks.pink_log.getStateFromMeta(0), 3);
						done = true;
					}
				}

				if(GeneralConfig.enableDebugMode && done)
					MainRegistry.logger.info("[Debug] Successfully spawned pink tree at " + x + " " + z);
			}

			if (WorldConfig.vaultfreq > 0 && GeneralConfig.enableVaults && rand.nextInt(WorldConfig.vaultfreq) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				if(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock().canPlaceTorchOnTop(world.getBlockState(new BlockPos(x, y - 1, z)), world, new BlockPos(x, y - 1, z))) {
					world.setBlockState(new BlockPos(x, y, z), ModBlocks.safe.getStateFromMeta(rand.nextInt(4) + 2), 2);

					TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
					if(tile instanceof TileEntitySafe) {
						TileEntitySafe safe = (TileEntitySafe) tile;

						switch(rand.nextInt(10)) {
						case 0: case 1: case 2: case 3:
							safe.setMod(1);
							WeightedRandomChestContent.generateChestContents(rand, ItemPool.getPool(ItemPoolsSingle.POOL_VAULT_RUSTY), safe, rand.nextInt(4) + 3);
							break;
						case 4: case 5: case 6:
							safe.setMod(0.1);
							WeightedRandomChestContent.generateChestContents(rand, ItemPool.getPool(ItemPoolsSingle.POOL_VAULT_STANDARD), safe, rand.nextInt(3) + 2);
							break;
						case 7: case 8:
							safe.setMod(0.02);
							WeightedRandomChestContent.generateChestContents(rand, ItemPool.getPool(ItemPoolsSingle.POOL_VAULT_REINFORCED), safe, rand.nextInt(3) + 1);
							break;
						case 9:
							safe.setMod(0.0);
							WeightedRandomChestContent.generateChestContents(rand, ItemPool.getPool(ItemPoolsSingle.POOL_VAULT_UNBREAKABLE), safe, rand.nextInt(2) + 1);
							break;
						}

						safe.setPins(rand.nextInt(999) + 1);
						safe.lock();

						if(rand.nextInt(10) < 3) safe.fillWithSpiders(); // 30% chance; those safes have been sitting there for ages, they gotta have some spiders in them
					}

					if(GeneralConfig.enableDebugMode)
						MainRegistry.logger.info("[Debug] Successfully spawned safe at " + x + " " + (y + 1) +" " + z);
				}

			}

			if((biome == Biomes.JUNGLE || biome == Biomes.JUNGLE_EDGE || biome == Biomes.JUNGLE_HILLS) &&
					WorldConfig.jungleStructure > 0 && rand.nextInt(WorldConfig.jungleStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);

				CellularDungeonFactory.jungle.generate(world, x, 20, z, world.rand);
				CellularDungeonFactory.jungle.generate(world, x, 24, z, world.rand);
				CellularDungeonFactory.jungle.generate(world, x, 28, z, world.rand);

				if(GeneralConfig.enableDebugMode)
					MainRegistry.logger.info("[Debug] Successfully spawned jungle dungeon at " + x + " 10 " + z);

				int y = world.getHeight(x, z);

				for(int f = 0; f < 3; f++)
					world.setBlockState(new BlockPos(x, y + f, z), ModBlocks.deco_titanium.getStateFromMeta(0), 3);
				world.setBlockState(new BlockPos(x, y + 3, z), Blocks.REDSTONE_BLOCK.getStateFromMeta(0), 3);
			}

			if (WorldConfig.arcticStructure > 0 && rand.nextInt(WorldConfig.arcticStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = 16 + rand.nextInt(32);
				new ArcticVault().trySpawn(world, x, y, z);
			}

			if (WorldConfig.pyramidStructure > 0 && biome.getDefaultTemperature() >= 2.0F && !biome.canRain() && rand.nextInt(WorldConfig.pyramidStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				new AncientTomb().build(world, rand, x, y, z);
			}
		}

		if(rand.nextInt(4) == 0) {
			int x = i + rand.nextInt(16) + 8;
			int y = 6 + rand.nextInt(13);
			int z = j + rand.nextInt(16) + 8;

			BlockPos keyholePos = new BlockPos(x, y, z);
			IBlockState keyholeState = world.getBlockState(keyholePos);
			if(keyholeState.getBlock().isReplaceableOreGen(keyholeState, world, keyholePos, BlockMatcher.forBlock(Blocks.STONE))) {
				world.setBlockState(new BlockPos(x, y, z), ModBlocks.stone_keyhole.getStateFromMeta(0), 3);
			}
		}

		genBlueprintChest(world, rand, i, j, 5000, 5000);
	}

	private static void genBlueprintChest(World world, Random rand, int i, int j, int boundsX, int boundsZ) {
		if(Math.abs(i) < 100 && Math.abs(j) < 100) return;
		if(rand.nextInt(20) < 10) return; // nextBoolean would have weird periodicity to it, hoping that a larger int range has more variance

		int cX = Math.abs(i) % boundsX;
		int cZ = Math.abs(j) % boundsZ;

		if(cX >= 0 && cX < 16 && cZ >= 0 && cZ < 16) {
			int x = i + 8;
			int z = j + 8;
			int y = world.getHeight(x, z) - rand.nextInt(2);

			world.setBlockState(new BlockPos(x, y, z), Blocks.CHEST.getStateFromMeta(0), 3);

			for(int a = x - 1; a <= x + 1; a++) for(int b = y - 1; b <= y + 1; b++) for(int c = z - 1; c <= z + 1; c++) {
				if(a != x || b != y || c != z) world.setBlockState(new BlockPos(a, b, c), Blocks.OBSIDIAN.getStateFromMeta(0), 3);
			}

			TileEntity tile = Compat.getTileStandard(world, x, y, z);

			if(tile instanceof TileEntityChest) WeightedRandomChestContent.generateChestContents(rand, ItemPool.getPool(ItemPoolsSingle.POOL_BLUEPRINTS), (TileEntityChest) tile, 50);
		}
	}

	private void generateNether(World world, Random rand, int i, int j) {
		if(WorldConfig.netherOre) {
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherUraniumuSpawn, 6, 0, 127, ModBlocks.ore_nether_uranium, Blocks.NETHERRACK);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherTungstenSpawn, 10, 0, 127, ModBlocks.ore_nether_tungsten, Blocks.NETHERRACK);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherSulfurSpawn, 12, 0, 127, ModBlocks.ore_nether_sulfur, Blocks.NETHERRACK);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherPhosphorusSpawn, 6, 0, 127, ModBlocks.ore_nether_fire, Blocks.NETHERRACK);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherCoalSpawn, 32, 16, 96, ModBlocks.ore_nether_coal, Blocks.NETHERRACK);
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherCobaltSpawn, 6, 100, 26, ModBlocks.ore_nether_cobalt, Blocks.NETHERRACK);

			if(GeneralConfig.enablePlutoniumOre)
				DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherPlutoniumSpawn, 4, 0, 127, ModBlocks.ore_nether_plutonium, Blocks.NETHERRACK);

			if(rand.nextInt(10) == 0) {
				@SuppressWarnings("unchecked")
				WeightedRandomGeneric<BedrockOreDefinition> item = (WeightedRandomGeneric<BedrockOreDefinition>) WeightedRandom.getRandomItem(rand, BedrockOre.weightedOresNether);
				BedrockOreDefinition def = item.get();
				int randPosX = i + rand.nextInt(2) + 8;
				int randPosZ = j + rand.nextInt(2) + 8;
				BedrockOre.generate(world, randPosX, randPosZ, def.stack, def.acid, def.color, def.tier, ModBlocks.stone_depth_nether, Blocks.NETHERRACK);
			}

			DepthDeposit.generateConditionNether(world, i, 0, 3, j, 7, 0.6D, ModBlocks.ore_depth_nether_neodymium, rand, 16);
			DepthDeposit.generateConditionNether(world, i, 125, 3, j, 7, 0.6D, ModBlocks.ore_depth_nether_neodymium, rand, 16);
		}

		for(int k = 0; k < 30; k++){
			int x = i + rand.nextInt(16);
			int z = j + rand.nextInt(16);
			int d = 16 + rand.nextInt(96);

			for(int y = d - 5; y <= d; y++)
				if(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock() == Blocks.AIR && world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.NETHERRACK)
					world.setBlockState(new BlockPos(x, y, z), ModBlocks.ore_nether_smoldering.getStateFromMeta(0), 3);
		}

		for(int k = 0; k < 1; k++){
			int x = i + rand.nextInt(16);
			int z = j + rand.nextInt(16);
			int d = 16 + rand.nextInt(96);

			for(int y = d - 5; y <= d; y++)
				if(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock() == Blocks.AIR && world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.NETHERRACK)
					world.setBlockState(new BlockPos(x, y, z), ModBlocks.geysir_nether.getStateFromMeta(0), 3);
		}
	}

	private void generateEnd(World world, Random rand, int i, int j) {

		if(WorldConfig.endOre) {
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.endTikiteSpawn, 6, 0, 127, ModBlocks.ore_tikite, Blocks.END_STONE);
		}
	}
}
