package com.hbm.entity.logic;

import com.hbm.config.GeneralConfig;
import com.hbm.entity.projectile.EntityBombletZeta;
import com.hbm.entity.projectile.EntityBoxcar;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.interfaces.NotableComments;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@NotableComments
public class EntityBomber extends EntityPlaneBase {
	private static final DataParameter<Byte> BOMBER_STYLE = EntityDataManager.createKey(EntityBomber.class, DataSerializers.BYTE);


	/* This was probably the dumbest fucking way that I could have handled this. Not gonna change it now, be glad I made a superclass at all. */
	int bombStart = 75;
	int bombStop = 125;
	int bombRate = 3;
	int type = 0;

	protected AudioWrapper audio;

	public EntityBomber(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.setSize(8.0F, 4.0F);
	}

	@Override
	public void entityInit() {
		super.entityInit();
		this.dataManager.register(BOMBER_STYLE, Byte.valueOf((byte) 0));
	}

	public byte getBomberStyle() {
		return this.dataManager.get(BOMBER_STYLE);
	}

	/** This sucks balls. Too bad! */
	@Override
	public void onUpdate() {
		super.onUpdate();

		if(this.world.isRemote) {
			if(this.dataManager.get(PLANE_HEALTH) > 0) {
				if(audio == null || !audio.isPlaying()) {
					int bomberType = this.dataManager.get(BOMBER_STYLE);
					System.out.println();
					if (bomberType >= 9) {
					    audio = MainRegistry.proxy.getLoopedSound("hbm:entity.jetloopbomb", (float) posX, (float) posY, (float) posZ, 2F, 250F, 1F, 20);
					} else {
					    audio = MainRegistry.proxy.getLoopedSound(bomberType <= 4 ? "hbm:entity.bomberSmallLoop" : "hbm:entity.bomberLoop", (float) posX, (float) posY, (float) posZ, 2F, 250F, 1F, 20);
					}
				audio.startSound();
				}
				audio.keepAlive();
				audio.updatePosition((float) posX, (float) posY, (float) posZ);
			} else {
				if(audio != null && audio.isPlaying()) {
					audio.stopSound();
					audio = null;
				}
			}
		}

		if(!this.world.isRemote && this.health > 0 && this.ticksExisted > bombStart && this.ticksExisted < bombStop && this.ticksExisted % bombRate == 0) {

			if(type == 3) {
				com.hbm.sound.VanillaWorldSoundEffects.play(this.world, (double) (posX + 0.5F), (double) (posY + 0.5F), (double) (posZ + 0.5F), "random.fizz", 5.0F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F);
				ExplosionChaos.spawnPoisonCloud(this.world, this.posX, this.posY - 1F, this.posZ, 10, 0.5, 3);

			} else if(type == 5) {

			} else if(type == 6) {
				com.hbm.sound.WorldSoundEffects.play(this.world, (double) (posX + 0.5F), (double) (posY + 0.5F), (double) (posZ + 0.5F), "hbm:weapon.missileTakeOff", 10.0F, 0.9F + rand.nextFloat() * 0.2F);
				EntityBoxcar rocket = new EntityBoxcar(this.world);
				rocket.posX = posX + rand.nextDouble() - 0.5;
				rocket.posY = posY - rand.nextDouble();
				rocket.posZ = posZ + rand.nextDouble() - 0.5;
				this.world.spawnEntity(rocket);

			} else if(type == 7) {
				com.hbm.sound.VanillaWorldSoundEffects.play(this.world, (double) (posX + 0.5F), (double) (posY + 0.5F), (double) (posZ + 0.5F), "random.fizz", 5.0F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F);
				ExplosionChaos.spawnPoisonCloud(this.world, this.posX, this.world.getHeight((int) this.posX, (int) this.posZ) + 2, this.posZ, 10, 1, 2);

			} else {
				com.hbm.sound.WorldSoundEffects.play(this.world, (double) (posX + 0.5F), (double) (posY + 0.5F), (double) (posZ + 0.5F), "hbm:entity.bombWhistle", 10.0F, 0.9F + rand.nextFloat() * 0.2F);
				EntityBombletZeta zeta = new EntityBombletZeta(this.world);
				zeta.rotation();
				zeta.type = type;
				zeta.posX = posX + rand.nextDouble() - 0.5;
				zeta.posY = posY - rand.nextDouble();
				zeta.posZ = posZ + rand.nextDouble() - 0.5;
				if(type == 0 || type == 9 ) {
					zeta.motionX = motionX + rand.nextGaussian() * 0.15; zeta.motionZ = motionZ + rand.nextGaussian() * 0.15;
				} else {
					zeta.motionX = motionX; zeta.motionZ = motionZ;
				}
				this.world.spawnEntity(zeta);
			}
		}
	}

	public void fac(World world, double x, double y, double z) {

		Vec3d vector = new Vec3d(world.rand.nextDouble() - 0.5, 0, world.rand.nextDouble() - 0.5);
		vector = vector.normalize();
		double scale = GeneralConfig.enableBomberShortMode ? 1 : 2;
		vector = new Vec3d(vector.x * scale, vector.y, vector.z * scale);

		this.setLocationAndAngles(x - vector.x * 100, y + 50, z - vector.z * 100, 0.0F, 0.0F);
		this.loadNeighboringChunks((int) (x / 16), (int) (z / 16));

		this.motionX = vector.x;
		this.motionZ = vector.z;
		this.motionY = 0.0D;

		if(type == 9) {
			this.motionX = vector.x * 1.5;
			this.motionZ = vector.z * 1.5;
		}

		this.rotation();

		int i = 1;

		int rand = world.rand.nextInt(7);

		switch(rand) {
		case 0: case 1: i = 1; break;
		case 2: case 3: i = 2; break;
		case 4: i = 5; break;
		case 5: i = 6; break;
		case 6: i = 7; break;
		}

		if(world.rand.nextInt(100) == 0) {
			rand = world.rand.nextInt(4);
			switch(rand) {
			case 0: i = 0; break;
			case 1: i = 3; break;
			case 2: i = 4; break;
			case 3: i = 8; break;
			}
		}

		this.dataManager.set(BOMBER_STYLE, (byte) i);
		this.setSize(8.0F, 4.0F);
	}

