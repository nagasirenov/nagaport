package com.hbm.entity.effect;

import java.awt.Color;
import java.util.ArrayList;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.main.MainRegistry;
import com.hbm.util.BobMathUtil;
import com.hbm.util.TrackerUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/*
 * Toroidial Convection Simulation Explosion Effect
 * Tor                             Ex
 */
public class EntityNukeTorex extends Entity {
	private static final DataParameter<Float> SCALE = EntityDataManager.createKey(EntityNukeTorex.class, DataSerializers.FLOAT);
	private static final DataParameter<Integer> COLOR_TYPE = EntityDataManager.createKey(EntityNukeTorex.class, DataSerializers.VARINT);


	public double coreHeight = 3;
	public double convectionHeight = 3;
	public double torusWidth = 3;
	public double rollerSize = 1;
	public double heat = 1;
	public double lastSpawnY = - 1;
	public ArrayList<Cloudlet> cloudlets = new ArrayList();
	//public static int cloudletLife = 200;

	public boolean didPlaySound = false;
	public boolean didShake = false;

	private boolean hasSufficientPressure = true;

	public EntityNukeTorex(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.setSize(1F, 50F);

		// Check current atmospheric pressure (wow, so difficult)
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(world, CBT_Atmosphere.class);
		if(CelestialBody.inOrbit(world) || atmosphere == null || atmosphere.getPressure() < 0.01) {
			hasSufficientPressure = false;
		}
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(SCALE, new Float(1));
		this.dataManager.register(COLOR_TYPE, new Integer(0));
	}

	@Override @SideOnly(Side.CLIENT) public int getBrightnessForRender(float interp) { return 15728880; }
	@Override public float getBrightness(float interp) { return 1.0F; }

