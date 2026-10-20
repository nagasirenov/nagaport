package com.hbm.tileentity.machine;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.lib.Library;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityMachineRadarLarge extends TileEntityMachineRadarNT {
	
	public static int radarLargeRange = 3_000;

	@Override
	public String getConfigName() {
		return "radar_large";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		radarLargeRange = IConfigurableMachine.grab(obj, "I:radarLargeRange", radarLargeRange);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:radarLargeRange").value(radarLargeRange);
	}
	
	@Override
	public int getRange() {
		return radarLargeRange;
	}

	@Override
	public DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ(), Library.POS_X),
				new DirPos(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ(), Library.NEG_X),
				new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() + 2, Library.POS_Z),
				new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() - 2, Library.NEG_Z),
		};
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 5,
					this.pos.getY(),
					this.pos.getZ() - 5,
					this.pos.getX() + 6,
					this.pos.getY() + 10,
					this.pos.getZ() + 6
					);
		}
		
		return bb;
	}
}
