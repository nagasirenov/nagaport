package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.generic.BlockAlgaeFilm;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.RenderAlgaeFilm;
import com.hbm.render.block.nativeblock.AtlasObjBakedModel;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
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

/** Automatic native loader registration; every resource reload replaces the source mesh. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class AlgaeFilmModelHandler {
    private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID, "blocks/algae_film");
    private static final ResourceLocation OBJ = new ResourceLocation(RefStrings.MODID, "models/blocks/algae_film.obj");
    private static final String PREFIX = "native_algae_film/";
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    private AlgaeFilmModelHandler() { }
    @SubscribeEvent public static void register(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) if(isOriginalOwner(block)) {
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing algae film ItemBlock " + block.getRegistryName());
            for(int meta = 0; meta < 16; meta++) ModelLoader.setCustomModelResourceLocation(item, meta,
                    new ModelResourceLocation(RefStrings.MODID + ":" + PREFIX + "item", "inventory"));
        }
    }
    private static boolean isOriginalOwner(Block block) {
        return block.getClass() == BlockAlgaeFilm.class
                && new ResourceLocation(RefStrings.MODID, "tile.algae_film").equals(block.getRegistryName());
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private ObjMesh mesh;
        @Override public void onResourceManagerReload(IResourceManager resources) { this.resources = resources; mesh = null; }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && path(location).startsWith(PREFIX);
        }
        private String path(ResourceLocation location) {
            String p = location.getPath();
            if(p.startsWith("models/")) p = p.substring(7);
            if(p.startsWith("block/")) p = p.substring(6);
            return p;
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            String route = path(location).substring(PREFIX.length());
            boolean item = route.equals("item");
            int meta = item ? 2 : Integer.parseInt(route);
            if(!item && (meta < 2 || meta > 5)) throw new IllegalArgumentException("Invalid algae film route " + location);
            if(mesh == null) try(IResource resource = resources.getResource(OBJ);
                    InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                mesh = ObjMeshParser.parse(reader, OBJ.toString(), false);
            }
            return new Model(mesh, meta, item);
        }
    }
    private static final class Model implements IModel {
        private final ObjMesh mesh;
        private final int meta;
        private final boolean item;
        Model(ObjMesh mesh, int meta, boolean item) { this.mesh = mesh; this.meta = meta; this.item = item; }
        @Override public Collection<ResourceLocation> getDependencies() { return Collections.emptyList(); }
        @Override public Collection<ResourceLocation> getTextures() { return Collections.singleton(TEXTURE); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> getter) {
            TextureAtlasSprite sprite = getter.apply(TEXTURE);
            // Native block/block display transforms center Y by -0.5 at render time,
            // equivalent to the original inventory GL translation.
            ItemCameraTransforms transforms = new AtlasObjBakedModel(Collections.<BakedQuad>emptyList(), sprite).getItemCameraTransforms();
            return new RenderAlgaeFilm(mesh, sprite, meta, item, transforms);
        }
    }
}
