package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import com.hbm.blocks.generic.BlockDecoToaster;
import com.hbm.blocks.generic.BlockDecoModel;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.DecoApplianceBakedModel;
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

/** Native atlas registration for the original toaster and computer definitions; no early ItemBlock identity lookup. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class DecoApplianceModelHandler {
    private DecoApplianceModelHandler() { }
    private static final String PREFIX = "native_appliance/";
    private static final Map<String,Definition> DEFINITIONS = definitions();
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!(block instanceof BlockDecoToaster) && block.getClass()!=BlockDecoModel.class) continue;
            ResourceLocation name = Objects.requireNonNull(block.getRegistryName(), "Appliance registry name");
            Definition definition = RefStrings.MODID.equals(name.getNamespace()) ? DEFINITIONS.get(name.getPath()) : null;
            if(definition == null || block.getClass() != (definition.computer ? BlockDecoModel.class : BlockDecoToaster.class))
                throw new IllegalArgumentException("Unspecified appliance owner " + name);
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing appliance ItemBlock " + name);
            for(int metadata = 0; metadata < definition.textures.length; metadata++)
                ModelLoader.setCustomModelResourceLocation(item,metadata,new ModelResourceLocation(route(name.getPath(),metadata,true),"inventory"));
        }
    }
    private static ResourceLocation route(String owner, int metadata, boolean inventory) {
        return new ResourceLocation(RefStrings.MODID,PREFIX+owner+"/"+metadata+(inventory?"/item":"/world"));
    }
    private static Map<String,Definition> definitions() {
        Map<String,Definition> result = new LinkedHashMap<>();
        result.put("tile.deco_toaster",new Definition(false,"toaster","toaster_iron","toaster_steel","toaster_wood"));
        result.put("tile.deco_computer",new Definition(true,"puter","deco_computer"));
        return Collections.unmodifiableMap(result);
    }
    private static final class Definition {
        final boolean computer;
        final ResourceLocation asset;
        final ResourceLocation[] textures;
        Definition(boolean computer,String model,String... materials) {
            this.computer=computer;
            asset=new ResourceLocation(RefStrings.MODID,"models/blocks/"+model+".obj");
            textures=new ResourceLocation[materials.length];
            for(int i=0;i<materials.length;i++) textures[i]=new ResourceLocation(RefStrings.MODID,"blocks/"+materials[i]);
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
                throw new IllegalArgumentException("Invalid appliance model route "+location);
            Definition definition=DEFINITIONS.get(fields[0]);
            if(definition == null) throw new IllegalArgumentException("Unknown appliance owner "+location);
            int metadata=Integer.parseInt(fields[1]);
            if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Appliance metadata "+metadata);
            ObjMesh mesh=meshes.get(definition.asset);
            if(mesh == null) {
                try(IResource resource=resources.getResource(definition.asset);
                        InputStreamReader reader=new InputStreamReader(resource.getInputStream(),StandardCharsets.UTF_8)) {
                    mesh=ObjMeshParser.parse(reader,definition.asset.toString(),false);
                }
                meshes.put(definition.asset,mesh);
            }
            return new ApplianceModel(definition,mesh,metadata,fields[2].equals("item"));
        }
        private static String normalized(ResourceLocation location) {
            String path=location.getPath();
            if(path.startsWith("models/")) path=path.substring(7);
            if(path.startsWith("block/")) path=path.substring(6);
            return path;
        }
    }
    private static final class ApplianceModel implements IModel {
        final Definition definition;
        final ObjMesh mesh;
        final int metadata;
        final boolean inventory;
        ApplianceModel(Definition definition,ObjMesh mesh,int metadata,boolean inventory) {
            this.definition=definition;this.mesh=mesh;this.metadata=metadata;this.inventory=inventory;
        }
        private ResourceLocation texture() { if(definition.computer) return definition.textures[0]; return definition.textures[inventory ? Math.floorMod(metadata,definition.textures.length) : (metadata % (definition.textures.length * 4)) / 4]; }
        @Override public Collection<ResourceLocation> getTextures() { return Collections.singleton(texture()); }
        @Override public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> sprites) {
            return new DecoApplianceBakedModel(mesh,definition.computer,metadata,inventory,sprites.apply(texture()));
        }
    }
}
