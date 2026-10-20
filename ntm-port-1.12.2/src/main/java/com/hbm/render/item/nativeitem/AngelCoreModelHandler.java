package com.hbm.render.item.nativeitem;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.hbm.render.mesh.MeshModel;

/** Client lifecycle for the real core mesh. Includes the explicitly approved new 1.12 context policies. */
public final class AngelCoreModelHandler {
    public static final ResourceLocation MATERIAL = new ResourceLocation("hbm", "textures/entity/bfa_body.png");
    public static final ResourceLocation PARTICLE = MaterialTextureAtlasSprite.spriteLocation(MATERIAL);
    private final StaticMeshItemRegistration registration;
    private final AngelCoreFrameCorrection frame;
    public AngelCoreModelHandler(Item item, MeshModel mesh, ResourceLocation texture) {
        registration = new StaticMeshItemRegistration(item, new com.hbm.render.item.ItemRenderAngelCore(mesh, texture),
                () -> new AngelCoreItemModel(AngelCorePerspectives.complete(),
                        Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(PARTICLE.toString())), 0);
        frame = new AngelCoreFrameCorrection(item);
    }
    @SubscribeEvent public void register(ModelRegistryEvent event) { registration.register(event); }
    @SubscribeEvent public void bake(ModelBakeEvent event) { registration.bake(event); }
    @SubscribeEvent public void texture(TextureStitchEvent.Pre event) { MaterialTextureAtlasSprite.register(event.getMap(), MATERIAL); }
    @SubscribeEvent public void frame(RenderItemInFrameEvent event) { frame.renderFrame(event); }
}
