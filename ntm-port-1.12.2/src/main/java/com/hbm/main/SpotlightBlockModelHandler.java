package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.BlockEnums.LightType;
import com.hbm.blocks.machine.Spotlight;
import com.hbm.blocks.machine.SpotlightModular;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.SpotlightBakedModel;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import net.minecraft.block.Block;
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

/** Native atlas registration for all six original lamp definitions; no early ItemBlock identity lookup. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class SpotlightBlockModelHandler {
    private SpotlightBlockModelHandler() { }
    private static final String PREFIX = "native_spotlight/";
    private static final Map<String,Definition> DEFINITIONS = definitions();
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!(block instanceof Spotlight)) continue;
            ResourceLocation name = Objects.requireNonNull(block.getRegistryName(), "Spotlight registry name");
            Definition definition = RefStrings.MODID.equals(name.getNamespace()) ? DEFINITIONS.get(name.getPath()) : null;
            Spotlight lamp = (Spotlight)block;
            if(definition == null || block.getClass() != (definition.modular ? SpotlightModular.class : Spotlight.class)
                    || lamp.type != definition.type || lamp.isOn != definition.on
                    || !lamp.getModelResource().equals(definition.asset) || !lamp.getModelTexture().equals(definition.texture))
                throw new IllegalArgumentException("Unspecified spotlight geometry/material owner " + name);
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing spotlight ItemBlock " + name);
            for(int metadata = 0; metadata < 16; metadata++)
                ModelLoader.setCustomModelResourceLocation(item,metadata,new ModelResourceLocation(route(name.getPath(),metadata,true),"inventory"));
        }
    }
    private static ResourceLocation route(String owner, int metadata, boolean inventory) {
        return new ResourceLocation(RefStrings.MODID,PREFIX+owner+"/"+metadata+(inventory?"/item":"/world"));
    }
    private static Map<String,Definition> definitions() {
        Map<String,Definition> result = new LinkedHashMap<>();
        for(boolean on : new boolean[] {true,false}) {
            String suffix = on ? "" : "_off";
            result.put("tile.spotlight_incandescent"+suffix,new Definition(LightType.INCANDESCENT,false,on,"cage_lamp"));
            result.put("tile.spotlight_fluoro"+suffix,new Definition(LightType.FLUORESCENT,true,on,"fluorescent_lamp"));
            result.put("tile.spotlight_halogen"+suffix,new Definition(LightType.HALOGEN,false,on,"flood_lamp"));
        }
        return Collections.unmodifiableMap(result);
    }
    private static final class Definition {
        final LightType type;
        final boolean modular, on;
        final ResourceLocation asset, texture;
        Definition(LightType type, boolean modular, boolean on, String material) {
            this.type=type; this.modular=modular; this.on=on;
            asset=new ResourceLocation(RefStrings.MODID,"models/lights/"+material+".obj");
            texture=new ResourceLocation(RefStrings.MODID,"blocks/"+material+(on?"":"_off"));
        }
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private final Map<ResourceLocation,ObjMesh> meshes = new HashMap<>();
        @Override public void onResourceManagerReload(IResourceManager resources) { this.resources=resources; meshes.clear(); }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && normalized(location).startsWith(PREFIX);
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            String[] fields=normalized(location).substring(PREFIX.length()).split("/");
            if(fields.length != 3 || !(fields[2].equals("world") || fields[2].equals("item")))
                throw new IllegalArgumentException("Invalid spotlight model route "+location);
            Definition definition=DEFINITIONS.get(fields[0]);
            if(definition == null) throw new IllegalArgumentException("Unknown spotlight owner "+location);
            int metadata=Integer.parseInt(fields[1]);
            if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Spotlight metadata "+metadata);
            ObjMesh mesh=meshes.get(definition.asset);
            if(mesh == null) {
                try(IResource resource=resources.getResource(definition.asset);
                        InputStreamReader reader=new InputStreamReader(resource.getInputStream(),StandardCharsets.UTF_8)) {
                    mesh=ObjMeshParser.parse(reader,definition.asset.toString(),false);
                }
                meshes.put(definition.asset,mesh);
            }
            return new LampModel(definition,mesh,metadata,fields[2].equals("item"));
        }
        private static String normalized(ResourceLocation location) {
            String path=location.getPath();
            if(path.startsWith("models/")) path=path.substring(7);
            if(path.startsWith("block/")) path=path.substring(6);
            return path;
        }
    }
    private static final class LampModel implements IModel {
        final Definition definition;
        final ObjMesh mesh;
        final int metadata;
        final boolean inventory;
        LampModel(Definition definition,ObjMesh mesh,int metadata,boolean inventory) {
            this.definition=definition;this.mesh=mesh;this.metadata=metadata;this.inventory=inventory;
        }
        @Override public Collection<ResourceLocation> getTextures() { return Collections.singleton(definition.texture); }
        @Override public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> sprites) {
            return new SpotlightBakedModel(mesh,definition.type,definition.modular,metadata,inventory,sprites.apply(definition.texture));
        }
    }
}
