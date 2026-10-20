package com.hbm.handler;

import java.util.HashMap;
import java.util.Random;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hbm.particle.ParticleSpentCasing;
import com.hbm.particle.SpentCasing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Config for the guns themselves on where to spawn casings and at what angle
 * @author uffr, hbm
 */
public class CasingEjector implements Cloneable {
	
	public static HashMap<Integer, CasingEjector> mappings = new HashMap<Integer, CasingEjector>();
	public static final Random rand = new Random();

	private int id;
	private static int nextId = 0;
	private Vec3d posOffset = new Vec3d(0, 0, 0);
	private Vec3d initialMotion = new Vec3d(0, 0, 0);
	@Deprecated private int casingAmount = 1;
	@Deprecated private boolean afterReload = false;
	@Deprecated private int delay = 0;
	private float randomYaw = 0F;
	private float randomPitch = 0F;
	
	public CasingEjector() {
		this.id = nextId;
		nextId++;
		
		mappings.put(id, this);
	}
	
	public CasingEjector setOffset(double x, double y, double z) {
		return setOffset(new Vec3d(x, y, z));
	}
	
	public CasingEjector setOffset(Vec3d vec) {
		this.posOffset = vec;
		return this;
	}
	
	public CasingEjector setMotion(double x, double y, double z) {
		return setMotion(new Vec3d(x, y, z));
	}
	
	public CasingEjector setMotion(Vec3d vec) {
		this.initialMotion = vec;
		return this;
	}
	
	@Deprecated public CasingEjector setAmount(int am) {
		this.casingAmount = am;
		return this;
	}
	
	@Deprecated public CasingEjector setAfterReload() {
		this.afterReload = true;
		return this;
	}
	
	@Deprecated public CasingEjector setDelay(int delay) {
		this.delay = delay;
		return this;
	}
	
	public CasingEjector setAngleRange(float yaw, float pitch) {
		this.randomYaw = yaw;
		this.randomPitch = pitch;
		return this;
	}
	
	public int getId() { return this.id; }
	public Vec3d getOffset() { return this.posOffset; }
	public Vec3d getMotion() { return this.initialMotion; }
	public int getAmount() { return this.casingAmount; }
	public boolean getAfterReload() { return this.afterReload; }
	public int getDelay() { return this.delay; }
	public float getYawFactor() { return this.randomYaw; }
	public float getPitchFactor() { return this.randomPitch; }

	@SideOnly(Side.CLIENT)
	public void spawnCasing(TextureManager textureManager, SpentCasing config, World world, double x, double y, double z, float pitch, float yaw, boolean crouched) {
		Vec3d rotatedMotionVec = rotateVector(getMotion(), pitch + (float) rand.nextGaussian() * getPitchFactor(), yaw + (float) rand.nextGaussian() * getPitchFactor(), getPitchFactor(), getPitchFactor());
		ParticleSpentCasing casing = new ParticleSpentCasing(textureManager, world, x, y, z, rotatedMotionVec.x, rotatedMotionVec.y, rotatedMotionVec.z, (float) (world.rand.nextGaussian() * 5F), (float) (world.rand.nextGaussian() * 10F), config, false, 0, 0, 0);

		offsetCasing(casing, getOffset(), pitch, yaw, crouched);

		casing.rotationPitch = (float) Math.toDegrees(pitch);
		casing.rotationYaw = (float) Math.toDegrees(yaw);
		
		Minecraft.getMinecraft().effectRenderer.addEffect(casing);
	}

	// Rotate a position
	@SideOnly(Side.CLIENT)
	private static void offsetCasing(ParticleSpentCasing casing, Vec3d offset, float pitch, float yaw, boolean crouched) {
		// x-axis offset, 0 if crouched to center
		final float oX = (float) (crouched ? 0 : offset.x);
		// Create rotation matrices for pitch and yaw
		final Matrix4f pitchMatrix = new Matrix4f(), yawMatrix = new Matrix4f();

		pitchMatrix.rotate(pitch, new Vector3f(1, 0, 0)); // modify axis of rotation
		yawMatrix.rotate(-yaw, new Vector3f(0, 1, 0));

		// Multiply matrices to get combined rotation matrix
		final Matrix4f rotMatrix = Matrix4f.mul(yawMatrix, pitchMatrix, null);
		// Create vector representing the offset and apply rotation
		final Vector4f offsetVector = new Vector4f(oX, (float) offset.y, (float) offset.z, 1); // set fourth coordinate to 1
		Matrix4f.transform(rotMatrix, offsetVector, offsetVector);
		final Vector3f result = new Vector3f(); // create result vector
		result.set(offsetVector.x, offsetVector.y, offsetVector.z); // set result vector using transformed coordinates
		// Apply rotation
		casing.offsetSpawn(result.x, result.y, result.z);
	}

	private static Vec3d rotateVector(Vec3d vector, float pitch, float yaw, float pitchFactor, float yawFactor) {

		final Matrix4f pitchMatrix = new Matrix4f(), yawMatrix = new Matrix4f();

		pitchMatrix.setIdentity();
		pitchMatrix.rotate(pitch, new Vector3f(1, 0, 0));

		yawMatrix.setIdentity();
		yawMatrix.rotate(-yaw, new Vector3f(0, 1, 0));
		
		// Apply randomness to vector
		final Vector4f vector4f = new Vector4f((float) (vector.x + rand.nextGaussian() * yawFactor), (float) (vector.y + rand.nextGaussian() * pitchFactor), (float) (vector.z + rand.nextGaussian() * yawFactor), 1);

		Matrix4f.transform(pitchMatrix, vector4f, vector4f);
		Matrix4f.transform(yawMatrix, vector4f, vector4f);

		return new Vec3d(vector4f.x, vector4f.y, vector4f.z);
	}
	
	public static CasingEjector fromId(int id) {
		return mappings.get(id);
	}
	
	@Override
	public CasingEjector clone() {
		try {
			return (CasingEjector) super.clone();
		} catch(CloneNotSupportedException e) {
			return new CasingEjector();
		}
	}
}
