package com.hbm.explosion;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.grenade.EntityGrenadeTau;
import com.hbm.entity.item.EntityFallingBlockNT;
import com.hbm.entity.particle.EntityCloudFX;
import com.hbm.entity.particle.EntityModFX;
import com.hbm.entity.particle.EntityOrangeFX;
import com.hbm.entity.particle.EntityPinkCloudFX;
import com.hbm.entity.projectile.EntityBullet;
import com.hbm.entity.projectile.EntityRocket;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.entity.projectile.EntitySchrab;
import com.hbm.interfaces.Spaghetti;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ArmorRegistry.HazardClass;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

@Deprecated
@Spaghetti("my eyes are bleeding")
public class ExplosionChaos { //TODO: destroy this entire class

	private final static Random random = new Random();
	private static Random rand = new Random();

	public static void hardenVirus(World world, int x, int y, int z, int bombStartStrength) {

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
						if (com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock() == ModBlocks.crystal_virus)
							{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = X; int nativeRegisteredWriteY = Y; int nativeRegisteredWriteZ = Z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.crystal_hardened; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
					}
				}
			}
		}
	}

	public static void spreadVirus(World world, int x, int y, int z, int bombStartStrength) {

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
						if (rand.nextInt(15) == 0 && com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock() != Blocks.AIR)
							world.setBlock(X, Y, Z, ModBlocks.cheater_virus_seed);
					}
				}
			}
		}
	}

	public static void pulse(World world, int x, int y, int z, int bombStartStrength) {

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
						
						if(com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock().getExplosionResistance(null) <= 70) {
							EntityFallingBlockNT entityfallingblock = new EntityFallingBlockNT(world, X + 0.5, Y + 0.5, Z + 0.5, com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock(), com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, X, Y, Z) ));
							world.spawnEntityInWorld(entityfallingblock);
						}
					}
				}
			}
		}
	}

	public static void explodeZOMG(World world, int x, int y, int z, int bombStartStrength) {

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
						if (!(com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock() == Blocks.BEDROCK && Y <= 0))
							{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = X; int nativePlacementY = Y; int nativePlacementZ = Z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
					}
				}
			}
		}
	}

	public static void decon(World world, int x, int y, int z, int radius) {

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
					if (ZZ < r22) {
						decontaminate(world, X, Y, Z);
					}
				}
			}
		}
	}

	/**
	 * Sets all flammable blocks on fire
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param bound
	 */
	public static void flameDeath(World world, int x, int y, int z, int bound) {

		int r = bound;
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
						if (com.hbm.lib.FacingCompat.isFlammable(com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock(), world, XX, YY, ZZ, ForgeDirection.UP)
								&& com.hbm.world.WorldBlockState.read(world, X, Y + 1, Z).getBlock() == Blocks.AIR) {
							{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = X; int nativePlacementY = Y + 1; int nativePlacementZ = Z; net.minecraft.block.Block nativePlacementBlock = Blocks.FIRE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
						}
					}
				}
			}
		}

	}

	/**
	 * Sets all blocks on fire
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param bound
	 */
	public static void burn(World world, int x, int y, int z, int bound) {

		int r = bound;
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
						if ((com.hbm.world.WorldBlockState.read(world, X, Y + 1, Z).getBlock() == Blocks.AIR
								|| com.hbm.world.WorldBlockState.read(world, X, Y + 1, Z).getBlock() == Blocks.SNOW_LAYER)
								&& com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock() != Blocks.AIR) {
							{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = X; int nativePlacementY = Y + 1; int nativePlacementZ = Z; net.minecraft.block.Block nativePlacementBlock = Blocks.FIRE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
						}
					}
				}
			}
		}

	}

	@Deprecated public static void spawnPoisonCloud(World world, double x, double y, double z, int count, double speed, int type) {
		
		for(int i = 0; i < count; i++) {
			
			EntityModFX fx = null;
			
			if(type == 1) {
				fx = new EntityCloudFX(world, x, y, z, 0.0, 0.0, 0.0);
			} else if(type == 2) {
				fx = new EntityPinkCloudFX(world, x, y, z, 0.0, 0.0, 0.0);
			} else {
				fx = new EntityOrangeFX(world, x, y, z, 0.0, 0.0, 0.0);
			}
			
			fx.motionY = rand.nextGaussian() * speed;
			fx.motionX = rand.nextGaussian() * speed;
			fx.motionZ = rand.nextGaussian() * speed;
			world.spawnEntityInWorld(fx);
		}
	}

	public static void spawnVolley(World world, double x, double y, double z, int count, double speed) {
		
		for(int i = 0; i < count; i++) {
			
			EntityModFX fx = new EntityOrangeFX(world, x, y, z, 0.0, 0.0, 0.0);
			
			fx.motionX = rand.nextGaussian() * speed;
			fx.motionZ = rand.nextGaussian() * speed;
			
			fx.motionY = rand.nextDouble() * speed * 7.5D;
			
			world.spawnEntityInWorld(fx);
		}
	}

	public static void cluster(World world, int x, int y, int z, int count, int gravity) {

		double d1 = 0;
		double d2 = 0;
		double d3 = 0;
		EntityRocket fragment;

		for (int i = 0; i < count; i++) {
			d1 = rand.nextDouble();
			d2 = rand.nextDouble();
			d3 = rand.nextDouble();

			if (rand.nextInt(2) == 0) {
				d1 *= -1;
			}

			if (rand.nextInt(2) == 0) {
				d3 *= -1;
			}

			fragment = new EntityRocket(world, x, y, z, d1, d2, d3, 0.0125D);

			world.spawnEntityInWorld(fragment);
		}
	}

	public static void schrab(World world, int x, int y, int z, int count, int gravity) {

		double d1 = 0;
		double d2 = 0;
		double d3 = 0;
		EntitySchrab fragment;

		for (int i = 0; i < count; i++) {
			d1 = rand.nextDouble();
			d2 = rand.nextDouble();
			d3 = rand.nextDouble();

			if (rand.nextInt(2) == 0) {
				d1 *= -1;
			}

			if (rand.nextInt(2) == 0) {
				d3 *= -1;
			}

			fragment = new EntitySchrab(world, x, y, z, d1, d2, d3, 0.0125D);

			world.spawnEntityInWorld(fragment);
		}
	}

	public static void frag(World world, int x, int y, int z, int count, boolean flame, Entity shooter) {

		double d1 = 0;
		double d2 = 0;
		double d3 = 0;
		EntityArrow fragment;

		for (int i = 0; i < count; i++) {
			d1 = rand.nextDouble();
			d2 = rand.nextDouble();
			d3 = rand.nextDouble();

			if (rand.nextInt(2) == 0) {
				d1 *= -1;
			}

			if (rand.nextInt(2) == 0) {
				d3 *= -1;
			}

			fragment = new EntityArrow(world, x, y, z);

			fragment.motionX = d1;
			fragment.motionY = d2;
			fragment.motionZ = d3;
			fragment.shootingEntity = shooter;

			fragment.setIsCritical(true);
			if (flame) {
				fragment.setFire(1000);
			}

			fragment.setDamage(2.5);

			world.spawnEntityInWorld(fragment);
		}
	}

	public static void poison(World world, double x, double y, double z, double range) {
		
		List<EntityLivingBase> affected = world.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(x - range, y - range, z - range, x + range, y + range, z + range));
		
		for(EntityLivingBase entity : affected) {
			
			if(entity.getDistance(x, y, z) > range)
				continue;
			
			if(ArmorRegistry.hasAnyProtection(entity, 3, HazardClass.GAS_LUNG, HazardClass.GAS_BLISTERING)) {
				ArmorUtil.damageGasMaskFilter(entity, 1);
			} else {
				entity.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.BLINDNESS, 5 * 20, 0));
				entity.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.POISON, 20 * 20, 2));
				entity.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.WITHER, 1 * 20, 1));
				entity.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SLOWNESS, 30 * 20, 1));
				entity.addPotionEffect(new PotionEffect(Potion.digSlowdown.getId(), 30 * 20, 2));
			}
		}
	}

	public static void pc(World world, double x, double y, double z, double range) {
		
		List<EntityLivingBase> affected = world.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(x - range, y - range, z - range, x + range, y + range, z + range));
		
		for(EntityLivingBase entity : affected) {
			
			if(entity.getDistance(x, y, z) > range)
				continue;
			
			ArmorUtil.damageSuit(entity, 0, 25);
			ArmorUtil.damageSuit(entity, 1, 25);
			ArmorUtil.damageSuit(entity, 2, 25);
			ArmorUtil.damageSuit(entity, 3, 25);
			entity.attackEntityFrom(ModDamageSource.pc, 5);
		}
	}

	public static void c(World world, double x, double y, double z, double range) {
		
		List<EntityLivingBase> affected = world.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(x - range, y - range, z - range, x + range, y + range, z + range));
		
		for(EntityLivingBase entity : affected) {
			
			if(entity.getDistance(x, y, z) > range)
				continue;
			
			ArmorUtil.damageSuit(entity, 0, 25);
			ArmorUtil.damageSuit(entity, 1, 25);
			ArmorUtil.damageSuit(entity, 2, 25);
			ArmorUtil.damageSuit(entity, 3, 25);
			
			if(ArmorUtil.checkForHazmat(entity))
				continue;
			
			if(entity.isPotionActive(HbmPotion.taint.id)) {
				entity.removePotionEffect(HbmPotion.taint.id);
				entity.addPotionEffect(new PotionEffect(HbmPotion.mutation.id, 1 * 60 * 60 * 20, 0, false));
			}
			
			entity.attackEntityFrom(ModDamageSource.cloud, 5);
		}
	}

	public static void floater(World world, int x, int y, int z, int radi, int height) {

		Block save;
		int meta;

		int r = radi;
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
						save = com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock();
						meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, X, Y, Z) );
						{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = X; int nativePlacementY = Y; int nativePlacementZ = Z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
						if (save != Blocks.AIR) {
							world.setBlock(X, Y + height, Z, save);
							world.setBlockMetadataWithNotify(X, Y + height, Z, meta, 2);
						}
					}
				}
			}
		}

	}

	public static void move(World world, int x, int y, int z, int radius, int a, int b, int c) {
		float f = radius;
		int i;
		int j;
		int k;
		double d5;
		double d6;
		double d7;
		double wat = radius;
		int rand = 0;

		radius *= 2.0F;
		i = MathHelper.floor(x - wat - 1.0D);
		j = MathHelper.floor(x + wat + 1.0D);
		k = MathHelper.floor(y - wat - 1.0D);
		int i2 = MathHelper.floor(y + wat + 1.0D);
		int l = MathHelper.floor(z - wat - 1.0D);
		int j2 = MathHelper.floor(z + wat + 1.0D);
		List list = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(i, k, l, j, i2, j2));

		for (int i1 = 0; i1 < list.size(); ++i1) {
			Entity entity = (Entity) list.get(i1);
			double d4 = entity.getDistance(x, y, z) / radius;

			if (d4 <= 1.0D) {
				d5 = entity.posX - x;
				d6 = entity.posY + entity.getEyeHeight() - y;
				d7 = entity.posZ - z;
				if (entity instanceof EntityLiving && !(entity instanceof EntitySheep)) {
					rand = random.nextInt(2);
					if (rand == 0) {
						((EntityLiving) entity).setCustomNameTag("Dinnerbone");
					} else {
						((EntityLiving) entity).setCustomNameTag("Grumm");
					}
				}

				if (entity instanceof EntitySheep) {
					((EntityLiving) entity).setCustomNameTag("jeb_");
				}

				double d9 = MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
				if (d9 < wat) {
					entity.setPosition(entity.posX += a, entity.posY += b, entity.posZ += c);
				}
			}
		}

		radius = (int) f;
	}

	public static void plasma(World world, int x, int y, int z, int radius) {
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
					if (ZZ < r22 + world.rand.nextInt(r22 / 2)) {
						if(com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock() != ModBlocks.statue_elb_f)
							world.setBlock(X, Y, Z, ModBlocks.plasma);
					}
				}
			}
		}
	}

	public static void tauMeSinPi(World world, double x, double y, double z, int count, Entity shooter,
			EntityGrenadeTau tau) {

		double d1 = 0;
		double d2 = 0;
		double d3 = 0;
		EntityBullet fragment;

		if (shooter != null && shooter instanceof EntityPlayer)
			for (int i = 0; i < count; i++) {
				d1 = rand.nextDouble();
				d2 = rand.nextDouble();
				d3 = rand.nextDouble();

				if (rand.nextInt(2) == 0) {
					d1 *= -1;
				}

				if (rand.nextInt(2) == 0) {
					d2 *= -1;
				}

				if (rand.nextInt(2) == 0) {
					d3 *= -1;
				}

				if (rand.nextInt(5) == 0) {
					fragment = new EntityBullet(world, (EntityPlayer) shooter, 3.0F, 35, 45, false, "tauDay", tau);
					fragment.setDamage(rand.nextInt(301) + 100);
				} else {
					fragment = new EntityBullet(world, (EntityPlayer) shooter, 3.0F, 35, 45, false, "eyyOk", tau);
					fragment.setDamage(rand.nextInt(11) + 35);
				}

				fragment.motionX = d1 * 5;
				fragment.motionY = d2 * 5;
				fragment.motionZ = d3 * 5;
				fragment.shootingEntity = shooter;

				fragment.setIsCritical(true);

				world.spawnEntityInWorld(fragment);
			}
	}

	public static void levelDown(World world, int x, int y, int z, int radius) {

		if(!world.isRemote)
		for (int i = x - radius; i <= x + radius; i++)
			for (int j = z - radius; j <= z + radius; j++) {
				
				Block b = com.hbm.world.WorldBlockState.read(world, i, y, j).getBlock();
				float k = b.getExplosionResistance(null);
						
				if(k < 6000 && b != Blocks.AIR) {
					
					EntityRubble rubble = new EntityRubble(world);
					rubble.posX = i + 0.5F;
					rubble.posY = y;
					rubble.posZ = j + 0.5F;
					
					rubble.motionY = 0.025F * 10 + 0.15F;
					rubble.setMetaBasedOnBlock(b, com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, i, y, j) ));
					
					world.spawnEntityInWorld(rubble);
					
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = i; int nativePlacementY = y; int nativePlacementZ = j; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}
	}

	public static void decontaminate(World world, int x, int y, int z) {
		
		Random random = new Random();

		if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.waste_earth && random.nextInt(3) != 0) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.GRASS; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}

		else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.waste_mycelium && random.nextInt(5) == 0) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.MYCELIUM; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}

		else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.waste_trinitite && random.nextInt(3) == 0) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.SAND; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}

		else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.waste_trinitite_red && random.nextInt(3) == 0) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.SAND; int nativePlacementMeta = 1; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}

		else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.waste_log && random.nextInt(3) != 0) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.LOG; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}

		else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.waste_planks && random.nextInt(3) != 0) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.PLANKS; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}

		else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.block_trinitite && random.nextInt(10) == 0) {
			world.setBlock(x, y, z, ModBlocks.block_lead);
		}

		else if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.block_waste && random.nextInt(10) == 0) {
			world.setBlock(x, y, z, ModBlocks.block_lead);
		}
		
		else if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.sellafield) {
			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
			
			if(meta > 0) {
				if(meta == 5 && random.nextInt(10) == 0)
					world.setBlockMetadataWithNotify(x, y, z, 4, 3);
				else if(random.nextInt(5) == 0)
					world.setBlockMetadataWithNotify(meta, x, y, z, meta - 1);
			} else if(random.nextInt(5) == 0)
				world.setBlock(y, z, meta, ModBlocks.sellafield_slaked);
		}
	}

}
