package com.hbm.entity.missile;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.MissileStruct;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.items.weapon.ItemCustomMissilePart.FuelType;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.items.weapon.ItemCustomMissilePart.WarheadType;
import com.hbm.main.MainRegistry;

import api.hbm.entity.IRadarDetectableNT;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class EntityMissileCustom extends EntityMissileBaseNT implements IChunkLoader {

	private static final DataParameter<Integer> HEALTH_SNAPSHOT = EntityDataManager.createKey(EntityMissileCustom.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> WARHEAD = EntityDataManager.createKey(EntityMissileCustom.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> FUSELAGE = EntityDataManager.createKey(EntityMissileCustom.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> FINS = EntityDataManager.createKey(EntityMissileCustom.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> THRUSTER = EntityDataManager.createKey(EntityMissileCustom.class, DataSerializers.VARINT);

	public float fuel;
	public float consumption;

	public int getWarheadId() {
		return this.dataManager.get(WARHEAD);
	}

	public int getFuselageId() {
		return this.dataManager.get(FUSELAGE);
	}

	public int getFinsId() {
		return this.dataManager.get(FINS);
	}

	public int getThrusterId() {
		return this.dataManager.get(THRUSTER);
	}

	public EntityMissileCustom(World world) {
		super(world);
	}

	public EntityMissileCustom(World world, float x, float y, float z, int a, int b, MissileStruct template) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.setLocationAndAngles(x, y, z, 0, 0);
		startX = (int) x;
		startZ = (int) z;
		targetX = a;
		targetZ = b;
		this.motionY = 2;

		Vec3d vector = new Vec3d(targetX - startX, 0, targetZ - startZ);
		accelXZ = decelY = 1 / vector.length();
		decelY *= 2;
		velocity = 0;

		this.dataManager.set(WARHEAD, Item.getIdFromItem(template.warhead));
		this.dataManager.set(FUSELAGE, Item.getIdFromItem(template.fuselage));
		this.dataManager.set(THRUSTER, Item.getIdFromItem(template.thruster));
		if(template.fins != null) {
			this.dataManager.set(FINS, Item.getIdFromItem(template.fins));
		} else {
			this.dataManager.set(FINS, Integer.valueOf(0));
		}

		ItemCustomMissilePart fuselage = (ItemCustomMissilePart) template.fuselage;
		ItemCustomMissilePart thruster = (ItemCustomMissilePart) template.thruster;

		this.fuel = (float) fuselage.getTankSize();
		this.consumption = (Float) thruster.attributes[1];

		this.setSize(1.5F, 1.5F);
	}

	@Override
	protected void killMissile() {
		if(!this.isDead) {
			this.setDead();
			ExplosionLarge.explode(this.world, posX, posY, posZ, 5, true, false, true);
			ExplosionLarge.spawnShrapnelShower(this.world, posX, posY, posZ, motionX, motionY, motionZ, 15, 0.075);
		}
	}

	@Override
	public void onUpdate() {

		ItemCustomMissilePart part = (ItemCustomMissilePart) Item.getItemById(this.dataManager.get(WARHEAD));
		WarheadType type = (WarheadType) part.attributes[0];
		if(type != null && type.updateCustom != null) {
			type.updateCustom.accept(this);
		}

		if(!this.world.isRemote) {
			if(this.hasPropulsion()) this.fuel -= this.consumption;
		}

		super.onUpdate();
	}

	@Override
	public boolean hasPropulsion() {
		return this.fuel > 0;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(HEALTH_SNAPSHOT, Integer.valueOf(this.health));
		this.dataManager.register(WARHEAD, Integer.valueOf(0));
		this.dataManager.register(FUSELAGE, Integer.valueOf(0));
		this.dataManager.register(FINS, Integer.valueOf(0));
		this.dataManager.register(THRUSTER, Integer.valueOf(0));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		fuel = nbt.getFloat("fuel");
		consumption = nbt.getFloat("consumption");
		this.dataManager.set(WARHEAD, nbt.getInteger("warhead"));
		this.dataManager.set(FUSELAGE, nbt.getInteger("fuselage"));
		this.dataManager.set(FINS, nbt.getInteger("fins"));
		this.dataManager.set(THRUSTER, nbt.getInteger("thruster"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setFloat("fuel", fuel);
		nbt.setFloat("consumption", consumption);
		nbt.setInteger("warhead", this.dataManager.get(WARHEAD));
		nbt.setInteger("fuselage", this.dataManager.get(FUSELAGE));
		nbt.setInteger("fins", this.dataManager.get(FINS));
		nbt.setInteger("thruster", this.dataManager.get(THRUSTER));
	}

	@Override
	protected void spawnContrail() {

		Vec3d v = new Vec3d(motionX, motionY, motionZ).normalize();
		String smoke = "";
		ItemCustomMissilePart part = (ItemCustomMissilePart) Item.getItemById(this.dataManager.get(FUSELAGE));
		FuelType type = (FuelType) part.attributes[0];

		switch(type) {
		case BALEFIRE: smoke = "exBalefire"; break;
		case HYDROGEN: smoke = "exHydrogen"; break;
		case KEROSENE: smoke = "exKerosene"; break;
		case SOLID: smoke = "exSolid"; break;
		case XENON: break;
		case HYDRAZINE: smoke = "exKerosene"; break;
		case METHALOX: smoke = "exKerosene"; break;
		}

		if(!smoke.isEmpty()) {
			for (int i = 0; i < velocity; i++) {
				NBTTagCompound data = new NBTTagCompound();
				data.setDouble("posX", posX - v.x * i);
				data.setDouble("posY", posY - v.y * i);
				data.setDouble("posZ", posZ - v.z * i);
				data.setString("type", smoke);
				MainRegistry.proxy.effectNT(data);
			}
		}
	}

	@Override
	public void onMissileImpact(RayTraceResult mop) { //TODO: demolish this steaming pile of shit

		ItemCustomMissilePart part = (ItemCustomMissilePart) Item.getItemById(this.dataManager.get(WARHEAD));

		WarheadType type = (WarheadType) part.attributes[0];
		float strength = (Float) part.attributes[1];

		if(type.impactCustom != null) {
			type.impactCustom.accept(this);
			return;
		}

		switch(type) {
		case HE:
			ExplosionLarge.explode(this.world, posX, posY, posZ, strength, true, false, true);
			ExplosionLarge.jolt(this.world, posX, posY, posZ, strength, (int) (strength * 50), 0.25);
			break;
		case INC:
			ExplosionLarge.explodeFire(this.world, posX, posY, posZ, strength, true, false, true);
			ExplosionLarge.jolt(this.world, posX, posY, posZ, strength * 1.5, (int) (strength * 50), 0.25);
			break;
		case CLUSTER:
			break;
		case BUSTER:
			ExplosionLarge.buster(this.world, posX, posY, posZ, new Vec3d(motionX, motionY, motionZ), strength, strength * 4);
			break;
		case NUCLEAR:
		case TX:
			this.world.spawnEntityInWorld(EntityNukeExplosionMK5.statFac(this.world, (int) strength, posX, posY, posZ));
			EntityNukeTorex.statFacStandard(this.world, posX, posY, posZ, strength);
			break;
		case BALEFIRE:
			EntityBalefire bf = new EntityBalefire(this.world);
			bf.posX = this.posX;
			bf.posY = this.posY;
			bf.posZ = this.posZ;
			bf.destructionRange = (int) strength;
			this.world.spawnEntityInWorld(bf);
			EntityNukeTorex.statFacBale(this.world, posX, posY, posZ, strength);
			break;
		case N2:
			this.world.spawnEntityInWorld(EntityNukeExplosionMK5.statFacNoRad(this.world, (int) strength, posX, posY, posZ));
			EntityNukeTorex.statFacStandard(this.world, posX, posY, posZ, strength);
			break;
		case TAINT:
			int r = (int) strength;
			for(int i = 0; i < r * 10; i++) {
				int a = rand.nextInt(r) + (int) posX - (r / 2 - 1);
				int b = rand.nextInt(r) + (int) posY - (r / 2 - 1);
				int c = rand.nextInt(r) + (int) posZ - (r / 2 - 1);
				Block block = com.hbm.world.WorldBlockState.read(this.world, a, b, c).getBlock();
				if(block.isNormalCube() && !block.isAir(this.world, a, b, c)) {
					this.world.setBlock(a, b, c, ModBlocks.taint, rand.nextInt(3) + 4, 2);
				}
			}
			break;
		case CLOUD:
			this.world.playAuxSFX(2002, (int) Math.round(this.posX), (int) Math.round(this.posY), (int) Math.round(this.posZ), 0);
			ExplosionChaos.spawnPoisonCloud(this.world, posX - motionX, posY - motionY, posZ - motionZ, 750, 2.5, 2);
			break;
		case TURBINE:
			ExplosionLarge.explode(this.world, posX, posY, posZ, 10, true, false, true);
			int count = (int) strength;
			Vec3d vec = new Vec3d(0.5, 0, 0);

			for(int i = 0; i < count; i++) {
				EntityBulletBaseNT blade = new EntityBulletBaseNT(this.world, BulletConfigSyncingUtil.TURBINE);
				blade.setPositionAndRotation(this.posX - this.motionX, this.posY - this.motionY + rand.nextGaussian(), this.posZ - this.motionZ, 0, 0);
				blade.motionX = vec.x;
				blade.motionZ = vec.z;
				this.world.spawnEntityInWorld(blade);
				vec = vec.rotateYaw((float) (Math.PI * 2F / (float) count));
			}

			break;
		default:
			break;

		}
	}

	@Override
	public String getUnlocalizedName() {

		ItemCustomMissilePart part = (ItemCustomMissilePart) Item.getItemById(this.dataManager.get(FUSELAGE));
		PartSize top = part.top;
		PartSize bottom = part.bottom;

		if(top == PartSize.SIZE_10 && bottom == PartSize.SIZE_10) return "radar.target.custom10";
		if(top == PartSize.SIZE_10 && bottom == PartSize.SIZE_15) return "radar.target.custom1015";
		if(top == PartSize.SIZE_15 && bottom == PartSize.SIZE_15) return "radar.target.custom15";
		if(top == PartSize.SIZE_15 && bottom == PartSize.SIZE_20) return "radar.target.custom1520";
		if(top == PartSize.SIZE_20 && bottom == PartSize.SIZE_20) return "radar.target.custom20";

		return "radar.target.custom";
	}

	@Override
	public int getBlipLevel() {

		ItemCustomMissilePart part = (ItemCustomMissilePart) Item.getItemById(this.dataManager.get(FUSELAGE));
		PartSize top = part.top;
		PartSize bottom = part.bottom;

		if(top == PartSize.SIZE_10 && bottom == PartSize.SIZE_10) return IRadarDetectableNT.TIER10;
		if(top == PartSize.SIZE_10 && bottom == PartSize.SIZE_15) return IRadarDetectableNT.TIER10_15;
		if(top == PartSize.SIZE_15 && bottom == PartSize.SIZE_15) return IRadarDetectableNT.TIER15;
		if(top == PartSize.SIZE_15 && bottom == PartSize.SIZE_20) return IRadarDetectableNT.TIER15_20;
		if(top == PartSize.SIZE_20 && bottom == PartSize.SIZE_20) return IRadarDetectableNT.TIER20;

		return IRadarDetectableNT.TIER1;
	}

	@Override public List<ItemStack> getDebris() { return new ArrayList(); }
	@Override public ItemStack getDebrisRareDrop() { return null; }

	@Override
	public ItemStack getMissileItemForInfo() {
		return new ItemStack(ModItems.missile_custom);
	}
}
