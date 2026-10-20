package com.hbm.particle;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleNT extends Particle {
	public void setMotion(double x, double y, double z) { this.motionX = x; this.motionY = y; this.motionZ = z; }

	
	public ParticleNT(World world, double x, double y, double z, ParticleDefinition definition) {
		super(world, x, y, z);
	}

	public ParticleNT(World world, double x, double y, double z, double moX, double moY, double moZ, ParticleDefinition definition) {
		this(world, x, y, z, definition);
		this.setMotion(moX, moY, moZ);
	}

}
