package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.network.CableDiode;
import com.hbm.blocks.network.CranePartitioner;
import com.hbm.render.block.RenderPartitioner;
import com.hbm.render.tileentity.NetworkBreakingRenderer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import com.hbm.render.block.nativeblock.AtlasObjBakedModel;
import com.hbm.render.block.nativeblock.DiodeBakedModel;
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
public final class DiodeNetworkModelHandler {
    private static final String PREFIX = "native_diode_network/";
    private static boolean registered;
    private DiodeNetworkModelHandler() { }
    public static String owner(Block block) {
        if(CableDiode.isNativeModelOwner(block)) return "diode";
        if(CranePartitioner.isNativeModelOwner(block)) return "partitioner";
        return null;
    }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) {
            ModelLoaderRegistry.registerLoader(new Loader());
            ClientRegistry.bindTileEntitySpecialRenderer(CableDiode.TileEntityDiode.class, new NetworkBreakingRenderer<CableDiode.TileEntityDiode>(true));
            ClientRegistry.bindTileEntitySpecialRenderer(CranePartitioner.TileEntityCranePartitioner.class, new NetworkBreakingRenderer<CranePartitioner.TileEntityCranePartitioner>(false));
            registered = true;
        }
        for(final Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            final String kind = owner(block); if(kind == null) continue;
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block) throw new IllegalStateException("Missing network ItemBlock: " + block.getRegistryName());
            ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
                @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return route(kind, block.getMetaFromState(state), false);
                }
            });
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata, route(kind, metadata, true));
        }
    }
    private static ModelResourceLocation route(String kind, int metadata, boolean item) {
        return new ModelResourceLocation("hbm:" + PREFIX + kind + "/" + metadata + "/" + (item ? "item" : "world"), item ? "inventory" : "normal");
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
            if(fields.length != 3 || !(fields[0].equals("diode") || fields[0].equals("partitioner")) || !(fields[2].equals("item") || fields[2].equals("world"))) return null;
            try { int metadata = Integer.parseInt(fields[1]); if(metadata < 0 || metadata > 15) return null; } catch(NumberFormatException e) { return null; }
            return fields;
        }
        @Override public boolean accepts(ResourceLocation location) { return fields(location) != null; }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            String[] fields = fields(location); if(fields == null) throw new IllegalArgumentException("Invalid network model route: " + location);
            String asset = fields[0].equals("diode") ? "cable_neo" : "crane_buffer";
            ObjMesh mesh = meshes.get(asset);
            if(mesh == null) {
                ResourceLocation resource = new ResourceLocation("hbm", "models/blocks/" + asset + ".obj");
                try(IResource input = resources.getResource(resource); InputStreamReader reader = new InputStreamReader(input.getInputStream(), StandardCharsets.UTF_8)) {
                    mesh = ObjMeshParser.parse(reader, resource.toString(), false);
                }
                meshes.put(asset, mesh);
            }
            return new Model(mesh, fields[0].equals("diode"), Integer.parseInt(fields[1]), fields[2].equals("item"));
        }
    }
    private static final class Model implements IModel {
        private final ObjMesh mesh; private final boolean diode, item; private final int metadata;
        private final List<ResourceLocation> textures = new ArrayList<>();
        Model(ObjMesh mesh, boolean diode, int metadata, boolean item) {
            this.mesh = mesh; this.diode = diode; this.metadata = metadata; this.item = item;
            String[] names = diode ? new String[] {"cable_diode", "hadron_coil_alloy", "cable_neo"}
                    : new String[] {"crane_partitioner_side", "crane_partitioner_back", "crane_top", "crane_splitter_inner", "crane_splitter_inner_side", "crane_splitter_belt"};
            for(String name : names) textures.add(new ResourceLocation("hbm", "blocks/" + name));
        }
        @Override public Collection<ResourceLocation> getTextures() { return Collections.unmodifiableList(textures); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            TextureAtlasSprite[] atlas = new TextureAtlasSprite[textures.size()]; for(int i = 0; i < atlas.length; i++) atlas[i] = sprites.apply(textures.get(i));
            return diode ? new DiodeBakedModel(mesh, metadata, item, atlas) : new AtlasObjBakedModel(RenderPartitioner.bake(mesh, metadata, item, atlas), atlas[0]);
        }
    }
}
