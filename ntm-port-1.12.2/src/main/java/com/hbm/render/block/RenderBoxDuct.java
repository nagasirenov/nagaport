package com.hbm.render.block;

import java.util.*;
import com.hbm.render.block.nativeblock.BoxDuctMaterials;
import com.hbm.render.block.nativeblock.BoxDuctMaterials.Kind;
import com.hbm.render.block.nativeblock.PneumaticCuboidGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Complete native geometry successor to the original box-duct ISBRH, not a RenderBlocks facade. */
public final class RenderBoxDuct {
    private RenderBoxDuct() { }
    public static List<BakedQuad> bake(BoxDuctMaterials materials, int metadata, int mask, boolean inventory, int color) {
        if(mask<0 || mask>63) throw new IllegalArgumentException("Invalid box-duct connection mask " + mask);
        boolean fluid = materials.kind != Kind.CABLE;
        double lower=.125D, upper=.875D, jLower=fluid?.0625D:.125D, jUpper=fluid?.9375D:.875D;
        if(fluid) { for(int i=2;i<13;i+=3) if(metadata>i) { lower+=.0625D;upper-=.0625D;jLower+=.0625D;jUpper-=.0625D; } }
        else { for(int i=0;i<5;i++) if(metadata>i) { lower+=.0625D;upper-=.0625D;jLower+=.0625D;jUpper-=.0625D; } }
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[6];
        for(int side=0;side<6;side++) sprites[side]=materials.face(metadata,mask,side,inventory);
        int[] rotations=new int[6];
        List<BakedQuad> quads=new ArrayList<>();
        if(inventory) {
            rotations[4]=1;rotations[5]=2;
            // Original single batch was UP, DOWN, EAST, WEST, SOUTH, NORTH.
            for(EnumFacing side:new EnumFacing[]{EnumFacing.UP,EnumFacing.DOWN,EnumFacing.EAST,EnumFacing.WEST,EnumFacing.SOUTH,EnumFacing.NORTH})
                quads.add(PneumaticCuboidGeometry.face(sprites[side.getIndex()],true,side,rotations[side.getIndex()],lower,lower,0,upper,upper,1,0));
            return Collections.unmodifiableList(quads);
        }
        if((mask&15)==0&&mask>0) {
            rotations[0]=1;rotations[1]=1;rotations[2]=2;rotations[3]=1;
            append(quads,sprites,rotations,0,lower,lower,1,upper,upper,color);
        } else if((mask&60)==0&&mask>0) {
            rotations[4]=1;rotations[5]=2;
            append(quads,sprites,rotations,lower,lower,0,upper,upper,1,color);
        } else if((mask&51)==0&&mask>0) {
            append(quads,sprites,rotations,lower,0,lower,upper,1,upper,color);
        } else {
            boolean curve=Integer.bitCount(mask)==2;
            if(curve) {
                if((mask&12)!=0&&(mask&48)!=0) { rotations[0]=1;rotations[1]=1; }
                if((mask&12)==0) { rotations[4]=1;rotations[5]=2;rotations[2]=2;rotations[3]=1; }
                jLower=lower;jUpper=upper;
            }
            append(quads,sprites,rotations,jLower,jLower,jLower,jUpper,jUpper,jUpper,color);
            if((mask&4)!=0) append(quads,sprites,rotations,lower,0,lower,upper,jLower,upper,color);
            if((mask&8)!=0) append(quads,sprites,rotations,lower,jUpper,lower,upper,1,upper,color);
            if((mask&16)!=0) append(quads,sprites,rotations,0,lower,lower,jLower,upper,upper,color);
            if((mask&32)!=0) append(quads,sprites,rotations,jUpper,lower,lower,1,upper,upper,color);
            if((mask&1)!=0) append(quads,sprites,rotations,lower,lower,0,upper,upper,jLower,color);
            if((mask&2)!=0) append(quads,sprites,rotations,lower,lower,jUpper,upper,upper,1,color);
        }
        return Collections.unmodifiableList(quads);
    }
    private static void append(List<BakedQuad> quads, TextureAtlasSprite[] sprites, int[] rotations,
            double x0,double y0,double z0,double x1,double y1,double z1,int color) {
        for(EnumFacing side:EnumFacing.values()) {
            BakedQuad face=PneumaticCuboidGeometry.face(sprites[side.getIndex()],false,side,rotations[side.getIndex()],x0,y0,z0,x1,y1,z1,0);
            if(color!=0xffffff) {
                int[] data=face.getVertexData().clone();int stride=face.getFormat().getIntegerSize(),at=face.getFormat().getColorOffset()/4;
                int rgba=0xff000000|(color&255)<<16|(color&0xff00)|(color>>>16&255);
                for(int v=0;v<4;v++) data[v*stride+at]=rgba;
                face=new BakedQuad(data,-1,side,face.getSprite(),true,face.getFormat());
            }
            quads.add(face);
        }
    }
}
