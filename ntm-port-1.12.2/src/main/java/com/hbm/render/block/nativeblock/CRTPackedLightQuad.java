package com.hbm.render.block.nativeblock;

import java.lang.reflect.Field;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.client.model.pipeline.VertexTransformer;

/**
 * Only CRT's lit Screen replaces packed light, after Forge's environmental lighter.
 * Original brightness 240 means block light 240, sky light zero, not full sky light.
 * No global light settings, shader state, geometry or colors are changed.
 *
 * Forge has no public getter for its parent consumer. Its stable unobfuscated field
 * is read to install a quad-local output adapter, then restored in finally. Native
 * vanilla's optional non-Forge fallback bypasses pipe, and damage retexturing loses
 * this wrapper; those routes remain explicit parity gates.
 */
public final class CRTPackedLightQuad extends BakedQuad {
    private static final Field PARENT = parentField();
    private final BakedQuad source;
    private final int packedLight;
    public CRTPackedLightQuad(BakedQuad source, int packedLight) {
        super(withLight(source, packedLight), source.getTintIndex(), source.getFace(), source.getSprite(), false, source.getFormat());
        this.source = source;
        this.packedLight = packedLight;
    }
    private static Field parentField() {
        try {
            Field field = QuadGatheringTransformer.class.getDeclaredField("parent");
            field.setAccessible(true);
            return field;
        } catch(ReflectiveOperationException failure) { throw new ExceptionInInitializerError(failure); }
    }
    private static int[] withLight(BakedQuad source, int light) {
        if(!source.getFormat().hasUvOffset(1)) throw new IllegalArgumentException("CRT world quad requires native lightmap UV1");
        int[] data = source.getVertexData().clone();
        int offset = source.getFormat().getUvOffsetById(1) / 4;
        int stride = source.getFormat().getIntegerSize();
        for(int vertex = 0; vertex < 4; vertex++) data[vertex * stride + offset] = light;
        return data;
    }
    private IVertexConsumer fixedLight(IVertexConsumer parent) {
        return new VertexTransformer(parent) {
            @Override public void put(int element, float... values) {
                VertexFormatElement field = getVertexFormat().getElement(element);
                if(field.getUsage() == VertexFormatElement.EnumUsage.UV && field.getIndex() == 1) {
                    // BLOCK's lightmap is signed SHORT2 in the native packing API.
                    super.put(element, (packedLight & 65535) / 32767F, (packedLight >>> 16 & 65535) / 32767F, 0, 1);
                } else super.put(element, values);
            }
        };
    }
    @Override public void pipe(IVertexConsumer consumer) {
        if(!(consumer instanceof VertexLighterFlat)) { source.pipe(fixedLight(consumer)); return; }
        VertexLighterFlat lighter = (VertexLighterFlat)consumer;
        try {
            IVertexConsumer parent = (IVertexConsumer)PARENT.get(lighter);
            try {
                lighter.setParent(fixedLight(parent));
                source.pipe(lighter);
            } finally { lighter.setParent(parent); }
        } catch(IllegalAccessException failure) { throw new IllegalStateException("Cannot access native CRT lighting output", failure); }
    }
}
