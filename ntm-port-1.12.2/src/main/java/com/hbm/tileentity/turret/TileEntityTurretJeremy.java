package com.hbm.tileentity.turret;

import java.util.ArrayList;
import java.util.List;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.gui.GUITurretJeremy;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactoryTurret;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.CasingCreator;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TileEntityTurretJeremy extends TileEntityTurretBaseNT {

	static List<Integer> configs = new ArrayList();

	static {
		configs.add(XFactoryTurret.shell_normal.id);
		configs.add(XFactoryTurret.shell_explosive.id);
		configs.add(XFactoryTurret.shell_ap.id);
		configs.add(XFactoryTurret.shell_du.id);
		configs.add(XFactoryTurret.shell_w9.id);
	}

	@Override
	protected List<Integer> getAmmoList() {
		return configs;
	}

	@Override
	public String getDefaultName() {
		return "container.turretJeremy";
	}

	@Override
	public double getDecetorGrace() {
		return 16D;
	}

	@Override
	public double getTurretDepression() {
		return 45D;
	}

	@Override
	public long getMaxPower() {
		return 10000;
	}

	@Override
	public double getBarrelLength() {
		return 4.25D;
	}

	@Override
	public double getDecetorRange() {
		return 80D;
	}

	int timer;
	int reload;

	@Override
	public void update() {

		if(reload > 0)
			reload--;

		if(reload == 1)
			com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:turret.jeremy_reload", 2.0F, 1.0F);

		super.update();
	}

	@Override
	public void updateFiringTick() {

		timer++;

		if(timer % 40 == 0) {

			BulletConfig conf = this.getFirstConfigLoaded();

			if(conf != null) {
				this.cachedCasingConfig = conf.casing;
				this.spawnBullet(conf, 50F);
				this.conusmeAmmo(conf.ammo);
				com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:turret.jeremy_fire", 4.0F, 1.0F);
				Vec3d pos = this.getTurretPos();
				com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(this.getBarrelLength(), 0, 0);
				vec.rotateAroundZRad((float) -this.rotationPitch);
				vec.rotateAroundYRad((float) -(this.rotationYaw + Math.PI * 0.5));

				reload = 20;

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaExt");
				data.setString("mode", "largeexplode");
				data.setFloat("size", 0F);
				data.setByte("count", (byte)5);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.x + vec.x, pos.y + vec.y, pos.z + vec.z), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 50));
			}
		}
	}

	@Override
	protected void spawnCasing() {

		if(cachedCasingConfig == null) return;

		Vec3d spawn = this.getCasingSpawnPos();
		float yaw = (float) Math.toDegrees(rotationYaw);
		float pitch = (float) -Math.toDegrees(this.rotationPitch);
		
		CasingCreator.composeEffect(this.world,
				spawn.x, spawn.y, spawn.z,
				yaw, pitch,
				-0.2, -0.2, 0,
				0.01, -5, 0,
				cachedCasingConfig.getName(),
				true, 100, 0.5, 20);
		
		cachedCasingConfig = null;
	}

	@Override
	protected Vec3d getCasingSpawnPos() {

		Vec3d pos = this.getTurretPos();
		com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(-2, 0, 0);
		vec.rotateAroundZRad((float) -this.rotationPitch);
		vec.rotateAroundYRad((float) -(this.rotationYaw + Math.PI * 0.5));

		return new Vec3d(pos.x + vec.x, pos.y + vec.y, pos.z + vec.z);
	}

	@Override
	public boolean usesCasings() {
		return true;
	}

	@Override
	public int casingDelay() {
		return 22;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretJeremy(player.inventory, this);
	}
}
