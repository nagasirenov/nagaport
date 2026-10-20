package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import com.hbm.blocks.generic.BlockDecoCRT;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.CRTBakedModel;
import com.hbm.render.item.nativeitem.AngelCoreFrameCorrection;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

/** Native CRT-only owner, resource and item-frame route. Item identity resolves after registry events. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class CRTModelHandler {
    private static final ResourceLocation OWNER = new ResourceLocation(RefStrings.MODID, "tile.deco_crt");
    private static final ResourceLocation ASSET = new ResourceLocation(RefStrings.MODID, "models/blocks/crt.obj");
    private static final String PREFIX = "native_crt/tile.deco_crt/";
    private static final String[] MATERIALS = { "crt_clean", "crt_broken", "crt_blinking", "crt_bsod" };
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    private static Item ownedItem;
    private CRTModelHandler() { }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        ownedItem = null;
        Block block = ForgeRegistries.BLOCKS.getValue(OWNER);
        if(block == null || block.getClass() != BlockDecoCRT.class || !OWNER.equals(block.getRegistryName())) return;
        Item item = Item.getItemFromBlock(block);
        if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
            throw new IllegalStateException("Missing CRT ItemBlock " + OWNER);
        for(int variant = 0; variant < MATERIALS.length; variant++)
            ModelLoader.setCustomModelResourceLocation(item, variant, new ModelResourceLocation(
                    new ResourceLocation(RefStrings.MODID, PREFIX + variant + "/item"), "inventory"));
        ownedItem = item;
    }
    @SubscribeEvent public static void frame(RenderItemInFrameEvent event) {
        if(ownedItem == null || event.getItem().isEmpty() || event.getItem().getItem() != ownedItem) return;
        double[] delta = AngelCoreFrameCorrection.translation(event.getEntityItemFrame().getRotation());
        GL11.glTranslated(delta[0], delta[1], delta[2]);
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private ObjMesh mesh;
        @Override public void onResourceManagerReload(IResourceManager resources) { this.resources = resources; mesh = null; }
        @Override public boolean accepts(ResourceLocation location) {
            if(!RefStrings.MODID.equals(location.getNamespace())) return false;
            String path = normalized(location);
            if(!path.startsWith(PREFIX)) return false;
            String[] fields = path.substring(PREFIX.length()).split("/", -1);
            return fields.length == 2 && (fields[1].equals("world") && fields[0].matches("[0-9]|1[0-5]")
                    || fields[1].equals("item") && fields[0].matches("[0-3]"));
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            if(!accepts(location)) throw new IllegalArgumentException("Invalid CRT model route " + location);
            String[] fields = normalized(location).substring(PREFIX.length()).split("/");
            if(resources == null) throw new IllegalStateException("CRT model resources are not loaded");
            if(mesh == null) {
                try(IResource resource = resources.getResource(ASSET);
                        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    mesh = ObjMeshParser.parse(reader, ASSET.toString(), false);
                }
            }
            return new CRTModel(mesh, Integer.parseInt(fields[0]), fields[1].equals("item"));
        }
        private static String normalized(ResourceLocation location) {
            String path = location.getPath();
            if(path.startsWith("models/")) path = path.substring(7);
            if(path.startsWith("block/")) path = path.substring(6);
            return path;
        }
    }
    private static final class CRTModel implements IModel {
        private final ObjMesh mesh;
        private final int metadata;
        private final boolean inventory;
        CRTModel(ObjMesh mesh, int metadata, boolean inventory) { this.mesh = mesh; this.metadata = metadata; this.inventory = inventory; }
        private ResourceLocation texture() {
            return new ResourceLocation(RefStrings.MODID, "blocks/" + MATERIALS[inventory ? metadata : metadata / 4]);
        }
        @Override public Collection<ResourceLocation> getTextures() { return Collections.singleton(texture()); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            return new CRTBakedModel(mesh, metadata, inventory, sprites.apply(texture()));
        }
    }
}
