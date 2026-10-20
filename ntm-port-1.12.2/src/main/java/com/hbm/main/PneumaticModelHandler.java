package com.hbm.main;

import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.network.PneumoTube;
import com.hbm.render.block.nativeblock.PneumaticBakedModel;
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

@Mod.EventBusSubscriber(modid="hbm",value=Side.CLIENT)
public final class PneumaticModelHandler {
    private static boolean registered;
    private static final ModelResourceLocation WORLD=new ModelResourceLocation("hbm:native_pneumatic/world","normal");
    private static final ModelResourceLocation ITEM=new ModelResourceLocation("hbm:native_pneumatic/item","inventory");
    private PneumaticModelHandler(){}
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event){
        if(!registered){ModelLoaderRegistry.registerLoader(new Loader());registered=true;}
        for(Block block:ForgeRegistries.BLOCKS.getValuesCollection())if(block.getClass()==PneumoTube.class && new ResourceLocation("hbm","tile.pneumatic_tube").equals(block.getRegistryName())){
            Item item=Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock)||((ItemBlock)item).getBlock()!=block)throw new IllegalStateException("Missing pneumatic ItemBlock: "+block.getRegistryName());
            ModelLoader.setCustomStateMapper(block,new StateMapperBase(){@Override protected ModelResourceLocation getModelResourceLocation(IBlockState state){return WORLD;}});
            for(int metadata=0;metadata<16;metadata++)ModelLoader.setCustomModelResourceLocation(item,metadata,ITEM);
        }
    }
    public static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager manager){}
        private static String path(ResourceLocation location){String p=location.getPath();if(p.startsWith("models/"))p=p.substring(7);if(p.startsWith("block/"))p=p.substring(6);return p;}
        @Override public boolean accepts(ResourceLocation location){return "hbm".equals(location.getNamespace())&&(path(location).equals("native_pneumatic/world")||path(location).equals("native_pneumatic/item"));}
        @Override public IModel loadModel(ResourceLocation location){if(!accepts(location))throw new IllegalArgumentException("Invalid pneumatic model: "+location);return new Model(path(location).endsWith("/item"));}
    }
    private static final class Model implements IModel {
        private final boolean item;
        private final List<ResourceLocation> textures=new ArrayList<>();
        Model(boolean item){this.item=item;for(String suffix:new String[]{"","_in","_out","_connector","_straight"})textures.add(new ResourceLocation("hbm","blocks/pneumatic_tube"+suffix));}
        @Override public Collection<ResourceLocation> getTextures(){return Collections.unmodifiableList(textures);}
        @Override public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> sprites){TextureAtlasSprite[] atlas=new TextureAtlasSprite[5];for(int i=0;i<5;i++)atlas[i]=sprites.apply(textures.get(i));return new PneumaticBakedModel(item,atlas);}
    }
}
