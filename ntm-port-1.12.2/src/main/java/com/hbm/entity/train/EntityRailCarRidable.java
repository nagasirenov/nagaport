package com.hbm.entity.train;

import java.util.ArrayList;
import java.util.List;

import com.hbm.main.MainRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public abstract class EntityRailCarRidable extends EntityRailCarCargo {

	public double engineSpeed;
	public SeatDummyEntity[] passengerSeats;

	public EntityRailCarRidable(World world) {
		super(world);
		this.passengerSeats = new SeatDummyEntity[this.getPassengerSeats().length];
	}

	/** Returns the linear speed added per tick when using powered movement */
	public abstract double getPoweredAcceleration();
	/** A mulitplier used on the speed either is there is no player in the train or if the parking brake is active */
	public abstract double getPassivBrake();
	/** The parking brake can be toggled, assuming a player is present, otherwise it is implicitly ON */
	public abstract boolean shouldUseEngineBrake(EntityPlayer player);
	/** The max speed the engine can provide in both directions */
	public abstract double getMaxPoweredSpeed();
	/** Whether the engine is turned on */
	public abstract boolean canAccelerate();
	/** Called every tick if acceleration is successful */
	public void consumeFuel() { }

	/** An additive to the engine's speed yielding the total speed, caused by uneven surfaces */
	public double getGravitySpeed() {
		return 0D;
	}

	@Override
	public double getCurrentSpeed() { // in its current form, only call once per tick

		Entity rider = this.getControllingPassenger();
		if(rider instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) rider;

			if(this.canAccelerate()) {
				if(player.moveForward > 0) {
					engineSpeed += this.getPoweredAcceleration();
					this.consumeFuel();
				} else if(player.moveForward < 0) {
					engineSpeed -= this.getPoweredAcceleration();
					this.consumeFuel();
				} else {
					if(this.shouldUseEngineBrake(player)) {
						engineSpeed *= this.getPassivBrake();
					} else {
						this.consumeFuel();
					}
				}
			} else {
				engineSpeed *= this.getPassivBrake();
			}

		} else {
			engineSpeed *= this.getPassivBrake();
		}

		double maxSpeed = this.getMaxPoweredSpeed();
		engineSpeed = MathHelper.clamp(engineSpeed, -maxSpeed, maxSpeed);

		return engineSpeed + this.getGravitySpeed();
	}

	@Override
	public boolean interactFirst(EntityPlayer player) {

		if(super.interactFirst(player)) return true;
		if(this.world.isRemote) return true;

		int nearestSeat = this.getNearestSeat(player);

		if(nearestSeat == -1) {
			player.startRiding(this);
		} else if(nearestSeat >= 0) {
			SeatDummyEntity dummySeat = new SeatDummyEntity(this.world, this, nearestSeat);
			Vec3d passengerSeat = this.getPassengerSeats()[nearestSeat];
			passengerSeat = passengerSeat.rotateYaw((float) (-this.rotationYaw * Math.PI / 180));
			double x = renderX + passengerSeat.x;
			double y = renderY + passengerSeat.y;
			double z = renderZ + passengerSeat.z;
			dummySeat.setPosition(x, y - 1, z);
			passengerSeats[nearestSeat] = dummySeat;
			this.world.spawnEntity(dummySeat);
			player.startRiding(dummySeat);
		}

		return true;
	}

	public int getNearestSeat(EntityPlayer player) {

		double nearestDist = Double.POSITIVE_INFINITY;
		int nearestSeat = -3;

		Vec3d[] seats = getPassengerSeats();
		Vec3d look = player.getLook(2).add(player.posX, player.posY + player.eyeHeight - player.getYOffset(), player.posZ);

		for(int i = 0; i < seats.length; i++) {

			Vec3d seat = seats[i];
			if(seat == null) continue;
			if(passengerSeats[i] != null) continue;

			seat = seat.rotateYaw((float) (-this.rotationYaw * Math.PI / 180));
			double x = renderX + seat.x;
			double y = renderY + seat.y;
			double z = renderZ + seat.z;

			Vec3d delta = new Vec3d(look.x - x, look.y - y, look.z - z);
			double dist = delta.length();

			if(dist < nearestDist) {
				nearestDist = dist;
				nearestSeat = i;
			}
		}

		if(!this.isBeingRidden()) {
			Vec3d seat = getRiderSeatPosition();
			seat = seat.rotateYaw((float) (-this.rotationYaw * Math.PI / 180));
			double x = renderX + seat.x;
			double y = renderY + seat.y;
			double z = renderZ + seat.z;

			Vec3d delta = new Vec3d(look.x - x, look.y - y, look.z - z);
			double dist = delta.length();

			if(dist < nearestDist) {
				nearestDist = dist;
				nearestSeat = -1;
			}
		}

		if(nearestDist > 180) return -2;

		return nearestSeat;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!this.world.isRemote) {

			Vec3d[] seats = this.getPassengerSeats();
			for(int i = 0; i < passengerSeats.length; i++) {
				SeatDummyEntity seat = passengerSeats[i];

				if(seat != null) {
					if(!seat.isBeingRidden()) {
						passengerSeats[i] = null;
						seat.setDead();
					} else {
						com.hbm.util.Vec3NT rot = new com.hbm.util.Vec3NT(seats[i]);
						rot.rotateAroundXRad((float) (this.rotationPitch * Math.PI / 180));
						rot.rotateAroundYRad((float) (-this.rotationYaw * Math.PI / 180));
						double x = renderX + rot.x;
						double y = renderY + rot.y;
						double z = renderZ + rot.z;
						seat.setPosition(x, y - 1, z);
					}
				}
			}
		}
	}

	public void updateRiderPosition() {

		com.hbm.util.Vec3NT offset = new com.hbm.util.Vec3NT(getRiderSeatPosition());
		offset.rotateAroundXRad((float) (this.rotationPitch * Math.PI / 180));
		offset.rotateAroundYRad((float) (-this.rotationYaw * Math.PI / 180));

		if(this.isBeingRidden()) {
			this.getPassengers().get(0).setPosition(this.renderX + offset.x, this.renderY + offset.y, this.renderZ + offset.z);
		}
	}

	/** Returns a Vec3d showing the relative position from the driver to the core */
	public abstract Vec3d getRiderSeatPosition();

	public abstract Vec3d[] getPassengerSeats();

	/** Dynamic seats generated when a player clicks near a seat-spot, moves and rotates with the train as one would expect. */
	public static class SeatDummyEntity extends Entity {

		private static final DataParameter<Integer> TRAIN_ID = EntityDataManager.createKey(SeatDummyEntity.class, DataSerializers.VARINT);
		private static final DataParameter<Integer> SEAT_INDEX = EntityDataManager.createKey(SeatDummyEntity.class, DataSerializers.VARINT);

		private int turnProgress;
		private double trainX;
		private double trainY;
		private double trainZ;
		public EntityRailCarRidable train;

		public SeatDummyEntity(World world) { super(world); this.setSize(0.5F, 0.1F);}
		public SeatDummyEntity(World world, EntityRailCarRidable train, int index) {
			this(world);
			this.train = train;
			if(train != null) this.dataManager.set(TRAIN_ID, train.getEntityId());
			this.dataManager.set(SEAT_INDEX, index);
		}

		@Override protected void entityInit() { this.dataManager.register(TRAIN_ID, new Integer(0)); this.dataManager.register(SEAT_INDEX, new Integer(0)); }
		@Override protected void writeEntityToNBT(NBTTagCompound nbt) { }
		@Override public boolean writeToNBTOptional(NBTTagCompound nbt) { return false; }
		@Override public void readEntityFromNBT(NBTTagCompound nbt) { this.setDead(); }

		@Override public void onUpdate() {
			if(!this.world.isRemote) {
				if(this.train == null || this.train.isDead) {
					this.setDead();
				}
			} else {

				if(this.turnProgress > 0) {
					this.prevRotationYaw = this.rotationYaw;
					double x = this.posX + (this.trainX - this.posX) / (double) this.turnProgress;
					double y = this.posY + (this.trainY - this.posY) / (double) this.turnProgress;
					double z = this.posZ + (this.trainZ - this.posZ) / (double) this.turnProgress;
					--this.turnProgress;
					this.setPosition(x, y, z);
				} else {
					this.setPosition(this.posX, this.posY, this.posZ);
				}
			}
		}

		@Override @SideOnly(Side.CLIENT) public void setPositionAndRotationDirect(double posX, double posY, double posZ, float yaw, float pitch, int turnProg, boolean teleport) {
			this.trainX = posX;
			this.trainY = posY;
			this.trainZ = posZ;
			this.turnProgress = turnProg + 2;
		}

		public void updateRiderPosition() {
			if(this.isBeingRidden()) {

				if(train == null) {
					int eid = this.dataManager.get(TRAIN_ID);
					Entity entity = this.world.getEntityByID(eid);
					if(entity instanceof EntityRailCarRidable) {
						train = (EntityRailCarRidable) entity;
					}
				}

				//fallback for when train is null
				if(train == null) {
					if(this.isBeingRidden()) this.getPassengers().get(0).setPosition(posX, posY + 1, posZ);
					return;
				}

				//doing it like this instead of with the position directly removes any discrepancies caused by entity tick order
				//mmhmhmhm silky smooth
				int index = this.dataManager.get(SEAT_INDEX);
				com.hbm.util.Vec3NT rot = new com.hbm.util.Vec3NT(this.train.getPassengerSeats()[index]);
				rot.rotateAroundXRad((float) (train.rotationPitch * Math.PI / 180));
				rot.rotateAroundYRad((float) (-train.rotationYaw * Math.PI / 180));
				double x = train.renderX + rot.x;
				double y = train.renderY + rot.y;
				double z = train.renderZ + rot.z;
				if(this.isBeingRidden()) this.getPassengers().get(0).setPosition(x, y, z);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
		List<String> text = new ArrayList();
		/*text.add("LTU: " + this.ltu);
		text.add("Front: " + this.coupledFront);
		text.add("Back: " + this.coupledBack);*/
		text.add("Nearest seat: " + this.getNearestSeat(MainRegistry.proxy.me()));
		//ILookOverlay.printGeneric(event, this.getClass().getSimpleName() + " " + this.hashCode(), 0xffff00, 0x404000, text);
	}
}
