package com.hbm.explosion;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.GameType;
import com.hbm.lib.ForgeDirection;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.VersatileConfig;
import com.hbm.entity.grenade.EntityGrenadeASchrab;
import com.hbm.entity.grenade.EntityGrenadeNuclear;
import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.entity.projectile.EntityExplosiveBeam;
import com.hbm.interfaces.Spaghetti;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ArmorUtil;

import api.hbm.energymk2.IEnergyHandlerMK2;
import cofh.api.energy.IEnergyProvider;

@Spaghetti("this sucks ass")
public class ExplosionNukeGeneric {

	private final static Random random = new Random();

	public static void empBlast(World world, int x, int y, int z, int bombStartStrength) {
		int r = bombStartStrength;
		int r2 = r * r;
		int r22 = r2 / 2;
		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22) {
						emp(world, X, Y, Z);
					}
				}
			}
		}
	}
	
	public static void dealDamage(World world, double x, double y, double z, double radius) {
		dealDamage(world, x, y, z, radius, 250F);
	}
	
	public static void dealDamage(World world, double x, double y, double z, double radius, float maxDamage) {

		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(x, y, z, x, y, z).expand(radius, radius, radius));
		
		for(Entity e : list) {
			
			double dist = e.getDistance(x, y, z);
			
			if(dist <= radius) {
				
				double entX = e.posX;
				double entY = e.posY + e.getEyeHeight();
				double entZ = e.posZ;
				
				if(!isExplosionExempt(e) && !Library.isObstructed(world, x, y, z, entX, entY, entZ)) {

					double damage = maxDamage * (radius - dist) / radius;
					e.attackEntityFrom(ModDamageSource.nuclearBlast, (float)damage);
					e.setFire(5);
					
					double knockX = e.posX - x;
					double knockY = e.posY + e.getEyeHeight() - y;
					double knockZ = e.posZ - z;
					
					Vec3d knock = new Vec3d(knockX, knockY, knockZ);
					knock = knock.normalize();
					
					e.motionX += knock.x * 0.2D;
					e.motionY += knock.y * 0.2D;
					e.motionZ += knock.z * 0.2D;
				}
			}
		}
	}
	
	@Spaghetti("just look at it")
	private static boolean isExplosionExempt(Entity e) {
		
		if (e instanceof EntityOcelot ||
				e instanceof EntityGrenadeASchrab ||
				e instanceof EntityGrenadeNuclear ||
				e instanceof EntityExplosiveBeam ||
				e instanceof EntityBulletBaseNT ||
				e instanceof EntityPlayer &&
				ArmorUtil.checkArmor((EntityPlayer) e, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots)) {
			return true;
		}
		
		if (e instanceof EntityPlayerMP && ((EntityPlayerMP)e).interactionManager.getGameType() == GameType.CREATIVE) {
			return true;
		}
		
		return false;
	}

	public static void vapor(World world, int x, int y, int z, int bombStartStrength) {
		int r = bombStartStrength * 2;
		int r2 = r * r;
		int r22 = r2 / 2;
		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22)
						vaporDest(world, X, Y, Z);
				}
			}
		}
	}

	public static int destruction(World world, int x, int y, int z) {
		int rand;
		if (!world.isRemote) {
			Block b = com.hbm.world.WorldBlockState.read(world, x,y,z).getBlock();
			if (b.getExplosionResistance(null)>=200f) {	//500 is the resistance of liquids
				//blocks to be spared
				int protection = (int)(b.getExplosionResistance(null)/300f);
				if (b == ModBlocks.brick_concrete) {
					rand = random.nextInt(8);
					if (rand == 0) {
						{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.GRAVEL; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
						return 0;
					}
				} else if (b == ModBlocks.brick_light) {
					rand = random.nextInt(3);
					if (rand == 0) {
						world.setBlock(x, y, z, ModBlocks.waste_planks, 0, 3);
						return 0;
					}else if (rand == 1){
						world.setBlock(x,y,z,ModBlocks.block_scrap,0,3);
						return 0;
					}
				} else if (b == ModBlocks.brick_obsidian) {
					rand = random.nextInt(20);
					if (rand == 0) {
						{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.OBSIDIAN; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
					}
				} else if (b == Blocks.OBSIDIAN) {
					world.setBlock(x, y, z, ModBlocks.gravel_obsidian, 0, 3);
					return 0;
				} else if(random.nextInt(protection+3)==0){
					world.setBlock(x, y, z, ModBlocks.block_scrap,0,3);
				}
				return protection;
			}else{//otherwise, kill the block!
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			}
		}
		return 0;
	}

	public static int vaporDest(World world, int x, int y, int z) {
		if (!world.isRemote) {
			Block b = com.hbm.world.WorldBlockState.read(world, x,y,z).getBlock();
			if (b.getExplosionResistance(null)<0.5f //most light things
					|| b == Blocks.WEB || b == ModBlocks.red_cable
					|| b instanceof BlockLiquid) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				return 0;
			} else if (b.getExplosionResistance(null)<=3.0f && !b.isOpaqueCube()){
				if(b != Blocks.CHEST && b != Blocks.FARMLAND){
					//destroy all medium resistance blocks that aren't chests or farmland
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
					return 0;
				}
			}
			
			if (com.hbm.lib.FacingCompat.isFlammable(b, world, x, y, z, ForgeDirection.UP)
					&& com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == Blocks.AIR) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y + 1; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.FIRE; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			}
			return (int)( b.getExplosionResistance(null)/300f);
		}
		return 0;
	}

	public static void waste(World world, int x, int y, int z, int radius) {
		int r = radius;
		int r2 = r * r;
		int r22 = r2 / 2;
		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22 + world.rand.nextInt(r22 / 5)) {
						if (com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock() != Blocks.AIR)
							wasteDest(world, X, Y, Z);
					}
				}
			}
		}
	}

	public static void wasteDest(World world, int x, int y, int z) {
		if (!world.isRemote) {
			int rand;
			Block b = com.hbm.world.WorldBlockState.read(world, x,y,z).getBlock();
			if (b == Blocks.wooden_door || b == Blocks.IRON_DOOR) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			}

			else if (b == Blocks.GRASS) {
				world.setBlock(x, y, z, ModBlocks.waste_earth);
			}

			else if (b == Blocks.MYCELIUM) {
				world.setBlock(x, y, z, ModBlocks.waste_mycelium);
			}

			else if (b == Blocks.SAND) {
				rand = random.nextInt(20);
				if (rand == 1 && com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) == 0) {
					world.setBlock(x, y, z, ModBlocks.waste_trinitite);
				}
				if (rand == 1 && com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) == 1) {
					world.setBlock(x, y, z, ModBlocks.waste_trinitite_red);
				}
			}

			else if (b == Blocks.CLAY) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.HARDENED_CLAY; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			}

			else if (b == Blocks.MOSSY_COBBLESTONE) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.COAL_ORE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			}

			else if (b == Blocks.COAL_ORE) {
				rand = random.nextInt(10);
				if (rand == 1 || rand == 2 || rand == 3) {
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.DIAMOND_ORE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
				if (rand == 9) {
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.EMERALD_ORE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}

			else if (b == Blocks.LOG || b == Blocks.LOG2) {
				world.setBlock(x, y, z, ModBlocks.waste_log);
			}

			else if (b == Blocks.BROWN_MUSHROOM_BLOCK) {
				if (com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) == 10) {
					world.setBlock(x, y, z, ModBlocks.waste_log);
				} else {
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}

			else if (b == Blocks.RED_MUSHROOM_BLOCK) {
				if (com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) == 10) {
					world.setBlock(x, y, z, ModBlocks.waste_log);
				} else {
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}
			
			else if (b.getDefaultState().getMaterial() == Material.wood && b.isOpaqueCube() && b != ModBlocks.waste_log) {
				world.setBlock(x, y, z, ModBlocks.waste_planks);
			}

			else if (b == ModBlocks.ore_uranium) {
				rand = random.nextInt(VersatileConfig.getSchrabOreChance());
				if (rand == 1) {
					world.setBlock(x, y, z, ModBlocks.ore_schrabidium);
				} else {
					world.setBlock(x, y, z, ModBlocks.ore_uranium_scorched);
				}
			}

			else if (b == ModBlocks.ore_nether_uranium) {
				rand = random.nextInt(VersatileConfig.getSchrabOreChance());
				if (rand == 1) {
					world.setBlock(x, y, z, ModBlocks.ore_nether_schrabidium);
				} else {
					world.setBlock(x, y, z, ModBlocks.ore_nether_uranium_scorched);
				}
			}

			else if (b == ModBlocks.ore_gneiss_uranium) {
				rand = random.nextInt(VersatileConfig.getSchrabOreChance());
				if (rand == 1) {
					world.setBlock(x, y, z, ModBlocks.ore_gneiss_schrabidium);
				} else {
					world.setBlock(x, y, z, ModBlocks.ore_gneiss_uranium_scorched);
				}
			}

		}
	}

	public static void wasteNoSchrab(World world, int x, int y, int z, int radius) {
		int r = radius;
		int r2 = r * r;
		int r22 = r2 / 2;
		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22 + world.rand.nextInt(r22 / 5)) {
						if (com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock() != Blocks.AIR)
							wasteDestNoSchrab(world, X, Y, Z);
					}
				}
			}
		}
	}

	public static void wasteDestNoSchrab(World world, int x, int y, int z) {
		if (!world.isRemote) {
			int rand;

			if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.GLASS || com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.STAINED_GLASS
					|| com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.wooden_door || com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.IRON_DOOR
					|| com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.LEAVES || com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.LEAVES2) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			}

			else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.GRASS) {
				world.setBlock(x, y, z, ModBlocks.waste_earth);
			}

			else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.MYCELIUM) {
				world.setBlock(x, y, z, ModBlocks.waste_mycelium);
			}

			else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.SAND) {
				rand = random.nextInt(20);
				if (rand == 1 && com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) == 0) {
					world.setBlock(x, y, z, ModBlocks.waste_trinitite);
				}
				if (rand == 1 && com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) == 1) {
					world.setBlock(x, y, z, ModBlocks.waste_trinitite_red);
				}
			}

			else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.CLAY) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.HARDENED_CLAY; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			}

			else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.MOSSY_COBBLESTONE) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.COAL_ORE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			}

			else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.COAL_ORE) {
				rand = random.nextInt(30);
				if (rand == 1 || rand == 2 || rand == 3) {
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.DIAMOND_ORE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
				if (rand == 29) {
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.EMERALD_ORE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}

			else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.LOG || com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.LOG2) {
				world.setBlock(x, y, z, ModBlocks.waste_log);
			}

			else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.PLANKS) {
				world.setBlock(x, y, z, ModBlocks.waste_planks);
			}

			else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.BROWN_MUSHROOM_BLOCK) {
				if (com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) == 10) {
					world.setBlock(x, y, z, ModBlocks.waste_log);
				} else {
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}

			else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.RED_MUSHROOM_BLOCK) {
				if (com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) == 10) {
					world.setBlock(x, y, z, ModBlocks.waste_log);
				} else {
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}
		}
	}

	public static void emp(World world, int x, int y, int z) {
		if (!world.isRemote) {
			
			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			
			if (te != null && te instanceof IEnergyHandlerMK2) {
				((IEnergyHandlerMK2)te).setPower(0);
				if(random.nextInt(5) < 1) world.setBlock(x, y, z, ModBlocks.block_electrical_scrap);
			}
			if (te != null && te instanceof IEnergyProvider) {

				((IEnergyProvider)te).extractEnergy(ForgeDirection.UP.toEnumFacing(), ((IEnergyProvider)te).getEnergyStored(ForgeDirection.UP.toEnumFacing()), false);
				((IEnergyProvider)te).extractEnergy(ForgeDirection.DOWN.toEnumFacing(), ((IEnergyProvider)te).getEnergyStored(ForgeDirection.DOWN.toEnumFacing()), false);
				((IEnergyProvider)te).extractEnergy(ForgeDirection.NORTH.toEnumFacing(), ((IEnergyProvider)te).getEnergyStored(ForgeDirection.NORTH.toEnumFacing()), false);
				((IEnergyProvider)te).extractEnergy(ForgeDirection.SOUTH.toEnumFacing(), ((IEnergyProvider)te).getEnergyStored(ForgeDirection.SOUTH.toEnumFacing()), false);
				((IEnergyProvider)te).extractEnergy(ForgeDirection.EAST.toEnumFacing(), ((IEnergyProvider)te).getEnergyStored(ForgeDirection.EAST.toEnumFacing()), false);
				((IEnergyProvider)te).extractEnergy(ForgeDirection.WEST.toEnumFacing(), ((IEnergyProvider)te).getEnergyStored(ForgeDirection.WEST.toEnumFacing()), false);
				
				if(random.nextInt(5) <= 1)
					world.setBlock(x, y, z, ModBlocks.block_electrical_scrap);
			}
		}
	}

	public static void solinium(World world, int x, int y, int z) {
		if (!world.isRemote) {
			Block b = com.hbm.world.WorldBlockState.read(world, x,y,z).getBlock();
			Material m = b.getDefaultState().getMaterial();
			
			if(b == Blocks.GRASS || b == Blocks.MYCELIUM || b == ModBlocks.waste_earth || b == ModBlocks.waste_mycelium) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.DIRT; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				return;
			}
			
			if(m == Material.CACTUS || m == Material.CORAL || m == Material.LEAVES || m == Material.PLANTS || m == Material.sponge || m == Material.VINE || m == Material.GOURD || m == Material.wood) {
				com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			}
		}
	}
}
