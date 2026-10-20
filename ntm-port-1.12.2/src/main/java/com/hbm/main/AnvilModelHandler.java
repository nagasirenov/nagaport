package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import com.hbm.blocks.machine.NTMAnvil;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.RenderAnvil;
import com.hbm.render.block.nativeblock.AtlasObjBakedModel;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
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

/** Reload-safe native atlas routing for the twelve original anvils and all metadata values. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class AnvilModelHandler {
    private AnvilModelHandler() { }
    private static final String PREFIX = "native_anvil/";
    private static final Collection<String> SKINS = Arrays.asList("iron", "lead", "steel", "desh", "ferrouranium",
            "saturnite", "bismuth_bronze", "arsenic_bronze", "schrabidate", "dnt", "osmiridium", "murky");
    private static boolean registered;
    private static ModelResourceLocation route(String skin, int metadata, boolean inventory) {
        return new ModelResourceLocation(RefStrings.MODID + ":" + PREFIX + skin + "/" + metadata + "/"
                + (inventory ? "item" : "world"), inventory ? "inventory" : "normal");
    }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(new Loader()); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) if(block.getClass() == NTMAnvil.class) {
            ResourceLocation name = block.getRegistryName();
            if(name == null || !RefStrings.MODID.equals(name.getNamespace()) || !name.getPath().startsWith("tile.anvil_"))
                continue;
            String skin = name.getPath().substring("tile.anvil_".length());
            if(!SKINS.contains(skin)) continue;
            ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
                @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return route(skin, block.getMetaFromState(state), false);
                }
            });
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing anvil ItemBlock " + name);
            for(int metadata = 0; metadata < 16; metadata++)
                ModelLoader.setCustomModelResourceLocation(item, metadata, route(skin, metadata, true));
        }
    }
    private static String normalized(ResourceLocation location) {
        String path = location.getPath();
        if(path.startsWith("models/")) path = path.substring(7);
        if(path.startsWith("block/")) path = path.substring(6);
        return path;
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private ObjMesh mesh;
        @Override public void onResourceManagerReload(IResourceManager resources) { this.resources = resources; mesh = null; }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && normalized(location).startsWith(PREFIX);
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            String[] fields = normalized(location).substring(PREFIX.length()).split("/");
            if(fields.length != 3 || !SKINS.contains(fields[0]) || !(fields[2].equals("item") || fields[2].equals("world")))
                throw new IllegalArgumentException("Invalid anvil route " + location);
            int metadata = Integer.parseInt(fields[1]);
            if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Invalid anvil metadata " + metadata);
            if(mesh == null) {
                ResourceLocation asset = new ResourceLocation(RefStrings.MODID, "models/blocks/anvil.obj");
                try(IResource resource = resources.getResource(asset);
                        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    mesh = ObjMeshParser.parse(reader, asset.toString(), false);
                }
            }
            return new Model(mesh, fields[0], metadata, fields[2].equals("item"));
        }
    }
    private static final class Model implements IModel {
        private final ObjMesh mesh;
        private final ResourceLocation top, side;
        private final int metadata;
        private final boolean inventory;
        Model(ObjMesh mesh, String skin, int metadata, boolean inventory) {
            this.mesh = mesh; this.metadata = metadata; this.inventory = inventory;
            top = new ResourceLocation(RefStrings.MODID, "blocks/anvil_" + skin);
            side = new ResourceLocation(RefStrings.MODID, "blocks/anvil_" + (skin.equals("murky") ? "steel" : skin));
        }
        @Override public Collection<ResourceLocation> getTextures() { return Arrays.asList(top, side); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            return new AtlasObjBakedModel(RenderAnvil.bake(mesh, metadata, inventory, sprites.apply(top), sprites.apply(side)), sprites.apply(side));
        }
    }
}