	@Override
	public void onUpdate() {

		double s = 1.5; //this.getScale();
		double cs = 1.5;
		int maxAge = this.getMaxAge();

		if(this.world.isRemote) {

			if(ticksExisted == 1) this.setScale((float) s);

			if(lastSpawnY == -1) {
				lastSpawnY = posY - 3;
			}

			if(ticksExisted < 100) this.world.lastLightningBolt = 2;

			int spawnTarget = Math.max(com.hbm.world.WorldHeight.read(this.world, (int) Math.floor(posX), (int) Math.floor(posZ)) - 3, 1);
			double moveSpeed = 0.5D;

			if(Math.abs(spawnTarget - lastSpawnY) < moveSpeed) {
				lastSpawnY = spawnTarget;
			} else {
				lastSpawnY += moveSpeed * Math.signum(spawnTarget - lastSpawnY);
			}

			if(!hasSufficientPressure) {
				// pondering my glowing orb
				if(ticksExisted < 40) {
					double range = (ticksExisted + rand.nextDouble()) * 0.6;
					int toSpawn = ticksExisted * 5;
					int lifetime = Math.max(800 - ticksExisted * 10, 40);

					for(int i = 0; i < toSpawn; i++) {
						double x = posX + rand.nextGaussian() * range;
						double y = posY + rand.nextGaussian() * range;
						double z = posZ + rand.nextGaussian() * range;
						Cloudlet cloud = new Cloudlet(x, y, z, (float)(rand.nextDouble() * 2D * Math.PI), 0, lifetime, TorexType.STARFISH);
						cloud.setScale(4F, 5F);
						cloud.setMotion(ticksExisted < 25 && rand.nextBoolean() ? 0.5 : 0.05);
						cloudlets.add(cloud);
					}
				}

				if(ticksExisted < 150 && !didPlaySound) {
					if(MainRegistry.proxy.me() != null && MainRegistry.proxy.me().getDistanceToEntity(this) < (ticksExisted * 1.5 + 1) * 1.5) {
						MainRegistry.proxy.playSoundClient(posX, posY, posZ, "hbm:weapon.nuclearExplosion", 10_000F, 1F);
						didPlaySound = true;
					}
				}

				for(Cloudlet cloud : cloudlets) cloud.update();
				coreHeight += 0.15 / s;
				torusWidth += 0.05 / s;
				rollerSize = torusWidth * 0.8;
				convectionHeight = coreHeight + rollerSize;

				int maxHeat = (int) (50 * cs);
				heat = maxHeat - Math.pow((maxHeat * this.ticksExisted) / maxAge, 1);

				cloudlets.removeIf(x -> x.isDead);

				return;
			}

			// spawn mush clouds
			double range = (torusWidth - rollerSize) * 0.25;
			double simSpeed = getSimulationSpeed();
			int toSpawn = (int) Math.ceil(10 * simSpeed * simSpeed);
			int lifetime = Math.min((ticksExisted * ticksExisted) + 200, maxAge - ticksExisted + 200);

			for(int i = 0; i < toSpawn; i++) {
				double x = posX + rand.nextGaussian() * range;
				double z = posZ + rand.nextGaussian() * range;
				Cloudlet cloud = new Cloudlet(x, lastSpawnY, z, (float)(rand.nextDouble() * 2D * Math.PI), 0, lifetime);
				cloud.setScale(1F + this.ticksExisted * 0.005F * (float) cs, 5F * (float) cs);
				cloudlets.add(cloud);
			}

			// spawn shock clouds
			if(ticksExisted < 150) {

				int cloudCount = ticksExisted * 5;
				int shockLife = Math.max(300 - ticksExisted * 20, 50);

				for(int i = 0; i < cloudCount; i++) {
					Vec3d vec = new Vec3d((ticksExisted * 1.5 + rand.nextDouble()) * 1.5, 0, 0);
					float rot = (float) (Math.PI * 2 * rand.nextDouble());
					vec = vec.rotateYaw(rot);
					this.cloudlets.add(new Cloudlet(vec.x + posX, com.hbm.world.WorldHeight.read(this.world, (int) (vec.x + posX) + 1, (int) (vec.z + posZ)), vec.z + posZ, rot, 0, shockLife, TorexType.SHOCK)
							.setScale(7F, 2F)
							.setMotion(ticksExisted > 15 ? 0.75 : 0));
				}

				if(!didPlaySound) {
					if(MainRegistry.proxy.me() != null && MainRegistry.proxy.me().getDistanceToEntity(this) < (ticksExisted * 1.5 + 1) * 1.5) {
						MainRegistry.proxy.playSoundClient(posX, posY, posZ, "hbm:weapon.nuclearExplosion", 10_000F, 1F);
						didPlaySound = true;
					}
				}
			}

			// spawn ring clouds
			if(ticksExisted < 130 * s) {
				lifetime *= s;
				for(int i = 0; i < 2; i++) {
					Cloudlet cloud = new Cloudlet(posX, posY + coreHeight, posZ, (float)(rand.nextDouble() * 2D * Math.PI), 0, lifetime, TorexType.RING);
					cloud.setScale(1F + this.ticksExisted * 0.0025F * (float) (cs * cs), 3F * (float) (cs * cs));
					cloudlets.add(cloud);
				}
			}

			// spawn condensation clouds
			if(ticksExisted > 130 * s && ticksExisted < 600 * s) {

				for(int i = 0; i < 20; i++) {
					for(int j = 0; j < 4; j++) {
						float angle = (float) (Math.PI * 2 * rand.nextDouble());
						com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(torusWidth + rollerSize * (5 + rand.nextDouble()), 0, 0);
						vec.rotateAroundZRad((float) (Math.PI / 45 * j));
						vec.rotateAroundYRad(angle);
						Cloudlet cloud = new Cloudlet(posX + vec.x, posY + coreHeight - 5 + j * s, posZ + vec.z, angle, 0, (int) ((20 + ticksExisted / 10) * (1 + rand.nextDouble() * 0.1)), TorexType.CONDENSATION);
						cloud.setScale(0.125F * (float) (cs), 3F * (float) (cs));
						cloudlets.add(cloud);
					}
				}
			}
			if(ticksExisted > 200 * s && ticksExisted < 600 * s) {

				for(int i = 0; i < 20; i++) {
					for(int j = 0; j < 4; j++) {
						float angle = (float) (Math.PI * 2 * rand.nextDouble());
						com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(torusWidth + rollerSize * (3 + rand.nextDouble() * 0.5), 0, 0);
						vec.rotateAroundZRad((float) (Math.PI / 45 * j));
						vec.rotateAroundYRad(angle);
						Cloudlet cloud = new Cloudlet(posX + vec.x, posY + coreHeight + 25 + j * cs, posZ + vec.z, angle, 0, (int) ((20 + ticksExisted / 10) * (1 + rand.nextDouble() * 0.1)), TorexType.CONDENSATION);
						cloud.setScale(0.125F * (float) (cs), 3F * (float) (cs));
						cloudlets.add(cloud);
					}
				}
			}

			for(Cloudlet cloud : cloudlets) {
				cloud.update();
			}
			coreHeight += 0.15 / s;
			torusWidth += 0.05 / s;
			rollerSize = torusWidth * 0.35;
			convectionHeight = coreHeight + rollerSize;

			int maxHeat = (int) (50 * cs);
			heat = maxHeat - Math.pow((maxHeat * this.ticksExisted) / maxAge, 1);

			cloudlets.removeIf(x -> x.isDead);
		}

		if(!this.world.isRemote && this.ticksExisted > maxAge) {
			this.setDead();
		}
	}

