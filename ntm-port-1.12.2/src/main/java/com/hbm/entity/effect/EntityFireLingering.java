package com.hbm.entity.effect;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.particle.helper.FlameCreator;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityFireLingering extends Entity {

	private static final DataParameter<Integer> FIRE_TYPE = EntityDataManager.createKey(EntityFireLingering.class, DataSerializers.VARINT);
	private static final DataParameter<Float> AREA_WIDTH = EntityDataManager.createKey(EntityFireLingering.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> AREA_HEIGHT = EntityDataManager.createKey(EntityFireLingering.class, DataSerializers.FLOAT);

	public static int TYPE_DIESEL = 0;
	public static int TYPE_BALEFIRE = 1;
	public static int TYPE_PHOSPHORUS = 2;
	public static int TYPE_OXY = 3;
	public static int TYPE_BLACK = 4;
	public int maxAge = 150;

	public EntityFireLingering(World world) {
		super(world);
	}

	public EntityFireLingering setArea(float width, float height) {
		this.dataManager.set(AREA_WIDTH, width);
		this.dataManager.set(AREA_HEIGHT, height);
		return this;
	}
	public EntityFireLingering setDuration(int duration){
		this.maxAge = duration;
		return this;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(FIRE_TYPE, new Integer(0));
		this.dataManager.register(AREA_WIDTH, new Float(0));
		this.dataManager.register(AREA_HEIGHT, new Float(0));
	}

	public EntityFireLingering setType(int type) {
		this.dataManager.set(FIRE_TYPE, type);
		return this;
	}

	public int getType() {
		return this.dataManager.get(FIRE_TYPE);
	}

	@Override
	public void onEntityUpdate() {

		float height = this.dataManager.get(AREA_HEIGHT);
		this.setSize(this.dataManager.get(AREA_WIDTH), height);
		this.setPosition(this.posX, this.posY, this.posZ);

		if(!this.world.isRemote) {

			if(this.ticksExisted >= maxAge) {
				this.setDead();
			}

			List<Entity> affected = this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(posX - width / 2, posY, posZ - width / 2, posX + width / 2, posY + height, posZ + width / 2));

			for(Entity e : affected) {
				if(e instanceof EntityLivingBase) {
					EntityLivingBase livng = (EntityLivingBase) e;
					HbmLivingProps props = HbmLivingProps.getData(livng);
					if(this.getType() == this.TYPE_DIESEL) if(props.fire < 60) props.fire = 60;
					if(this.getType() == this.TYPE_PHOSPHORUS) if(props.fire < 300) props.fire = 300;
					if(this.getType() == this.TYPE_BALEFIRE) if(props.balefire < 100) props.balefire = 100;
					if(this.getType() == this.TYPE_BLACK) if(props.blackFire < 200) props.blackFire = 200; else props.blackFire += 5;
				} else {
					e.setFire(4);
				}
			}
		} else {

			for(int i = 0; i < (width >= 5 ? 2 : 1); i++) {
				double x = posX - width / 2 + rand.nextDouble() * width;
				double z = posZ - width / 2 + rand.nextDouble() * width;

				Vec3d up = new Vec3d(x, posY + height, z);
				Vec3d down = new Vec3d(x, posY - height, z);
				RayTraceResult mop = this.world.rayTraceBlocks(up, down, false, true, true);
				if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) down = mop.hitVec;
				if(this.getType() == this.TYPE_DIESEL) FlameCreator.composeEffectClient(this.world, x, down.y, z, FlameCreator.META_FIRE);
				if(this.getType() == this.TYPE_PHOSPHORUS) FlameCreator.composeEffectClient(this.world, x, down.y, z, FlameCreator.META_FIRE);
				if(this.getType() == this.TYPE_BALEFIRE) FlameCreator.composeEffectClient(this.world, x, down.y, z, FlameCreator.META_BALEFIRE);
				if(this.getType() == this.TYPE_BLACK) FlameCreator.composeEffectClient(this.world, x, down.y, z, FlameCreator.META_BLACK);
			}
		}
	}

	@Override @SideOnly(Side.CLIENT) public boolean canRenderOnFire() { return false; }
	@Override protected void writeEntityToNBT(NBTTagCompound nbt) { }
	@Override public boolean writeToNBTOptional(NBTTagCompound nbt) { return false; }
	@Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) { return nbt; }
	@Override public void readEntityFromNBT(NBTTagCompound nbt) { this.setDead(); }
}
