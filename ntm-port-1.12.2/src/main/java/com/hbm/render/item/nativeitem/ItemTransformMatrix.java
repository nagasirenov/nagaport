package com.hbm.render.item.nativeitem;

import javax.vecmath.Matrix4f;

/** Ordered OpenGL-style postmultiplication, without GL access or model-center conversion. */
public final class ItemTransformMatrix {
    private final Matrix4f matrix = new Matrix4f();
    public ItemTransformMatrix() { matrix.setIdentity(); }
    public ItemTransformMatrix multiply(Matrix4f next) { matrix.mul(next); return this; }
    public ItemTransformMatrix translate(double x, double y, double z) {
        Matrix4f next = identity(); next.m03 = (float)x; next.m13 = (float)y; next.m23 = (float)z;
        return multiply(next);
    }
    public ItemTransformMatrix scale(double x, double y, double z) {
        Matrix4f next = identity(); next.m00 = (float)x; next.m11 = (float)y; next.m22 = (float)z;
        return multiply(next);
    }
    public ItemTransformMatrix scale(double size) { return scale(size, size, size); }
    public ItemTransformMatrix rotateX(double degrees) {
        Matrix4f next = new Matrix4f(); next.rotX((float)Math.toRadians(degrees)); return multiply(next);
    }
    public ItemTransformMatrix rotateY(double degrees) {
        Matrix4f next = new Matrix4f(); next.rotY((float)Math.toRadians(degrees)); return multiply(next);
    }
    public ItemTransformMatrix rotateZ(double degrees) {
        Matrix4f next = new Matrix4f(); next.rotZ((float)Math.toRadians(degrees)); return multiply(next);
    }
    public Matrix4f build() { return new Matrix4f(matrix); }
    private static Matrix4f identity() { Matrix4f value = new Matrix4f(); value.setIdentity(); return value; }
}