	public EntityNukeTorex setScale(float scale) {
		if(!this.world.isRemote) getDataManager().set(SCALE, scale);
		this.coreHeight = this.coreHeight / 1.5D * scale;
		this.convectionHeight = this.convectionHeight / 1.5D * scale;
		this.torusWidth = this.torusWidth / 1.5D * scale;
		this.rollerSize = this.rollerSize / 1.5D * scale;
		return this;
	}

	/** Updates synchronized scale only, without resizing local simulation fields. */
	public void setSynchronizedScale(float scale) {
		this.dataManager.set(SCALE, scale);
	}

	public EntityNukeTorex setType(int type) {
		this.dataManager.set(COLOR_TYPE, type);
		return this;
	}

	public double getSimulationSpeed() {

		int lifetime = getMaxAge();
		int simSlow = lifetime / 4;
		int simStop = lifetime / 2;
		int life = EntityNukeTorex.this.ticksExisted;

		if(life > simStop) {
			return 0D;
		}

		if(life > simSlow) {
			return 1D - ((double)(life - simSlow) / (double)(simStop - simSlow));
		}

		return 1.0D;
	}

	public double getScale() {
		return this.dataManager.get(SCALE);
	}

	public double getSaturation() {
		double d = (double) this.ticksExisted / (double) this.getMaxAge();
		return 1D - (d * d * d * d);
	}

	public double getGreying() {

		int lifetime = getMaxAge();
		int greying = lifetime * 3 / 4;

		if(ticksExisted > greying) {
			return 1 + ((double)(ticksExisted - greying) / (double)(lifetime - greying));
		}

		return 1D;
	}

	public float getAlpha() {

		int lifetime = getMaxAge();
		int fadeOut = lifetime * 3 / 4;
		int life = EntityNukeTorex.this.ticksExisted;

		if(life > fadeOut) {
			float fac = (float)(life - fadeOut) / (float)(lifetime - fadeOut);
			return 1F - fac;
		}

		return 1.0F;
	}

	public int getMaxAge() {
		double s = this.getScale();
		return (int) (45 * 20 * s);
	}

	public class Cloudlet {

		public double posX;
		public double posY;
		public double posZ;
		public double prevPosX;
		public double prevPosY;
		public double prevPosZ;
		public double motionX;
		public double motionY;
		public double motionZ;
		public int age;
		public int cloudletLife;
		public float angle;
		public boolean isDead = false;
		float rangeMod = 1.0F;
		public float colorMod = 1.0F;
		public Vec3d color;
		public Vec3d prevColor;
		public TorexType type;

		public Cloudlet(double posX, double posY, double posZ, float angle, int age, int maxAge) {
			this(posX, posY, posZ, angle, age, maxAge, TorexType.STANDARD);
		}

		public Cloudlet(double posX, double posY, double posZ, float angle, int age, int maxAge, TorexType type) {
			this.posX = posX;
			this.posY = posY;
			this.posZ = posZ;
			this.age = age;
			this.cloudletLife = maxAge;
			this.angle = angle;
			this.rangeMod = 0.3F + rand.nextFloat() * 0.7F;
			this.colorMod = 0.8F + rand.nextFloat() * 0.2F;
			this.type = type;

			this.updateColor();
		}

