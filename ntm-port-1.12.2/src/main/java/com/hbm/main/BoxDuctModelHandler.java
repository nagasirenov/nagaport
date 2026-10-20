package com.hbm.main;

import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.network.BoxDuctRenderState;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.BoxDuctBakedModel;
import com.hbm.render.block.nativeblock.BoxDuctMaterials;
import com.hbm.render.block.nativeblock.BoxDuctMaterials.Kind;
import com.hbm.render.item.nativeitem.AngelCoreFrameCorrection;
import com.hbm.render.tileentity.BoxDuctBreakingRenderer;
import com.hbm.tileentity.network.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

/** Three original registered box-duct identities, metadata models, and original non-retextured damage. */
@Mod.EventBusSubscriber(modid=RefStrings.MODID,value=Side.CLIENT)
public final class BoxDuctModelHandler {
    private static final String[] NAMES={"tile.fluid_duct_box","tile.fluid_duct_exhaust","tile.red_cable_box"};
    private static final Loader LOADER=new Loader();
    private static final Set<Item> OWNED_ITEMS=Collections.newSetFromMap(new IdentityHashMap<Item,Boolean>());
    private static boolean registered;
    private BoxDuctModelHandler() { }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) {
            ModelLoaderRegistry.registerLoader(LOADER);
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPipeBaseNT.class,new BoxDuctBreakingRenderer<>());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPipeExhaust.class,new BoxDuctBreakingRenderer<>());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCableBaseNT.class,new BoxDuctBreakingRenderer<>());
            registered=true;
        }
        OWNED_ITEMS.clear();
        for(Block block:ForgeRegistries.BLOCKS.getValuesCollection()) {
            int family=BoxDuctRenderState.family(block);if(family<0)continue;
            Item item=Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock)||((ItemBlock)item).getBlock()!=block) throw new IllegalStateException("Missing box-duct ItemBlock "+block.getRegistryName());
            for(int metadata=0;metadata<16;metadata++) ModelLoader.setCustomModelResourceLocation(item,metadata,
                    new ModelResourceLocation(new ResourceLocation(RefStrings.MODID,"native_boxduct/"+NAMES[family]+"/"+metadata+"/item"),"inventory"));
            OWNED_ITEMS.add(item);
        }
    }
    @SubscribeEvent public static void frame(RenderItemInFrameEvent event) {
        if(event.getItem().isEmpty() || !OWNED_ITEMS.contains(event.getItem().getItem()))return;
        double[] delta=AngelCoreFrameCorrection.translation(event.getEntityItemFrame().getRotation());
        GL11.glTranslated(delta[0],delta[1],delta[2]);
    }
    private static String path(ResourceLocation location) {
        String path=location.getPath();if(path.startsWith("models/"))path=path.substring(7);if(path.startsWith("block/"))path=path.substring(6);return path;
    }
    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager resources) { }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace())&&path(location).matches("native_boxduct/tile\\.(fluid_duct_box|fluid_duct_exhaust|red_cable_box)/([0-9]|1[0-5])/(world|item)");
        }
        @Override public IModel loadModel(ResourceLocation location) {
            if(!accepts(location))throw new IllegalArgumentException("Invalid box-duct model route "+location);
            String[] parts=path(location).split("/");int family=Arrays.asList(NAMES).indexOf(parts[1]);
            return new DuctModel(Kind.values()[family],Integer.parseInt(parts[2]),parts[3].equals("item"));
        }
    }
    private static final class DuctModel implements IModel {
        final Kind kind;final int metadata;final boolean inventory;
        DuctModel(Kind kind,int metadata,boolean inventory) { this.kind=kind;this.metadata=metadata;this.inventory=inventory; }
        @Override public Collection<ResourceLocation> getTextures() { return BoxDuctMaterials.textures(kind); }
        @Override public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> sprites) {
            return new BoxDuctBakedModel(new BoxDuctMaterials(kind,sprites),metadata,inventory);
        }
    }
}
