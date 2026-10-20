package com.hbm.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import com.hbm.blocks.generic.BlockPedestal;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.PedestalBakedModel;
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

/** Registry-time item identity lookup and reload-safe atlas model construction. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class PedestalModelHandler {
    private PedestalModelHandler() { }
    private static final ResourceLocation TOP = new ResourceLocation(RefStrings.MODID, "blocks/pedestal_top");
    private static final ResourceLocation SIDE = new ResourceLocation(RefStrings.MODID, "blocks/pedestal_side");
    private static final ModelResourceLocation WORLD = new ModelResourceLocation(RefStrings.MODID + ":native_pedestal/world", "normal");
    private static final ModelResourceLocation ITEM = new ModelResourceLocation(RefStrings.MODID + ":native_pedestal/item", "inventory");
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(new Loader()); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) if(block.getClass() == BlockPedestal.class && new ResourceLocation(RefStrings.MODID, "tile.pedestal").equals(block.getRegistryName())) {
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing pedestal ItemBlock " + block.getRegistryName());
            ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
                @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) { return WORLD; }
            });
            ModelLoader.setCustomModelResourceLocation(item, 0, ITEM);
        }
    }
    private static String path(ResourceLocation location) {
        String path = location.getPath();
        if(path.startsWith("models/")) path = path.substring(7);
        if(path.startsWith("block/")) path = path.substring(6);
        return path;
    }
    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager resources) { }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && (path(location).equals("native_pedestal/world") || path(location).equals("native_pedestal/item"));
        }
        @Override public IModel loadModel(ResourceLocation location) {
            if(!accepts(location)) throw new IllegalArgumentException("Unknown pedestal route " + location);
            return new Model(path(location).endsWith("/item"));
        }
    }
    private static final class Model implements IModel {
        private final boolean inventory;
        Model(boolean inventory) { this.inventory = inventory; }
        @Override public Collection<ResourceLocation> getTextures() { return Arrays.asList(TOP, SIDE); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            return new PedestalBakedModel(sprites.apply(TOP), sprites.apply(SIDE), inventory);
        }
    }
}