		private void update() {

			age++;

			if(age > cloudletLife) {
				this.isDead = true;
			}

			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;

			Vec3d simPos = new Vec3d(EntityNukeTorex.this.posX - this.posX, 0, EntityNukeTorex.this.posZ - this.posZ);
			double simPosX = EntityNukeTorex.this.posX + simPos.length();
			double simPosZ = EntityNukeTorex.this.posZ + 0D;

			if(this.type == TorexType.STANDARD) {
				Vec3d convection = getConvectionMotion(simPosX, simPosZ);
				Vec3d lift = getLiftMotion(simPosX, simPosZ);

				double factor = MathHelper.clamp((this.posY - EntityNukeTorex.this.posY) / EntityNukeTorex.this.coreHeight, (double) (0), (double) (1));
				this.motionX = convection.x * factor + lift.x * (1D - factor);
				this.motionY = convection.y * factor + lift.y * (1D - factor);
				this.motionZ = convection.z * factor + lift.z * (1D - factor);
			} else if(this.type == TorexType.SHOCK) {
				double factor = MathHelper.clamp((this.posY - EntityNukeTorex.this.posY) / EntityNukeTorex.this.coreHeight, (double) (0), (double) (1));
				Vec3d motion = new Vec3d(1, 0, 0);
				motion = motion.rotateYaw(this.angle);
				this.motionX = motion.x * factor;
				this.motionY = motion.y * factor;
				this.motionZ = motion.z * factor;
			} else if(this.type == TorexType.RING) {
				Vec3d motion = getRingMotion(simPosX, simPosZ);
				this.motionX = motion.x;
				this.motionY = motion.y;
				this.motionZ = motion.z;
			} else if(this.type == TorexType.CONDENSATION) {
				Vec3d motion = getCondensationMotion();
				this.motionX = motion.x;
				this.motionY = motion.y;
				this.motionZ = motion.z;
			} else if(this.type == TorexType.STARFISH) {
				double factor = MathHelper.clamp((this.posY - EntityNukeTorex.this.posY) / EntityNukeTorex.this.coreHeight, (double) (0), (double) (1));
				Vec3d motion = new Vec3d(1, 0, 0);
				motion = motion.rotateYaw(this.angle);
				this.motionX = motion.x * factor;
				this.motionY = motion.y * factor;
				this.motionZ = motion.z * factor;
			}

			double mult = this.motionMult * getSimulationSpeed();

			this.posX += this.motionX * mult;
			this.posY += this.motionY * mult;
			this.posZ += this.motionZ * mult;

			this.updateColor();
		}

		private Vec3d getCondensationMotion() {
			Vec3d delta = new Vec3d(posX - EntityNukeTorex.this.posX, 0, posZ - EntityNukeTorex.this.posZ);
			double speed = 0.00002 * EntityNukeTorex.this.ticksExisted;
			return new Vec3d(delta.x * speed, delta.y, delta.z * speed);
		}

		private Vec3d getRingMotion(double simPosX, double simPosZ) {

			if(simPosX > EntityNukeTorex.this.posX + torusWidth * 2)
				return new Vec3d(0, 0, 0);

			/* the position of the torus' outer ring center */
			Vec3d torusPos = new Vec3d(
					(EntityNukeTorex.this.posX + torusWidth),
					(EntityNukeTorex.this.posY + coreHeight * 0.5),
					EntityNukeTorex.this.posZ);

			/* the difference between the cloudlet and the torus' ring center */
			Vec3d delta = new Vec3d(torusPos.x - simPosX, torusPos.y - this.posY, torusPos.z - simPosZ);

			/* the distance this cloudlet wants to achieve to the torus' ring center */
			double roller = EntityNukeTorex.this.rollerSize * this.rangeMod * 0.25;
			/* the distance between this cloudlet and the torus' outer ring perimeter */
			double dist = delta.length() / roller - 1D;

			/* euler function based on how far the cloudlet is away from the perimeter */
			double func = 1D - Math.pow(Math.E, -dist); // [0;1]
			/* just an approximation, but it's good enough */
			float angle = (float) (func * Math.PI * 0.5D); // [0;90°]

			/* vector going from the ring center in the direction of the cloudlet, stopping at the perimeter */
			com.hbm.util.Vec3NT rot = new com.hbm.util.Vec3NT(-delta.x / dist, -delta.y / dist, -delta.z / dist);
			rot.rotateAroundZRad(angle);

			/* the direction from the cloudlet to the target position on the perimeter */
			Vec3d motion = new Vec3d(
					torusPos.x + rot.x - simPosX,
					torusPos.y + rot.y - this.posY,
					torusPos.z + rot.z - simPosZ);

			double speed = 0.001D;
			motion = motion.scale(speed).normalize();
			motion = motion.rotateYaw(this.angle);

			return motion;
		}

