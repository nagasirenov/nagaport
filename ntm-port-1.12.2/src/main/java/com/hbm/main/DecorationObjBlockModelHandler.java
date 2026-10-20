package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.generic.BlockSteelBeam;
import com.hbm.blocks.generic.BlockCanCrate;
import com.hbm.blocks.generic.BlockSupplyCrate;
import com.hbm.blocks.machine.rbmk.*;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.RenderSteelBeam;
import com.hbm.render.block.RenderConserve;
import com.hbm.render.block.RenderPribris;
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

/** Seven audited flat-OBJ owners, deliberately excluding DecoBlock's AO and TESR paths. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class DecorationObjBlockModelHandler {
    private DecorationObjBlockModelHandler() { }
    private static final String PREFIX = "native_decoration_obj/";
    private static final Map<String, Definition> DEFINITIONS = definitions();
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!(block instanceof BlockSteelBeam || block instanceof BlockCanCrate
                    || block instanceof BlockSupplyCrate || block instanceof RBMKDebris)) continue;
            ResourceLocation name = block.getRegistryName();
            if(name == null || !RefStrings.MODID.equals(name.getNamespace())) continue;
            Definition definition = DEFINITIONS.get(name.getPath());
            if(definition == null || block.getClass() != definition.owner)
                throw new IllegalArgumentException("Unspecified decoration OBJ owner " + name);
            // Blocks and their ItemBlocks have been flushed to the native registries before this event.
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing decoration OBJ ItemBlock " + name);
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata,
                    new ModelResourceLocation(RefStrings.MODID + ":" + PREFIX + name.getPath() + "/" + metadata + "/item", "inventory"));
        }
    }
    private static Map<String, Definition> definitions() {
        Map<String, Definition> result = new LinkedHashMap<>();
        result.put("tile.steel_beam", new Definition(BlockSteelBeam.class, "models/blocks/beam.obj", "steel_beam"));
        result.put("tile.crate_can", new Definition(BlockCanCrate.class, "models/blocks/conservecrate.obj", "crate_can"));
        result.put("tile.crate_supply", new Definition(BlockSupplyCrate.class, "models/blocks/conservecrate.obj", "crate_can"));
        result.put("tile.pribris", new Definition(RBMKDebris.class, "models/rbmk/debris.obj", "rbmk/rbmk_debris"));
        result.put("tile.pribris_burning", new Definition(RBMKDebrisBurning.class, "models/rbmk/debris.obj", "rbmk/rbmk_debris_burning"));
        result.put("tile.pribris_radiating", new Definition(RBMKDebrisRadiating.class, "models/rbmk/debris.obj", "rbmk/rbmk_debris_radiating"));
        result.put("tile.pribris_digamma", new Definition(RBMKDebrisDigamma.class, "models/rbmk/debris.obj", "rbmk/rbmk_debris_digamma"));
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
                throw new IllegalArgumentException("Invalid decoration OBJ route " + location);
            Definition definition = DEFINITIONS.get(fields[0]);
            if(definition == null) throw new IllegalArgumentException("Unknown decoration OBJ block " + fields[0]);
            int metadata = Integer.parseInt(fields[1]);
            if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Invalid decoration OBJ metadata " + metadata);
            // Original geometry/material do not depend on metadata, including all debris decay states.
            return new DecorationModel(definition, mesh(definition.asset), fields[2].equals("item"));
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
        DecorationModel(Definition definition, ObjMesh mesh, boolean inventory) {
            this.definition = definition; this.mesh = mesh; this.inventory = inventory;
        }
        @Override public Collection<ResourceLocation> getTextures() { return Collections.singleton(definition.texture); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            TextureAtlasSprite sprite = sprites.apply(definition.texture);
            List<BakedQuad> quads;
            if(definition.owner == BlockSteelBeam.class) quads = RenderSteelBeam.bake(mesh, inventory, sprite);
            else if(definition.owner == BlockCanCrate.class || definition.owner == BlockSupplyCrate.class)
                quads = RenderConserve.bake(mesh, inventory, sprite);
            else quads = RenderPribris.bake(mesh, inventory, sprite);
            return new AtlasObjBakedModel(quads, sprite);
        }
    }
}
