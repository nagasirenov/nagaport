package com.hbm.render.item.nativeitem.callback;

import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.StaticMeshItemRegistration;
import com.hbm.render.item.nativeitem.AngelCoreFrameCorrection;

import com.hbm.render.item.ItemRenderBase;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** Item-keyed native ownership with original library -> tile -> item override order. */
public final class CallbackItemRegistration {
    private final Map<Item,ItemRenderBase> renderers;
    public CallbackItemRegistration(Map<Item,ItemRenderBase> source) {
        Map<Item,ItemRenderBase> copy=new IdentityHashMap<>();
        for(Map.Entry<Item,ItemRenderBase> e:source.entrySet())copy.put(Objects.requireNonNull(e.getKey()),Objects.requireNonNull(e.getValue()));
        renderers=Collections.unmodifiableMap(copy);
    }
    public Set<Item> items(){return renderers.keySet();}
    private ModelResourceLocation location(Item item){return StaticMeshItemRegistration.modelLocation(item);}
    @SubscribeEvent public void register(ModelRegistryEvent event){
        for(Item item:renderers.keySet()){
            ModelResourceLocation key=location(item);
            ModelBakery.registerItemVariants(item,key);
            ModelLoader.setCustomMeshDefinition(item,stack->key);
        }
    }
    @SubscribeEvent public void bake(ModelBakeEvent event){
        Map<Item,CallbackItemModel> models=new IdentityHashMap<>();
        for(Map.Entry<Item,ItemRenderBase> e:renderers.entrySet()){
            IBakedModel original=event.getModelRegistry().getObject(location(e.getKey()));
            TextureAtlasSprite sprite=original==null?Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite():original.getParticleTexture();
            if(sprite==Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite())
                org.apache.logging.log4j.LogManager.getLogger("hbm").warn("Native callback mesh is retained, but its atlas particle is unresolved: {}. Verify the item texture registration before gameplay acceptance.",e.getKey().getRegistryName());
            models.put(e.getKey(),new CallbackItemModel(sprite,e.getValue(),original));
        }
        for(Map.Entry<Item,CallbackItemModel> e:models.entrySet()){
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