	public static EntityBomber statFacCarpet(World world, double x, double y, double z) {
		EntityBomber bomber = new EntityBomber(world);
		bomber.timer = 200;
		bomber.bombStart = 50;
		bomber.bombStop = 100;
		bomber.bombRate = 2;
		bomber.fac(world, x, y, z);
		bomber.type = 0;
		return bomber;
	}

	public static EntityBomber statFacNapalm(World world, double x, double y, double z) {
		EntityBomber bomber = new EntityBomber(world);
		bomber.timer = 200;
		bomber.bombStart = 50;
		bomber.bombStop = 100;
		bomber.bombRate = 5;
		bomber.fac(world, x, y, z);
		bomber.type = 1;
		return bomber;
	}

	public static EntityBomber statFacChlorine(World world, double x, double y, double z) {
		EntityBomber bomber = new EntityBomber(world);
		bomber.timer = 200;
		bomber.bombStart = 50;
		bomber.bombStop = 100;
		bomber.bombRate = 4;
		bomber.fac(world, x, y, z);
		bomber.type = 2;
		return bomber;
	}

	public static EntityBomber statFacOrange(World world, double x, double y, double z) {
		EntityBomber bomber = new EntityBomber(world);
		bomber.timer = 200;
		bomber.bombStart = 75;
		bomber.bombStop = 125;
		bomber.bombRate = 1;
		bomber.fac(world, x, y, z);
		bomber.type = 3;
		return bomber;
	}

	public static EntityBomber statFacABomb(World world, double x, double y, double z) {
		EntityBomber bomber = new EntityBomber(world);
		bomber.timer = 200;
		bomber.bombStart = 60;
		bomber.bombStop = 70;
		bomber.bombRate = 65;
		bomber.fac(world, x, y, z);
		int i = 1;

		int rand = world.rand.nextInt(3);

		switch(rand) {
		case 0: i = 5; break;
		case 1: i = 6; break;
		case 2: i = 7; break;
		}
		if(world.rand.nextInt(100) == 0) i = 8;

		bomber.dataManager.set(BOMBER_STYLE, (byte) i);
		bomber.type = 4;
		return bomber;
	}

	public static EntityBomber statFacStinger(World world, double x, double y, double z) {
		EntityBomber bomber = new EntityBomber(world);
		bomber.timer = 200;
		bomber.bombStart = 50;
		bomber.bombStop = 150;
		bomber.bombRate = 10;
		bomber.fac(world, x, y, z);
		bomber.dataManager.set(BOMBER_STYLE, (byte) 4);
		bomber.type = 5;
		return bomber;
	}

	public static EntityBomber statFacBoxcar(World world, double x, double y, double z) {
		EntityBomber bomber = new EntityBomber(world);
		bomber.timer = 200;
		bomber.bombStart = 50;
		bomber.bombStop = 150;
		bomber.bombRate = 10;
		bomber.fac(world, x, y, z);
		bomber.dataManager.set(BOMBER_STYLE, (byte) 6);
		bomber.type = 6;
		return bomber;
	}

	public static EntityBomber statFacPC(World world, double x, double y, double z) {
		EntityBomber bomber = new EntityBomber(world);
		bomber.timer = 200;
		bomber.bombStart = 75;
		bomber.bombStop = 125;
		bomber.bombRate = 1;
		bomber.fac(world, x, y, z);
		bomber.dataManager.set(BOMBER_STYLE, (byte) 6);
		bomber.type = 7;
		return bomber;
	}

    public static EntityBomber statFacCV(World world, double x, double y, double z) {
    	EntityBomber bomber = new EntityBomber(world);

    	bomber.timer = 200;
    	bomber.bombStart = 75;
    	bomber.bombStop = 125;
    	bomber.bombRate = 90000;

    	bomber.fac(world, x, y+16, z); //adding the y value breaks things, sorry bluehat

    	bomber.dataManager.set(BOMBER_STYLE, (byte)9);

    	bomber.type = 8;

    	return bomber;
    }

	public static EntityBomber statFacCarpetJet(World world, double x, double y, double z) {
		EntityBomber bomber = new EntityBomber(world);

		bomber.timer = 150;
		bomber.bombStart = 10;
		bomber.bombStop = 70;
		bomber.bombRate = 3;
		bomber.type = 9;

		bomber.fac(world, x, y, z);
		//bomber.addVelocity(0.6, 0, 0.6);

    	bomber.dataManager.set(BOMBER_STYLE, (byte)10);
		return bomber;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		bombStart = nbt.getInteger("bombStart");
		bombStop = nbt.getInteger("bombStop");
		bombRate = nbt.getInteger("bombRate");
		type = nbt.getInteger("type");
		this.dataManager.set(BOMBER_STYLE, nbt.getByte("style"));
		this.setSize(8.0F, 4.0F);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("bombStart", bombStart);
		nbt.setInteger("bombStop", bombStop);
		nbt.setInteger("bombRate", bombRate);
		nbt.setInteger("type", type);
		nbt.setByte("style", this.dataManager.get(BOMBER_STYLE));
	}
}
