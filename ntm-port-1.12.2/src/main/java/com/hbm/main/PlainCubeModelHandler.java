package com.hbm.main;

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.PlainCubeTextures;
import com.hbm.lib.RefStrings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Native vanilla cube geometry, atlas material and display transforms.
 * Bindings are resolved after block AND item registration, never at construction.
 * ModelLoaderRegistry owns the reload cache; no baked model/sprite is retained here.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class PlainCubeModelHandler {
    private static final String PREFIX = "native_plain_cube/";
    private static final Map<String, String> TEXTURES = new HashMap<>();
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    private PlainCubeModelHandler() { }

    @SubscribeEvent public static void register(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        TEXTURES.clear();
        for(Map.Entry<Block, String> entry : PlainCubeTextures.bindings().entrySet()) {
            Block block = entry.getKey();
            ResourceLocation name = block.getRegistryName();
            if(name == null || !RefStrings.MODID.equals(name.getNamespace()))
                throw new IllegalStateException("Unregistered NTM plain cube " + block);
            final String route = PREFIX + name.getPath();
            ResourceLocation texture = new ResourceLocation(entry.getValue());
            TEXTURES.put(route, new ResourceLocation(texture.getNamespace(), "blocks/" + texture.getPath()).toString());
            ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
                @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return new ModelResourceLocation(RefStrings.MODID + ":" + route, "normal");
                }
            });
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing plain cube ItemBlock " + name);
            final ModelResourceLocation inventory = new ModelResourceLocation(
                    RefStrings.MODID + ":" + route, "inventory");
            // Register the bake dependency once; the original icon ignores ALL raw
            // stack damage, not only the 0..15 block-state metadata range.
            ModelLoader.setCustomModelResourceLocation(item, 0, inventory);
            ModelLoader.setCustomMeshDefinition(item, stack -> inventory);
        }
    }

    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager resources) { }
        private String path(ResourceLocation location) {
            String path = location.getPath();
            if(path.startsWith("models/")) path = path.substring(7);
            if(path.startsWith("block/")) path = path.substring(6);
            return path;
        }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && path(location).startsWith(PREFIX);
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            String texture = TEXTURES.get(path(location));
            if(texture == null) throw new IllegalArgumentException("Unknown plain cube route " + location);
            return new com.hbm.render.model.PlainCubeModel(
                    ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/cube_all"))
                    .retexture(ImmutableMap.of("all", texture)));
        }
    }
}
