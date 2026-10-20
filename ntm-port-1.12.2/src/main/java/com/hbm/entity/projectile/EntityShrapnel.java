package com.hbm.entity.projectile;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNT.ExAttrib;
import com.hbm.lib.ModDamageSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityShrapnel extends EntityThrowable {

	private static final DataParameter<Byte> SHRAPNEL_TYPE = EntityDataManager.createKey(EntityShrapnel.class, DataSerializers.BYTE);

	public EntityShrapnel(World p_i1773_1_) {
		super(p_i1773_1_);
		this.isImmuneToFire = true;
	}

	public EntityShrapnel(World p_i1774_1_, EntityLivingBase p_i1774_2_) {
		super(p_i1774_1_, p_i1774_2_);
	}

	@Override
	public void entityInit() {
		this.dataManager.register(SHRAPNEL_TYPE, Byte.valueOf((byte) 0));
	}

	public EntityShrapnel(World p_i1775_1_, double p_i1775_2_, double p_i1775_4_, double p_i1775_6_) {
		super(p_i1775_1_, p_i1775_2_, p_i1775_4_, p_i1775_6_);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(this.world.isRemote && this.dataManager.get(SHRAPNEL_TYPE) == 1)
			this.world.spawnParticle("flame", posX, posY, posZ, 0.0, 0.0, 0.0);
	}

	@Override
	protected void onImpact(RayTraceResult mop) {
		int blockX = mop.getBlockPos().getX();
		int blockY = mop.getBlockPos().getY();
		int blockZ = mop.getBlockPos().getZ();
		if(mop.entityHit != null) {
			byte b0 = 15;

			mop.entityHit.attackEntityFrom(ModDamageSource.shrapnel, b0);
		}

		if(this.ticksExisted > 5) {
			
			if(!this.world.isRemote)
				this.setDead();

			int b = this.dataManager.get(SHRAPNEL_TYPE);
			if(b == 2 || b == 4) {
				
				if(!this.world.isRemote) {
					if(motionY < -0.2D) {
						
						if(com.hbm.world.WorldBlockState.read(this.world, blockX, blockY + 1, blockZ).getBlock().isReplaceable(this.world, blockX, blockY + 1, blockZ))
							this.world.setBlock(blockX, blockY + 1, blockZ, b == 2 ? ModBlocks.volcanic_lava_block : ModBlocks.rad_lava_block);
						
						for(int x = blockX - 1; x <= blockX + 1; x++) {
							for(int y = blockY; y <= blockY + 2; y++) {
								for(int z = blockZ - 1; z <= blockZ + 1; z++) {
									if(com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock() == Blocks.AIR)
										this.world.setBlock(x, y, z, ModBlocks.gas_monoxide);
								}
							}
						}
					}
					
					if(motionY > 0) {
						ExplosionNT explosion = new ExplosionNT(this.world, null, blockX + 0.5, blockY + 0.5, blockZ + 0.5, 7);
						explosion.addAttrib(ExAttrib.NODROP);
						explosion.addAttrib(b == 2 ? ExAttrib.LAVA_V : ExAttrib.LAVA_R);
						explosion.addAttrib(ExAttrib.NOSOUND);
						explosion.addAttrib(ExAttrib.ALLMOD);
						explosion.addAttrib(ExAttrib.NOHURT);
						explosion.explode();
					}
				}
				
			} else if(this.dataManager.get(SHRAPNEL_TYPE) == 3) {
				
				if(com.hbm.world.WorldBlockState.read(this.world, blockX, blockY + 1, blockZ).getBlock().isReplaceable(this.world, blockX, blockY + 1, blockZ)) {
					this.world.setBlock(blockX, blockY + 1, blockZ, ModBlocks.mud_block);
				}
				
			} else {
				
				for(int i = 0; i < 5; i++) this.world.spawnParticle("lava", posX, posY, posZ, 0.0, 0.0, 0.0);
			}

			com.hbm.sound.VanillaWorldSoundEffects.play(this.world, posX, posY, posZ, "random.fizz", 1.0F, 1.0F);
		}
	}

	public byte getShrapnelType() {
		return this.dataManager.get(SHRAPNEL_TYPE);
	}

	public void setTrail(boolean b) {
		this.dataManager.set(SHRAPNEL_TYPE, (byte) (b ? 1 : 0));
	}

	public void setVolcano(boolean b) {
		this.dataManager.set(SHRAPNEL_TYPE, (byte) (b ? 2 : 0));
	}

	public void setWatz(boolean b) {
		this.dataManager.set(SHRAPNEL_TYPE, (byte) (b ? 3 : 0));
	}

	public void setRadVolcano(boolean b) {
		this.dataManager.set(SHRAPNEL_TYPE, (byte) (b ? 4 : 0));
	}

	@Override
	public boolean writeToNBTOptional(NBTTagCompound nbt) {
		return false;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.setDead();
	}
}
