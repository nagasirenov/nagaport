package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.generic.BlockPipe;
import com.hbm.blocks.generic.DecoPoleTop;
import com.hbm.blocks.generic.HEVBattery;
import com.hbm.blocks.generic.BrownBarrel;
import com.hbm.blocks.generic.YellowBarrel;
import com.hbm.blocks.generic.RedBarrel;
import com.hbm.blocks.machine.BlockFluidBarrel;
import com.hbm.blocks.machine.MachineCapacitor;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.*;
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

/** Native atlas route for the audited five-renderer, 45-block flat OBJ family. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class AtlasObjBlockModelHandler {
    private AtlasObjBlockModelHandler() { }
    private static final String PREFIX = "native_atlas_obj/";
    private static final Map<String, Definition> DEFINITIONS = definitions();
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!(block instanceof BlockPipe || block instanceof MachineCapacitor || block instanceof BlockFluidBarrel
                    || block instanceof HEVBattery || block instanceof DecoPoleTop
                    || block instanceof BrownBarrel || block instanceof YellowBarrel || block instanceof RedBarrel)) continue;
            ResourceLocation name = block.getRegistryName();
            if(name == null || !RefStrings.MODID.equals(name.getNamespace())) continue;
            Definition definition = DEFINITIONS.get(name.getPath());
            if(definition == null || !definition.owner.isInstance(block))
                throw new IllegalArgumentException("Unspecified atlas OBJ owner " + name);
            if(block instanceof BlockPipe && (((BlockPipe)block).rType != definition.shape
                    || !((BlockPipe)block).getSideTexture().equals("hbm:" + definition.textures[1])))
                throw new IllegalArgumentException("Decorative pipe geometry/material mismatch: " + name);
            if(block instanceof MachineCapacitor && !((MachineCapacitor)block).getModelMaterial().equals(definition.material))
                throw new IllegalArgumentException("Capacitor material mismatch: " + name);
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing atlas OBJ ItemBlock " + name);
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata,
                    new ModelResourceLocation(RefStrings.MODID + ":" + PREFIX + name.getPath() + "/" + metadata + "/item", "inventory"));
        }
    }
    private static Map<String, Definition> definitions() {
        Map<String, Definition> result = new LinkedHashMap<>();
        String[] variants = {"", "_rusted", "_green", "_green_rusted", "_red", "_marked"};
        String[] skins = {"", "_rusty", "_green", "_green_rusty", "_red", "_marked"};
        String[] shapes = {"", "_rim", "_quad", "_framed"};
        for(int shape = 0; shape < shapes.length; shape++) for(int skin = 0; skin < skins.length; skin++) {
            String top = "pipe_top" + skins[skin];
            result.put("tile.deco_pipe" + shapes[shape] + variants[skin], new Definition(BlockPipe.class, "pipe", shape, null,
                    top, top, "pipe_side" + skins[skin], "pipe_frame", "pipe_mesh"));
        }
        String[] materials = {"copper", "gold", "niobium", "complex", "tantalium", "schrabidate"};
        String[] particles = {"block_copper", "minecraft:gold_block", "block_niobium", "block_cadmium", "block_tantalium", "block_schrabidate"};
        for(int i = 0; i < materials.length; i++) {
            String material = materials[i], texture = "capacitor_" + material;
            result.put("tile.capacitor_" + material, new Definition(MachineCapacitor.class, "capacitor", 0, material,
                    particles[i], texture + "_top", texture + "_side", texture + "_bottom", texture + "_inner_top", texture + "_inner_side"));
        }
        for(String material : new String[] {"plastic", "corroded", "iron", "steel", "tcalloy", "antimatter"}) {
            String texture = "barrel_" + material;
            result.put("tile." + texture, new Definition(BlockFluidBarrel.class, "barrel", 0, null, texture, texture));
        }
        result.put("tile.hev_battery", new Definition(HEVBattery.class, "battery", 0, null, "hev_battery", "hev_battery"));
        result.put("tile.pole_top", new Definition(DecoPoleTop.class, "antenna_top", 0, null, "deco_pole_top", "deco_pole_top"));
        String[] barrels = {"red", "pink", "yellow", "oil", "vitrified", "lox", "taint"};
        for(String barrel : barrels) {
            Class<? extends Block> owner = barrel.equals("oil") ? BrownBarrel.class
                    : barrel.equals("yellow") || barrel.equals("vitrified") ? YellowBarrel.class : RedBarrel.class;
            String texture = "barrel_" + (barrel.equals("oil") ? "oily" : barrel);
            result.put("tile." + barrel + "_barrel", new Definition(owner, "barrel", 0, null, texture, texture));
        }
        return Collections.unmodifiableMap(result);
    }
    private static final class Definition {
        final Class<? extends Block> owner;
        final String asset, material, particle;
        final int shape;
        final String[] textures;
        Definition(Class<? extends Block> owner, String asset, int shape, String material, String particle, String... textures) {
            this.owner = owner; this.asset = asset; this.shape = shape; this.material = material;
            this.particle = particle; this.textures = textures;
        }
        ResourceLocation texture(String name) {
            ResourceLocation location = new ResourceLocation(name.indexOf(':') >= 0 ? name : RefStrings.MODID + ":" + name);
            return new ResourceLocation(location.getNamespace(), "blocks/" + location.getPath());
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
                throw new IllegalArgumentException("Invalid atlas OBJ route " + location);
            Definition definition = DEFINITIONS.get(fields[0]);
            if(definition == null) throw new IllegalArgumentException("Unknown atlas OBJ block " + fields[0]);
            int metadata = Integer.parseInt(fields[1]);
            if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Invalid atlas OBJ metadata " + metadata);
            String asset = definition.asset;
            if(definition.owner == BlockPipe.class) asset = new String[] {"pipe", "pipe_rim", "pipe_quad", "pipe_rim"}[definition.shape];
            return new AtlasModel(definition, mesh(asset), definition.owner == BlockPipe.class && definition.shape == 3 ? mesh("pipe_frame") : null,
                    metadata, fields[2].equals("item"));
        }
        private ObjMesh mesh(String name) throws Exception {
            ObjMesh mesh = meshes.get(name);
            if(mesh == null) {
                ResourceLocation asset = new ResourceLocation(RefStrings.MODID, "models/blocks/" + name + ".obj");
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
    private static final class AtlasModel implements IModel {
        private final Definition definition;
        private final ObjMesh body, frame;
        private final int metadata;
        private final boolean inventory;
        AtlasModel(Definition definition, ObjMesh body, ObjMesh frame, int metadata, boolean inventory) {
            this.definition = definition; this.body = body; this.frame = frame; this.metadata = metadata; this.inventory = inventory;
        }
        @Override public Collection<ResourceLocation> getTextures() {
            Set<ResourceLocation> result = new LinkedHashSet<>();
            for(String texture : definition.textures) result.add(definition.texture(texture));
            result.add(definition.texture(definition.particle));
            return result;
        }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            TextureAtlasSprite[] textures = new TextureAtlasSprite[definition.textures.length];
            for(int i = 0; i < textures.length; i++) textures[i] = sprites.apply(definition.texture(definition.textures[i]));
            List<BakedQuad> quads;
            if(definition.owner == BlockPipe.class) quads = RenderPipe.bake(body, frame, definition.shape, metadata, inventory, textures[0], textures[1], textures[2], textures[3]);
            else if(definition.owner == MachineCapacitor.class) quads = RenderCapacitor.bake(body, metadata, inventory, textures);
            else if(definition.asset.equals("barrel")) quads = RenderBarrel.bake(body, inventory, textures[0]);
            else if(definition.owner == HEVBattery.class) quads = RenderBattery.bake(body, inventory, textures[0]);
            else if(definition.owner == DecoPoleTop.class) quads = RenderAntennaTop.bake(body, inventory, textures[0]);
            else throw new IllegalStateException("Unsupported atlas OBJ owner");
            return new AtlasObjBakedModel(quads, sprites.apply(definition.texture(definition.particle)));
        }
    }
}
