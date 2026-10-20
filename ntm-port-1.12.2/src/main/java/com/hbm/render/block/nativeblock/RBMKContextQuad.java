package com.hbm.render.block.nativeblock;

import java.lang.reflect.Field;
import com.hbm.blocks.machine.rbmk.RBMKRenderState.Snapshot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.*;

/**
 * Scoped native lighter context for raised pipe/lid cubes and original already-lit OBJ faces.
 * The original OBJ bypassed standard-block AO entirely, even alongside AO-lit cube faces.
 * Native Forge flat and smooth pipelines are restored in finally; vanilla's optional pipe-bypassing
 * fallback and default damage retexturing remain explicitly tested acceptance gaps.
 */
final class RBMKContextQuad extends BakedQuad {
    private static final Field PARENT = field(QuadGatheringTransformer.class, "parent");
    private static final Field INFO = field(VertexLighterFlat.class, "blockInfo");
    private final BakedQuad source;
    private final Snapshot snapshot;
    private final int shift;
    private final boolean obj;
    RBMKContextQuad(BakedQuad source, Snapshot snapshot, int shift, boolean obj) {
        super(data(source, shift), source.getTintIndex(), source.getFace(), source.getSprite(), source.shouldApplyDiffuseLighting(), source.getFormat());
        this.source = source; this.snapshot = snapshot; this.shift = shift; this.obj = obj;
    }
    private static Field field(Class<?> owner, String name) {
        try { Field field = owner.getDeclaredField(name); field.setAccessible(true); return field; }
        catch(ReflectiveOperationException failure) { throw new ExceptionInInitializerError(failure); }
    }
    private static int[] data(BakedQuad source, int shift) {
        int[] data = source.getVertexData().clone(); int stride = source.getFormat().getIntegerSize();
        for(int v = 0; v < 4; v++) data[v*stride+1] = Float.floatToRawIntBits(Float.intBitsToFloat(data[v*stride+1]) + shift);
        return data;
    }
    @Override public void pipe(IVertexConsumer consumer) {
        if(!(consumer instanceof VertexLighterFlat)) { LightUtil.putBakedQuad(consumer, this); return; }
        VertexLighterFlat lighter = (VertexLighterFlat)consumer;
        try {
            IVertexConsumer parent = (IVertexConsumer)PARENT.get(lighter);
            BlockInfo info = (BlockInfo)INFO.get(lighter);
            IBlockAccess oldWorld = info.getWorld(); IBlockState oldState = info.getState(); BlockPos oldPos = info.getBlockPos();
            final int[] vertex = {0};
            final int colorOffset = source.getFormat().getColorOffset() / 4;
            final int stride = source.getFormat().getIntegerSize();
            try {
                lighter.setParent(new VertexTransformer(parent) {
                    @Override public void put(int element, float... values) {
                        VertexFormatElement field = getVertexFormat().getElement(element);
                        if(field.getUsage() == VertexFormatElement.EnumUsage.POSITION && shift != 0) {
                            values = values.clone(); values[1] += shift;
                        } else if(obj && field.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
                            int color = source.getVertexData()[vertex[0] * stride + colorOffset];
                            values = new float[] {(color & 255)/255F, (color >>> 8 & 255)/255F, (color >>> 16 & 255)/255F, (color >>> 24 & 255)/255F};
                        } else if(obj && field.getUsage() == VertexFormatElement.EnumUsage.UV && field.getIndex() == 1) {
                            values = new float[] {(snapshot.packedLight & 65535)/32767F, (snapshot.packedLight >>> 16 & 65535)/32767F, 0, 1};
                        }
                        super.put(element, values);
                        if(element == getVertexFormat().getElementCount() - 1) vertex[0]++;
                    }
                });
                if(shift != 0) {
                    lighter.setWorld(snapshot.world); lighter.setState(snapshot.state); lighter.setBlockPos(snapshot.pos.up(shift)); lighter.updateBlockInfo();
                }
                source.pipe(lighter);
            } finally {
                lighter.setParent(parent);
                if(shift != 0) {
                    lighter.setWorld(oldWorld); lighter.setState(oldState); lighter.setBlockPos(oldPos); lighter.updateBlockInfo();
                }
            }
        } catch(IllegalAccessException failure) { throw new IllegalStateException("Cannot access native RBMK lighting context", failure); }
    }
}
