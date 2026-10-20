package com.hbm.tileentity.machine.storage;

import com.hbm.blocks.BlockDummyable;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.tileentity.IOverpressurable;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineOrbus extends TileEntityBarrel implements IOverpressurable{

	public TileEntityMachineOrbus() {
		super(512000);
	}
	
	@Override
	public String getDefaultName() {
		return "container.orbus";
	}
	
	@Override
	public void checkFluidInteraction() { } //NO!

	protected DirPos[] conPos;
	
	@Override
	protected DirPos[] getConPos() {
		
		if(conPos != null)
			return conPos;
		
		conPos = new DirPos[8];
		
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

		for(int i = -1; i < 6; i += 6) {
			ForgeDirection out = i == -1 ? ForgeDirection.DOWN : ForgeDirection.UP;
			int index = i == -1 ? 0 : 4;
			conPos[index + 0] = new DirPos(this.pos.getX(),								this.pos.getY() + i,	this.pos.getZ(),								out);
			conPos[index + 1] = new DirPos(this.pos.getX() + dir.offsetX,				this.pos.getY() + i,	this.pos.getZ() + dir.offsetZ,				out);
			conPos[index + 2] = new DirPos(this.pos.getX() + rot.offsetX,				this.pos.getY() + i,	this.pos.getZ() + rot.offsetZ,				out);
			conPos[index + 3] = new DirPos(this.pos.getX() + dir.offsetX + rot.offsetX,	this.pos.getY() + i,	this.pos.getZ() + dir.offsetZ + rot.offsetZ,	out);
		}
		
		return conPos;
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 2,
					this.pos.getY(),
					this.pos.getZ() - 2,
					this.pos.getX() + 3,
					this.pos.getY() + 5,
					this.pos.getZ() + 3
					);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
	// apathy is a tragedy and boredom is a crime
	@Override
	public void explode(World world, int x, int y, int z) {
		if(tank.getTankType() == Fluids.AMAT || tank.getTankType() == Fluids.ASCHRAB) {
		//if(this.hasExploded) return;
	    	float amat = Math.min(tank.getFill()/100,90);
	    	float aschrab = Math.min(tank.getFill()/100,90);
	    	if(this.hasExploded && !this.world.isRemote) {
	    		if(amat>0)
	    		{
	    			if(amat >= 25)
	    			{
	    				EntityBalefire bf = new EntityBalefire(this.world);
	    				bf.antimatter();
	    	    		bf.setPosition(this.pos.getX(), this.pos.getY(), this.pos.getZ());
	    				bf.destructionRange = (int) amat;
	    				this.world.spawnEntityInWorld(bf);
	    				EntityNukeTorex.startFacAnti(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), amat * 1.5F);
	    				return;
	    			}
	    			else
	    			{
	    				new ExplosionVNT(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), amat).makeAmat().explode();
	    			}
	    		}
	    		if(aschrab>0)
	    		{
	    			EntityNukeExplosionMK3 ex = EntityNukeExplosionMK3.statFacFleija(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, (int) aschrab);
	    			if(!ex.isDead) {
	    				this.world.spawnEntityInWorld(ex);
	    	
	    				EntityCloudFleija cloud = new EntityCloudFleija(this.world, (int) aschrab);
	    				cloud.setPosition(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5);
	    				this.world.spawnEntityInWorld(cloud);
	    			}
	    			return;			
	    		}	
	    	}
		}
	    	this.markChanged();
	    			
	}
}
