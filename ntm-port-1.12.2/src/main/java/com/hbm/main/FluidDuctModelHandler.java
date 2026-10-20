package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import com.hbm.blocks.network.FluidDuctStandard;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.FluidDuctBakedModel;
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
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Native loader for the standard fluid duct's three original skins and two OBJ passes. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class FluidDuctModelHandler {
    private FluidDuctModelHandler() { }
    private static final String PREFIX = "native_fluid_duct/";
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!(block instanceof FluidDuctStandard)) continue;
            ResourceLocation name = block.getRegistryName();
            if(name == null || !RefStrings.MODID.equals(name.getNamespace())) continue;
            if(!name.getPath().equals("tile.fluid_duct_neo")) throw new IllegalArgumentException("Unspecified standard duct skin for " + name);
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing standard duct ItemBlock: " + name);
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata,
                    new ModelResourceLocation(RefStrings.MODID + ":" + PREFIX + Math.abs(metadata % 3) + "/item", "inventory"));
        }
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private ObjMesh duct;
        @Override public void onResourceManagerReload(IResourceManager manager) { resources = manager; duct = null; }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && normalizedPath(location).startsWith(PREFIX);
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            String[] fields = normalizedPath(location).substring(PREFIX.length()).split("/");
            if(fields.length != 2 || !(fields[1].equals("item") || fields[1].equals("world")))
                throw new IllegalArgumentException("Invalid standard duct model " + location);
            int metadata = Integer.parseInt(fields[0]);
            if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Invalid standard duct metadata " + metadata);
            if(duct == null) {
                ResourceLocation asset = new ResourceLocation(RefStrings.MODID, "models/blocks/pipe_neo.obj");
                try(IResource resource = resources.getResource(asset);
                        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    duct = ObjMeshParser.parse(reader, asset.toString(), false);
                }
            }
            return new DuctModel(duct, metadata, fields[1].equals("item"));
        }
        private static String normalizedPath(ResourceLocation location) {
            String path = location.getPath();
            if(path.startsWith("models/")) path = path.substring(7);
            if(path.startsWith("block/")) path = path.substring(6);
            return path;
        }
    }
    private static final class DuctModel implements IModel {
        private final ObjMesh mesh;
        private final int metadata;
        private final boolean inventory;
        private final ResourceLocation base, overlay;
        DuctModel(ObjMesh mesh, int metadata, boolean inventory) {
            this.mesh = mesh; this.metadata = metadata; this.inventory = inventory;
            String skin = new String[] {"pipe_neo", "pipe_silver", "pipe_colored"}[Math.abs(metadata % 3)];
            base = new ResourceLocation(RefStrings.MODID, "blocks/" + skin);
            overlay = new ResourceLocation(RefStrings.MODID, "blocks/" + skin + "_overlay");
        }
        @Override public Collection<ResourceLocation> getTextures() { return Arrays.asList(base, overlay); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            return new FluidDuctBakedModel(mesh, metadata, inventory, sprites.apply(base), sprites.apply(overlay), Fluids.NONE.getColor());
        }
    }
}
