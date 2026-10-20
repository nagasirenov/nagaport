package com.hbm.tileentity.machine.storage;

import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityMachineBAT9000 extends TileEntityBarrel {

	public TileEntityMachineBAT9000() {
		super(2048000);
	}
	
	@Override
	public String getDefaultName() {
		return "container.bat9000";
	}
	
	@Override
	public void checkFluidInteraction() {
		
		if(tank.getTankType().isAntimatter()) {
			com.hbm.world.WorldBlockDestruction.destroy(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), false);
			this.world.newExplosion(null, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, 10, true, true);
		}
	}
	
	@Override
	protected DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ() + 3, Library.POS_Z),
				new DirPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() + 3, Library.POS_Z),
				new DirPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ() - 3, Library.NEG_Z),
				new DirPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() - 3, Library.NEG_Z),
				new DirPos(this.pos.getX() + 3, this.pos.getY(), this.pos.getZ() + 1, Library.POS_X),
				new DirPos(this.pos.getX() - 3, this.pos.getY(), this.pos.getZ() + 1, Library.NEG_X),
				new DirPos(this.pos.getX() + 3, this.pos.getY(), this.pos.getZ() - 1, Library.POS_X),
				new DirPos(this.pos.getX() - 3, this.pos.getY(), this.pos.getZ() - 1, Library.NEG_X)
		};
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
}
