package com.hbm.render.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.BakedItemModel;
import java.util.WeakHashMap;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Items physically displayed by machines, not GUI overlays or dropped stacks.
 * Decoration and normal entity previews retain their distinct count/bob/spread.
 * Original frame-mode flat-icon offsets and pixel extrusion are explicit; no removed global RenderItem.renderInFrame switch.
 */
public final class NativeMachineItemRenderer {
    private static final ResourceLocation GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private final WeakHashMap<IBakedModel, Optional<List<BakedQuad>>> icons = new WeakHashMap<>();
    /** The old RenderDecoItem suppressed bob/spread/count; RenderManager did not. */
    public enum Display { DECORATION_FRAME, ENTITY_FRAME }
    /** A known native item model may retain its own original block-helper frame draw. */
    public interface FrameItemModel {
        void renderFrame(EntityItem entity, double x, double y, double z, float yaw, float partialTicks, Display display);
    }

    /** Context-selective wrappers may decline the entity callback and retain the
     * complete native base model for ordinary machine-frame count/icon behavior. */
    public interface FrameBaseModel {
        IBakedModel machineFrameModel(ItemStack stack);
    }

    public void renderFrame(EntityItem entity, double x, double y, double z,
            float yaw, float partialTicks, Display display) {
        ItemStack stack = entity.getItem();
        if(stack.isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem renderer = mc.getRenderItem(); // never cache the pre-load-complete owner
        IBakedModel model = renderer.getItemModelWithOverrides(stack, entity.world, null);
        if(model instanceof FrameBaseModel) model = ((FrameBaseModel)model).machineFrameModel(stack);
        if(model instanceof FrameItemModel) {
            ((FrameItemModel)model).renderFrame(entity, x, y, z, yaw, partialTicks, display);
            return;
        }
        List<BakedQuad> layers = cachedIconLayers(model);
        boolean cube = !model.isBuiltInRenderer() && model.isGui3d() && stack.getItem() instanceof net.minecraft.item.ItemBlock;
        if(!cube && layers == null) {
            // NTM's complete custom models already compensate native GROUND caller
            // transforms. Their old non-BLOCK_3D callback drew one copy, not a pile.
            // Unknown external models retain their complete native renderer; no
            // particle-sprite flattening or unsupported legacy-parity assertion.
            RenderEntityItem custom = new OneItemRenderer(mc.getRenderManager(), renderer, display == Display.ENTITY_FRAME);
            custom.doRender(entity, x, y, z, yaw, partialTicks);
            return;
        }
        boolean decoration = display == Display.DECORATION_FRAME;
        float bob = decoration ? 0 : net.minecraft.util.math.MathHelper.sin((entity.getAge() + partialTicks) / 10F + entity.hoverStart) * .1F + .1F;
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        ITextureObject texture = mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        texture.setBlurMipmap(false, false);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate((float)x, (float)y + bob, (float)z);
            GlStateManager.enableRescaleNormal();
            java.util.Random random = new java.util.Random(187L);
            if(cube) {
                boolean translucent = ((net.minecraft.item.ItemBlock)stack.getItem()).getBlock().getRenderLayer() == net.minecraft.util.BlockRenderLayer.TRANSLUCENT;
                if(translucent) {
                    GlStateManager.alphaFunc(GL11.GL_GREATER, .1F);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                }
                applyBlockFrame(entity.getAge(), partialTicks, entity.hoverStart);
                int copies = decoration ? 1 : blockCount(stack.getCount());
                for(int i = 0; i < copies; i++) {
                    GlStateManager.pushMatrix();
                    try {
                        if(i > 0) GlStateManager.translate(spread(random, .2F) / .25F, spread(random, .2F) / .25F, spread(random, .2F) / .25F);
                        drawModel(mc, model, stack);
                    } finally { GlStateManager.popMatrix(); }
                }
                if(translucent) GlStateManager.disableBlend();
            } else {
                boolean fancy = mc.gameSettings.fancyGraphics;
                GlStateManager.scale(.5128205F, .5128205F, .5128205F);
                GlStateManager.translate(0, -.05F, 0);
                int copies = decoration ? 1 : fancy ? iconCount(stack.getCount()) : blockCount(stack.getCount());
                for(BakedQuad layer : layers) {
                    random.setSeed(187L); // original multipass spread repeats per layer
                    GlStateManager.pushMatrix();
                    try {
                        if(fancy) {
                            GlStateManager.rotate(180, 0, 1, 0);
                            GlStateManager.translate(-.5F, -.25F, -(.0625F + .021875F) * copies / 2F);
                        }
                        for(int i = 0; i < copies; i++) {
                            if(fancy) {
                                if(i > 0 && !decoration) {
                                    float dx = spread(random, .3F) / .5F;
                                    float dy = spread(random, .3F) / .5F;
                                    spread(random, .3F); // Forge consumes Z even though its XY spread only uses fixed Z
                                    GlStateManager.translate(dx, dy, .0625F + .021875F);
                                } else GlStateManager.translate(0, 0, .0625F + .021875F);
                            }
                            GlStateManager.pushMatrix();
                            try {
                                if(!fancy && i > 0) GlStateManager.translate(spread(random, .3F), spread(random, .3F), spread(random, .3F));
                                TextureAtlasSprite sprite = layer.getSprite();
                                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                                int tint = layer.hasTintIndex() ? mc.getItemColors().colorMultiplier(stack, layer.getTintIndex()) : 0xFFFFFF;
                                GlStateManager.color((tint >> 16 & 255) / 255F, (tint >> 8 & 255) / 255F, (tint & 255) / 255F, 1F);
                                if(fancy) {
                                    NativeIconExtrusion.draw(sprite.getMaxU(), sprite.getMinV(), sprite.getMinU(), sprite.getMaxV(), sprite.getIconWidth(), sprite.getIconHeight(), .0625F);
                                    if(stack.hasEffect()) drawGlint(mc);
                                } else drawFlat(sprite);
                            } finally { GlStateManager.popMatrix(); }
                        }
                    } finally { GlStateManager.popMatrix(); }
                }
            }
        } finally {
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
            mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            texture.restoreLastBlurMipmap();
        }
    }
    private static float spread(java.util.Random random, float scale) { return (random.nextFloat() * 2F - 1F) * scale; }
    public static int blockCount(int count) { return count > 40 ? 5 : count > 20 ? 4 : count > 5 ? 3 : count > 1 ? 2 : 1; }
    public static int iconCount(int count) { return count < 2 ? 1 : count < 16 ? 2 : count < 32 ? 3 : 4; }
    static void drawModel(Minecraft mc, IBakedModel model, ItemStack stack) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        NativeItemQuadRenderer.appendModel(buffer, model, stack, mc.getItemColors());
        Tessellator.getInstance().draw();
    }
    /** Native representation of the original ordinary ItemBlock 3-D branch. */
    public static boolean isBlockModel(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.ItemBlock
                && NativeGuiItemDraw.baseModel(Minecraft.getMinecraft().getRenderItem(), stack).isGui3d();
    }

    // Native baked item models are immutable. Weak keys let resource reloads release
    // old models; unknown/custom model implementations retain native rendering.
    private List<BakedQuad> cachedIconLayers(IBakedModel model) {
        if(model.getClass() != BakedItemModel.class) return null;
        Optional<List<BakedQuad>> cached = icons.get(model);
        if(cached == null) {
            List<BakedQuad> found = iconLayers(model);
            cached = found == null ? Optional.empty() : Optional.of(Collections.unmodifiableList(found));
            icons.put(model, cached);
        }
        return cached.orElse(null);
    }
    public static void applyBlockFrame(int age, float partialTicks, float hoverStart) {
        GlStateManager.rotate(((age + partialTicks) / 20F + hoverStart) * 57.295776F, 0, 1, 0);
        GlStateManager.scale(1.25F, 1.25F, 1.25F);
        GlStateManager.translate(0F, .05F, 0F);
        GlStateManager.rotate(-90F, 0, 1, 0);
        GlStateManager.scale(.25F, .25F, .25F);
        GlStateManager.translate(-.5F, -.5F, -.5F);
    }
    public static void applyIconFrame(boolean fancy) {
        GlStateManager.scale(.5128205F, .5128205F, .5128205F);
        GlStateManager.translate(0F, -.05F, 0F);
        if(fancy) {
            GlStateManager.rotate(180F, 0, 1, 0);
            GlStateManager.translate(-.5F, -.25F, -(.0625F + .021875F) / 2F);
            GlStateManager.translate(0F, 0F, .0625F + .021875F);
        }
    }

    public static final class OneItemRenderer extends RenderEntityItem {
        private final boolean bob;
        public OneItemRenderer(RenderManager manager, RenderItem renderer, boolean bob) { super(manager, renderer); this.bob = bob; }
        @Override protected int getModelCount(ItemStack stack) { return 1; }
        @Override public boolean shouldBob() { return bob; }
        @Override public boolean shouldSpreadItems() { return false; }
    }

    /** Recognize full generated icon fronts, never flatten arbitrary custom geometry. */
    public static List<BakedQuad> iconLayers(IBakedModel model) {
        if(model.isBuiltInRenderer() || model.isGui3d()) return null;
        List<BakedQuad> all = new ArrayList<>();
        for(EnumFacing face : EnumFacing.values()) all.addAll(model.getQuads(null, face, 0L));
        all.addAll(model.getQuads(null, null, 0L));
        List<BakedQuad> fronts = new ArrayList<>();
        for(BakedQuad quad : all) if(quad.getFace() == EnumFacing.SOUTH) fronts.add(quad);
        if(fronts.isEmpty()) return null;
        List<BakedQuad> remaining = new ArrayList<>(all);
        for(BakedQuad front : fronts) {
            if(!unitIconFront(front)) return null;
            // Verify every side/back as well as the front. A custom model with a
            // generated-looking front must not lose its other geometry or UVs.
            for(BakedQuad expected : ItemLayerModel.getQuadsForSprite(front.getTintIndex(),
                    front.getSprite(), DefaultVertexFormats.ITEM, Optional.empty())) {
                int found = -1;
                for(int i=0; i<remaining.size(); i++) if(sameQuad(expected, remaining.get(i))) { found=i; break; }
                if(found < 0) return null;
                remaining.remove(found);
            }
        }
        return remaining.isEmpty() ? fronts : null;
    }
    private static boolean sameQuad(BakedQuad a, BakedQuad b) {
        return a.getFace()==b.getFace() && a.getTintIndex()==b.getTintIndex()
                && a.getSprite()==b.getSprite() && a.shouldApplyDiffuseLighting()==b.shouldApplyDiffuseLighting()
                && a.getFormat().equals(b.getFormat()) && Arrays.equals(a.getVertexData(),b.getVertexData());
    }
    private static boolean unitIconFront(BakedQuad quad) {
        if(quad.getSprite() == null || !quad.getFormat().equals(DefaultVertexFormats.ITEM)) return false;
        int[] vertices = quad.getVertexData();
        if(vertices.length != 28) return false;
        int corners = 0;
        for(int i=0; i<4; i++) {
            float x=Float.intBitsToFloat(vertices[i*7]), y=Float.intBitsToFloat(vertices[i*7+1]), z=Float.intBitsToFloat(vertices[i*7+2]);
            if((x!=0F && x!=1F) || (y!=0F && y!=1F) || z!=.53125F) return false;
            corners |= 1 << ((x==1F ? 1 : 0) + (y==1F ? 2 : 0));
        }
        return corners == 15;
    }
    private static void drawFlat(TextureAtlasSprite sprite) {
        BufferBuilder b=Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
        b.pos(-.5F,-.25F,0).tex(sprite.getMinU(),sprite.getMaxV()).normal(0,1,0).endVertex();
        b.pos(.5F,-.25F,0).tex(sprite.getMaxU(),sprite.getMaxV()).normal(0,1,0).endVertex();
        b.pos(.5F,.75F,0).tex(sprite.getMaxU(),sprite.getMinV()).normal(0,1,0).endVertex();
        b.pos(-.5F,.75F,0).tex(sprite.getMinU(),sprite.getMinV()).normal(0,1,0).endVertex();
        Tessellator.getInstance().draw();
    }
    private static void drawGlint(Minecraft mc) {
        GlStateManager.depthFunc(GL11.GL_EQUAL);
        GlStateManager.disableLighting();
        mc.getTextureManager().bindTexture(GLINT);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
        GlStateManager.color(.5F*.76F,.25F*.76F,.8F*.76F,1F);
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        for(int pass=0; pass<2; pass++) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(.125F,.125F,.125F);
            long period = pass==0 ? 3000L : 4873L;
            float offset=(float)(Minecraft.getSystemTime()%period)/(float)period*8F;
            GlStateManager.translate(pass==0 ? offset : -offset,0,0);
            GlStateManager.rotate(pass==0 ? -50F : 10F,0,0,1);
            NativeIconExtrusion.draw(0,0,1,1,255,255,.0625F);
            GlStateManager.popMatrix();
        }
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
    }
}
