package com.hbm.main;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import com.google.common.collect.ImmutableList;
import com.hbm.blocks.generic.BlockWand;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.WandBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.Blocks;
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

/** Only the original wand_air owner; ItemBlock construction remains deferred. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class WandModelHandler {
    private static final ResourceLocation OWNER = new ResourceLocation(RefStrings.MODID, "tile.wand_air");
    private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID, "blocks/wand_air");
    private static final String PREFIX = "native_wand/";
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    private WandModelHandler() { }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(block.getClass() != BlockWand.class || !OWNER.equals(block.getRegistryName())
                    || ((BlockWand)block).exportAs != Blocks.AIR) continue;
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing wand ItemBlock " + OWNER);
            ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
                @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return new ModelResourceLocation(OWNER, "normal");
                }
            });
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata,
                    new ModelResourceLocation(RefStrings.MODID + ":" + PREFIX + "item", "inventory"));
        }
    }
    private static String path(ResourceLocation location) {
        String path = location.getPath();
        if(path.startsWith("models/")) path = path.substring(7);
        if(path.startsWith("block/")) path = path.substring(6);
        return path;
    }
    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager manager) { }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace())
                    && (path(location).equals(PREFIX + "world") || path(location).equals(PREFIX + "item"));
        }
        @Override public IModel loadModel(ResourceLocation location) {
            if(!accepts(location)) throw new IllegalArgumentException("Unknown wand model route " + location);
            return path(location).endsWith("/item") ? new ItemLayerModel(ImmutableList.of(TEXTURE)) : new Model();
        }
    }
    private static final class Model implements IModel {
        @Override public Collection<ResourceLocation> getTextures() { return Collections.singletonList(TEXTURE); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            return new WandBakedModel(sprites.apply(TEXTURE));
        }
    }
}
