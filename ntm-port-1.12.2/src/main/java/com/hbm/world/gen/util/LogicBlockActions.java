package com.hbm.world.gen.util;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TextFormatting;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockSkeletonHolder;
import com.hbm.blocks.generic.LogicBlock;
import com.hbm.entity.item.EntityFallingBlockNT;
import com.hbm.entity.missile.EntityMissileTier2;
import com.hbm.entity.mob.EntityUndeadSoldier;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.tileentity.TileEntityDoorGeneric;
import com.hbm.tileentity.machine.storage.TileEntityCrateBase;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.MobUtil;
import com.hbm.util.Vec3NT;
import com.hbm.world.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

import java.util.*;
import java.util.function.Consumer;

public class LogicBlockActions {

	public static LinkedHashMap<String, Consumer<LogicBlock.TileEntityLogicBlock>> actions = new LinkedHashMap<>();

	public static Consumer<LogicBlock.TileEntityLogicBlock> PHASE_ABERRATOR = (tile) -> {
		World world = tile.getWorld();
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();
		if (tile.phase == 1 || tile.phase == 2) {
			tile.player = world.getClosestPlayer(x, y, z, 25, false);
			if (tile.timer == 0) {
				Vec3NT vec = new Vec3NT(20, 0, 0);
				for (int i = 0; i < 10; i++) {

					if(vec.x > 8) vec.x += world.rand.nextInt(10) - 5;

					EntityUndeadSoldier mob = new EntityUndeadSoldier(world);
					for (int j = 0; j < 7; j++) {
						mob.setPositionAndRotation(x + 0.5 + vec.x, world.getHeight((int) (x + 0.5 + vec.x),(int) (z + 0.5 + vec.z)), z + 0.5 + vec.z, i * 36F, 0);
						if (mob.getCanSpawnHere()) {
							mob.onInitialSpawn(world.getDifficultyForLocation(mob.getPosition()), null);
							if(tile.player != null){
								mob.setAttackTarget(tile.player);
							}
							world.spawnEntity(mob);
							break;
						}
					}
					vec.rotateAroundYDeg(36D);
				}
			}
		}
		if (tile.phase > 2) {
			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y + 18, z));
			if (te instanceof BlockSkeletonHolder.TileEntitySkeletonHolder) {
				BlockSkeletonHolder.TileEntitySkeletonHolder skeleton = (BlockSkeletonHolder.TileEntitySkeletonHolder) te;
				if (world.rand.nextInt(5) == 0) {
					skeleton.item = new ItemStack(ModItems.item_secret, 1, ItemEnums.EnumSecretType.ABERRATOR.ordinal());
				} else {
					skeleton.item = new ItemStack(ModItems.clay_tablet, 1, 1);
				}
				skeleton.markDirty();
				com.hbm.world.WorldBlockState.markBlockForUpdate(world, x, y + 18, z);
			}
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.OBSIDIAN; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> COLLAPSE_ROOF_RAD_5 = (tile) -> {
		World world = tile.getWorld();
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();

		if(tile.phase == 0) return;

		//from explosionChaos because i cannot be assed
		int r = 4;
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

						if (com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock().getExplosionResistance(null) <= 70) {
							EntityFallingBlockNT entityfallingblock = new EntityFallingBlockNT(world, X + 0.5, Y + 0.5, Z + 0.5, com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock(), com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, X, Y, Z) ));
							world.spawnEntity(entityfallingblock);
						}
					}
				}
			}
		}
		{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
//		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.block_steel); this is useless
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> COLLAPSE_ROOF_RAD_10 = (tile) -> {
		World world = tile.getWorld();
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();

		if(tile.phase == 0) return;

		int r = 8;
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

						if (com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock().getExplosionResistance(null) <= 70) {
							EntityFallingBlockNT entityfallingblock = new EntityFallingBlockNT(world, X + 0.5, Y + 0.5, Z + 0.5, com.hbm.world.WorldBlockState.read(world, X, Y, Z).getBlock(), com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, X, Y, Z) ));
							world.spawnEntity(entityfallingblock);
						}
					}
				}
			}
		}
		{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> FODDER_WAVE = (tile) -> {
		World world = tile.getWorld();
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();
		if (tile.phase == 1) {
			Vec3NT vec = new Vec3NT(5, 0, 0);
			for (int i = 0; i < 10; i++) {
				EntityZombie mob = new EntityZombie(world);
				mob.setPositionAndRotation(x + 0.5 + vec.x, world.getHeight(x,z), z + 0.5 + vec.z, i * 36F, 0);
				MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolAdv, new Random());
				world.spawnEntity(mob);

				vec.rotateAroundYDeg(36D);
			}
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.block_steel);
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> SKELETON_GUN_TIER_1 = (tile) -> {
		World world = tile.getWorld();
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();
		if (tile.phase == 1) {
			EntitySkeleton mob = new EntitySkeleton(world);
			mob.setPositionAndRotation(x, y, z, 0, 0);
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolGunsTier1, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolMasks, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolRanged, new Random());
			world.spawnEntity(mob);
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> SKELETON_GUN_TIER_2 = (tile) -> {
		World world = tile.getWorld();
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();
		if (tile.phase == 1) {
			EntitySkeleton mob = new EntitySkeleton(world);
			mob.setPositionAndRotation(x, y, z, 0, 0);
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolGunsTier2, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolMasks, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolTierArmor, new Random());
			world.spawnEntity(mob);
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> SKELETON_GUN_TIER_3 = (tile) -> {
		World world = tile.getWorld();
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();
		if (tile.phase == 1) {
			EntitySkeleton mob = new EntitySkeleton(world);
			mob.setPositionAndRotation(x, y, z, 0, 0);
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolGunsTier3, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolMasks, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolAdvRanged, new Random());
			world.spawnEntity(mob);
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> ZOMBIE_TIER_1 = (tile) -> {
		World world = tile.getWorld();
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();
		if (tile.phase == 1) {
			EntityZombie mob = new EntityZombie(world);
			mob.setPositionAndRotation(x, y, z, 0, 0);
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolMelee, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolTierArmor, new Random());
			world.spawnEntity(mob);
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> ZOMBIE_TIER_2 = (tile) -> {
		World world = tile.getWorld();
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();
		if (tile.phase == 1) {
			EntityZombie mob = new EntityZombie(world);
			mob.setPositionAndRotation(x, y, z, 0, 0);
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolTierArmor, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolMelee, new Random());
			world.spawnEntity(mob);
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> PUZZLE_TEST = (tile) -> {
		World world = tile.getWorld();
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();

		if(tile.phase == 2){
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.crate_steel);

			EntityLightningBolt blitz = new EntityLightningBolt(world, x, world.getHeight(x, z) + 2, z, false);
			world.spawnEntity(blitz);

			TileEntityCrateBase crate = (TileEntityCrateBase) world.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));
			((IInventory)crate).setInventorySlotContents(15, new ItemStack(ModItems.gun_bolter));
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> MISSILE_STRIKE = (tile) -> {
		World world = tile.getWorld();
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();

		if(tile.phase != 1) return;

		world.getClosestPlayer(x, y, z, 25, false).sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "[COMMAND UNIT]"+ TextFormatting.RESET + " Missile Fired"));

		ForgeDirection parallel = tile.direction.getRotation(ForgeDirection.UP);

		EntityMissileTier2.EntityMissileStrong missile =
				new EntityMissileTier2.EntityMissileStrong(
						world,
						x + tile.direction.offsetX * 300,
						200,
						z + tile.direction.offsetZ * 300,
						x + parallel.offsetX * 30 + tile.direction.offsetX * 30,
						z + parallel.offsetZ * 30 + tile.direction.offsetZ * 30);
		WorldUtil.loadAndSpawnEntityInWorld(missile);

		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.block_electrical_scrap);
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> RAD_CONTAINMENT_SYSTEM = (tile) -> {
		World world = tile.getWorld();
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();

		ForgeDirection direction = tile.direction.getOpposite();
		ForgeDirection rot = direction.getRotation(ForgeDirection.UP);

		AxisAlignedBB bb = new AxisAlignedBB(x - rot.offsetX, y - 1, z - rot.offsetZ, x + rot.offsetX + direction.offsetX * 15, y + 1, z + rot.offsetZ + direction.offsetZ * 15).grow(2,2,2);

		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, bb);

		for(EntityLivingBase e : entities) {

			Vec3d vec = new Vec3d(e.posX - (x + 0.5), (e.posY + e.getEyeHeight()) - (y + 0.5), e.posZ - (z + 0.5));
			double len = vec.length();
			vec = vec.normalize();

			len = Math.max(len,1D);

			float res = 0;

			for(int i = 1; i < len; i++) {

				int ix = (int)Math.floor(x + 0.5 + vec.x * i);
				int iy = (int)Math.floor(y + 0.5 + vec.y * i);
				int iz = (int)Math.floor(z + 0.5 + vec.z * i);

				res += com.hbm.world.WorldBlockState.read(world, ix, iy, iz).getBlock().getExplosionResistance(null);
			}

			if(res < 1)
				res = 1;

			float eRads = 100F;
			eRads /= (float)res;
			eRads /= (float)(len * len);

			ContaminationUtil.contaminate(e, ContaminationUtil.HazardType.RADIATION, ContaminationUtil.ContaminationType.HAZMAT2, eRads);
		}

		if (tile.phase == 2 && tile.timer > 40){
			world.getClosestPlayer(x, y, z, 25, false).sendMessage(new TextComponentString(
				TextFormatting.LIGHT_PURPLE + "[RAD CONTAINMENT SYSTEM]" +
					TextFormatting.RESET + " Diagnostics found containment failure, commencing lockdown"));

			for(int i = 1; i < 20; i++) {
				int checkX, checkY, checkZ;
				checkX = x + direction.offsetX * i;
				checkY = y + 1;
				checkZ = z + direction.offsetZ * i;
				Block block = com.hbm.world.WorldBlockState.read(world, checkX, checkY,checkZ).getBlock();
				TileEntity te = null;
				if(block instanceof  BlockDummyable){
					int[] coreCoords = ((BlockDummyable) block).findCore(world,checkX,checkY,checkZ);
					te = world.getTileEntity(new net.minecraft.util.math.BlockPos(coreCoords[0], coreCoords[1], coreCoords[2]));
				}

				if (te instanceof TileEntityDoorGeneric) {
					TileEntityDoorGeneric door = (TileEntityDoorGeneric) te;
					door.setPins(456);
					door.close();
					door.lock();
					break;
				}
			}

			tile.phase = 3;
		}
	};

	public static List<String> getActionNames(){
		return new ArrayList<>(actions.keySet());
	}

	//register new actions here
	static{
		//example actions
		actions.put("FODDER_WAVE", FODDER_WAVE);
		actions.put("ABERRATOR", PHASE_ABERRATOR);
		actions.put("COLLAPSE_ROOF_RAD_5", COLLAPSE_ROOF_RAD_5);
		actions.put("COLLAPSE_ROOF_RAD_10", COLLAPSE_ROOF_RAD_10);
		actions.put("PUZZLE_TEST", PUZZLE_TEST);
		actions.put("MISSILE_STRIKE", MISSILE_STRIKE);
		actions.put("IRRADIATE_ENTITIES_AOE", RAD_CONTAINMENT_SYSTEM);

		//Mob Block Actions
		actions.put("SKELETON_GUN_TIER_1", SKELETON_GUN_TIER_1);
		actions.put("SKELETON_GUN_TIER_2", SKELETON_GUN_TIER_2);
		actions.put("SKELETON_GUN_TIER_3", SKELETON_GUN_TIER_3);

		actions.put("ZOMBIE_TIER_1", ZOMBIE_TIER_1);
		actions.put("ZOMBIE_TIER_2", ZOMBIE_TIER_2);
	}

}
