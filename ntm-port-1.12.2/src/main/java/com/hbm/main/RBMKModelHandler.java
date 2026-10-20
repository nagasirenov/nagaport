package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.machine.rbmk.RBMKRenderState;
import com.hbm.blocks.machine.rbmk.RBMKRenderState.Profile;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.*;
import com.hbm.render.mesh.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.*;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Exact registry/class ownership; original model assets are reparsed on each native resource reload. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class RBMKModelHandler {
    private static final String PREFIX = "native_rbmk/";
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    private RBMKModelHandler() { }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            Profile profile = RBMKRenderState.profile(block);
            if(profile == null) continue;
            ModelLoader.setCustomStateMapper(block, owner -> {
                Map<IBlockState, ModelResourceLocation> states = new IdentityHashMap<>();
                ModelResourceLocation location = new ModelResourceLocation(RefStrings.MODID + ":" + PREFIX + profile.name + "/world", "normal");
                for(IBlockState state : owner.getBlockState().getValidStates()) states.put(state, location);
                return states;
            });
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing RBMK ItemBlock " + block.getRegistryName());
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata,
                    new ModelResourceLocation(RefStrings.MODID + ":" + PREFIX + profile.name + "/item", "inventory"));
        }
    }
    private static String path(ResourceLocation location) {
        String path = location.getPath();
        if(path.startsWith("models/")) path = path.substring(7);
        if(path.startsWith("block/")) path = path.substring(6);
        return path;
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private final Map<String, ObjMesh> meshes = new HashMap<>();
        @Override public void onResourceManagerReload(IResourceManager manager) { resources = manager; meshes.clear(); }
        @Override public boolean accepts(ResourceLocation location) {
            if(!RefStrings.MODID.equals(location.getNamespace())) return false;
            String[] fields = path(location).split("/", -1);
            return fields.length == 3 && fields[0].equals("native_rbmk") && RBMKRenderState.profile(fields[1]) != null
                    && (fields[2].equals("item") || fields[2].equals("world"));
        }
        private ObjMesh mesh(String name, boolean mixed) throws Exception {
            ObjMesh mesh = meshes.get(name);
            if(mesh == null) {
                ResourceLocation asset = new ResourceLocation(RefStrings.MODID, "models/rbmk/" + name + ".obj");
                try(IResource resource = resources.getResource(asset);
                        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    mesh = ObjMeshParser.parse(reader, asset.toString(), mixed);
                }
                meshes.put(name, mesh);
            }
            return mesh;
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            if(!accepts(location)) throw new IllegalArgumentException("Unknown RBMK model route " + location);
            if(resources == null) throw new IllegalStateException("RBMK model loader has no resource manager");
            String[] fields = path(location).split("/");
            Profile profile = RBMKRenderState.profile(fields[1]);
            boolean item = fields[2].equals("item");
            ObjMesh element = null, rods = null, control = null;
            if(profile.family == RBMKRenderState.Family.ROD) {
                element = mesh("rbmk_element", true);
                if(item) rods = mesh("rbmk_element_rods", false);
            }
            if(item && profile.ownLid) control = mesh("rbmk_rods", false);
            return new ColumnModel(profile, item, element, rods, control);
        }
    }
    private static final class ColumnModel implements IModel {
        private final Profile profile;
        private final boolean item;
        private final ObjMesh element, rods, control;
        ColumnModel(Profile profile, boolean item, ObjMesh element, ObjMesh rods, ObjMesh control) {
            this.profile = profile; this.item = item; this.element = element; this.rods = rods; this.control = control;
        }
        @Override public Collection<ResourceLocation> getTextures() { return RBMKMaterials.textures(profile); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            return new RBMKBakedModel(new RBMKMaterials(profile, sprites), element, rods, control, item);
        }
    }
}
