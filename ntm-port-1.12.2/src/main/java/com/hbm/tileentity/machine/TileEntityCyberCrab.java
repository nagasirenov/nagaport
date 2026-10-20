package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.entity.mob.EntityCyberCrab;
import com.hbm.entity.mob.EntityTeslaCrab;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityCyberCrab extends TileEntity implements net.minecraft.util.ITickable {
	
	int age = 0;

	@Override
	public void update() {
		
		if (!this.world.isRemote) {
			
			age++;
			if(age > 200 && com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY() + 1, this.pos.getZ()).getBlock() == Blocks.AIR && this.world.getClosestPlayer(this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, 25) != null) {
				List<Entity> entities = this.world.getEntitiesWithinAABB(EntityCyberCrab.class,
						AxisAlignedBB.getBoundingBox(this.pos.getX() - 5, this.pos.getY() - 2, this.pos.getZ() - 5, this.pos.getX() + 6,
								this.pos.getY() + 4, this.pos.getZ() + 6));
				
				if(entities.size() < 5) {
					
					EntityCyberCrab crab;
					
					if(this.world.rand.nextInt(5) == 0)
						crab = new EntityTeslaCrab(this.world);
					else
						crab = new EntityCyberCrab(this.world);
					
					crab.setPosition(this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5);
					this.world.spawnEntityInWorld(crab);
				}
				
				age = 0;
			}
		}
	}

}
