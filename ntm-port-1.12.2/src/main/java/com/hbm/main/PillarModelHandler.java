package com.hbm.main;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.machine.BlockPillar;
import com.hbm.blocks.machine.BlockPillarPWR;
import com.hbm.lib.RefStrings;
import com.hbm.render.model.PlainCubeModel;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
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

/** Native model/atlas lifecycle for the two audited pillar owners only.
 * Inheritance, state, registration, item classes and tooltip behavior are untouched.
 * ModelLoaderRegistry owns reload caching; this handler retains no baked sprites.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class PillarModelHandler {
    private static final String PREFIX = "native_pillar/";
    private static final Map<String, BlockPillar> BLOCKS = new HashMap<>();
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    private PillarModelHandler() { }

    @SubscribeEvent public static void register(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        BLOCKS.clear();
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(block.getClass() != BlockPillar.class && block.getClass() != BlockPillarPWR.class) continue;
            ResourceLocation name = block.getRegistryName();
            if(name == null || !RefStrings.MODID.equals(name.getNamespace())) continue;
            final String route = PREFIX + name.getPath();
            BLOCKS.put(route, (BlockPillar) block);
            ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
                @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return new ModelResourceLocation(RefStrings.MODID + ":" + route, "normal");
                }
            });
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock) item).getBlock() != block)
                throw new IllegalStateException("Missing pillar ItemBlock " + name);
            final ModelResourceLocation inventory = new ModelResourceLocation(
                    RefStrings.MODID + ":" + route, "inventory");
            ModelLoader.setCustomModelResourceLocation(item, 0, inventory);
            // Original getIcon ignores all raw item metadata, not merely 0..15.
            ModelLoader.setCustomMeshDefinition(item, stack -> inventory);
        }
    }

    private static ResourceLocation texture(String name) {
        ResourceLocation texture = new ResourceLocation(name);
        return new ResourceLocation(texture.getNamespace(), "blocks/" + texture.getPath());
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
            BlockPillar block = BLOCKS.get(path(location));
            if(block == null) throw new IllegalArgumentException("Unknown pillar route " + location);
            String top = texture(block.getTextureName(0)).toString();
            String side = texture(block.getTextureName(2)).toString();
            IModel cube = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/cube"))
                    .retexture(ImmutableMap.<String, String>builder()
                            .put("down", top).put("up", top).put("particle", top)
                            .put("north", side).put("south", side).put("west", side).put("east", side).build());
            return new PillarModel(new PlainCubeModel(cube), texture(block.getAlternateTextureName()));
        }
    }

    /** The alternate icon was registered in 1.7 even though no face selected it. */
    private static final class PillarModel implements IModel {
        private final IModel cube;
        private final ResourceLocation alternate;
        PillarModel(IModel cube, ResourceLocation alternate) { this.cube = cube; this.alternate = alternate; }
        @Override public Collection<ResourceLocation> getDependencies() { return cube.getDependencies(); }
        @Override public Collection<ResourceLocation> getTextures() {
            Set<ResourceLocation> textures = new HashSet<>(cube.getTextures());
            textures.add(alternate);
            return textures;
        }
        @Override public IModelState getDefaultState() { return cube.getDefaultState(); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format,
                Function<ResourceLocation, TextureAtlasSprite> textures) {
            return cube.bake(state, format, textures);
        }
    }
}
