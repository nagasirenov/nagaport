package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import com.google.common.collect.ImmutableList;
import com.hbm.blocks.machine.SolarMirror;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.SolarMirrorBakedModel;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Exact original owner, atlas material, and flat-texture inventory path. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class SolarMirrorModelHandler {
    private SolarMirrorModelHandler() { }
    private static final ResourceLocation OWNER = new ResourceLocation(RefStrings.MODID, "tile.solar_mirror");
    private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID, "blocks/solar_mirror");
    private static final String PREFIX = "native_solar_mirror/";
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(block.getClass() != SolarMirror.class || !OWNER.equals(block.getRegistryName())) continue;
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing solar mirror ItemBlock " + OWNER);
            ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
                @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return new ModelResourceLocation(OWNER, "normal");
                }
            });
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata,
                    new ModelResourceLocation(RefStrings.MODID + ":" + PREFIX + "item", "inventory"));
        }
    }
    private static String path(ResourceLocation location) {
        String path = location.getPath();
        if(path.startsWith("models/")) path = path.substring(7);
        if(path.startsWith("block/")) path = path.substring(6);
        return path;
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private ObjMesh mesh;
        @Override public void onResourceManagerReload(IResourceManager manager) { resources = manager; mesh = null; }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace())
                    && (path(location).equals(PREFIX + "world") || path(location).equals(PREFIX + "item"));
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            if(!accepts(location)) throw new IllegalArgumentException("Unknown solar mirror route " + location);
            if(path(location).endsWith("/item")) return new ItemLayerModel(ImmutableList.of(TEXTURE));
            if(mesh == null) {
                ResourceLocation asset = new ResourceLocation(RefStrings.MODID, "models/machines/solar_mirror.obj");
                try(IResource resource = resources.getResource(asset);
                        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    mesh = ObjMeshParser.parse(reader, asset.toString(), false);
                }
            }
            return new Model(mesh);
        }
    }
    private static final class Model implements IModel {
        private final ObjMesh mesh;
        Model(ObjMesh mesh) { this.mesh = mesh; }
        @Override public Collection<ResourceLocation> getTextures() { return Collections.singletonList(TEXTURE); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            return new SolarMirrorBakedModel(mesh, sprites.apply(TEXTURE));
        }
    }
}
