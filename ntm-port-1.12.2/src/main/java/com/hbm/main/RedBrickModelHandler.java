package com.hbm.main;

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockRedBrick;
import com.hbm.lib.RefStrings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Original six-face metadata material selection, including raw nibble values 6..15. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class RedBrickModelHandler {
    private static final String PREFIX = "native_red_brick/";
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    private RedBrickModelHandler() { }

    @SubscribeEvent public static void register(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        Block block = ModBlocks.brick_red;
        if(block == null || block.getClass() != BlockRedBrick.class)
            throw new IllegalStateException("Unexpected red brick geometry owner");
        ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
            @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return location(state.getValue(BlockRedBrick.METADATA), "normal");
            }
        });
        Item item = Item.getItemFromBlock(block);
        if(!(item instanceof ItemBlock) || ((ItemBlock) item).getBlock() != block)
            throw new IllegalStateException("Missing red brick ItemBlock");
        // This standard ItemBlock has no item subtypes: legacy inventory used meta 0.
        ModelResourceLocation inventory = location(0, "inventory");
        ModelLoader.setCustomModelResourceLocation(item, 0, inventory);
        ModelLoader.setCustomMeshDefinition(item, stack -> inventory);
    }

    private static ModelResourceLocation location(int metadata, String variant) {
        return new ModelResourceLocation(RefStrings.MODID + ":" + PREFIX + metadata, variant);
    }

    private static String path(ResourceLocation location) {
        String path = location.getPath();
        if(path.startsWith("models/")) path = path.substring(7);
        if(path.startsWith("block/")) path = path.substring(6);
        if(path.startsWith("item/")) path = path.substring(5);
        return path;
    }

    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager manager) { }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && path(location).startsWith(PREFIX);
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            int metadata = Integer.parseInt(path(location).substring(PREFIX.length()));
            if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Invalid red brick metadata " + metadata);
            Map<String, String> textures = new HashMap<>();
            for(EnumFacing face : EnumFacing.values()) {
                String texture = face.getIndex() == metadata
                        ? (metadata < 2 ? "brick_red_top" : "brick_red") : "brick_base";
                textures.put(face.getName(), "hbm:blocks/" + texture);
            }
            textures.put("particle", "hbm:blocks/brick_base");
            return ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/cube"))
                    .retexture(ImmutableMap.copyOf(textures));
        }
    }
}
