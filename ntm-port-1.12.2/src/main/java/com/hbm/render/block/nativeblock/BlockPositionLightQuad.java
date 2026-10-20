package com.hbm.render.block.nativeblock;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;

/** Unculled, already-shaded geometry that samples the block's own packed light, as the original ISBRH did. */
final class BlockPositionLightQuad extends BakedQuad {
    BlockPositionLightQuad(BakedQuad source) {
        super(source.getVertexData(), source.getTintIndex(), samplingFace(source), source.getSprite(), false, source.getFormat());
    }
    private static EnumFacing samplingFace(BakedQuad quad) {
        // In vanilla's flat fallback, a non-flat axis cannot be marked as a boundary
        // face. Select a spanning axis rather than move vertices off their exact planes.
        int[] data = quad.getVertexData();
        int stride = quad.getFormat().getIntegerSize();
        for(int axis = 0; axis < 3; axis++) {
            float first = Float.intBitsToFloat(data[axis]);
            for(int vertex = 1; vertex < 4; vertex++) {
                if(Float.intBitsToFloat(data[vertex * stride + axis]) != first)
                    return axis == 0 ? EnumFacing.WEST : axis == 1 ? EnumFacing.DOWN : EnumFacing.NORTH;
            }
        }
        throw new IllegalArgumentException("A block-position-lit quad must not collapse to a point");
    }
    @Override public void pipe(final IVertexConsumer consumer) {
        // VertexLighterFlat chooses packedLight[0] when no directional normal is
        // supplied. Shade is already in COLOR; the block buffer has no normal channel.
        LightUtil.putBakedQuad(new IVertexConsumer() {
            @Override public VertexFormat getVertexFormat() { return consumer.getVertexFormat(); }
            @Override public void setQuadTint(int tint) { consumer.setQuadTint(tint); }
            @Override public void setQuadOrientation(EnumFacing face) { consumer.setQuadOrientation(face); }
            @Override public void setApplyDiffuseLighting(boolean diffuse) { consumer.setApplyDiffuseLighting(false); }
            @Override public void setTexture(TextureAtlasSprite texture) { consumer.setTexture(texture); }
            @Override public void put(int element, float... values) {
                if(getVertexFormat().getElement(element).getUsage() == EnumUsage.NORMAL) consumer.put(element, 0, 0, 0, 0);
                else consumer.put(element, values);
            }
        }, this);
    }
}
