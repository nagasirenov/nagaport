package com.hbm.main;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import com.hbm.blocks.generic.BlockSteelDecoration;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.SteelDecorationBakedModel;
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

/** Owns only the two audited NTM wall/corner identities, never unrelated decorations. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class SteelDecorationModelHandler {
    private static final String PREFIX = "native_steel/";
    private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID, "blocks/steel_wall");
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    private SteelDecorationModelHandler() { }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(block.getClass() != BlockSteelDecoration.class) continue;
            ResourceLocation name = block.getRegistryName();
            if(name == null || !RefStrings.MODID.equals(name.getNamespace())) continue;
            boolean corner = name.getPath().equals("tile.steel_corner");
            if(!corner && !name.getPath().equals("tile.steel_wall")) continue;
            if(((BlockSteelDecoration)block).isCorner() != corner) continue;
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing steel decoration ItemBlock " + name);
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata,
                    new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, PREFIX + name.getPath() + "/" + metadata + "/item"), "inventory"));
        }
    }
    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager resources) { }
        @Override public boolean accepts(ResourceLocation location) {
            if(!RefStrings.MODID.equals(location.getNamespace())) return false;
            String[] fields = normalized(location).split("/", -1);
            return fields.length == 4 && fields[0].equals("native_steel")
                    && (fields[1].equals("tile.steel_wall") || fields[1].equals("tile.steel_corner"))
                    && fields[2].matches("[0-9]|1[0-5]")
                    && (fields[3].equals("world") || fields[3].equals("item"));
        }
        @Override public IModel loadModel(ResourceLocation location) {
            if(!accepts(location)) throw new IllegalArgumentException("Unknown steel decoration model route " + location);
            String[] fields = normalized(location).substring(PREFIX.length()).split("/");
            if(fields.length != 3 || !(fields[2].equals("world") || fields[2].equals("item")))
                throw new IllegalArgumentException("Invalid steel decoration model route " + location);
            boolean corner = fields[0].equals("tile.steel_corner");
            if(!corner && !fields[0].equals("tile.steel_wall")) throw new IllegalArgumentException("Unknown steel decoration owner " + location);
            int metadata = Integer.parseInt(fields[1]);
            if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Invalid steel decoration metadata " + metadata);
            return new SteelModel(corner, metadata, fields[2].equals("item"));
        }
        private static String normalized(ResourceLocation location) {
            String path = location.getPath();
            if(path.startsWith("models/")) path = path.substring(7);
            if(path.startsWith("block/")) path = path.substring(6);
            return path;
        }
    }
    private static final class SteelModel implements IModel {
        final boolean corner, inventory;
        final int metadata;
        SteelModel(boolean corner, int metadata, boolean inventory) { this.corner = corner; this.metadata = metadata; this.inventory = inventory; }
        @Override public Collection<ResourceLocation> getTextures() { return Collections.singleton(TEXTURE); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            return new SteelDecorationBakedModel(corner, metadata, inventory, sprites.apply(TEXTURE));
        }
    }
}
