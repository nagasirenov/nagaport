package com.hbm.util;

import net.minecraft.util.math.Vec3d;

/** Mutable vector helper for calculations that update components in place. */
public class Vec3NT {

	public double x;
	public double y;
	public double z;

	public Vec3NT(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3NT(Vec3NT vec) {
		this(vec.x, vec.y, vec.z);
	}

	public Vec3NT(Vec3d vec) {
		this(vec.x, vec.y, vec.z);
	}

	public Vec3d toVec3d() {
		return new Vec3d(this.x, this.y, this.z);
	}

	public Vec3NT normalizeSelf() {
		double len = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
		if(len < 1.0E-4D) {
			return multiply(0D);
		} else {
			return multiply(1D / len);
		}
	}

	public Vec3NT add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vec3NT add(Vec3NT vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}

	public Vec3NT add(Vec3d vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}

	public Vec3NT multiply(double m) {
		this.x *= m;
		this.y *= m;
		this.z *= m;
		return this;
	}

	public Vec3NT multiply(double x, double y, double z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}

	public double distanceTo(double x, double y, double z) {
		double dX = x - this.x;
		double dY = y - this.y;
		double dZ = z - this.z;
		return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
	}

	public double distanceTo(Vec3NT vec) {
		return this.distanceTo(vec.x, vec.y, vec.z);
	}

	public double distanceTo(Vec3d vec) {
		return this.distanceTo(vec.x, vec.y, vec.z);
	}

	public double length() {
		return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public Vec3NT setComponents(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Vec3NT rotateAroundXRad(double alpha) {
		double cos = Math.cos(alpha);
		double sin = Math.sin(alpha);
		double x = this.x;
		double y = this.y * cos + this.z * sin;
		double z = this.z * cos - this.y * sin;
		return this.setComponents(x, y, z);
	}

	public Vec3NT rotateAroundYRad(double alpha) {
		double cos = Math.cos(alpha);
		double sin = Math.sin(alpha);
		double x = this.x * cos + this.z * sin;
		double y = this.y;
		double z = this.z * cos - this.x * sin;
		return this.setComponents(x, y, z);
	}

	public Vec3NT rotateAroundZRad(double alpha) {
		double cos = Math.cos(alpha);
		double sin = Math.sin(alpha);
		double x = this.x * cos + this.y * sin;
		double y = this.y * cos - this.x * sin;
		double z = this.z;
		return this.setComponents(x, y, z);
	}

	public Vec3NT rotateAroundXDeg(double alpha) {
		return this.rotateAroundXRad(alpha / 180D * Math.PI);
	}

	public Vec3NT rotateAroundYDeg(double alpha) {
		return this.rotateAroundYRad(alpha / 180D * Math.PI);
	}

	public Vec3NT rotateAroundZDeg(double alpha) {
		return this.rotateAroundZRad(alpha / 180D * Math.PI);
	}
	
	public static double getMinX(Vec3NT... vecs) {
		double min = Double.POSITIVE_INFINITY;
		for(Vec3NT vec : vecs) if(vec.x < min) min = vec.x;
		return min;
	}
	
	public static double getMinY(Vec3NT... vecs) {
		double min = Double.POSITIVE_INFINITY;
		for(Vec3NT vec : vecs) if(vec.y < min) min = vec.y;
		return min;
	}
	
	public static double getMinZ(Vec3NT... vecs) {
		double min = Double.POSITIVE_INFINITY;
		for(Vec3NT vec : vecs) if(vec.z < min) min = vec.z;
		return min;
	}
	
	public static double getMaxX(Vec3NT... vecs) {
		double max = Double.NEGATIVE_INFINITY;
		for(Vec3NT vec : vecs) if(vec.x > max) max = vec.x;
		return max;
	}
	
	public static double getMaxY(Vec3NT... vecs) {
		double max = Double.NEGATIVE_INFINITY;
		for(Vec3NT vec : vecs) if(vec.y > max) max = vec.y;
		return max;
	}
	
	public static double getMaxZ(Vec3NT... vecs) {
		double max = Double.NEGATIVE_INFINITY;
		for(Vec3NT vec : vecs) if(vec.z > max) max = vec.z;
		return max;
	}
}
