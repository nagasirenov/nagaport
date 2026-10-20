package com.hbm.render.util;

import java.util.HashSet;
import java.util.Set;
import com.hbm.blocks.BlockBase;
import com.hbm.render.icon.IIcon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraft.util.ResourceLocation;

/** The rubble mesh uses the original side-zero material, not a particle texture. */
public final class NativeBlockMaterial {
    private static final Set<String> UNRESOLVED = new HashSet<>();
    private NativeBlockMaterial() { }

    public static ResourceLocation texture(Block block, int metadata) {
        if(block == net.minecraft.init.Blocks.AIR) return texturePath("minecraft:missingno", false);
        if(block instanceof BlockBase) {
            IIcon icon = ((BlockBase)block).getIcon(0, metadata);
            // RenderBlocks' safe-icon lookup used the missing icon when null.
            return texturePath(icon == null ? "minecraft:missingno" : icon.getIconName(), false);
        }
        IBlockState state = block.getStateFromMeta(metadata);
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
        if(state.getRenderType() != net.minecraft.util.EnumBlockRenderType.MODEL
                || model == Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getMissingModel()) model = null;
        String sprite = model == null ? null : bottomSprite(model, state);
        // Original BlockFlower.getIcon(side, meta) ignores side. These two exact
        // vanilla owners may therefore use their one real cross-model material;
        // this is not a particle-texture policy for arbitrary bushes/models.
        if(block == net.minecraft.init.Blocks.YELLOW_FLOWER || block == net.minecraft.init.Blocks.RED_FLOWER) {
            if(metadata < 0) throw new IllegalArgumentException("Negative original flower metadata");
            sprite = model == null ? null : flowerSprite(model, state);
        }
        if(sprite != null) return texturePath(sprite, true);
        String identity = block.getRegistryName() + "@" + metadata;
        if(UNRESOLVED.add(identity)) org.apache.logging.log4j.LogManager.getLogger("hbm-rubble").warn(
                "Rubble {} has no unique native downward material. Original render-error boundary retained; no particle-texture substitution is approved.", identity);
        // RenderRubble owns the original local catch boundary. This is an explicit
        // unresolved compatibility gate, not a claim that invisible rubble is parity.
        throw new IllegalArgumentException("Ambiguous or absent rubble bottom material: " + identity);
    }

    public static String bottomSprite(IBakedModel model, IBlockState state) {
        return faceSprite(model, state, EnumFacing.DOWN);
    }

    /** A unique face material only; texture-path policy belongs to the consumer. */
    public static String faceSprite(IBakedModel model, IBlockState state, EnumFacing side) {
        if(side == null) throw new IllegalArgumentException("A concrete face is required");
        if(model == null || model.isBuiltInRenderer()) return null;
        Set<String> materials = new HashSet<>();
        for(BakedQuad quad : model.getQuads(state, side, 0))
            if(geometricFace(quad) == side && quad.getSprite() != null) materials.add(quad.getSprite().getIconName());
        for(BakedQuad quad : model.getQuads(state, null, 0))
            if(geometricFace(quad) == side && quad.getSprite() != null)
                materials.add(quad.getSprite().getIconName());
        return materials.size() == 1 ? materials.iterator().next() : null;
    }

    private static String flowerSprite(IBakedModel model, IBlockState state) {
        if(model == null || model.isBuiltInRenderer()) return null;
        Set<String> materials = new HashSet<>();
        for(EnumFacing side : EnumFacing.values()) for(BakedQuad quad : model.getQuads(state, side, 0))
            if(quad.getSprite() != null) materials.add(quad.getSprite().getIconName());
        for(BakedQuad quad : model.getQuads(state, null, 0))
            if(quad.getSprite() != null) materials.add(quad.getSprite().getIconName());
        return materials.size() == 1 ? materials.iterator().next() : null;
    }

    private static EnumFacing geometricFace(BakedQuad quad) {
        // Some native NTM world quads deliberately use a different getFace() only
        // to sample block-position light. Material selection must inspect geometry.
        VertexFormat format = quad.getFormat();
        int position = -1;
        for(int i = 0; i < format.getElementCount(); i++)
            if(format.getElement(i).getUsage() == VertexFormatElement.EnumUsage.POSITION) position = i;
        if(position < 0) return null;
        float[][] p = new float[4][4];
        for(int i = 0; i < 4; i++) LightUtil.unpack(quad.getVertexData(), p[i], format, i, position);
        for(int i = 1; i < 3; i++) {
            float ax = p[i][0]-p[0][0], ay = p[i][1]-p[0][1], az = p[i][2]-p[0][2];
            float bx = p[i+1][0]-p[0][0], by = p[i+1][1]-p[0][1], bz = p[i+1][2]-p[0][2];
            float nx = ay*bz-az*by, ny = az*bx-ax*bz, nz = ax*by-ay*bx;
            if(nx*nx+ny*ny+nz*nz > 1.0E-12F) return EnumFacing.getFacingFromVector(nx, ny, nz);
        }
        return null;
    }

    public static ResourceLocation texturePath(String name, boolean nativeSprite) {
        if(name == null || name.isEmpty()) name = nativeSprite ? "minecraft:blocks/stone" : "minecraft:stone";
        ResourceLocation icon = new ResourceLocation(name);
        String path = icon.getPath();
        // Native sprites carry their blocks/ folder. Legacy icon names did not.
        if(!nativeSprite) path = "blocks/" + path;
        return new ResourceLocation(icon.getNamespace(), "textures/" + path + ".png");
    }
}
