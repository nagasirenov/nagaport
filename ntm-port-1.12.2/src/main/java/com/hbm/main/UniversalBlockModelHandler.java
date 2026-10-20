package com.hbm.main;

import java.util.*;
import java.util.function.Function;
import com.hbm.lib.RefStrings;
import com.hbm.blocks.generic.BlockSandbags;
import com.hbm.blocks.generic.BlockBarrier;
import com.hbm.blocks.generic.BlockWoodStructure;
import com.hbm.blocks.generic.BlockRebar;
import com.hbm.blocks.machine.rbmk.RBMKDisplay;
import com.hbm.blocks.machine.rbmk.RBMKGauge;
import com.hbm.blocks.machine.rbmk.RBMKKeyPad;
import com.hbm.render.block.ISBRHUniversal;
import com.hbm.render.block.nativeblock.UniversalModelFactory;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Registry-time native item and world routes, with no eager ItemBlock capture. */
@Mod.EventBusSubscriber(modid=RefStrings.MODID,value=Side.CLIENT)
public final class UniversalBlockModelHandler {
    private UniversalBlockModelHandler() { }
    private static final String PREFIX="native_universal/";
    private static final Map<String,String> TEXTURES=textures();
    private static final Loader LOADER=new Loader();
    private static boolean registered;
    private static Map<String,String> textures() {
        Map<String,String> result=new LinkedHashMap<>();
        result.put("tile.sandbags","sandbags");result.put("tile.wood_barrier","wood_barrier");
        result.put("tile.wood_structure","wood_barrier");result.put("tile.rebar","rebar");
        for(String name:new String[]{"rbmk_display","rbmk_key_pad","rbmk_gauge"}) result.put("tile."+name,"rbmk/rbmk_display");
        return Collections.unmodifiableMap(result);
    }
    private static Class<? extends Block> ownerClass(String name) {
        switch(name) {
        case "tile.sandbags": return BlockSandbags.class;
        case "tile.wood_barrier": return BlockBarrier.class;
        case "tile.wood_structure": return BlockWoodStructure.class;
        case "tile.rebar": return BlockRebar.class;
        case "tile.rbmk_display": return RBMKDisplay.class;
        case "tile.rbmk_key_pad": return RBMKKeyPad.class;
        case "tile.rbmk_gauge": return RBMKGauge.class;
        default: return null;
        }
    }
    private static boolean owns(Block block, ResourceLocation name) {
        return name != null && name.getNamespace().equals(RefStrings.MODID) && block.getClass() == ownerClass(name.getPath());
    }
    public static ResourceLocation route(ResourceLocation owner,int metadata,boolean inventory) {
        return new ResourceLocation(RefStrings.MODID,PREFIX+owner.getPath()+"/"+metadata+(inventory?"/item":"/world"));
    }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER);registered=true; }
        for(Block block:ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!(block instanceof ISBRHUniversal)) continue;
            ResourceLocation name=Objects.requireNonNull(block.getRegistryName(),"Universal block registry name");
            if(!owns(block,name)) continue;
            Item item=Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock)||((ItemBlock)item).getBlock()!=block) throw new IllegalStateException("Missing universal ItemBlock "+name);
            ModelLoader.setCustomStateMapper(block,new StateMapperBase() {
                @Override protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return new ModelResourceLocation(route(name,block.getMetaFromState(state),false),"normal");
                }
            });
            for(int meta=0;meta<16;meta++) ModelLoader.setCustomModelResourceLocation(item,meta,new ModelResourceLocation(route(name,meta,true),"inventory"));
        }
    }
    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager resources) { }
        @Override public boolean accepts(ResourceLocation location) { return location.getNamespace().equals(RefStrings.MODID)&&normalized(location).startsWith(PREFIX); }
        @Override public IModel loadModel(ResourceLocation location) {
            String[] fields=normalized(location).substring(PREFIX.length()).split("/");
            if(fields.length!=3||!(fields[2].equals("world")||fields[2].equals("item"))) throw new IllegalArgumentException("Invalid universal model route "+location);
            String texture=TEXTURES.get(fields[0]);
            if(texture==null) throw new IllegalArgumentException("Unknown universal model owner "+location);
            int metadata=Integer.parseInt(fields[1]);
            if(metadata<0||metadata>15) throw new IllegalArgumentException("Invalid universal metadata "+metadata);
            Block block=ForgeRegistries.BLOCKS.getValue(new ResourceLocation(RefStrings.MODID,fields[0]));
            if(block == null || block.getClass() != ownerClass(fields[0])) throw new IllegalArgumentException("Universal block not registered "+fields[0]);
            ResourceLocation material=new ResourceLocation(RefStrings.MODID,"blocks/"+texture);
            return new IModel() {
                @Override public Collection<ResourceLocation> getTextures() {
                    return fields[0].equals("tile.rebar")?Arrays.asList(material,new ResourceLocation(RefStrings.MODID,"blocks/concrete_liquid")):Collections.singleton(material);
                }
                @Override public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> sprites) {
                    return UniversalModelFactory.bake(block,metadata,fields[2].equals("item"),sprites.apply(material));
                }
            };
        }
        private static String normalized(ResourceLocation location) {
            String path=location.getPath();if(path.startsWith("models/"))path=path.substring(7);if(path.startsWith("block/"))path=path.substring(6);return path;
        }
    }
}
