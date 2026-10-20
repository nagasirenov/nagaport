package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import com.hbm.blocks.bomb.DetCord;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.DetCordBakedModel;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
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
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Native resource-loader ownership for the original detonator cord geometry. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class DetCordModelHandler {
    private DetCordModelHandler() { }
    private static final String PREFIX = "native_detcord/";
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!(block instanceof DetCord)) continue;
            ResourceLocation name = block.getRegistryName();
            if(name == null || !RefStrings.MODID.equals(name.getNamespace())) continue;
            if(!name.getPath().equals("tile.det_cord")) throw new IllegalArgumentException("Unspecified detonator cord geometry for " + name);
            ModelLoader.setCustomStateMapper(block, new StateMap.Builder().build());
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing detonator cord ItemBlock: " + name);
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata,
                    new ModelResourceLocation(RefStrings.MODID + ":" + PREFIX + "cord" + "/item", "inventory"));
        }
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private ObjMesh cable;
        @Override public void onResourceManagerReload(IResourceManager manager) { resources = manager; cable = null; }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && normalizedPath(location).startsWith(PREFIX);
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            String[] fields = normalizedPath(location).substring(PREFIX.length()).split("/");
            if(fields.length != 2 || !fields[0].equals("cord")
                    || !(fields[1].equals("item") || fields[1].equals("world"))) throw new IllegalArgumentException("Invalid connection model " + location);
            if(cable == null) {
                ResourceLocation asset = new ResourceLocation(RefStrings.MODID, "models/blocks/cable_neo.obj");
                try(IResource resource = resources.getResource(asset);
                        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    cable = ObjMeshParser.parse(reader, asset.toString(), false);
                }
            }
            return new CordModel(cable, fields[1].equals("item"));
        }
        private static String normalizedPath(ResourceLocation location) {
            String path = location.getPath();
            if(path.startsWith("models/")) path = path.substring(7);
            if(path.startsWith("block/")) path = path.substring(6);
            return path;
        }
    }
    private static final class CordModel implements IModel {
        private final ObjMesh mesh;
        private final boolean inventory;
        private final ResourceLocation texture;
        CordModel(ObjMesh mesh, boolean inventory) {
            this.mesh = mesh; this.inventory = inventory;
            texture = new ResourceLocation(RefStrings.MODID, "blocks/det_cord");
        }
        @Override public Collection<ResourceLocation> getTextures() { return Collections.singleton(texture); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            return new DetCordBakedModel(mesh, inventory, sprites.apply(texture));
        }
    }
}
