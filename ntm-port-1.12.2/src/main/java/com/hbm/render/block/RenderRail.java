package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Native geometry for every original RenderRail consumer, including the shared funnel. */
public final class RenderRail {
    private RenderRail() { }
    public enum Kind {
        NARROW_STRAIGHT, NARROW_CURVE, STRAIGHT, SHORT, CURVE, CURVE7, CURVE9, RAMP, BUFFER, SWITCH, SWITCH_FLIPPED, FUNNEL
    }
    public static List<BakedQuad> bake(ObjMesh mesh, Kind kind, int metadata, boolean inventory, int sign, TextureAtlasSprite... sprites) {
        if(metadata < 0 || metadata > 15 || sign < 0 || sign > 2) throw new IllegalArgumentException("Invalid original rail state");
        List<BakedQuad> quads = new ArrayList<>();
        if(!inventory && kind != Kind.NARROW_STRAIGHT && kind != Kind.FUNNEL && metadata < 12) return Collections.emptyList();
        float yaw = 0;
        double x = inventory ? 0 : .5, y = 0, z = inventory ? 0 : .5;
        if(!inventory) {
            if(kind == Kind.NARROW_STRAIGHT) {
                if(metadata == 2 || metadata == 3 || metadata == 12 || metadata == 13) yaw = 90F / 180F * (float)Math.PI;
            } else if(kind == Kind.NARROW_CURVE) {
                if(metadata == 12) yaw = 90F / 180F * (float)Math.PI;
                if(metadata == 14) yaw = 180F / 180F * (float)Math.PI;
                if(metadata == 13) yaw = 270F / 180F * (float)Math.PI;
            } else if(kind == Kind.STRAIGHT || kind == Kind.SHORT) {
                if(metadata == 14 || metadata == 15) yaw = 90F / 180F * (float)Math.PI;
            } else if(kind != Kind.FUNNEL) {
                if(metadata == 15) yaw = 90F / 180F * (float)Math.PI;
                if(metadata == 12) yaw = 180F / 180F * (float)Math.PI;
                if(metadata == 14) yaw = 270F / 180F * (float)Math.PI;
            }
            if(kind == Kind.STRAIGHT || kind == Kind.SHORT || kind == Kind.RAMP || kind == Kind.BUFFER || kind == Kind.SWITCH || kind == Kind.SWITCH_FLIPPED) {
                if(metadata == 12) x += .5;
                if(metadata == 13) x -= .5;
                if(metadata == 14) z -= .5;
                if(metadata == 15) z += .5;
            }
        }
        if(kind == Kind.FUNNEL) {
            AtlasObjGeometry.appendPart(quads, mesh, "Top", sprites[0], inventory, 0, 0, 1, x, y, z);
            AtlasObjGeometry.appendPart(quads, mesh, "Bottom", sprites[1], inventory, 0, 0, 1, x, y, z);
            AtlasObjGeometry.appendPart(quads, mesh, "Side", sprites[2], inventory, 0, 0, 1, x, y, z);
        } else if(kind == Kind.SWITCH || kind == Kind.SWITCH_FLIPPED) {
            AtlasObjGeometry.appendPart(quads, mesh, "Rail", sprites[0], inventory, yaw, 0, 1, x, y, z);
            if(!inventory && sign != 0) AtlasObjGeometry.appendPart(quads, mesh, sign == 2 ? "SignTurn" : "SignStraight", sprites[1], false, yaw, 0, 1, x, y, z);
        } else AtlasObjGeometry.appendWhole(quads, mesh, sprites[0], inventory, yaw, 0, 1, x, y, z);
        if(inventory) {
            double[] transform = inventoryMatrix(kind);
            List<BakedQuad> transformed = new ArrayList<>();
            for(BakedQuad quad : quads) transformed.add(transform(quad, transform));
            quads = transformed;
        }
        return Collections.unmodifiableList(quads);
    }
    /** GL's original postmultiplication order, then the native model's half-block pivot. */
    private static double[] inventoryMatrix(Kind kind) {
        double[] matrix = identity();
        if(kind == Kind.NARROW_CURVE) {
            matrix = multiply(matrix, scale(.2)); matrix = multiply(matrix, translate(2.5, -.0625, -1.5));
        } else if(kind == Kind.CURVE || kind == Kind.CURVE7 || kind == Kind.CURVE9) {
            matrix = multiply(matrix, scale(kind == Kind.CURVE ? .3 : kind == Kind.CURVE7 ? .225 : .175));
            matrix = multiply(matrix, rotate(45, false)); matrix = multiply(matrix, rotate(60, true));
            double shift = kind == Kind.CURVE ? 2 : kind == Kind.CURVE7 ? 3 : 4;
            matrix = multiply(matrix, translate(shift, 0, shift));
        } else {
            matrix = multiply(matrix, translate(0, kind == Kind.FUNNEL ? -.5 : -.0625, kind == Kind.SWITCH ? -.1875 : 0));
            if(kind != Kind.NARROW_STRAIGHT && kind != Kind.FUNNEL) {
                matrix = multiply(matrix, rotate(90, false));
                matrix = multiply(matrix, scale(kind == Kind.SHORT ? .7 : kind == Kind.SWITCH || kind == Kind.SWITCH_FLIPPED ? .1 : .3));
            }
        }
        return multiply(translate(.5, .5, .5), matrix);
    }
    private static double[] identity() { return new double[] {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1}; }
    private static double[] translate(double x, double y, double z) { double[] m = identity(); m[3]=x; m[7]=y; m[11]=z; return m; }
    private static double[] scale(double value) { double[] m = identity(); m[0]=m[5]=m[10]=value; return m; }
    private static double[] rotate(double degrees, boolean xAxis) {
        double[] m = identity(); double c = Math.cos(Math.toRadians(degrees)), s = Math.sin(Math.toRadians(degrees));
        if(xAxis) { m[5]=c; m[6]=-s; m[9]=s; m[10]=c; }
        else { m[0]=c; m[2]=s; m[8]=-s; m[10]=c; }
        return m;
    }
    private static double[] multiply(double[] a, double[] b) {
        double[] out = new double[16];
        for(int row=0;row<4;row++) for(int col=0;col<4;col++) for(int k=0;k<4;k++) out[row*4+col] += a[row*4+k]*b[k*4+col];
        return out;
    }
    private static BakedQuad transform(BakedQuad source, double[] matrix) {
        int[] data = source.getVertexData().clone(); int stride = source.getFormat().getIntegerSize();
        for(int v=0;v<4;v++) {
            int at=v*stride; double x=Float.intBitsToFloat(data[at]), y=Float.intBitsToFloat(data[at+1]), z=Float.intBitsToFloat(data[at+2]);
            for(int axis=0;axis<3;axis++) data[at+axis]=Float.floatToRawIntBits((float)(matrix[axis*4]*x+matrix[axis*4+1]*y+matrix[axis*4+2]*z+matrix[axis*4+3]));
            if(source.getFormat().hasNormal()) {
                int offset=at+source.getFormat().getNormalOffset()/4, packed=data[offset];
                double nx=(byte)packed/127D, ny=(byte)(packed>>>8)/127D, nz=(byte)(packed>>>16)/127D;
                double[] n={matrix[0]*nx+matrix[1]*ny+matrix[2]*nz,matrix[4]*nx+matrix[5]*ny+matrix[6]*nz,matrix[8]*nx+matrix[9]*ny+matrix[10]*nz};
                double scale=Math.sqrt(matrix[0]*matrix[0]+matrix[1]*matrix[1]+matrix[2]*matrix[2]);
                data[offset]=(packed&0xff000000)|((byte)(n[0]/scale*127)&255)|((byte)(n[1]/scale*127)&255)<<8|((byte)(n[2]/scale*127)&255)<<16;
            }
        }
        return new BakedQuad(data, source.getTintIndex(), source.getFace(), source.getSprite(), source.shouldApplyDiffuseLighting(), source.getFormat());
    }
}
