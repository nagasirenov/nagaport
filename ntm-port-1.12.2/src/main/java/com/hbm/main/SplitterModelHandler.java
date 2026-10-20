package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import com.hbm.render.block.nativeblock.AtlasObjBakedModel;
import com.hbm.blocks.network.CraneSplitter;
import com.hbm.tileentity.network.TileEntityCraneSplitter;
import com.hbm.render.block.RenderSplitter;
import com.hbm.render.tileentity.SplitterBreakingRenderer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.*;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Narrow ownership: exact original block classes and registry identities only. */
@Mod.EventBusSubscriber(modid = "hbm", value = Side.CLIENT)
public final class SplitterModelHandler {
    private static final String PREFIX = "native_splitter/";
    private static boolean registered;
    private SplitterModelHandler() { }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) {
            ModelLoaderRegistry.registerLoader(new Loader());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCraneSplitter.class, new SplitterBreakingRenderer());
            registered = true;
        }
        for(final Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!CraneSplitter.isNativeModelOwner(block)) continue;
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block) throw new IllegalStateException("Missing splitter ItemBlock: " + block.getRegistryName());
            ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
                @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return route(block.getMetaFromState(state), false);
                }
            });
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata, route(metadata, true));
        }
    }
    private static ModelResourceLocation route(int metadata, boolean item) {
        return new ModelResourceLocation("hbm:" + PREFIX + metadata + "/" + (item ? "item" : "world"), item ? "inventory" : "normal");
    }
    public static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private final Map<String, ObjMesh> meshes = new HashMap<>();
        @Override public void onResourceManagerReload(IResourceManager manager) { resources = manager; meshes.clear(); }
        private static String path(ResourceLocation location) {
            String p = location.getPath(); if(p.startsWith("models/")) p = p.substring(7); if(p.startsWith("block/")) p = p.substring(6); return p;
        }
        private static String[] fields(ResourceLocation location) {
            if(!"hbm".equals(location.getNamespace()) || !path(location).startsWith(PREFIX)) return null;
            String[] fields = path(location).substring(PREFIX.length()).split("/", -1);
            if(fields.length != 2 || !(fields[1].equals("item") || fields[1].equals("world"))) return null;
            try { int metadata = Integer.parseInt(fields[0]); if(metadata < 0 || metadata > 15) return null; } catch(NumberFormatException e) { return null; }
            return fields;
        }
        @Override public boolean accepts(ResourceLocation location) { return fields(location) != null; }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            String[] fields = fields(location); if(fields == null) throw new IllegalArgumentException("Invalid splitter model route: " + location);
            String asset = "splitter";
            ObjMesh mesh = meshes.get(asset);
            if(mesh == null) {
                ResourceLocation resource = new ResourceLocation("hbm", "models/blocks/" + asset + ".obj");
                try(IResource input = resources.getResource(resource); InputStreamReader reader = new InputStreamReader(input.getInputStream(), StandardCharsets.UTF_8)) {
                    mesh = ObjMeshParser.parse(reader, resource.toString(), false);
                }
                meshes.put(asset, mesh);
            }
            return new Model(mesh, Integer.parseInt(fields[0]), fields[1].equals("item"));
        }
    }
    private static final class Model implements IModel {
        private final ObjMesh mesh; private final boolean item; private final int metadata;
        private static final ResourceLocation PARTICLE = new ResourceLocation("hbm", "blocks/crane_side");
        private final List<ResourceLocation> textures = new ArrayList<>();
        Model(ObjMesh mesh, int metadata, boolean item) {
            this.mesh = mesh; this.metadata = metadata; this.item = item;
            String[] names = {"crane_splitter_top_left", "crane_splitter_top_right", "crane_splitter_front_left", "crane_splitter_front_right", "crane_splitter_back_left", "crane_splitter_back_right", "crane_splitter_left", "crane_splitter_right", "crane_splitter_belt", "crane_splitter_inner", "crane_splitter_inner_side"};
            for(String name : names) textures.add(new ResourceLocation("hbm", "blocks/" + name));
            textures.add(PARTICLE); // Original superclass block icon was crane_side; not a twelfth OBJ material.
        }
        @Override public Collection<ResourceLocation> getTextures() { return Collections.unmodifiableList(textures); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            TextureAtlasSprite[] atlas = new TextureAtlasSprite[11]; for(int i = 0; i < atlas.length; i++) atlas[i] = sprites.apply(textures.get(i));
            return new AtlasObjBakedModel(RenderSplitter.bake(mesh, metadata, item, atlas), sprites.apply(PARTICLE));
        }
    }
}
