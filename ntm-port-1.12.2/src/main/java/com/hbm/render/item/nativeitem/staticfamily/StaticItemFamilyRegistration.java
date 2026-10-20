package com.hbm.render.item.nativeitem.staticfamily;

import java.util.*;
import java.util.function.Function;
import org.lwjgl.opengl.GL11;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.hbm.render.item.nativeitem.AngelCoreFrameCorrection;
import com.hbm.render.item.nativeitem.StaticMeshItemRegistration;

/** Native item model, material sprite and frame ownership for an audited static family only. */
public final class StaticItemFamilyRegistration {
    private final List<StaticItemDescriptor> descriptors;
    private final Set<Item> identities;
    private final Function<ResourceLocation, TextureAtlasSprite> sprites;
    public StaticItemFamilyRegistration(Collection<StaticItemDescriptor> descriptors) {
        this(descriptors, location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
    }
    public StaticItemFamilyRegistration(Collection<StaticItemDescriptor> descriptors,
            Function<ResourceLocation, TextureAtlasSprite> sprites) {
        this.descriptors = Collections.unmodifiableList(new ArrayList<>(descriptors));
        Set<Item> owned = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
        for(StaticItemDescriptor descriptor : this.descriptors)
            if(!owned.add(descriptor.item())) throw new IllegalArgumentException("Duplicate native item owner: " + descriptor.item().getRegistryName());
        this.identities = Collections.unmodifiableSet(owned);
        this.sprites = Objects.requireNonNull(sprites, "sprites");
    }
    public Set<Item> items() { return identities; }
    @SubscribeEvent public void register(ModelRegistryEvent event) {
        for(StaticItemDescriptor descriptor : descriptors) {
            ModelBakery.registerItemVariants(descriptor.item(), descriptor.modelLocation());
            // Old renderer ownership was item-keyed, not damage-specific.
            ModelLoader.setCustomMeshDefinition(descriptor.item(), descriptor::modelLocation);
        }
    }
    @SubscribeEvent public void bake(ModelBakeEvent event) {
        // Validate every complete model before publishing any family member.
        List<OrdinaryMeshItemModel> models = new ArrayList<>();
        for(StaticItemDescriptor descriptor : descriptors) {
            OrdinaryMeshItemModel model = descriptor.bake(sprites.apply(descriptor.particle()));
            StaticMeshItemRegistration.requireComplete(model);
            models.add(model.withBaseModel(event.getModelRegistry().getObject(descriptor.modelLocation())));
        }
        for(int i = 0; i < descriptors.size(); i++) {
            StaticItemDescriptor descriptor = descriptors.get(i);
            event.getModelRegistry().putObject(descriptor.modelLocation(), models.get(i));
            descriptor.item().setTileEntityItemStackRenderer(descriptor.renderer());
        }
    }
    @SubscribeEvent public void texture(TextureStitchEvent.Pre event) {
        for(StaticItemDescriptor descriptor : descriptors) descriptor.registerParticle(event.getMap());
    }
    @SubscribeEvent public void frame(RenderItemInFrameEvent event) {
        if(event.getItem().isEmpty() || !identities.contains(event.getItem().getItem())) return;
        double[] delta = AngelCoreFrameCorrection.translation(event.getEntityItemFrame().getRotation());
        GL11.glTranslated(delta[0], delta[1], delta[2]);
    }
}
