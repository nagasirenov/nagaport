package com.hbm.entity.projectile;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.potion.HbmPotion;
import com.hbm.tileentity.machine.rbmk.RBMKDials;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityRBMKDebris extends EntityDebrisBase {

	public float rot;
	public float lastRot;
	private boolean hasSizeSet = false;

	public EntityRBMKDebris(World world) {
		super(world);
	}

	public EntityRBMKDebris(World world, double x, double y, double z, DebrisType type) {
		super(world);
		this.setPosition(x, y, z);
		this.setType(type);
	}

	@Override
	public boolean interactFirst(EntityPlayer player) {

		if(!this.world.isRemote) {

			switch(this.getType()) {
			case BLANK: if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_metal))) this.setDead(); break;
			case ELEMENT: if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_metal))) this.setDead(); break;
			case FUEL: if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_fuel))) this.setDead(); break;
			case GRAPHITE: if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_graphite))) this.setDead(); break;
			case LID: if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.rbmk_lid))) this.setDead(); break;
			case ROD: if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_metal))) this.setDead(); break;
			}

			player.inventoryContainer.detectAndSendChanges();
		}

		return false;
	}

	@Override
	public void onUpdate() {

		if(!hasSizeSet) {

			switch(this.getType()) {
			case BLANK: this.setSize(0.5F, 0.5F); break;
			case ELEMENT: this.setSize(1F, 1F); break;
			case FUEL: this.setSize(0.25F, 0.25F); break;
			case GRAPHITE: this.setSize(0.25F, 0.25F); break;
			case LID: this.setSize(1F, 0.5F); break;
			case ROD: this.setSize(0.75F, 0.5F); break;
			}

			hasSizeSet = true;
		}

		if(!this.world.isRemote) {
			if(this.getType() == DebrisType.LID && motionY > 0) {

				Vec3d pos = new Vec3d(posX, posY, posZ);
				Vec3d next = new Vec3d(posX + motionX * 2, posY + motionY * 2, posZ + motionZ * 2);
				RayTraceResult mop = this.world.rayTraceBlocks(pos, next, false, false, false);

				if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {

					int x = mop.getBlockPos().getX();
					int y = mop.getBlockPos().getY();
					int z = mop.getBlockPos().getZ();

					for(int i = -1; i <= 1; i++) {
						for(int j = -1; j <= 1; j++) {
							for(int k = -1; k <= 1; k++) {

								int rn = Math.abs(i) + Math.abs(j) + Math.abs(k);

								if(rn <= 1 || rand.nextInt(rn) == 0)
									com.hbm.world.WorldAirRemoval.remove(this.world, x + i, y + j, z + k);
							}
						}
					}

					this.setDead();
				}
			}

			if(this.getType() == DebrisType.FUEL || this.getType() == DebrisType.GRAPHITE) {
				List<EntityLivingBase> entities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(2.5, 2.5, 2.5));

				int level = this.getType() == DebrisType.FUEL ? 9 : 4;
				for(EntityLivingBase e : entities) {
					e.addPotionEffect(new PotionEffect(HbmPotion.radiation, 60 * 20, level));
				}
			}

			if(!RBMKDials.getPermaScrap(this.world) && this.ticksExisted > getLifetime() + this.getEntityId() % 50)
				this.setDead();
		}

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.motionY -= 0.04D;
		this.move(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);

		this.lastRot = this.rot;

		if(this.onGround) {
			this.motionX *= 0.85D;
			this.motionZ *= 0.85D;
			this.motionY *= -0.5D;

		} else {

			this.rot += 10F;

			if(rot >= 360F) {
				this.rot -= 360F;
				this.lastRot -= 360F;
			}
		}
	}

	@Override
	protected int getLifetime() {

		switch(this.getType()) {
		case BLANK: return 3 * 60 * 20;
		case ELEMENT: return 3 * 60 * 20;
		case FUEL: return 10 * 60 * 20;
		case GRAPHITE: return 15 * 60 * 20;
		case LID: return 30 * 20;
		case ROD: return 60 * 20;
		default: return 0;
		}
	}

	public void setType(DebrisType type) {
		this.dataManager.set(DEBRIS_TYPE, type.ordinal());
	}

	public DebrisType getType() {
		return DebrisType.values()[Math.abs(this.dataManager.get(DEBRIS_TYPE)) % DebrisType.values().length];
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		this.dataManager.set(DEBRIS_TYPE, nbt.getInteger("debtype"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("debtype", this.dataManager.get(DEBRIS_TYPE));
	}

	public static enum DebrisType {
		BLANK,		//just a metal beam
		ELEMENT,	//the entire casing of a fuel assembly because fuck you
		FUEL,		//spicy
		ROD,		//solid boron rod
		GRAPHITE,	//spicy rock
		LID;		//the all destroying harbinger of annihilation
	}
}
