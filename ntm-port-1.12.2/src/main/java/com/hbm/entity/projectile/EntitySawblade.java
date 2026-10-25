package com.hbm.entity.projectile;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class EntitySawblade extends EntityThrowableInterp {

	private static final DataParameter<Integer> ORIENTATION = EntityDataManager.createKey(EntitySawblade.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> META = EntityDataManager.createKey(EntitySawblade.class, DataSerializers.VARINT);

	public EntitySawblade(World world) {
		super(world);
		this.setSize(1F, 1F);
	}

	public EntitySawblade(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setSize(1F, 1F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(ORIENTATION, new Integer(0));
		this.dataManager.register(META, new Integer(0));
	}

	public EntitySawblade setOrientation(int rot) {
		this.dataManager.set(ORIENTATION, rot);
		return this;
	}

	public int getOrientation() {
		return this.dataManager.get(ORIENTATION);
	}

	public int getMeta() {
		return this.dataManager.get(META);
	}


		@Override
	public boolean processInitialInteract(net.minecraft.entity.player.EntityPlayer player, net.minecraft.util.EnumHand hand) {
		return interactFirst(player);
	}

public boolean interactFirst(EntityPlayer player) {

		if(!this.world.isRemote) {

			if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.sawblade)))
				this.setDead();

			player.inventoryContainer.detectAndSendChanges();
		}

		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	protected void onImpact(RayTraceResult mop) {

		if(this.world != null && mop != null && mop.typeOfHit == Type.ENTITY && mop.entityHit.isEntityAlive()) {
			Entity e = mop.entityHit;
			e.attackEntityFrom(ModDamageSource.rubble, 1000);
			if(!e.isEntityAlive() && e instanceof EntityLivingBase) {
				NBTTagCompound vdat = new NBTTagCompound();
				vdat.setString("type", "giblets");
				vdat.setInteger("ent", e.getEntityId());
				vdat.setInteger("cDiv", 5);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(vdat, e.posX, e.posY + e.height * 0.5, e.posZ), new TargetPoint(e.dimension, e.posX, e.posY + e.height * 0.5, e.posZ, 150));

				com.hbm.sound.VanillaWorldSoundEffects.play(this.world, e.posX, e.posY, e.posZ, "mob.zombie.woodbreak", 2.0F, 0.95F + this.world.rand.nextFloat() * 0.2F);
			}
		}

		if(this.ticksExisted > 1 && this.world != null && mop != null && mop.typeOfHit == Type.BLOCK) {

			int orientation = this.dataManager.get(ORIENTATION);

			if(orientation < 6) {

				if(new Vec3d(motionX, motionY, motionZ).length() < 0.75) {
					this.dataManager.set(ORIENTATION, orientation + 6);
					orientation += 6;
				} else {
					net.minecraft.util.EnumFacing side = mop.sideHit;
					this.motionX *= 1 - (Math.abs(side.getXOffset()) * 2);
					this.motionY *= 1 - (Math.abs(side.getYOffset()) * 2);
					this.motionZ *= 1 - (Math.abs(side.getZOffset()) * 2);
					this.world.createExplosion(this, posX, posY, posZ, 3F, false);

					if(this.world.getBlockState(mop.getBlockPos()).getBlock().getExplosionResistance(this) < 50) {
						com.hbm.world.WorldBlockDestruction.destroy(this.world, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), false);
					}
				}
			}

			if(orientation >= 6) {
				this.motionX = 0;
				this.motionY = 0;
				this.motionZ = 0;
				this.inGround = true;
			}
		}
	}

	@Override
	public void onUpdate() {

		if(!this.world.isRemote) {
			int orientation = this.dataManager.get(ORIENTATION);
			if(orientation >= 6 && !this.inGround) {
				this.dataManager.set(ORIENTATION, orientation - 6);
			}
		}

		super.onUpdate();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}

	@Override
	public double getGravityVelocity() {
		return inGround ? 0 : 0.03D;
	}

	@Override
	protected int groundDespawn() {
		return 0;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("rot", this.getOrientation());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.setOrientation(nbt.getInteger("rot"));
	}
}
