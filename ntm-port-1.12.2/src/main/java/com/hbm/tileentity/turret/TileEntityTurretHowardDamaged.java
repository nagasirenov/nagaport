package com.hbm.tileentity.turret;

import com.hbm.config.WeaponConfig;
import com.hbm.handler.guncfg.GunDGKFactory;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.EntityDamageUtil;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TileEntityTurretHowardDamaged extends TileEntityTurretHoward {

	@Override
	public boolean hasPower() { //does not need power
		return true;
	}

	@Override
	public boolean isOn() { //is always on
		return true;
	}

	@Override
	public double getTurretYawSpeed() {
		return 3D;
	}

	@Override
	public double getTurretPitchSpeed() {
		return 2D;
	}

	@Override
	public double getDecetorRange() {
		return 16D;
	}

	@Override
	public double getDecetorGrace() {
		return 5D;
	}

	@Override
	public boolean hasThermalVision() {
		return false;
	}

	@Override
	public boolean entityAcceptableTarget(Entity e) { //will fire at any living entity

		if(e instanceof EntityPlayer && ((EntityPlayer)e).capabilities.isCreativeMode)
			return false;

		return e instanceof EntityLivingBase;
	}

	@Override
	public void updateFiringTick() {

		timer++;

		if(this.tPos != null) {

			if(timer % 4 == 0) {

				com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:turret.howard_fire", 4.0F, 0.7F + this.world.rand.nextFloat() * 0.3F);

				this.cachedCasingConfig = GunDGKFactory.CASINGDGK;
				this.spawnCasing();

				if(this.world.rand.nextInt(100) + 1 <= WeaponConfig.ciwsHitrate * 0.5)
					EntityDamageUtil.attackEntityFromIgnoreIFrame(this.target, ModDamageSource.shrapnel, 2F + this.world.rand.nextInt(2));

				Vec3d pos = this.getTurretPos();
				com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(this.getBarrelLength(), 0, 0);
				vec.rotateAroundZRad((float) -this.rotationPitch);
				vec.rotateAroundYRad((float) -(this.rotationYaw + Math.PI * 0.5));

				com.hbm.util.Vec3NT hOff = new com.hbm.util.Vec3NT(0, 0.25, 0);
				hOff.rotateAroundZRad((float) -this.rotationPitch);
				hOff.rotateAroundYRad((float) -(this.rotationYaw + Math.PI * 0.5));

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaExt");
				data.setString("mode", "largeexplode");
				data.setFloat("size", 1.5F);
				data.setByte("count", (byte)1);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.x + vec.x + hOff.x, pos.y + vec.y + hOff.y, pos.z + vec.z + hOff.z), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 50));
			}
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}
}
