package com.hbm.entity.mob;

import com.hbm.entity.effect.EntityMist;
import com.hbm.inventory.fluid.Fluids;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityCreeperPhosgene extends EntityCreeper {

	public EntityCreeperPhosgene(World world) {
		super(world);
		net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.entity.monster.EntityCreeper.class, this, 20, "fuseTime", "field_82225_f"); //ehehehehehe
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if(!source.isDamageAbsolute() && !source.isUnblockable()) {
			amount -= 4F;
		}

		if(amount < 0) return false;

		return super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean getCanSpawnHere() {
		return super.getCanSpawnHere() && this.dimension == 0;
	}

	public void func_146077_cc() {

		if(!this.world.isRemote) {
			this.setDead();

			this.world.createExplosion(this, posX, posY + this.height / 2, posZ, 2F, false);
			EntityMist mist = new EntityMist(this.world);
			mist.setType(Fluids.PHOSGENE);
			mist.setPosition(posX, posY, posZ);
			mist.setArea(10, 5);
			mist.setDuration(150);
			this.world.spawnEntity(mist);
		}
	}
}
