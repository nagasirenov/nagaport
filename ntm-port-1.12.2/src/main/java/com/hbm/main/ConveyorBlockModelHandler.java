package com.hbm.main;

import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.network.*;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.ConveyorBakedModel;
import com.hbm.render.block.nativeblock.ConveyorBakedModel.Kind;
import com.hbm.render.block.nativeblock.ConveyorMaterials;
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
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Native states/models for the six registered conveyors; no old renderer registry or early ItemBlock lookup. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class ConveyorBlockModelHandler {
    private ConveyorBlockModelHandler() { }
    private static final String PREFIX = "native_conveyor/";
    private static final ResourceLocation LIFT_ICON = new ResourceLocation(RefStrings.MODID,"item/conveyor_lift");
    private static final Map<ResourceLocation,Definition> DEFINITIONS = definitions();
    private static final Loader LOADER = new Loader();
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered = true; }
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!(block instanceof BlockConveyorBase || block instanceof BlockConveyorClassic)) continue;
            ResourceLocation name = Objects.requireNonNull(block.getRegistryName(),"Conveyor registry name");
            if(block instanceof BlockConveyorClassic) {
                // Classic is not instantiated in this mod. An explicit native material is required
                // before a separately registered Classic can opt into this same slab pipeline.
                DEFINITIONS.put(name,new Definition(BlockConveyorClassic.class,Kind.CLASSIC,((BlockConveyorClassic)block).getModelTexture()));
                ModelLoader.setCustomStateMapper(block,new StateMapperBase() {
                    @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                        return new ModelResourceLocation(route(name,block.getMetaFromState(state),false),"normal");
                    }
                });
            }
            Definition definition = DEFINITIONS.get(name);
            if(definition == null || block.getClass() != definition.owner)
                throw new IllegalArgumentException("Unspecified conveyor owner " + name);
            Item item = Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock() != block)
                throw new IllegalStateException("Missing conveyor ItemBlock " + name);
            for(int metadata = 0; metadata < 16; metadata++)
                ModelLoader.setCustomModelResourceLocation(item,metadata,inventoryModelLocation(block,metadata));
        }
    }
    /** Exact inventory route for native conveyor-wand reuse; never substitute world-neighbor geometry. */
    public static ModelResourceLocation inventoryModelLocation(Block block, int metadata) {
        return new ModelResourceLocation(route(Objects.requireNonNull(block.getRegistryName()),metadata,true),"inventory");
    }
    private static ResourceLocation route(ResourceLocation owner, int metadata, boolean inventory) {
        if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Conveyor metadata " + metadata);
        return new ResourceLocation(RefStrings.MODID,PREFIX+owner.getNamespace()+"/"+owner.getPath()+"/"+metadata+(inventory?"/item":"/world"));
    }
    private static Map<ResourceLocation,Definition> definitions() {
        Map<ResourceLocation,Definition> result = new LinkedHashMap<>();
        add(result,"conveyor",BlockConveyor.class,Kind.BELT,"conveyor");
        add(result,"conveyor_express",BlockConveyorExpress.class,Kind.BELT,"conveyor_express");
        add(result,"conveyor_double",BlockConveyorDouble.class,Kind.BELT,"conveyor_double");
        add(result,"conveyor_triple",BlockConveyorTriple.class,Kind.BELT,"conveyor_triple");
        add(result,"conveyor_lift",BlockConveyorLift.class,Kind.LIFT,"conveyor");
        add(result,"conveyor_chute",BlockConveyorChute.class,Kind.CHUTE,"conveyor");
        return result;
    }
    private static void add(Map<ResourceLocation,Definition> target,String name,Class<? extends Block> owner,Kind kind,String texture) {
        target.put(new ResourceLocation(RefStrings.MODID,"tile."+name),new Definition(owner,kind,new ResourceLocation(RefStrings.MODID,"blocks/"+texture)));
    }
    private static final class Definition {
        final Class<? extends Block> owner;
        final Kind kind;
        final ResourceLocation belt;
        Definition(Class<? extends Block> owner,Kind kind,ResourceLocation belt) { this.owner=owner;this.kind=kind;this.belt=belt; }
        ResourceLocation suffix(String suffix) { return new ResourceLocation(belt.getNamespace(),belt.getPath()+suffix); }
        List<ResourceLocation> textures() {
            List<ResourceLocation> textures = new ArrayList<>();textures.add(belt);textures.add(new ResourceLocation(RefStrings.MODID,"blocks/conveyor_side"));
            if(kind == Kind.BELT) { textures.add(suffix("_curve_left")); textures.add(suffix("_curve_right")); }
            if(kind == Kind.LIFT || kind == Kind.CHUTE) textures.add(new ResourceLocation(RefStrings.MODID,"blocks/concrete"));
            if(kind == Kind.LIFT) textures.add(new ResourceLocation("minecraft","blocks/iron_block"));
            if(kind == Kind.CHUTE) textures.add(new ResourceLocation(RefStrings.MODID,"blocks/grate_top"));
            return Collections.unmodifiableList(textures);
        }
    }
    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager resources) { }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && normalized(location).startsWith(PREFIX);
        }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            String[] fields = normalized(location).substring(PREFIX.length()).split("/");
            if(fields.length != 4 || !(fields[3].equals("world") || fields[3].equals("item")))
                throw new IllegalArgumentException("Invalid conveyor model route " + location);
            Definition definition = DEFINITIONS.get(new ResourceLocation(fields[0],fields[1]));
            if(definition == null) throw new IllegalArgumentException("Unknown conveyor model " + location);
            int metadata = Integer.parseInt(fields[2]);
            if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Conveyor metadata " + metadata);
            boolean inventory = fields[3].equals("item");
            // Original lift shouldRender3DInInventory=false: use its real flat atlas icon,
            // not the empty legacy 3D callback and not invented lift inventory geometry.
            if(definition.kind == Kind.LIFT && inventory) return ModelLoaderRegistry.getModel(LIFT_ICON);
            return new ConveyorModel(definition,metadata,inventory);
        }
        private static String normalized(ResourceLocation location) {
            String path=location.getPath();
            if(path.startsWith("models/")) path=path.substring(7);
            if(path.startsWith("block/")) path=path.substring(6);
            return path;
        }
    }
    private static final class ConveyorModel implements IModel {
        final Definition definition;
        final int metadata;
        final boolean inventory;
        ConveyorModel(Definition definition,int metadata,boolean inventory) { this.definition=definition;this.metadata=metadata;this.inventory=inventory; }
        @Override public Collection<ResourceLocation> getTextures() { return definition.textures(); }
        @Override public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> sprites) {
            TextureAtlasSprite belt=sprites.apply(definition.belt);
            TextureAtlasSprite side=sprites.apply(new ResourceLocation(RefStrings.MODID,"blocks/conveyor_side"));
            TextureAtlasSprite left=null,right=null,concrete=null,iron=null,grate=null;
            if(definition.kind == Kind.BELT) { left=sprites.apply(definition.suffix("_curve_left"));right=sprites.apply(definition.suffix("_curve_right")); }
            if(definition.kind == Kind.LIFT || definition.kind == Kind.CHUTE) concrete=sprites.apply(new ResourceLocation(RefStrings.MODID,"blocks/concrete"));
            if(definition.kind == Kind.LIFT) iron=sprites.apply(new ResourceLocation("minecraft","blocks/iron_block"));
            if(definition.kind == Kind.CHUTE) grate=sprites.apply(new ResourceLocation(RefStrings.MODID,"blocks/grate_top"));
            return new ConveyorBakedModel(definition.kind,metadata,inventory,new ConveyorMaterials(belt,side,left,right,concrete,iron,grate));
        }
    }
}
