package com.hbm.render.block.ct;

import java.util.*;
import com.hbm.render.block.nativeblock.ConveyorCuboidGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

/** Original four-fragment full-cube geometry, now native baked quads, not a RenderBlocks facade. */
public final class RenderBlocksCT {
    private RenderBlocksCT() { }
    // Lexical TL,TR,BL,BR. The original face renderer ignores mutable block bounds.
    private static final float[][][] CORNERS = {
        {{0,0,1},{1,0,1},{0,0,0},{1,0,0}}, {{0,1,0},{1,1,0},{0,1,1},{1,1,1}},
        {{1,1,0},{0,1,0},{1,0,0},{0,0,0}}, {{0,1,1},{1,1,1},{0,0,1},{1,0,1}},
        {{0,1,0},{0,1,1},{0,0,0},{0,0,1}}, {{1,1,1},{1,1,0},{1,0,1},{1,0,0}}
    };
    public static List<BakedQuad> bakeFace(TextureAtlasSprite full, TextureAtlasSprite connected, EnumFacing face, int mask) {
        List<BakedQuad> result = new ArrayList<>(4);
        for(int quadrant=0; quadrant<4; quadrant++) {
            int fragment=CTContext.fragment(mask, quadrant);
            TextureAtlasSprite sprite=fragment<4 ? full : connected;
            int sub=fragment<4 ? 2 : 4;
            float cellU=(sprite.getMaxU()-sprite.getMinU())/sub;
            float cellV=(sprite.getMaxV()-sprite.getMinV())/sub;
            float u=sprite.getMinU(),v=sprite.getMinV();
            if(CT.isV(fragment)||CT.isJ(fragment)) u+=cellU*2;
            if(CT.isH(fragment)||CT.isJ(fragment)) v+=cellV*2;
            if(CT.isR(fragment)) u+=cellU;
            if(CT.isB(fragment)) v+=cellV;
            float left=(quadrant&1)*.5F, top=(quadrant>>>1)*.5F;
            result.add(quad(sprite, face, left, top, .5F, u, v, cellU, cellV));
        }
        return Collections.unmodifiableList(result);
    }
    /** Unsplit reference for the native full-face corner light solver, in TR,TL,BL,BR order. */
    public static BakedQuad fullFace(TextureAtlasSprite sprite, EnumFacing face) {
        return quad(sprite, face, 0, 0, 1, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU()-sprite.getMinU(), sprite.getMaxV()-sprite.getMinV());
    }
    private static BakedQuad quad(TextureAtlasSprite sprite, EnumFacing face, float left, float top, float span, float u, float v, float cellU, float cellV) {
        VertexFormat format=DefaultVertexFormats.ITEM;
        UnpackedBakedQuad.Builder builder=new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite); builder.setQuadTint(-1); builder.setQuadOrientation(face);
        builder.setApplyDiffuseLighting(true); builder.setContractUVs(false);
        float[][] uv={{1,0},{0,0},{0,1},{1,1}}; // Original rotational TR,TL,BL,BR.
        float[][] corners=CORNERS[face.getIndex()];
        for(float[] corner : uv) {
            float h=left+corner[0]*span, w=top+corner[1]*span;
            float[] position=new float[3];
            for(int axis=0;axis<3;axis++) position[axis]=corners[0][axis]+(corners[1][axis]-corners[0][axis])*h+(corners[2][axis]-corners[0][axis])*w;
            for(int e=0;e<format.getElementCount();e++) {
                switch(format.getElement(e).getUsage()) {
                    case POSITION: builder.put(e,position[0],position[1],position[2],1); break;
                    case COLOR: builder.put(e,1,1,1,1); break;
                    case UV: builder.put(e,u+corner[0]*cellU,v+corner[1]*cellV,0,1); break;
                    case NORMAL: builder.put(e,face.getXOffset(),face.getYOffset(),face.getZOffset(),0); break;
                    default: builder.put(e);
                }
            }
        }
        return builder.build();
    }
    public static List<BakedQuad> bakeInventory(TextureAtlasSprite full) {
        List<BakedQuad> result=new ArrayList<>(6);
        for(EnumFacing face:EnumFacing.values()) {
            BakedQuad original=ConveyorCuboidGeometry.face(full,true,face,0,0,0,0,1,1,1,0);
            int[] data=original.getVertexData().clone(); int stride=original.getFormat().getIntegerSize();
            EnumFacing rotated=face.getAxis()==EnumFacing.Axis.Y ? face : face.rotateYCCW();
            int normal=original.getFormat().getNormalOffset()/4;
            for(int vertex=0;vertex<4;vertex++) {
                int at=vertex*stride; float x=Float.intBitsToFloat(data[at]);
                data[at]=data[at+2]; data[at+2]=Float.floatToRawIntBits(1F-x);
                data[at+normal]=(rotated.getXOffset()*127 & 255)|((rotated.getYOffset()*127 & 255)<<8)|((rotated.getZOffset()*127 & 255)<<16);
            }
            result.add(new BakedQuad(data,-1,rotated,full,true,original.getFormat()));
        }
        return Collections.unmodifiableList(result);
    }
}
