package com.hbm.render.block.nativeblock;

import java.util.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

/** Original standard cuboids, with RenderBlocksNT's world-only N/E mirror fix. */
public final class UniversalCuboidGeometry {
    private UniversalCuboidGeometry() { }
    private static final int[][] CORNERS = {{4,0,1,5},{2,6,7,3},{3,1,0,2},{6,4,5,7},{2,0,4,6},{7,5,1,3}};
    public static List<BakedQuad> bake(List<AxisAlignedBB> boxes, TextureAtlasSprite sprite, boolean inventory, double yOffset) {
        List<BakedQuad> result=new ArrayList<>();
        for(AxisAlignedBB box:boxes) for(EnumFacing side:EnumFacing.values()) result.add(face(box,sprite,inventory,yOffset,side));
        return Collections.unmodifiableList(result);
    }
    public static BakedQuad face(AxisAlignedBB box, TextureAtlasSprite sprite, boolean inventory, double yOffset, EnumFacing side) {
        VertexFormat format=DefaultVertexFormats.ITEM;
        UnpackedBakedQuad.Builder builder=new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);builder.setQuadTint(-1);builder.setContractUVs(false);builder.setApplyDiffuseLighting(true);
        EnumFacing normal=inventory && side.getAxis().isHorizontal()?side.rotateYCCW():side;
        builder.setQuadOrientation(normal);
        for(int corner:CORNERS[side.getIndex()]) {
            boolean highX=(corner&1)!=0,highY=(corner&2)!=0,highZ=(corner&4)!=0;
            double x=highX?box.maxX:box.minX,y=highY?box.maxY:box.minY,z=highZ?box.maxZ:box.minZ;
            // An out-of-range bound reset the complete UV interval in RenderBlocks,
            // rather than clipping the geometry or sampling beyond the atlas sprite.
            double tx=box.minX<0||box.maxX>1?(highX?1:0):x;
            double ty=box.minY<0||box.maxY>1?(highY?1:0):y;
            double tz=box.minZ<0||box.maxZ>1?(highZ?1:0):z;
            double reverseX=box.minX<0||box.maxX>1?1-tx:(highX?box.minX:box.maxX);
            double reverseZ=box.minZ<0||box.maxZ>1?1-tz:(highZ?box.minZ:box.maxZ);
            double u,v;
            switch(side) {
            case DOWN: case UP:u=tx;v=tz;break;
            case NORTH:u=inventory?reverseX:1-tx;v=1-ty;break;
            case SOUTH:u=tx;v=1-ty;break;
            case WEST:u=tz;v=1-ty;break;
            case EAST:u=inventory?reverseZ:1-tz;v=1-ty;break;
            default:throw new AssertionError(side);
            }
            for(int e=0;e<format.getElementCount();e++) switch(format.getElement(e).getUsage()) {
            case POSITION:builder.put(e,(float)(inventory?z:x),(float)(y+yOffset),(float)(inventory?1-x:z),1);break;
            case COLOR:builder.put(e,1,1,1,1);break;
            case UV:builder.put(e,sprite.getInterpolatedU(u*16),sprite.getInterpolatedV(v*16),0,1);break;
            case NORMAL:builder.put(e,normal.getXOffset(),normal.getYOffset(),normal.getZOffset(),0);break;
            default:builder.put(e);
            }
        }
        return builder.build();
    }
}
