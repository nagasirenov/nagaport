package com.hbm.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import com.google.common.collect.ImmutableList;
import com.hbm.blocks.generic.BlockGrate;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.GrateBakedModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
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

/** Exactly the original steel_grate and steel_grate_wide owners and their flat item icons. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class GrateModelHandler {
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    private GrateModelHandler() { }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(block.getClass() != BlockGrate.class) continue;
            ResourceLocation name = block.getRegistryName();
            if(name == null || !RefStrings.MODID.equals(name.getNamespace())) continue;
            boolean wide = name.getPath().equals("tile.steel_grate_wide");
            if(!wide && !name.getPath().equals("tile.steel_grate")) continue;
            if(((BlockGrate)block).isWide() != wide) continue;
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing grate ItemBlock " + name);
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata,
                    new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, "native_grate/" + name.getPath() + "/item"), "inventory"));
        }
    }
    private static String path(ResourceLocation location) {
        String path = location.getPath();
        if(path.startsWith("models/")) path = path.substring(7);
        if(path.startsWith("block/")) path = path.substring(6);
        return path;
    }
    private static ResourceLocation top(boolean wide) { return new ResourceLocation(RefStrings.MODID, "blocks/" + (wide ? "grate_wide_top" : "grate_top")); }
    private static final ResourceLocation SIDE = new ResourceLocation(RefStrings.MODID, "blocks/grate_side");
    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager resources) { }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && path(location).matches("native_grate/tile\\.steel_grate(_wide)?/(item|([0-9]|1[0-5])/world)");
        }
        @Override public IModel loadModel(ResourceLocation location) {
            if(!accepts(location)) throw new IllegalArgumentException("Unknown grate model route " + location);
            String[] fields = path(location).split("/");
            boolean wide = fields[1].equals("tile.steel_grate_wide");
            if(fields[2].equals("item")) return new ItemLayerModel(ImmutableList.of(top(wide)));
            return new GrateModel(wide, Integer.parseInt(fields[2]));
        }
    }
    private static final class GrateModel implements IModel {
        private final boolean wide;
        private final int metadata;
        GrateModel(boolean wide, int metadata) { this.wide = wide; this.metadata = metadata; }
        @Override public Collection<ResourceLocation> getTextures() { return Arrays.asList(top(wide), SIDE); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            return new GrateBakedModel(metadata, sprites.apply(top(wide)), sprites.apply(SIDE));
        }
    }
}
