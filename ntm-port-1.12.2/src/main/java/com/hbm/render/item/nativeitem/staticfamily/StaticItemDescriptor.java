package com.hbm.render.item.nativeitem.staticfamily;

import java.util.Objects;
import javax.vecmath.Matrix4f;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** One explicit original item identity, literal inventory matrix and complete ordered mesh draw. */
public final class StaticItemDescriptor {
    private final Item item;
    private final Matrix4f inventory;
    private final ResourceLocation particle;
    private final boolean mipAlignedParticle;
    private final OrderedStaticMeshItemRenderer renderer;
    public StaticItemDescriptor(Item item, Matrix4f inventory, ResourceLocation particle, Runnable draw) {
        this(item, inventory, particle, false, draw);
    }
    public StaticItemDescriptor(Item item, Matrix4f inventory, ResourceLocation particle, boolean mipAlignedParticle, Runnable draw) {
        this.item = Objects.requireNonNull(item, "item");
        this.inventory = new Matrix4f(Objects.requireNonNull(inventory, "inventory"));
        this.particle = Objects.requireNonNull(particle, "particle");
        this.mipAlignedParticle = mipAlignedParticle;
        this.renderer = new OrderedStaticMeshItemRenderer(draw);
    }
    public Item item() { return item; }
    public ResourceLocation particle() {
        return mipAlignedParticle ? com.hbm.render.item.nativeitem.MaterialTextureAtlasSprite.spriteLocation(particleImage()) : particle;
    }
    private ResourceLocation particleImage() {
        return new ResourceLocation(particle.getNamespace(), "textures/" + particle.getPath() + ".png");
    }
    public void registerParticle(net.minecraft.client.renderer.texture.TextureMap atlas) {
        if(mipAlignedParticle) com.hbm.render.item.nativeitem.MaterialTextureAtlasSprite.register(atlas, particleImage());
        else atlas.registerSprite(particle);
    }
    public Matrix4f inventory() { return new Matrix4f(inventory); }
    public OrderedStaticMeshItemRenderer renderer() { return renderer; }
    public ModelResourceLocation modelLocation(ItemStack stack) { return modelLocation(); }
    public ModelResourceLocation modelLocation() {
        if(item.getRegistryName() == null) throw new IllegalStateException("Item must be registered before native model ownership");
        return new ModelResourceLocation(item.getRegistryName(), "inventory");
    }
    public OrdinaryMeshItemModel bake(TextureAtlasSprite sprite) {
        return new OrdinaryMeshItemModel(StaticItemPerspectives.complete(inventory), sprite);
    }
}