		/* simulated on a 2D-plane along the X/Y axis */
		private Vec3d getConvectionMotion(double simPosX, double simPosZ) {

			/* the position of the torus' outer ring center */
			Vec3d torusPos = new Vec3d(
					(EntityNukeTorex.this.posX + torusWidth),
					(EntityNukeTorex.this.posY + coreHeight),
					EntityNukeTorex.this.posZ);

			/* the difference between the cloudlet and the torus' ring center */
			Vec3d delta = new Vec3d(torusPos.x - simPosX, torusPos.y - this.posY, torusPos.z - simPosZ);

			/* the distance this cloudlet wants to achieve to the torus' ring center */
			double roller = EntityNukeTorex.this.rollerSize * this.rangeMod;
			/* the distance between this cloudlet and the torus' outer ring perimeter */
			double dist = delta.length() / roller - 1D;

			/* euler function based on how far the cloudlet is away from the perimeter */
			double func = 1D - Math.pow(Math.E, -dist); // [0;1]
			/* just an approximation, but it's good enough */
			float angle = (float) (func * Math.PI * 0.5D); // [0;90°]

			/* vector going from the ring center in the direction of the cloudlet, stopping at the perimeter */
			com.hbm.util.Vec3NT rot = new com.hbm.util.Vec3NT(-delta.x / dist, -delta.y / dist, -delta.z / dist);
			rot.rotateAroundZRad(angle);

			/* the direction from the cloudlet to the target position on the perimeter */
			Vec3d motion = new Vec3d(
					torusPos.x + rot.x - simPosX,
					torusPos.y + rot.y - this.posY,
					torusPos.z + rot.z - simPosZ);

			motion = motion.normalize();
			motion = motion.rotateYaw(this.angle);

			return motion;
		}

		private Vec3d getLiftMotion(double simPosX, double simPosZ) {
			double scale = MathHelper.clamp(1D - (simPosX - (EntityNukeTorex.this.posX + torusWidth)), (double) (0), (double) (1));

			Vec3d motion = new Vec3d(EntityNukeTorex.this.posX - this.posX, (EntityNukeTorex.this.posY + convectionHeight) - this.posY, EntityNukeTorex.this.posZ - this.posZ);
			return motion.normalize().scale(scale);
		}

		private void updateColor() {
			this.prevColor = this.color;

			double exX = EntityNukeTorex.this.posX;
			double exY = EntityNukeTorex.this.posY + EntityNukeTorex.this.coreHeight;
			double exZ = EntityNukeTorex.this.posZ;

			double distX = exX - posX;
			double distY = exY - posY;
			double distZ = exZ - posZ;

			double distSq = distX * distX + distY * distY + distZ * distZ;
			distSq /= EntityNukeTorex.this.heat;
			double dist = Math.sqrt(distSq);

			dist = Math.max(dist, 1);
			double col = 2D / dist;

			int type = EntityNukeTorex.this.dataManager.get(COLOR_TYPE);

			if(this.type == TorexType.STARFISH) {
				this.color = new Vec3d(
						Math.max(col * 8, 0.5),
						Math.max(col * 1.5, 0.1),
						Math.max(col * 0.5, 0.05)
					);
			} else if(type == 1) {
				this.color = new Vec3d(
						Math.max(col * 1, 0.25),
						Math.max(col * 2, 0.25),
						Math.max(col * 0.5, 0.25)
						);
			} else if(type == 2) {
				Color color = Color.getHSBColor(this.angle / 2F / (float) Math.PI, 1F, 1F);
				if(this.type == TorexType.RING) {
					this.color = new Vec3d(
							Math.max(col * 1, 0.25),
							Math.max(col * 1, 0.25),
							Math.max(col * 1, 0.25)
							);
				} else {
					this.color = new Vec3d(color.getRed() / 255D, color.getGreen() / 255D, color.getBlue() / 255D);
				}
			} else {
				this.color = new Vec3d(
						Math.max(col * 2, 0.25),
						Math.max(col * 1.5, 0.25),
						Math.max(col * 0.5, 0.25)
						);
			}
		}

