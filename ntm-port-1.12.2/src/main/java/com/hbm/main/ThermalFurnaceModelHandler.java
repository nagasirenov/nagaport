package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import com.hbm.blocks.machine.*;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.DiFurnaceExtensionBakedModel;
import com.hbm.render.block.nativeblock.ThermalFurnaceBakedModel;
import com.hbm.render.block.nativeblock.ThermalFurnaceBakedModel.Family;
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
import net.minecraft.util.EnumFacing;
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

/** Native model-loader ownership for exactly the four single-block thermal furnace types and their extension. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class ThermalFurnaceModelHandler {
    private ThermalFurnaceModelHandler() { }
    private static final String PREFIX = "native_thermal/";
    private static final Loader LOADER = new Loader();
    private static boolean registered;

    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) {
            ModelLoaderRegistry.registerLoader(LOADER);
            registered = true;
        }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            ResourceLocation name = block.getRegistryName();
            if(name == null || !RefStrings.MODID.equals(name.getNamespace())) continue;
            Family family = family(block);
            if(family == null && !(block instanceof MachineDiFurnaceExtension)) continue;
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing thermal ItemBlock: " + name);
            for(int metadata = 0; metadata < 16; metadata++) {
                String path = family == null ? PREFIX + "extension/true"
                        : modelPath(family, ((BlockThermalFurnace)block).isActive(), true, metadata, false);
                ModelLoader.setCustomModelResourceLocation(item, metadata, new ModelResourceLocation(RefStrings.MODID + ":" + path, "inventory"));
            }
        }
    }
    public static Family family(Block block) {
        if(block instanceof MachineBrickFurnace) return Family.BRICK;
        if(block instanceof MachineRtgFurnace) return Family.RTG;
        if(block instanceof MachineDiFurnaceRTG) return Family.RTG_DIFURNACE;
        if(block instanceof MachineDiFurnace) return Family.DIFURNACE;
        return null;
    }
    public static String modelPath(Family family, boolean active, boolean inventory, int metadata, boolean covered) {
        return PREFIX + family.name().toLowerCase(Locale.ROOT) + "/" + active + "/" + inventory + "/" + metadata + "/" + covered;
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private ObjMesh extension;
        @Override public void onResourceManagerReload(IResourceManager manager) { resources = manager; extension = null; }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && normalizedPath(location).startsWith(PREFIX);
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            String path = normalizedPath(location);
            String[] fields = path.substring(PREFIX.length()).split("/");
            if(fields.length == 2 && fields[0].equals("extension")) {
                if(extension == null) {
                    ResourceLocation asset = new ResourceLocation(RefStrings.MODID, "models/blocks/difurnace_extension.obj");
                    try(IResource resource = resources.getResource(asset);
                            InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                        extension = ObjMeshParser.parse(reader, asset.toString(), false);
                    }
                }
                return new ThermalModel(extension, parseBoolean(fields[1]));
            }
            if(fields.length != 5) throw new IllegalArgumentException("Invalid native thermal model " + location);
            return new ThermalModel(Family.valueOf(fields[0].toUpperCase(Locale.ROOT)), parseBoolean(fields[1]),
                    parseBoolean(fields[2]), Integer.parseInt(fields[3]), parseBoolean(fields[4]));
        }
        private static String normalizedPath(ResourceLocation location) {
            String path = location.getPath();
            // Native blockstate Variant adds block/, then ModelLoaderRegistry adds models/.
            // Direct inventory ModelResourceLocations retain the original path.
            if(path.startsWith("models/")) path = path.substring(7);
            if(path.startsWith("block/")) path = path.substring(6);
            return path;
        }
        private static boolean parseBoolean(String value) {
            if(!value.equals("true") && !value.equals("false")) throw new IllegalArgumentException("Invalid boolean " + value);
            return Boolean.parseBoolean(value);
        }
    }
    private static final class ThermalModel implements IModel {
        private final Family family;
        private final boolean active, inventory, covered;
        private final int metadata;
        private final ObjMesh extension;
        ThermalModel(Family family, boolean active, boolean inventory, int metadata, boolean covered) {
            if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Invalid thermal metadata " + metadata);
            this.family = family; this.active = active; this.inventory = inventory;
            this.metadata = metadata; this.covered = covered; this.extension = null;
        }
        ThermalModel(ObjMesh mesh, boolean inventory) {
            this.family = null; this.active = false; this.inventory = inventory;
            this.metadata = 0; this.covered = false; this.extension = mesh;
        }
        @Override public Collection<ResourceLocation> getTextures() {
            Set<ResourceLocation> textures = new LinkedHashSet<>();
            if(extension != null) {
                for(String name : new String[] {"difurnace_top_off_alt", "brick_fire", "difurnace_extension"})
                    textures.add(new ResourceLocation(RefStrings.MODID, "blocks/" + name));
            } else {
                for(EnumFacing side : EnumFacing.values()) textures.add(new ResourceLocation(ThermalFurnaceBakedModel.texture(family, active, inventory, covered, metadata, side)));
                textures.add(new ResourceLocation(ThermalFurnaceBakedModel.texture(family, active, true, false, metadata, EnumFacing.DOWN)));
            }
            return textures;
        }
        @Override public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> sprites) {
            Function<String, TextureAtlasSprite> atlas = name -> sprites.apply(new ResourceLocation(name));
            return extension != null ? new DiFurnaceExtensionBakedModel(extension, inventory, atlas)
                    : new ThermalFurnaceBakedModel(family, active, inventory, metadata, covered, atlas);
        }
    }
}
