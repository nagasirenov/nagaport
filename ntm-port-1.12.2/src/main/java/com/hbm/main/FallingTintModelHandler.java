package com.hbm.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import com.hbm.blocks.generic.BlockFallingTint;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.FallingTintBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import org.lwjgl.util.vector.Vector3f;
import java.util.*;
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
public final class FallingTintModelHandler {
    private FallingTintModelHandler() { }
    private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID, "blocks/laythe_silt");
    private static final ModelResourceLocation WORLD = new ModelResourceLocation(RefStrings.MODID + ":native_falling_tint/world", "normal");
    private static final ModelResourceLocation ITEM = new ModelResourceLocation(RefStrings.MODID + ":native_falling_tint/item", "inventory");
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(new Loader()); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) if(block.getClass() == BlockFallingTint.class && new ResourceLocation(RefStrings.MODID, "tile.laythe_silt").equals(block.getRegistryName())) {
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing falling_tint ItemBlock " + block.getRegistryName());
            ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
                @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) { return WORLD; }
            });
            for(int meta = 0; meta < 16; meta++) ModelLoader.setCustomModelResourceLocation(item, meta, ITEM);
        }
    }

    public static void registerColorHandlers() {
        for (Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if (block.getClass() != BlockFallingTint.class
                    || !new ResourceLocation(RefStrings.MODID, "tile.laythe_silt").equals(block.getRegistryName())) continue;
            Item item = Item.getItemFromBlock(block);
            if (!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing falling_tint ItemBlock " + block.getRegistryName());
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                    (state, world, pos, tint) -> BlockFallingTint.color(block.getMetaFromState(state)), block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, tint) -> 0xFFFFFF, item);
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
            return RefStrings.MODID.equals(location.getNamespace()) && (path(location).equals("native_falling_tint/world") || path(location).equals("native_falling_tint/item"));
        }
        @Override public IModel loadModel(ResourceLocation location) {
            if(!accepts(location)) throw new IllegalArgumentException("Unknown falling_tint route " + location);
            return new Model(path(location).endsWith("/item"));
        }
    }
    private static final class Model implements IModel {
        private final boolean inventory;
        Model(boolean inventory) { this.inventory = inventory; }
        @Override public Collection<ResourceLocation> getTextures() { return Collections.singletonList(TEXTURE); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            TextureAtlasSprite sprite = sprites.apply(TEXTURE);
            List<BakedQuad> quads = new ArrayList<>();
            FaceBakery bakery = new FaceBakery();
            for(EnumFacing face : EnumFacing.values()) {
                BlockPartFace part = new BlockPartFace(null, 0, TEXTURE.toString(), new BlockFaceUV(new float[]{0,0,16,16}, 0));
                quads.add(bakery.makeBakedQuad(new Vector3f(0,0,0), new Vector3f(16,16,16), part, sprite, face, ModelRotation.X0_Y0, null, false, true));
            }
            return new FallingTintBakedModel(quads, sprite);
        }
    }
}
