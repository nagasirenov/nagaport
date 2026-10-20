package com.hbm.render.tileentity;

import java.nio.ByteBuffer;
import java.util.*;
import com.hbm.blocks.network.CableDiode;
import com.hbm.blocks.network.CranePartitioner;
import com.hbm.render.block.nativeblock.AtlasObjBakedModel;
import com.hbm.render.block.nativeblock.DiodeBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/** Breaking-only native TESR. Ordinary geometry stays in the immutable chunk/item models. */
public class NetworkBreakingRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T> {
    private final boolean diode;
    public NetworkBreakingRenderer(boolean diode) { this.diode = diode; }
    public boolean owns(TileEntity tile, IBlockState state) {
        if(tile == null || state == null) return false;
        Block block = state.getBlock();
        return diode ? tile.getClass() == CableDiode.TileEntityDiode.class && CableDiode.isNativeModelOwner(block)
                : tile.getClass() == CranePartitioner.TileEntityCranePartitioner.class && CranePartitioner.isNativeModelOwner(block);
    }
    @Override public void render(T tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        // The dispatcher also calls us during ordinary tile rendering. No model, world or GL work then.
        if(destroyStage < 0 || destroyStage > 9 || tile == null || tile.getWorld() == null) return;
        BlockPos pos = tile.getPos();
        IBlockState state = tile.getWorld().getBlockState(pos);
        if(!owns(tile, state)) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        IBakedModel model = minecraft.getBlockRendererDispatcher().getModelForState(state);
        if(diode && !(model instanceof DiodeBakedModel)) throw new IllegalStateException("Missing native diode breaking model");
        IBlockState extended = state.getBlock().getExtendedState(state, tile.getWorld(), pos);
        List<BakedQuad> quads = diode ? ((DiodeBakedModel)model).getAllQuads(extended) : model.getQuads(extended, null, 0);
        TextureAtlasSprite crack = minecraft.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage);
        BufferBuilder buffer = buildBuffer(tile.getWorld(), state, pos, quads, crack, diode,
                Minecraft.isAmbientOcclusionEnabled(), minecraft.getBlockRendererDispatcher().getBlockModelRenderer(), x - pos.getX(), y - pos.getY(), z - pos.getZ());
        draw(buffer, minecraft.getTextureMapBlocks());
    }
    /** Preserve original plate override, then coil override, then clear-before-OBJ semantics. */
    public static List<BakedQuad> breakingQuads(List<BakedQuad> original, TextureAtlasSprite crack, boolean diode) {
        List<BakedQuad> result = new ArrayList<>(original);
        if(diode) {
            if(result.size() < 12) throw new IllegalArgumentException("Diode must contain its two original cuboids");
            for(int i = 0; i < 6; i++) result.set(i, new BakedQuadRetextured(result.get(i), crack));
        }
        return Collections.unmodifiableList(result);
    }
    /**
     * Native smooth/flat lighting is evaluated before dropping COLOR. The original 1.7 damage
     * dispatcher disabled Tessellator colors, so neither cube AO RGB nor OBJ shade RGB belonged
     * in the damage upload. Lightmap, texture, geometry and standard-cube AO light remain.
     */
    public static BufferBuilder buildBuffer(IBlockAccess world, IBlockState state, BlockPos pos,
            List<BakedQuad> original, TextureAtlasSprite crack, boolean diode, boolean ambient,
            BlockModelRenderer renderer, double offsetX, double offsetY, double offsetZ) {
        List<BakedQuad> quads = breakingQuads(original, crack, diode);
        BufferBuilder source = new BufferBuilder(65536);
        source.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        source.setTranslation(offsetX, offsetY, offsetZ);
        if(diode) {
            IBakedModel cubes = new AtlasObjBakedModel(quads.subList(0, 12), quads.get(0).getSprite());
            if(ambient) renderer.renderModelSmooth(world, cubes, state, pos, source, true, 0);
            else renderer.renderModelFlat(world, cubes, state, pos, source, true, 0);
        }
        int start = diode ? 12 : 0;
        if(quads.size() > start) {
            IBakedModel arms = new AtlasObjBakedModel(quads.subList(start, quads.size()), quads.get(start).getSprite());
            renderer.renderModelFlat(world, arms, state, pos, source, true, 0);
        }
        source.finishDrawing();
        VertexFormat format = new VertexFormat().addElement(DefaultVertexFormats.POSITION_3F)
                .addElement(DefaultVertexFormats.TEX_2F).addElement(DefaultVertexFormats.TEX_2S);
        BufferBuilder output = new BufferBuilder(65536);
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
    /** GPU boundary only; all engine geometry/light decisions can also be exercised headlessly. */
    protected void draw(BufferBuilder buffer, TextureMap atlas) {
        // Raw GL changes are bracketed and restored without mutating GlStateManager's caches.
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);
        try {
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlas.getGlTextureId());
            GL11.glEnable(GL11.GL_BLEND);
            GL14.glBlendFuncSeparate(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR, GL11.GL_ONE, GL11.GL_ZERO);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, .1F);
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(-3F, -3F);
            GL11.glColor4f(1F, 1F, 1F, .5F);
            new WorldVertexBufferUploader().draw(buffer);
        } finally {
            GL11.glPopClientAttrib();
            GL11.glPopAttrib();
        }
    }
}
