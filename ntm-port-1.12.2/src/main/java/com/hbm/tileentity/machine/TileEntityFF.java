package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class TileEntityFF extends TileEntity implements net.minecraft.util.ITickable {

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	List<Entity> outside = new ArrayList();
	List<Entity> inside = new ArrayList();
	
	@Override
	public void update() {
		
		//if(!worldObj.isRemote)
			prototype(10F);
	}
	
	public void prototype(float rad) {

		List<Entity> oLegacy = new ArrayList(outside);
		List<Entity> iLegacy = new ArrayList(inside);

		outside.clear();
		inside.clear();
		
		List<Object> list = this.world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(this.pos.getX() + 0.5 - (rad + 25), this.pos.getY() + 0.5 - (rad + 25), this.pos.getZ() + 0.5 - (rad + 25), this.pos.getX() + 0.5 + (rad + 25), this.pos.getY() + 0.5 + (rad + 25), this.pos.getZ() + 0.5 + (rad + 25)));
		
		for(Object o : list) {
			
			if(o instanceof Entity && !(o instanceof EntityPlayer)) {
				Entity entity = (Entity)o;
				
				double dist = Math.sqrt(Math.pow(this.pos.getX() + 0.5 - entity.posX, 2) + Math.pow(this.pos.getY() + 0.5 - entity.posY, 2) + Math.pow(this.pos.getZ() + 0.5 - entity.posZ, 2));
				
				boolean out = dist > rad;
				
				//if the entity has not been registered yet
				if(!oLegacy.contains(entity) && !iLegacy.contains(entity)) {
					if(out) {
						outside.add(entity);
					} else {
						inside.add(entity);
					}
					
				//if the entity has been detected before
				} else {
					
					//if the entity has crossed inwards
					if(oLegacy.contains(entity) && !out) {
						Vec3d vec = new Vec3d(this.pos.getX() + 0.5 - entity.posX, this.pos.getY() + 0.5 - entity.posY, this.pos.getZ() + 0.5 - entity.posZ);
						vec = vec.normalize();
						
						double mx = -vec.x * (rad + 1);
						double my = -vec.y * (rad + 1);
						double mz = -vec.z * (rad + 1);
						
						entity.setLocationAndAngles(this.pos.getX() + 0.5 + mx, this.pos.getY() + 0.5 + my, this.pos.getZ() + 0.5 + mz, 0, 0);
						
						double mo = Math.sqrt(Math.pow(entity.motionX, 2) + Math.pow(entity.motionY, 2) + Math.pow(entity.motionZ, 2));

						entity.motionX = vec.x * -mo;
						entity.motionY = vec.y * -mo;
						entity.motionZ = vec.z * -mo;

						entity.posX -= entity.motionX;
						entity.posY -= entity.motionY;
						entity.posZ -= entity.motionZ;

			    		this.world.playSoundAtEntity(entity, "hbm:weapon.sparkShoot", 2.5F, 1.0F);
						outside.add(entity);
					} else
					
					//if the entity has crossed outwards
					if(iLegacy.contains(entity) && out) {
						Vec3d vec = new Vec3d(this.pos.getX() + 0.5 - entity.posX, this.pos.getY() + 0.5 - entity.posY, this.pos.getZ() + 0.5 - entity.posZ);
						vec = vec.normalize();
						
						double mx = -vec.x * (rad - 1);
						double my = -vec.y * (rad - 1);
						double mz = -vec.z * (rad - 1);

						entity.setLocationAndAngles(this.pos.getX() + 0.5 + mx, this.pos.getY() + 0.5 + my, this.pos.getZ() + 0.5 + mz, 0, 0);
						
						double mo = Math.sqrt(Math.pow(entity.motionX, 2) + Math.pow(entity.motionY, 2) + Math.pow(entity.motionZ, 2));

						entity.motionX = vec.x * mo;
						entity.motionY = vec.y * mo;
						entity.motionZ = vec.z * mo;

						entity.posX -= entity.motionX;
						entity.posY -= entity.motionY;
						entity.posZ -= entity.motionZ;

			    		this.world.playSoundAtEntity(entity, "hbm:weapon.sparkShoot", 2.5F, 1.0F);
						inside.add(entity);
					} else {
						
						if(out) {
							outside.add(entity);
						} else {
							inside.add(entity);
						}
					}
				}
			}
		}
	}
}
