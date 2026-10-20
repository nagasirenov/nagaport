package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import com.hbm.blocks.SideRotationModelState.Snapshot;
import com.hbm.render.block.nativeblock.ConveyorCuboidGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

/** Native full-cube faces; each original misleading UV field maps to its actual geometric face. */
public final class RenderBlockSideRotation {
    private static final int[] ROTATION_SOURCE_SIDE = {0,1,3,2,5,4};
    private RenderBlockSideRotation() { }
    public static List<BakedQuad> bake(Snapshot snapshot,boolean inventory,Function<ResourceLocation,TextureAtlasSprite> sprites) {
        List<BakedQuad> result=new ArrayList<>();
        for(EnumFacing side:EnumFacing.values()) {
            // Original assignments were North<-5, South<-4, West<-2, East<-3.
            // Actual old face methods read East for -Z, West for +Z, North for -X,
            // South for +X. Preserve that composition, including editor side faces.
            int sourceSide=ROTATION_SOURCE_SIDE[side.getIndex()];
            int rotation=snapshot.rotation(sourceSide);
            // Original face methods used default UVs unless the field was exactly 1, 2 or 3.
            if(rotation < 1 || rotation > 3) rotation=0;
            BakedQuad quad=ConveyorCuboidGeometry.face(sprites.apply(snapshot.texture(side.getIndex())),inventory,side,rotation,0,0,0,1,1,1,0);
            result.add(inventory ? inventoryRotation(quad) : quad);
        }
        return Collections.unmodifiableList(result);
    }
    private static BakedQuad inventoryRotation(BakedQuad source) {
        // Original glRotatef(90,0,1,0) then glTranslatef(-.5,-.5,-.5),
        // recentered by +.5 for native block-item coordinates. UVs stay on their source vertices.
        VertexFormat format=source.getFormat();
        UnpackedBakedQuad.Builder builder=new UnpackedBakedQuad.Builder(format);
        EnumFacing side=source.getFace();
        builder.setTexture(source.getSprite());builder.setQuadTint(-1);
        builder.setQuadOrientation(side.getAxis()==EnumFacing.Axis.Y ? side : side.rotateYCCW());
        builder.setApplyDiffuseLighting(true);builder.setContractUVs(false);
        for(int vertex=0;vertex<4;vertex++) for(int element=0;element<format.getElementCount();element++) {
            float[] values=new float[4];LightUtil.unpack(source.getVertexData(),values,format,vertex,element);
            VertexFormatElement.EnumUsage usage=format.getElement(element).getUsage();
            if(usage==VertexFormatElement.EnumUsage.POSITION) {float x=values[0];values[0]=values[2];values[2]=1-x;}
            else if(usage==VertexFormatElement.EnumUsage.NORMAL) {float x=values[0];values[0]=values[2];values[2]=-x;}
            builder.put(element,values);
        }
        return builder.build();
    }
}
