package com.hbm.entity.item;

import api.hbm.block.IFuckingExplode;
import com.hbm.util.PersistentRegistryRef;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityTNTPrimedBase extends Entity {

	private static final DataParameter<Integer> BLOCK_ID = EntityDataManager.createKey(EntityTNTPrimedBase.class, DataSerializers.VARINT);


	public boolean detonateOnCollision;
	public int fuse;
	private EntityLivingBase tntPlacedBy;
	private boolean doesSpin; // SSSSSSSPEEEEEEEEEEEEEEEEEEEEEEEN
	private float momentumPitch;
	private float momentumYaw;
	private int legacyTile = -1;

	public EntityTNTPrimedBase(World world) {
		super(world);
		this.preventEntitySpawning = true;
		this.setSize(0.98F, 0.98F);
		this.fuse = 80;
		this.detonateOnCollision = false;

		doesSpin = true;
		momentumPitch = (float) (world.rand.nextGaussian() * 5F);
		momentumYaw =(float) (world.rand.nextGaussian() * 10F);
	}

	public EntityTNTPrimedBase(World world, double x, double y, double z, EntityLivingBase entity, Block bomb) {
		this(world);
		this.setPosition(x, y, z);
		float f = (float) (Math.random() * Math.PI * 2.0D);
		this.motionX = (double) (-((float) Math.sin((double) f)) * 0.02F);
		this.motionY = 0.2D;
		this.motionZ = (double) (-((float) Math.cos((double) f)) * 0.02F);
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
		this.tntPlacedBy = entity;
		this.dataManager.set(BLOCK_ID, Block.getIdFromBlock(bomb));
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(BLOCK_ID, 0);
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return !this.isDead;
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= 0.04D;
		this.move(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.98D;
		this.motionY *= 0.98D;
		this.motionZ *= 0.98D;

		if(doesSpin) {
			rotationPitch += momentumPitch;
			rotationYaw += momentumYaw;
		}

		if(this.onGround) {
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
			this.motionY *= -0.5D;
		}

		if(this.fuse-- <= 0 || (this.detonateOnCollision && this.collided)) {
			this.setDead();

			if(!this.world.isRemote) {
				this.explode();
			}
		} else {
			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
		}
	}

	private void explode() {
		IFuckingExplode bomb = this.getBomb();
		if(bomb != null) bomb.explodeEntity(this.world, posX, posY, posZ, this);
	}

	public IFuckingExplode getBomb() {
		Block block = getBlock();
		return block instanceof IFuckingExplode ? (IFuckingExplode) block : null;
	}

	public Block getBlock() {
		return Block.getBlockById(this.dataManager.get(BLOCK_ID));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setByte("Fuse", (byte) this.fuse);
		if(this.legacyTile >= 0) {
			nbt.setInteger("Tile", this.legacyTile);
		} else {
			PersistentRegistryRef.writeBlock(nbt, "Tile", this.getBlock());
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		this.fuse = nbt.getByte("Fuse");
		Block block = PersistentRegistryRef.readBlock(nbt, "Tile");
		if(block != null) {
			this.dataManager.set(BLOCK_ID, Block.getIdFromBlock(block));
		} else {
			this.legacyTile = nbt.getInteger("Tile");
			this.dataManager.set(BLOCK_ID, 0);
		}
	}

	@SideOnly(Side.CLIENT)
	public float getShadowSize() {
		return 0.0F;
	}

	public EntityLivingBase getTntPlacedBy() {
		return this.tntPlacedBy;
	}
}
