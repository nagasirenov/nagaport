package com.hbm.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.lwjgl.util.vector.Vector3f;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDynamicSlag;
import com.hbm.lib.RefStrings;
import com.hbm.render.item.nativeitem.NativeItemParticles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** 1.7.10 slag: standard block renderer with bounds from the amount, per-material icon and molten tint. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class SlagModelHandler {
    private static final String ROUTE = "native_slag/world";
    private static final ModelResourceLocation WORLD = new ModelResourceLocation(RefStrings.MODID + ":" + ROUTE, "normal");
    private static boolean registered;

    private SlagModelHandler() { }

    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(new Loader()); registered = true; }
        ModelLoader.setCustomStateMapper(ModBlocks.slag, new StateMapperBase() {
            @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) { return WORLD; }
        });
    }

    public static void registerColorHandlers() {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, world, pos, tint) ->
                world == null || pos == null ? 0xFFFFFF
                        : ((BlockDynamicSlag) ModBlocks.slag).colorMultiplier(world, pos.getX(), pos.getY(), pos.getZ()),
                ModBlocks.slag);
    }

    private static String path(ResourceLocation location) {
        String path = location.getPath();
        if(path.startsWith("models/")) path = path.substring(7);
        if(path.startsWith("block/")) path = path.substring(6);
        return path;
    }

    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager resources) { }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && path(location).equals(ROUTE);
        }
        @Override public IModel loadModel(ResourceLocation location) {
            if(!accepts(location)) throw new IllegalArgumentException("Unknown slag route " + location);
            return new IModel() {
                @Override public Collection<ResourceLocation> getTextures() { return Collections.emptyList(); }
                @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
                    return new Baked();
                }
            };
        }
    }

    private static final class Baked implements IBakedModel {
        private static final FaceBakery BAKERY = new FaceBakery();
        /** Quads per (sprite, height in 1/1000 block); slag heights are coarse, so this stays small. */
        private final Map<String, Map<EnumFacing, List<BakedQuad>>> cache = new ConcurrentHashMap<>();

        private static TextureAtlasSprite sprite(com.hbm.render.icon.IIcon icon) {
            TextureAtlasSprite sprite = NativeItemParticles.spriteOf(icon, "SlagModelHandler");
            if(sprite != null) return sprite;
            NativeStateFallbackHandler.warnMissingSprite("hbm:tile.slag: " + (icon == null ? "null icon" : icon.getIconName()));
            return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }

        @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
            BlockDynamicSlag.RenderSnapshot snap = BlockDynamicSlag.RenderSnapshot.of(state);
            TextureAtlasSprite sprite = sprite(((BlockDynamicSlag) ModBlocks.slag).iconFor(snap.mat));
            int height = Math.max(1, Math.min(1000, Math.round(snap.height * 1000F)));
            Map<EnumFacing, List<BakedQuad>> faces = cache.computeIfAbsent(sprite.getIconName() + "@" + height, key -> bake(sprite, height / 1000F));
            List<BakedQuad> quads = faces.get(side);
            return quads == null ? Collections.emptyList() : quads;
        }

        /** Key null holds faces that must not be culled (a partial top). */
        private static Map<EnumFacing, List<BakedQuad>> bake(TextureAtlasSprite sprite, float height) {
            Map<EnumFacing, List<BakedQuad>> faces = new java.util.HashMap<>();
            for(EnumFacing facing : EnumFacing.values()) {
                EnumFacing cull = facing == EnumFacing.UP && height < 1F ? null : facing;
                // Vanilla default UVs: side faces sample the lower part of the texture, like 1.7.10 bounds.
                float[] uv = facing.getAxis() == EnumFacing.Axis.Y ? new float[] {0, 0, 16, 16}
                        : new float[] {0, 16F - height * 16F, 16, 16};
                BlockPartFace part = new BlockPartFace(cull, 0, "", new BlockFaceUV(uv, 0));
                BakedQuad quad = BAKERY.makeBakedQuad(new Vector3f(0, 0, 0), new Vector3f(16, height * 16F, 16),
                        part, sprite, facing, ModelRotation.X0_Y0, null, false, true);
                faces.computeIfAbsent(cull, key -> new ArrayList<>()).add(quad);
            }
            return faces;
        }

        @Override public boolean isAmbientOcclusion() { return true; }
        @Override public boolean isGui3d() { return true; }
        @Override public boolean isBuiltInRenderer() { return false; }
        @Override public TextureAtlasSprite getParticleTexture() { return sprite(((BlockDynamicSlag) ModBlocks.slag).legacyIcon()); }
        @Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
        @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
    }
}
