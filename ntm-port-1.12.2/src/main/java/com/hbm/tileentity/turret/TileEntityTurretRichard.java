package com.hbm.tileentity.turret;

import java.util.ArrayList;
import java.util.List;

import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.inventory.gui.GUITurretRichard;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactoryRocket;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TileEntityTurretRichard extends TileEntityTurretBaseNT {

	static List<Integer> configs = new ArrayList();

	static {
		for(BulletConfig cfg : XFactoryRocket.rocket_ml) configs.add(cfg.id);
	}

	@Override
	protected List<Integer> getAmmoList() {
		return configs;
	}

	@Override
	public String getName() {
		return "container.turretRichard";
	}

	@Override
	public double getTurretDepression() {
		return 25D;
	}

	@Override
	public double getTurretElevation() {
		return 25D;
	}

	@Override
	public double getBarrelLength() {
		return 1.25D;
	}

	@Override
	public long getMaxPower() {
		return 10000;
	}

	@Override
	public double getDecetorGrace() {
		return 8D;
	}

	@Override
	public double getDecetorRange() {
		return 64D;
	}

	int timer;
	public int loaded;
	int reload;

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(!worldObj.isRemote) {

			if(reload > 0) {
				reload--;

				if(reload == 0)
					this.loaded = 17;
			}

			if(loaded <= 0 && reload <= 0 && this.getFirstConfigLoaded() != null) {
				reload = 100;
			}

			if(this.getFirstConfigLoaded() == null) {
				this.loaded = 0;
			}

			this.isTurretPacket = true;
			this.networkPackNT(250);
			this.isTurretPacket = false;
		}
	}

	// wow so descriptive, i dont wanna hear it; it solves the problem
	private boolean isTurretPacket = false;

	@Override
	public void serialize(ByteBuf buf) {
		if (this.isTurretPacket) {
			buf.writeBoolean(true);
			buf.writeInt(this.loaded);
		} else {
			buf.writeBoolean(false);
			super.serialize(buf);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		if(buf.readBoolean()) {
			this.loaded = buf.readInt();
		} else
			super.deserialize(buf);
	}

	@Override
	public void updateFiringTick() {

		if(reload > 0)
			return;

		timer++;

		if(timer > 0 && timer % 10 == 0) {

			BulletConfig conf = this.getFirstConfigLoaded();

			if(conf != null) {
				this.spawnBullet(conf, 30F);
				this.conusmeAmmo(conf.ammo);
				this.worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hbm:turret.richard_fire", 2.0F, 1.0F);
				this.loaded--;

				//if(conf.ammo.equals(new ComparableStack(ModItems.ammo_standard, EnumAmmo.ROCKET_DEMO))) timer = -50;

			} else {
				this.loaded = 0;
			}
		}
	}

	@Override
	public void spawnBullet(BulletConfig bullet, float baseDamage) {

		Vec3d pos = this.getTurretPos();
		com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(this.getBarrelLength(), 0, 0);
		vec.rotateAroundZRad((float) -this.rotationPitch);
		vec.rotateAroundYRad((float) -(this.rotationYaw + Math.PI * 0.5));

		EntityBulletBaseMK4 proj = new EntityBulletBaseMK4(worldObj, bullet, baseDamage, bullet.spread, (float) rotationYaw, (float) rotationPitch);
		proj.setPositionAndRotation(pos.x + vec.x, pos.y + vec.y, pos.z + vec.z, proj.rotationYaw, proj.rotationPitch);
		proj.lockonTarget = this.target;
		worldObj.spawnEntityInWorld(proj);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.loaded = nbt.getInteger("loaded");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("loaded", this.loaded);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretRichard(player.inventory, this);
	}
}
