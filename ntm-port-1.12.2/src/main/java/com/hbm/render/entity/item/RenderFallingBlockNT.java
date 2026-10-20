package com.hbm.render.entity.item;

import com.hbm.entity.item.EntityFallingBlockNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

/** One native block-model path for ordinary cubes and the original anvil OBJ. */
public class RenderFallingBlockNT extends Render<EntityFallingBlockNT> {
    public RenderFallingBlockNT(RenderManager manager) { super(manager); shadowSize = .5F; }
    @Override public void doRender(EntityFallingBlockNT entity, double x, double y, double z, float yaw, float partialTicks) {
        BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
        IBlockState state = entity.getFallingState();
        if(state.getBlock() == entity.world.getBlockState(pos).getBlock()) return;
        state = state.getActualState(entity.world, pos);
        bindEntityTexture(entity);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        try {
            GlStateManager.translate(x, y, z);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            buffer.setTranslation(-pos.getX() - .5, -pos.getY() - .5, -pos.getZ() - .5);
            BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            dispatcher.getBlockModelRenderer().renderModel(entity.world, dispatcher.getModelForState(state), state, pos, buffer, false);
            buffer.setTranslation(0, 0, 0);
            Tessellator.getInstance().draw();
        } finally {
            buffer.setTranslation(0, 0, 0);
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
        super.doRender(entity, x, y, z, yaw, partialTicks);
    }
    @Override protected ResourceLocation getEntityTexture(EntityFallingBlockNT entity) { return TextureMap.LOCATION_BLOCKS_TEXTURE; }
}