		public Vec3d getInterpPos(float interp) {
			float scale = (float) EntityNukeTorex.this.getScale();
			Vec3d base = new Vec3d(
					prevPosX + (posX - prevPosX) * interp,
					prevPosY + (posY - prevPosY) * interp,
					prevPosZ + (posZ - prevPosZ) * interp);

			if(this.type != TorexType.SHOCK) { //no rescale for the shockwave as this messes with the positions
				base = new Vec3d(
					((base.x) - EntityNukeTorex.this.posX) * scale + EntityNukeTorex.this.posX,
					((base.y) - EntityNukeTorex.this.posY) * scale + EntityNukeTorex.this.posY,
					((base.z) - EntityNukeTorex.this.posZ) * scale + EntityNukeTorex.this.posZ);
			}

			return base;
		}

		public Vec3d getInterpColor(float interp) {

			if(this.type == TorexType.CONDENSATION) {
				return new Vec3d(1F, 1F, 1F);
			}

			double greying = EntityNukeTorex.this.getGreying();

			if(this.type == TorexType.RING) {
				greying += 1;
			}

			return new Vec3d(
					(prevColor.x + (color.x - prevColor.x) * interp) * greying,
					(prevColor.y + (color.y - prevColor.y) * interp) * greying,
					(prevColor.z + (color.z - prevColor.z) * interp) * greying);
		}

		public float getAlpha() {
			float alpha = (1F - ((float)age / (float)cloudletLife)) * EntityNukeTorex.this.getAlpha();
			if(this.type == TorexType.CONDENSATION) alpha *= 0.25;
			return alpha;
		}

		private float startingScale = 1;
		private float growingScale = 5F;

		public float getScale() {
			float base = startingScale + ((float)age / (float)cloudletLife) * growingScale;
			if(this.type != TorexType.SHOCK) base *= (float) EntityNukeTorex.this.getScale();
			return base;
		}

		public Cloudlet setScale(float start, float grow) {
			this.startingScale = start;
			this.growingScale = grow;
			return this;
		}

		private double motionMult = 1F;

		public Cloudlet setMotion(double mult) {
			this.motionMult = mult;
			return this;
		}
	}

	public static enum TorexType {
		STANDARD,
		SHOCK,
		RING,
		CONDENSATION,
		STARFISH,
	}

	@Override protected void writeEntityToNBT(NBTTagCompound nbt) { }
	@Override public boolean writeToNBTOptional(NBTTagCompound nbt) { return false; }
	@Override public void readEntityFromNBT(NBTTagCompound nbt) { this.setDead(); }

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}

	public static void statFacStandard(World world, double x, double y, double z, float scale) { statFac(world, x, y, z, scale, 0); }
	public static void statFacBale(World world, double x, double y, double z, float scale) { statFac(world, x, y, z, scale, 1); }

	public static void statFac(World world, double x, double y, double z, float scale, int type) {
		EntityNukeTorex torex = new EntityNukeTorex(world).setScale(MathHelper.clamp((float) BobMathUtil.squirt(scale * 0.01) * 1.5F, 0.5F, 5F));
		torex.setType(type);
		torex.setPosition(x, y, z);
		torex.forceSpawn = true;
		world.spawnEntityInWorld(torex);
		TrackerUtil.setTrackingRange(world, torex, 1000);
	}
	public static void startFacAnti(World world, double x, double y, double z, float scale) {
		EntityNukeTorex torex = new EntityNukeTorex(world).setScale(MathHelper.clamp(scale * 0.01F, 0.5F, 5F)).setType(2);
		torex.setPosition(x, y, z);
		world.spawnEntityInWorld(torex);
		TrackerUtil.setTrackingRange(world, torex, 1000);
	}
}
