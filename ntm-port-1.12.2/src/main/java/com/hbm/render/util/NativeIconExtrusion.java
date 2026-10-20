package com.hbm.render.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/** Pixel-column icon extrusion used by NTM item glints and machine displays.
 * Keeps the original float arithmetic, half-texel sampling and face order.
 * Uses the current GL color, as the original extrusion did; owns only its draw.
 */
public final class NativeIconExtrusion {
    private NativeIconExtrusion() { }
    public static void draw(float u0, float v0, float u1, float v1,
            int width, int height, float depth) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
        append(buffer, u0, v0, u1, v1, width, height, depth);
        Tessellator.getInstance().draw();
    }
    public static void append(BufferBuilder b, float u0, float v0, float u1, float v1,
            int width, int height, float depth) {
        vertex(b,0,0,0,u0,v1,0,0,1); vertex(b,1,0,0,u1,v1,0,0,1);
        vertex(b,1,1,0,u1,v0,0,0,1); vertex(b,0,1,0,u0,v0,0,0,1);
        float back = 0F - depth;
        vertex(b,0,1,back,u0,v0,0,0,-1); vertex(b,1,1,back,u1,v0,0,0,-1);
        vertex(b,1,0,back,u1,v1,0,0,-1); vertex(b,0,0,back,u0,v1,0,0,-1);
        float halfU = .5F * (u0 - u1) / (float)width;
        float halfV = .5F * (v1 - v0) / (float)height;
        for(int i=0; i<width; i++) {
            float fraction = (float)i / (float)width;
            float u = u0 + (u1-u0)*fraction - halfU;
            vertex(b,fraction,0,back,u,v1,-1,0,0); vertex(b,fraction,0,0,u,v1,-1,0,0);
            vertex(b,fraction,1,0,u,v0,-1,0,0); vertex(b,fraction,1,back,u,v0,-1,0,0);
        }
        for(int i=0; i<width; i++) {
            float fraction = (float)i / (float)width;
            float u = u0 + (u1-u0)*fraction - halfU;
            float x = fraction + 1F / (float)width;
            vertex(b,x,1,back,u,v0,1,0,0); vertex(b,x,1,0,u,v0,1,0,0);
            vertex(b,x,0,0,u,v1,1,0,0); vertex(b,x,0,back,u,v1,1,0,0);
        }
        for(int i=0; i<height; i++) {
            float fraction = (float)i / (float)height;
            float v = v1 + (v0-v1)*fraction - halfV;
            float y = fraction + 1F / (float)height;
            vertex(b,0,y,0,u0,v,0,1,0); vertex(b,1,y,0,u1,v,0,1,0);
            vertex(b,1,y,back,u1,v,0,1,0); vertex(b,0,y,back,u0,v,0,1,0);
        }
        for(int i=0; i<height; i++) {
            float fraction = (float)i / (float)height;
            float v = v1 + (v0-v1)*fraction - halfV;
            vertex(b,1,fraction,0,u1,v,0,-1,0); vertex(b,0,fraction,0,u0,v,0,-1,0);
            vertex(b,0,fraction,back,u0,v,0,-1,0); vertex(b,1,fraction,back,u1,v,0,-1,0);
        }
    }
    private static void vertex(BufferBuilder b, double x, double y, double z,
            double u, double v, float nx, float ny, float nz) {
        b.pos(x,y,z).tex(u,v).normal(nx,ny,nz).endVertex();
    }
}
