package com.hbm.main;

import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.generic.BlockChain;
import com.hbm.blocks.generic.BlockMetalFence;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.StructuralBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
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

/** Late registry identity and native flat-item routes, just as the original ISBRH inventory flags specified. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class StructuralBlockModelHandler {
    private static final String PREFIX = "native_structural/";
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    private StructuralBlockModelHandler() { }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            ResourceLocation name = block.getRegistryName();
            // Own only these two registrations. Addons may reuse either block
            // class with their own model pipeline, even within the hbm namespace.
            if(name == null || !RefStrings.MODID.equals(name.getNamespace())) continue;
            boolean chain = "tile.dungeon_chain".equals(name.getPath());
            if(!chain && !"tile.fence_metal".equals(name.getPath())) continue;
            // An addon can replace an owned registration and supply its own
            // models. Never override or reject that replacement's model route.
            if(block.getClass() != (chain ? BlockChain.class : BlockMetalFence.class)) continue;

            ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
                @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return new ModelResourceLocation(name, !chain && state.getValue(BlockMetalFence.META) != 0 ? "post" : "normal");
                }
            });
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing structural ItemBlock " + name);
            for(int meta = 0; meta < 16; meta++) ModelLoader.setCustomModelResourceLocation(item, meta, route(chain, true, !chain && meta != 0));
        }
    }
    private static ModelResourceLocation route(boolean chain, boolean item, boolean post) {
        return new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, PREFIX + (chain ? "chain" : "fence") + "/" + (item ? post ? "post" : "item" : "world")), item ? "inventory" : "normal");
    }
    private static ResourceLocation texture(boolean chain, boolean secondary) {
        return new ResourceLocation(RefStrings.MODID, "blocks/" + (chain ? secondary ? "chain_end" : "chain" : secondary ? "fence_metal_post" : "fence_metal"));
    }
    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager resources) { }
        private String path(ResourceLocation location) {
            String path = location.getPath();
            if(path.startsWith("models/")) path = path.substring(7);
            if(path.startsWith("block/")) path = path.substring(6);
            return path;
        }
        @Override public boolean accepts(ResourceLocation location) { return RefStrings.MODID.equals(location.getNamespace()) && path(location).startsWith(PREFIX); }
        @Override public IModel loadModel(ResourceLocation location) {
            String[] parts = path(location).substring(PREFIX.length()).split("/");
            if(parts.length != 2 || !(parts[0].equals("chain") || parts[0].equals("fence"))
                    || !(parts[1].equals("world") || parts[1].equals("world_post") || parts[1].equals("item") || parts[1].equals("post")))
                throw new IllegalArgumentException("Invalid structural model " + location);
            boolean chain = parts[0].equals("chain");
            if(!parts[1].equals("world") && !parts[1].equals("world_post")) return new ItemLayerModel(com.google.common.collect.ImmutableList.of(texture(chain, parts[1].equals("post"))));
            return new StructuralModel(chain, parts[1].equals("world_post"));
        }
    }
    private static final class StructuralModel implements IModel {
        private final boolean chain, post;
        StructuralModel(boolean chain, boolean post) { this.chain = chain; this.post = post; }
        @Override public Collection<ResourceLocation> getTextures() { return Arrays.asList(texture(chain, false), texture(chain, true)); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            return new StructuralBakedModel(chain, post, sprites.apply(texture(chain, false)), sprites.apply(texture(chain, true)));
        }
    }
}
