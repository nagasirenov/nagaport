package com.hbm.entity.logic;

import org.apache.logging.log4j.Level;

import com.hbm.config.GeneralConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Impact;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionTom;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.TomSaveData;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityTomBlast extends EntityExplosionChunkloading {

	public int age = 0;
	public int destructionRange = 0;
	public ExplosionTom exp;
	public int speed = 1;
	public boolean did = false;

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		age = nbt.getInteger("age");
		destructionRange = nbt.getInteger("destructionRange");
		speed = nbt.getInteger("speed");
		did = nbt.getBoolean("did");

		exp = new ExplosionTom((int) this.posX, (int) this.posY, (int) this.posZ, this.world, this.destructionRange);
		exp.readFromNbt(nbt, "exp_");

		this.did = true;

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("age", age);
		nbt.setInteger("destructionRange", destructionRange);
		nbt.setInteger("speed", speed);
		nbt.setBoolean("did", did);

		if(exp != null)
			exp.saveToNbt(nbt, "exp_");

	}

	public EntityTomBlast(World p_i1582_1_) {
		super(p_i1582_1_);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!this.world.isRemote) loadChunk((int) Math.floor(posX / 16D), (int) Math.floor(posZ / 16D));

		if(!this.did) {

			if(GeneralConfig.enableExtendedLogging && !this.world.isRemote)
				MainRegistry.logger.log(Level.INFO, "[NUKE] Initialized TOM explosion at " + posX + " / " + posY + " / " + posZ + " with strength " + destructionRange + "!");

			exp = new ExplosionTom((int) this.posX, (int) this.posY, (int) this.posZ, this.world, this.destructionRange);

			if(!this.world.isRemote) {
				CelestialBody body = CelestialBody.getBody(this.world);
				body.modifyTraits(new CBT_Impact(this.world.getTotalWorldTime()));
			}

			this.did = true;
		}

		speed += 1; // increase speed to keep up with expansion

		boolean flag = false;
		for(int i = 0; i < this.speed; i++) {
			flag = exp.update();

			if(flag) {
				this.setDead();
				TomSaveData data = TomSaveData.forWorld(this.world);
				data.impact = true;
				data.fire = 1F;
				data.markDirty();
			}
		}

		if(rand.nextInt(5) == 0)
			com.hbm.sound.VanillaWorldSoundEffects.play(this.world, this.posX, this.posY, this.posZ, "random.explode", 10000.0F, 0.8F + this.rand.nextFloat() * 0.2F);

		if(!flag) {
			com.hbm.sound.VanillaWorldSoundEffects.play(this.world, this.posX, this.posY, this.posZ, "ambient.weather.thunder", 10000.0F, 0.8F + this.rand.nextFloat() * 0.2F);
			ExplosionNukeGeneric.dealDamage(this.world, this.posX, this.posY, this.posZ, this.destructionRange * 2);
		}

		age++;
	}
}
