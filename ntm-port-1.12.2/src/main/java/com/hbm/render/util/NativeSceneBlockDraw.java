package com.hbm.render.util;

import java.nio.ByteBuffer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Native finite-scene draws. Model, fluid, actual/extended state and lighting stay with Minecraft. */
public final class NativeSceneBlockDraw {
    private NativeSceneBlockDraw() { }

    public static boolean append(IBlockAccess world, IBlockState state, BlockPos pos, BufferBuilder buffer) {
        return Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(state, pos, world, buffer);
    }

    /** Slice printer passed the source block to RenderBlocks but read metadata at (dx, 0, dz). */
    public static IBlockState sliceState(Block sourceBlock, IBlockAccess liveWorld, BlockPos destination) {
        IBlockState atDestination = liveWorld.getBlockState(destination);
        int metadata = atDestination.getBlock().getMetaFromState(atDestination);
        return sourceBlock.getStateFromMeta(metadata);
    }

    /**
     * The preview's old disableColor suppressed upload, not lighting evaluation. Native model
     * rendering still needs the complete BLOCK format; copy its position/UV/lightmap afterward.
     * Do not substitute noColor(), which uploads white and overrides the cyan/red GL tint.
     */
    public static BufferBuilder finishColorless(BufferBuilder source) {
        if(!DefaultVertexFormats.BLOCK.equals(source.getVertexFormat())) {
            throw new IllegalArgumentException("Scene preview requires native BLOCK vertices");
        }
        source.finishDrawing();
        VertexFormat format = new VertexFormat().addElement(DefaultVertexFormats.POSITION_3F)
                .addElement(DefaultVertexFormats.TEX_2F).addElement(DefaultVertexFormats.TEX_2S);
        BufferBuilder output = new BufferBuilder(Math.max(256, source.getVertexCount() * 6));
        output.begin(GL11.GL_QUADS, format);
        ByteBuffer data = source.getByteBuffer();
        for(int vertex = 0; vertex < source.getVertexCount(); vertex++) {
            int at = vertex * 28;
            output.pos(data.getFloat(at), data.getFloat(at + 4), data.getFloat(at + 8))
                    .tex(data.getFloat(at + 16), data.getFloat(at + 20))
                    .lightmap(data.getShort(at + 26) & 65535, data.getShort(at + 24) & 65535).endVertex();
        }
        output.finishDrawing();
        return output;
    }

    public static void drawColorless(BufferBuilder source) {
        new WorldVertexBufferUploader().draw(finishColorless(source));
    }
}
