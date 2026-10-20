package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TileEntityZirnoxDestroyed extends TileEntity implements net.minecraft.util.ITickable {

	public boolean onFire = true;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		onFire = nbt.getBoolean("fire");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("onFire", onFire);
		return nbt;
	}

	@Override
	public void update() {
		if(!this.world.isRemote) {
			radiate(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());

			if(this.world.rand.nextInt(5000) == 0)
				onFire = false;

			if(onFire && this.world.getTotalWorldTime() % 50 == 0) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "rbmkflame");
				data.setInteger("maxAge", 90);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, this.pos.getX() + 0.25 + this.world.rand.nextDouble() * 0.5, this.pos.getY() + 1.75, this.pos.getZ() + 0.25 + this.world.rand.nextDouble() * 0.5), new TargetPoint(this.world.provider.getDimension(), this.pos.getX() + 0.5, this.pos.getY() + 1.75, this.pos.getZ() + 0.5, 75));
				MainRegistry.proxy.effectNT(data);
				com.hbm.sound.VanillaWorldSoundEffects.play(this.world, this.pos.getX() + 0.5F, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "fire.fire", 1.0F + this.world.rand.nextFloat(), this.world.rand.nextFloat() * 0.7F + 0.3F);
			}
		}
	}

	private void radiate(World world, int x, int y, int z) {

		float rads = onFire ? 500000F : 75000F;
		double range = 100D;

		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5).expand(range, range, range));

		for(EntityLivingBase e : entities) {

			Vec3d vec = new Vec3d(e.posX - (x + 0.5), (e.posY + e.getEyeHeight()) - (y + 0.5), e.posZ - (z + 0.5));
			double len = vec.length();
			vec = vec.normalize();

			float res = 0;

			for(int i = 1; i < len; i++) {

				int ix = (int)Math.floor(x + 0.5 + vec.x * i);
				int iy = (int)Math.floor(y + 0.5 + vec.y * i);
				int iz = (int)Math.floor(z + 0.5 + vec.z * i);

				res += com.hbm.world.WorldBlockState.read(world, ix, iy, iz).getBlock().getExplosionResistance(null);
			}

			if(res < 1)
				res = 1;

			float eRads = rads;
			eRads /= (float)res;
			eRads /= (float)(len * len);

			ContaminationUtil.contaminate(e, HazardType.RADIATION, ContaminationType.CREATIVE, eRads);

			if(onFire && len < 5) {
				e.attackEntityFrom(DamageSource.onFire, 2);
			}
		}
	}

	public AxisAlignedBB getRenderBoundingBox() {
		return AxisAlignedBB.getBoundingBox(this.pos.getX() - 3, this.pos.getY(), this.pos.getZ() - 3, this.pos.getX() + 4, this.pos.getY() + 3, this.pos.getZ() + 4);
	}

	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
