package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityMachineMiningLaser;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RenderLaserMiner extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float interpolation) {

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y - 1, z + 0.5);
		
		TileEntityMachineMiningLaser laser = (TileEntityMachineMiningLaser)te;

		double tx = (laser.targetX - laser.lastTargetX) * interpolation + laser.lastTargetX;
		double ty = (laser.targetY - laser.lastTargetY) * interpolation + laser.lastTargetY;
		double tz = (laser.targetZ - laser.lastTargetZ) * interpolation + laser.lastTargetZ;
		double vx = tx - laser.getPos().getX();
		double vy = ty - laser.getPos().getY() + 3;
		double vz = tz - laser.getPos().getZ();
		
		Vec3d nVec = new Vec3d(vx, vy, vz);
		nVec = nVec.normalize();
		
		double d = 1.5D;
		nVec = nVec.scale(d);
		
		Vec3d vec = new Vec3d(vx - nVec.x, vy - nVec.y, vz - nVec.z);

		double length = vec.length();
		double yaw = Math.toDegrees(Math.atan2(vec.x, vec.z));
		double sqrt = MathHelper.sqrt(vec.x * vec.x + vec.z * vec.z);
		double pitch = Math.toDegrees(Math.atan2(vec.y, sqrt));
		//turns out using tan(vec.y, length) was inaccurate,
		//the emitter wouldn't match the laser perfectly when pointing down

		bindTexture(ResourceManager.mining_laser_base_tex);
		ResourceManager.mining_laser.renderPart("Base");

		//GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glPushMatrix();
		GL11.glRotated(yaw, 0, 1, 0);
		bindTexture(ResourceManager.mining_laser_pivot_tex);
		ResourceManager.mining_laser.renderPart("Pivot");
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		GL11.glRotated(yaw, 0, 1, 0);
		GL11.glTranslated(0, -1, 0);
		GL11.glRotated(pitch + 90, -1, 0, 0);
		GL11.glTranslated(0, 1, 0);
		bindTexture(ResourceManager.mining_laser_laser_tex);
		ResourceManager.mining_laser.renderPart("Laser");
		GL11.glPopMatrix();
		//GL11.glShadeModel(GL11.GL_FLAT);
		
		if(laser.beam) {
			length = vec.length();
			GL11.glTranslated(nVec.x, nVec.y - 1, nVec.z);
			int range = (int)Math.ceil(length * 0.5);
	        BeamPronter.prontBeam(vec, EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xa00000, 0xa00000, (int)te.getWorld().getTotalWorldTime() * -25 % 360, range * 2, 0.075F, 3, 0.025F, 0.5F);
	        BeamPronter.prontBeam(vec, EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xa00000, 0xa00000, (int)te.getWorld().getTotalWorldTime() * -25 % 360 + 120, range * 2, 0.075F, 3, 0.025F, range);
	        BeamPronter.prontBeam(vec, EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xa00000, 0xa00000, (int)te.getWorld().getTotalWorldTime() * -25 % 360 + 240, range * 2, 0.075F, 3, 0.025F, 0.5F);
		}
        
		GL11.glPopMatrix();
	}

}
