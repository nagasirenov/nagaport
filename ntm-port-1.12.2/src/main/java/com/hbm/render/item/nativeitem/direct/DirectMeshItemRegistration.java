package com.hbm.render.item.nativeitem.direct;

import com.hbm.render.item.nativeitem.StaticMeshItemRegistration;
import com.hbm.render.item.nativeitem.AngelCoreFrameCorrection;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** Native registration for explicitly owned plain-item direct meshes; preserves source particles on reload. */
public final class DirectMeshItemRegistration {
    private final Map<Item,DirectMeshItemRenderer> renderers;
    public DirectMeshItemRegistration(Map<Item,DirectMeshItemRenderer> source) {
        Map<Item,DirectMeshItemRenderer> copy=new IdentityHashMap<>();
        for(Map.Entry<Item,DirectMeshItemRenderer> e:source.entrySet())copy.put(Objects.requireNonNull(e.getKey()),Objects.requireNonNull(e.getValue()));
        renderers=Collections.unmodifiableMap(copy);
    }
    public Set<Item> items(){return renderers.keySet();}
    private ModelResourceLocation location(Item item){return StaticMeshItemRegistration.modelLocation(item);}
    @SubscribeEvent public void register(ModelRegistryEvent event){
        for(Item item:renderers.keySet()){
            ModelResourceLocation key=location(item);
            ModelBakery.registerItemVariants(item,key);
            com.hbm.main.BakeOwnedModelLoader.registerMissing(key, "DirectMeshItemRegistration");
            ModelLoader.setCustomMeshDefinition(item,stack->key);
        }
    }
    @SubscribeEvent public void bake(ModelBakeEvent event){
        Map<Item,DirectMeshItemModel> models=new IdentityHashMap<>();
        IBakedModel missing=event.getModelRegistry().getObject(net.minecraft.client.renderer.block.model.ModelBakery.MODEL_MISSING);
        for(Map.Entry<Item,DirectMeshItemRenderer> e:renderers.entrySet()){
            IBakedModel original=event.getModelRegistry().getObject(location(e.getKey()));
            TextureAtlasSprite sprite=com.hbm.render.item.nativeitem.NativeItemParticles.resolve(e.getKey(),original);
            if(sprite==null) {
                org.apache.logging.log4j.LogManager.getLogger("hbm").warn("Native direct mesh is retained, but its atlas particle is unresolved: {}. Verify the item texture registration before gameplay acceptance.",e.getKey().getRegistryName());
                sprite=Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
            }
            models.put(e.getKey(),new DirectMeshItemModel(sprite,e.getValue(),
                    com.hbm.render.item.nativeitem.NativeItemParticles.baseModel(e.getKey(),original,missing,sprite)));
        }
        for(Map.Entry<Item,DirectMeshItemModel> e:models.entrySet()){
            event.getModelRegistry().putObject(location(e.getKey()),e.getValue());
            e.getKey().setTileEntityItemStackRenderer(renderers.get(e.getKey()));
        }
    }
    @SubscribeEvent public void frame(RenderItemInFrameEvent event){
        if(event.getItem().isEmpty()||!renderers.containsKey(event.getItem().getItem()))return;
        double[] delta=AngelCoreFrameCorrection.translation(event.getEntityItemFrame().getRotation());
        GL11.glTranslated(delta[0],delta[1],delta[2]);
    }
}
