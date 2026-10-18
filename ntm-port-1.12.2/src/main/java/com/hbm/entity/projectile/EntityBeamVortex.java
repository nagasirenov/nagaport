package com.hbm.entity.projectile;

import java.util.List;

import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityBeamVortex extends EntityBeamBase {

	public EntityBeamVortex(World world) {
		super(world);
	}

	public EntityBeamVortex(World world, EntityPlayer player) {
		super(world, player);
	}
	
	@Override
	public void onUpdate() {
		
		if(this.ticksExisted > 10)
			this.setDead();
		
		if(this.ticksExisted > 1)
			return;
		
		int range = 100;
		
		EntityPlayer player = worldObj.getPlayerEntityByName(this.dataWatcher.getWatchableObjectString(20));
		
		if(player != null) {
			
			RayTraceResult pos = Library.rayTrace(player, range, 1, false, true, false);
			
			if(pos == null)
				return;
			
			worldObj.spawnParticle("cloud", pos.hitVec.x, pos.hitVec.y, pos.hitVec.z, 0, 0, 0);
			worldObj.playSound(pos.hitVec.x, pos.hitVec.y, pos.hitVec.z, "random.fizz", 1, 1, true);
			
			List<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(pos.hitVec.x - 1, pos.hitVec.y - 1, pos.hitVec.z - 1, pos.hitVec.x + 1, pos.hitVec.y + 1, pos.hitVec.z + 1));
			
			for(Entity e : list)
				e.attackEntityFrom(ModDamageSource.radiation, 5);
		}
	}

}
