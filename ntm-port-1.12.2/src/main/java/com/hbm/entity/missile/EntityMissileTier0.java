package com.hbm.entity.missile;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;

import com.hbm.items.weapon.sedna.factory.GunFactory.EnumAmmo;
import com.hbm.world.WorldUtil;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public abstract class EntityMissileTier0 extends EntityMissileBaseNT {

	public EntityMissileTier0(World world) { super(world); }
	public EntityMissileTier0(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }

	@Override
	public List<ItemStack> getDebris() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.add(new ItemStack(ModItems.wire_fine, 4, Mats.MAT_ALUMINIUM.id));
		list.add(new ItemStack(ModItems.plate_titanium, 4));
		list.add(new ItemStack(ModItems.shell, 2, Mats.MAT_ALUMINIUM.id));
		list.add(new ItemStack(ModItems.ducttape, 1));
		return list;
	}

	@Override
	protected float getContrailScale() {
		return 0.5F;
	}
	
	public static class EntityMissileTest extends EntityMissileTier0 {
		public EntityMissileTest(World world) { super(world); }
		public EntityMissileTest(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public ItemStack getDebrisRareDrop() { return null; }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_test); }
		
		@Override public void onMissileImpact(RayTraceResult mop) {
			int x = (int) Math.floor(posX);
			int y = (int) Math.floor(posY);
			int z = (int) Math.floor(posZ);
			int range = 50;

			for(int iX = -range; iX <= range; iX++) {
				for(int iY = -range; iY <= range; iY++) {
					for(int iZ = -range; iZ <= range; iZ++) {
						double dist = Math.sqrt(iX * iX + iY * iY + iZ * iZ);
						if(dist > range) continue;
						Block block = com.hbm.world.WorldBlockState.read(this.world, x + iX, y + iY, z + iZ).getBlock();
						int meta = this.world.getBlockMetadata(x + iX, y + iY, z + iZ);
						int charMeta = (int) MathHelper.clamp(12 - (dist / range) * (dist / range) * 13, (double) (0), (double) (12));
						
						if(block.isNormalCube()) {
							if(block != ModBlocks.sellafield_slaked || meta < charMeta) {
								this.world.setBlock(x + iX, y + iY, z + iZ, ModBlocks.sellafield_slaked, charMeta, 3);
							}
						} else {
							this.world.setBlock(x + iX, y + iY, z + iZ, Blocks.AIR);
						}
					}
				}
			}
		}
	}
	
	public static class EntityMissileMicro extends EntityMissileTier0 {
		public EntityMissileMicro(World world) { super(world); }
		public EntityMissileMicro(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) { ExplosionNukeSmall.explode(this.world, posX, posY + 0.5, posZ, ExplosionNukeSmall.PARAMS_HIGH); }
		@Override public ItemStack getDebrisRareDrop() { return DictFrame.fromOne(ModItems.ammo_standard, EnumAmmo.NUKE_HIGH); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_micro); }
	}
	
	public static class EntityMissileSchrabidium extends EntityMissileTier0 {
		public EntityMissileSchrabidium(World world) { super(world); }
		public EntityMissileSchrabidium(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			EntityNukeExplosionMK3 ex = EntityNukeExplosionMK3.statFacFleija(this.world, posX, posY, posZ, BombConfig.aSchrabRadius);
			if(!ex.isDead) {
				WorldUtil.loadAndSpawnEntityInWorld(ex);
				EntityCloudFleija cloud = new EntityCloudFleija(this.world, BombConfig.aSchrabRadius);
				cloud.posX = this.posX;
				cloud.posY = this.posY;
				cloud.posZ = this.posZ;
				this.world.spawnEntityInWorld(cloud);
			}
		}
		@Override public ItemStack getDebrisRareDrop() { return null; }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_schrabidium); }
	}
	
	public static class EntityMissileBHole extends EntityMissileTier0 {
		public EntityMissileBHole(World world) { super(world); }
		public EntityMissileBHole(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			this.world.createExplosion(this, this.posX, this.posY, this.posZ, 1.5F, true);
			EntityBlackHole bl = new EntityBlackHole(this.world, 1.5F);
			bl.posX = this.posX;
			bl.posY = this.posY;
			bl.posZ = this.posZ;
			this.world.spawnEntityInWorld(bl);
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModItems.grenade_black_hole, 1); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_bhole); }
	}
	
	public static class EntityMissileTaint extends EntityMissileTier0 {
		public EntityMissileTaint(World world) { super(world); }
		public EntityMissileTaint(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			this.world.createExplosion(this, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 5.0F, true);
			for(int i = 0; i < 100; i++) {
				int a = rand.nextInt(11) + mop.getBlockPos().getX() - 5;
				int b = rand.nextInt(11) + mop.getBlockPos().getY() - 5;
				int c = rand.nextInt(11) + mop.getBlockPos().getZ() - 5;
				Block block = com.hbm.world.WorldBlockState.read(this.world, a, b, c).getBlock();
				if(block.isNormalCube() && !block.isAir(this.world, a, b, c)) {
					this.world.setBlock(a, b, c, ModBlocks.taint, 0, 2);
				}
			}
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModItems.powder_spark_mix, 1); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_taint); }
	}
	
	public static class EntityMissileEMP extends EntityMissileTier0 {
		public EntityMissileEMP(World world) { super(world); }
		public EntityMissileEMP(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			ExplosionNukeGeneric.empBlast(this.world, (int)posX, (int)posY, (int)posZ, 50);
			EntityEMPBlast wave = new EntityEMPBlast(this.world, 50);
			wave.posX = posX;
			wave.posY = posY;
			wave.posZ = posZ;
			this.world.spawnEntityInWorld(wave);
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModBlocks.emp_bomb, 1); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_emp); }
	}
}
