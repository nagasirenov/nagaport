package com.hbm.render.block.nativeblock;

import java.lang.reflect.Field;
import com.hbm.blocks.generic.BlockGrate.RenderSnapshot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.*;

/**
 * Grate-only adaptation of the native Forge lighter's public context setters.
 * Normalized faces must be lit at the original shifted render origin, then moved
 * back into the owning chunk's coordinate frame. No Minecraft/Forge class is replaced.
 *
 * Forge does not expose the current consumer/context; the two stable, unobfuscated
 * Forge fields are read only so both can be restored in finally. The actual native
 * Flat/SmoothAo light algorithms still run. Vanilla's optional non-Forge fallback
 * bypasses pipe entirely, and damage retexturing drops this wrapper: those remain
 * explicit visual gates, not a claim that translated vertices preserve their light.
 */
final class GrateShiftedLightQuad extends BakedQuad {
    private static final Field PARENT = field(QuadGatheringTransformer.class, "parent");
    private static final Field INFO = field(VertexLighterFlat.class, "blockInfo");
    private final BakedQuad normalized;
    private final RenderSnapshot snapshot;
    private final int shift;
    GrateShiftedLightQuad(BakedQuad normalized, RenderSnapshot snapshot, int shift) {
        super(translate(normalized, shift), normalized.getTintIndex(), normalized.getFace(), normalized.getSprite(),
                normalized.shouldApplyDiffuseLighting(), normalized.getFormat());
        this.normalized = normalized;
        this.snapshot = snapshot;
        this.shift = shift;
    }
    private static Field field(Class<?> owner, String name) {
        try { Field field = owner.getDeclaredField(name); field.setAccessible(true); return field; }
        catch(ReflectiveOperationException error) { throw new ExceptionInInitializerError(error); }
    }
    private static int[] translate(BakedQuad quad, int shift) {
        int[] data = quad.getVertexData().clone();
        int stride = quad.getFormat().getIntegerSize();
        for(int i = 0; i < 4; i++) data[i * stride + 1] = Float.floatToRawIntBits(Float.intBitsToFloat(data[i * stride + 1]) + shift);
        return data;
    }
    @Override public void pipe(IVertexConsumer consumer) {
        if(!(consumer instanceof VertexLighterFlat)) { LightUtil.putBakedQuad(consumer, this); return; }
        VertexLighterFlat lighter = (VertexLighterFlat)consumer;
        try {
            IVertexConsumer parent = (IVertexConsumer)PARENT.get(lighter);
            BlockInfo info = (BlockInfo)INFO.get(lighter);
            IBlockAccess world = info.getWorld();
            IBlockState state = info.getState();
            BlockPos pos = info.getBlockPos();
            try {
                lighter.setParent(new VertexTransformer(parent) {
                    @Override public void put(int element, float... values) {
                        if(getVertexFormat().getElement(element).getUsage() == net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage.POSITION) {
                            values = values.clone();
                            values[1] += shift;
                        }
                        super.put(element, values);
                    }
                });
                lighter.setWorld(snapshot.world);
                lighter.setState(snapshot.state);
                lighter.setBlockPos(snapshot.renderPos);
                lighter.updateBlockInfo();
                normalized.pipe(lighter);
            } finally {
                lighter.setParent(parent);
                lighter.setWorld(world);
                lighter.setState(state);
                lighter.setBlockPos(pos);
                lighter.updateBlockInfo();
            }
        } catch(IllegalAccessException error) { throw new IllegalStateException("Cannot access native grate lighting context", error); }
    }
}
