package com.hbm.render.entity;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import com.hbm.render.model.LegacyBaseBakedModel;
import com.hbm.render.block.nativeblock.ConveyorCuboidGeometry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

/** Only the inventory geometry used by primed blocks and the carried spider block.
 * No item-camera, dropped-item, enchantment or TEISR pass belongs to these entities.
 * Unlike RenderItem, this deliberately leaves color out of the vertex layout: the
 * original brightness/tint and the TNT flash's fractional alpha are GL color state.
 */
public final class NativeBlockEntityDraw {
    private NativeBlockEntityDraw() { }

    public static void render(Block block, int metadata, float brightness, boolean tint) {
        // Default unsynchronized block ID zero was invisible in the original.
        if(block == net.minecraft.init.Blocks.AIR) return;
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack stack = new ItemStack(block, 1, metadata);
        IBakedModel model = mc.getRenderItem().getItemModelMesher().getItemModel(stack);
        if(model == mc.getRenderItem().getItemModelMesher().getModelManager().getMissingModel()
                || model.isBuiltInRenderer())
            throw new IllegalStateException("No baked block-entity inventory geometry for " + block.getRegistryName());
        IBlockState state = block.getStateFromMeta(metadata);
        // The old ordinary-block branch leaves this quarter-turn in the caller's
        // matrix. Audited NTM custom item models already contain their own original
        // rotations; applying this again would turn charges, barrels and cord twice.
        boolean ordinary = model instanceof LegacyBaseBakedModel
                || ("minecraft".equals(block.getRegistryName().getNamespace()) && state.isFullCube());
        if(ordinary) GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(-.5F, -.5F, -.5F);
        try {
            Set<BakedQuad> emitted = Collections.newSetFromMap(new IdentityHashMap<BakedQuad, Boolean>());
            for(EnumFacing face : EnumFacing.values())
                for(BakedQuad quad : model.getQuads(null, face, 0))
                    if(emitted.add(quad)) drawQuad(ordinary ? inventoryCubeFace(quad) : quad, state, brightness, tint);
            for(BakedQuad quad : model.getQuads(null, null, 0))
                if(emitted.add(quad)) drawQuad(ordinary ? inventoryCubeFace(quad) : quad, state, brightness, tint);
        } finally { GlStateManager.translate(.5F, .5F, .5F); }
    }

    private static BakedQuad inventoryCubeFace(BakedQuad quad) {
        // FaceBakery contracts UVs and reverses the old bottom V direction. Reuse
        // the already-audited original cuboid UV mapping, only for ordinary cubes.
        BakedQuad face = ConveyorCuboidGeometry.face(quad.getSprite(), true, quad.getFace(),
                0, 0, 0, 0, 1, 1, 1, 0);
        return new BakedQuad(face.getVertexData(), quad.getTintIndex(), face.getFace(),
                face.getSprite(), false, face.getFormat());
    }

    private static void drawQuad(BakedQuad quad, IBlockState state, float brightness, boolean tint) {
        if(tint) {
            int color = quad.hasTintIndex() ? Minecraft.getMinecraft().getBlockColors()
                    .colorMultiplier(state, null, null, quad.getTintIndex()) : 0xFFFFFF;
            GlStateManager.color((color >> 16 & 255) / 255F * brightness,
                    (color >> 8 & 255) / 255F * brightness, (color & 255) / 255F * brightness, 1);
        }
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
        appendQuad(buffer, quad);
        Tessellator.getInstance().draw();
    }

    /** Copy actual baked positions, atlas coordinates and normals without world AO,
     * baked diffuse color, lightmap or alpha replacing the caller's inventory color. */
    public static void appendQuad(BufferBuilder buffer, BakedQuad quad) {
        VertexFormat format = quad.getFormat();
        int position = -1, uv = -1, normal = -1;
        for(int e = 0; e < format.getElementCount(); e++) {
            VertexFormatElement element = format.getElement(e);
            if(element.getUsage() == VertexFormatElement.EnumUsage.POSITION) position = e;
            if(element.getUsage() == VertexFormatElement.EnumUsage.UV && element.getIndex() == 0) uv = e;
            if(element.getUsage() == VertexFormatElement.EnumUsage.NORMAL) normal = e;
        }
        if(position < 0 || uv < 0) throw new IllegalArgumentException("Block entity quad lacks positions/UVs");
        float[] p = new float[4], t = new float[4], n = new float[4];
        for(int v = 0; v < 4; v++) {
            LightUtil.unpack(quad.getVertexData(), p, format, v, position);
            LightUtil.unpack(quad.getVertexData(), t, format, v, uv);
            if(normal >= 0) LightUtil.unpack(quad.getVertexData(), n, format, v, normal);
            else { n[0] = quad.getFace().getXOffset(); n[1] = quad.getFace().getYOffset(); n[2] = quad.getFace().getZOffset(); }
            buffer.pos(p[0], p[1], p[2]).tex(t[0], t[1]).normal(n[0], n[1], n[2]).endVertex();
        }
    }
}
