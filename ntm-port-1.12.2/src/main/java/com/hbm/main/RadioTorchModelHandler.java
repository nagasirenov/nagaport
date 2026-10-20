package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import com.google.common.collect.ImmutableList;
import com.hbm.blocks.network.*;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.RadioTorchBakedModel;
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
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import com.hbm.render.tileentity.RadioTorchBreakingRenderer;
import com.hbm.tileentity.network.*;

/** Exactly the six registered original RTTY torches; programmer remains unregistered as before. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class RadioTorchModelHandler {
    private static final String[] NAMES = {"sender", "receiver", "counter", "logic", "reader", "controller"};
    private static final Class<?>[] OWNERS = {RadioTorchSender.class, RadioTorchReceiver.class, RadioTorchCounter.class,
            RadioTorchLogic.class, RadioTorchReader.class, RadioTorchController.class};
    private static final String[] OFF = {"rtty_sender_off", "rtty_rec_off", "rtty_counter", "rtty_logic_off", "rtty_reader", "rtty_controller"};
    private static final String[] ON = {"rtty_sender_on", "rtty_rec_on", "rtty_counter", "rtty_logic_on", "rtty_reader", "rtty_controller"};
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    private RadioTorchModelHandler() { }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) {
            ModelLoaderRegistry.registerLoader(LOADER);
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRadioTorchSender.class, new RadioTorchBreakingRenderer<>());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRadioTorchReceiver.class, new RadioTorchBreakingRenderer<>());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRadioTorchCounter.class, new RadioTorchBreakingRenderer<>());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRadioTorchLogic.class, new RadioTorchBreakingRenderer<>());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRadioTorchReader.class, new RadioTorchBreakingRenderer<>());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRadioTorchController.class, new RadioTorchBreakingRenderer<>());
            registered = true;
        }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            ResourceLocation name = block.getRegistryName();
            if(name == null || !RefStrings.MODID.equals(name.getNamespace())) continue;
            int family = family(name.getPath());
            if(family < 0 || block.getClass() != OWNERS[family]) continue;
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing radio torch ItemBlock " + name);
            for(int metadata = 0; metadata < 16; metadata++) ModelLoader.setCustomModelResourceLocation(item, metadata,
                    new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, "native_rtty/" + name.getPath() + "/item"), "inventory"));
        }
    }
    private static int family(String name) {
        for(int i = 0; i < NAMES.length; i++) if(name.equals("tile.radio_torch_" + NAMES[i])) return i;
        return -1;
    }
    private static String path(ResourceLocation location) {
        String path = location.getPath();
        if(path.startsWith("models/")) path = path.substring(7);
        if(path.startsWith("block/")) path = path.substring(6);
        return path;
    }
    private static ResourceLocation texture(int family, boolean active) {
        return new ResourceLocation(RefStrings.MODID, "blocks/" + (active ? ON : OFF)[family]);
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private ObjMesh mesh;
        @Override public void onResourceManagerReload(IResourceManager resources) { this.resources = resources; mesh = null; }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && path(location).matches("native_rtty/tile\\.radio_torch_(sender|receiver|counter|logic|reader|controller)/(item|([0-9]|1[0-5])/world)");
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            if(!accepts(location)) throw new IllegalArgumentException("Unknown radio torch model route " + location);
            String[] fields = path(location).split("/");
            int family = family(fields[1]);
            if(fields[2].equals("item")) return new ItemLayerModel(ImmutableList.of(texture(family, false)));
            if(mesh == null) {
                ResourceLocation asset = new ResourceLocation(RefStrings.MODID, "models/blocks/rtty.obj");
                try(IResource resource = resources.getResource(asset);
                        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    mesh = ObjMeshParser.parse(reader, asset.toString(), false);
                }
            }
            return new TorchModel(mesh, family, Integer.parseInt(fields[2]));
        }
    }
    private static final class TorchModel implements IModel {
        private final ObjMesh mesh;
        private final int family, metadata;
        TorchModel(ObjMesh mesh, int family, int metadata) { this.mesh = mesh; this.family = family; this.metadata = metadata; }
        @Override public Collection<ResourceLocation> getTextures() { return Arrays.asList(texture(family, false), texture(family, true)); }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            return new RadioTorchBakedModel(mesh, metadata, sprites.apply(texture(family, false)), sprites.apply(texture(family, true)));
        }
    }
}
