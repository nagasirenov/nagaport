package com.hbm.tileentity.deco;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.particle.EntityChlorineFX;
import com.hbm.entity.particle.EntityCloudFX;
import com.hbm.entity.particle.EntityPinkCloudFX;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

public class TileEntityVent extends TileEntity implements net.minecraft.util.ITickable {
	
	Random rand = new Random();

	public void update() {
		
		if(!this.world.isRemote && this.world.isBlockIndirectlyGettingPowered(this.pos.getX(), this.pos.getY(), this.pos.getZ())) {
			Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()).getBlock();

			if(b == ModBlocks.vent_chlorine) {
				//if(rand.nextInt(1) == 0) {
					double x = rand.nextGaussian() * 1.5;
					double y = rand.nextGaussian() * 1.5;
					double z = rand.nextGaussian() * 1.5;
					
					if(!com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + (int)x, this.pos.getY() + (int)y, this.pos.getZ() + (int)z).getBlock().isNormalCube()) {
						this.world.spawnEntityInWorld(new EntityChlorineFX(this.world, this.pos.getX() + (int)x, this.pos.getY() + (int)y, this.pos.getZ() + (int)z, x/2, y/2, z/2));
					}
				//}
			}
			if(b == ModBlocks.vent_cloud) {
				//if(rand.nextInt(50) == 0) {
				double x = rand.nextGaussian() * 1.75;
				double y = rand.nextGaussian() * 1.75;
				double z = rand.nextGaussian() * 1.75;
				
				if(!com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + (int)x, this.pos.getY() + (int)y, this.pos.getZ() + (int)z).getBlock().isNormalCube()) {
					this.world.spawnEntityInWorld(new EntityCloudFX(this.world, this.pos.getX() + (int)x, this.pos.getY() + (int)y, this.pos.getZ() + (int)z, x/2, y/2, z/2));
					}
				//}
			}
			if(b == ModBlocks.vent_pink_cloud) {
				//if(rand.nextInt(65) == 0) {
				double x = rand.nextGaussian() * 2;
				double y = rand.nextGaussian() * 2;
				double z = rand.nextGaussian() * 2;
				
				if(!com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + (int)x, this.pos.getY() + (int)y, this.pos.getZ() + (int)z).getBlock().isNormalCube()) {
					this.world.spawnEntityInWorld(new EntityPinkCloudFX(this.world, this.pos.getX() + (int)x, this.pos.getY() + (int)y, this.pos.getZ() + (int)z, x/2, y/2, z/2));
					}
				//}
			}
		}
		
		/*if(worldObj.isRemote) {

			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "exhaust");
			data.setString("mode", "soyuz");
			data.setInteger("count", 1);
			data.setDouble("width", 0);
			data.setDouble("posX", xCoord + 0.5);
			data.setDouble("posY", yCoord - 1);
			data.setDouble("posZ", zCoord + 0.5);
			
			MainRegistry.proxy.effectNT(data);
		}*/
	}
}
