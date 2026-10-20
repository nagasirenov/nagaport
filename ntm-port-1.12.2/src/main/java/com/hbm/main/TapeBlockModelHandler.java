package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.generic.DecoTapeRecorder;
import com.hbm.blocks.generic.DecoSteelPoles;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.RenderTapeBlock;
import com.hbm.render.block.nativeblock.AtlasObjBakedModel;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Native tape/pole state and item routes, with resource-reload mesh invalidation. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class TapeBlockModelHandler {
    private TapeBlockModelHandler() { }
    private static final String PREFIX = "native_tape/";
    private static final Map<String, Definition> DEFINITIONS = definitions();
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(block.getClass() != DecoTapeRecorder.class && block.getClass() != DecoSteelPoles.class) continue;
            ResourceLocation name = block.getRegistryName();
            if(name == null || !RefStrings.MODID.equals(name.getNamespace())) continue;
            Definition definition = DEFINITIONS.get(name.getPath());
            if(definition == null || block.getClass() != definition.owner) continue;
            // Blocks and their ItemBlocks have been flushed to the native registries before this event.
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing tape/pole OBJ ItemBlock " + name);
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata,
                    new ModelResourceLocation(RefStrings.MODID + ":" + PREFIX + name.getPath() + "/" + metadata + "/item", "inventory"));
        }
    }
    private static Map<String, Definition> definitions() {
        Map<String, Definition> result = new LinkedHashMap<>();
        result.put("tile.tape_recorder", new Definition(DecoTapeRecorder.class, "models/blocks/taperecorder.obj", "deco_tape_recorder"));
        result.put("tile.steel_poles", new Definition(DecoSteelPoles.class, "models/blocks/pole.obj", "steel_beam"));
        return Collections.unmodifiableMap(result);
    }
    private static final class Definition {
        final Class<? extends Block> owner;
        final String asset;
        final ResourceLocation texture;
        Definition(Class<? extends Block> owner, String asset, String texture) {
            this.owner = owner; this.asset = asset;
            this.texture = new ResourceLocation(RefStrings.MODID, "blocks/" + texture);
        }
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private final Map<String, ObjMesh> meshes = new HashMap<>();
        @Override public void onResourceManagerReload(IResourceManager resources) { this.resources = resources; meshes.clear(); }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && normalized(location).startsWith(PREFIX);
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            String[] fields = normalized(location).substring(PREFIX.length()).split("/");
            if(fields.length != 3 || !(fields[2].equals("item") || fields[2].equals("world")))
                throw new IllegalArgumentException("Invalid tape/pole OBJ route " + location);
            Definition definition = DEFINITIONS.get(fields[0]);
            if(definition == null) throw new IllegalArgumentException("Unknown tape/pole OBJ block " + fields[0]);
            int metadata = Integer.parseInt(fields[1]);
            if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Invalid tape/pole OBJ metadata " + metadata);
            return new DecorationModel(definition, mesh(definition.asset), metadata, fields[2].equals("item"));
        }
        private ObjMesh mesh(String name) throws Exception {
            ObjMesh mesh = meshes.get(name);
            if(mesh == null) {
                ResourceLocation asset = new ResourceLocation(RefStrings.MODID, name);
                try(IResource resource = resources.getResource(asset);
                        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    mesh = ObjMeshParser.parse(reader, asset.toString(), false);
                }
                meshes.put(name, mesh);
            }
            return mesh;
        }
        private static String normalized(ResourceLocation location) {
            String path = location.getPath();
            if(path.startsWith("models/")) path = path.substring(7);
            if(path.startsWith("block/")) path = path.substring(6);
            return path;
        }
    }
    private static final class DecorationModel implements IModel {
        private final Definition definition;
        private final ObjMesh mesh;
        private final boolean inventory;
        private final int metadata;
        private final ResourceLocation texture;
        DecorationModel(Definition definition, ObjMesh mesh, int metadata, boolean inventory) {
            this.definition = definition; this.mesh = mesh; this.inventory = inventory; this.metadata = metadata;
            this.texture = definition.texture;
        }
        @Override public Collection<ResourceLocation> getTextures() { return Collections.singleton(texture); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            TextureAtlasSprite sprite = sprites.apply(texture);
            List<BakedQuad> quads = RenderTapeBlock.bake(mesh, definition.owner == DecoSteelPoles.class, metadata, inventory, sprite);
            return new AtlasObjBakedModel(quads, sprite);
        }
    }
}
