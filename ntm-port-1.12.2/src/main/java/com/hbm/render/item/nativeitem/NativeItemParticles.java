package com.hbm.render.item.nativeitem;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.LegacyBlockTextures;
import com.hbm.blocks.PlainCubeTextures;
import com.hbm.main.BakeOwnedModelLoader;
import com.hbm.render.icon.LegacyTextureAtlasSprite;
import com.hbm.render.item.NativeItemTextureRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Central owner of native item particles. Native inventory routes bake from
 * tracked missing placeholders, so a wrapper model cannot take its particle or
 * fallback geometry from the baked model. The particle comes from the first real
 * source: the baked model's own particle, an explicit bind(Item, modelTexture)
 * declaration, the declared item texture, or the ItemBlock's legacy block icon.
 * Bound textures are stitched in TextureStitchEvent.Pre; a source with no backing
 * PNG never suppresses the unresolved warning and never substitutes missingno.
 */
@SideOnly(Side.CLIENT)
public final class NativeItemParticles {

    private static final ResourceLocation GENERATED = new ResourceLocation("minecraft", "item/generated");
    private static final ResourceLocation HANDHELD = new ResourceLocation("minecraft", "item/handheld");

    private static final Map<Item, ResourceLocation> BOUND = new IdentityHashMap<>();
    private static final Map<String, Integer> SOURCES = new TreeMap<>();
    private static boolean registered;

    private NativeItemParticles() { }

    /** Declares the model texture (textures/...png) this item's native renderer binds.
     * The texture is stitched as a MaterialTextureAtlasSprite: its real rectangle is
     * placed on a square canvas, because Forge's ordinary loader rejects rectangular
     * static images and would leave a broken sprite behind. */
    public static void bind(Item item, ResourceLocation modelTexture) {
        if(item == null || modelTexture == null) throw new IllegalArgumentException("Native particle bind requires an item and a texture");
        MaterialTextureAtlasSprite.spriteLocation(modelTexture); // validates the material path shape
        BOUND.put(item, modelTexture);
        if(!registered) {
            MinecraftForge.EVENT_BUS.register(new NativeItemParticles());
            registered = true;
        }
    }

    @SubscribeEvent
    public void stitch(TextureStitchEvent.Pre event) {
        SOURCES.clear();
        for(ResourceLocation texture : BOUND.values()) MaterialTextureAtlasSprite.register(event.getMap(), texture);
    }

    /** First real particle for a native-owned item; null keeps the honest unresolved warning. */
    public static TextureAtlasSprite resolve(Item item, IBakedModel original) {
        TextureMap atlas = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite baked = original == null ? null : original.getParticleTexture();
        if(baked != null && baked != atlas.getMissingSprite()) return source(item, baked, "baked-model");
        TextureAtlasSprite sprite = boundSprite(atlas, item);
        if(sprite != null) return source(item, sprite, "bind");
        String declared = NativeItemTextureRegistry.texture(item);
        if(declared != null) {
            sprite = atlasSprite(atlas, NativeItemTextureRegistry.atlasLocation(declared));
            if(sprite != null) return source(item, sprite, "declared-texture");
        }
        if(item instanceof ItemBlock) {
            sprite = blockIconSprite((ItemBlock) item, atlas);
            if(sprite != null) return source(item, sprite, "block-icon");
        }
        return null;
    }

    /** Real base model for native wrappers: keeps a genuine owner model, and
     * replaces only a tracked placeholder with generated geometry of the same
     * particle, so frame, moving-item and GUI fallback draws show real textures
     * instead of the missing cube. */
    public static IBakedModel baseModel(Item item, IBakedModel original, IBakedModel missing, TextureAtlasSprite particle) {
        if(original == null || particle == null) return original;
        TextureMap atlas = Minecraft.getMinecraft().getTextureMapBlocks();
        if(particle == atlas.getMissingSprite()) return original;
        boolean placeholder = original == missing
                || original.getClass().getName().equals("net.minecraftforge.client.model.FancyMissingModel$BakedModel")
                || BakeOwnedModelLoader.isPlaceholder(original);
        if(!placeholder) return original;
        try {
            IModel model = ModelLoaderRegistry.getModel(item.isFull3D() ? HANDHELD : GENERATED)
                    .retexture(ImmutableMap.of("layer0", particle.getIconName()));
            return model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
                    location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
        } catch(Exception failure) {
            org.apache.logging.log4j.LogManager.getLogger("hbm").warn("Native item {} keeps its placeholder base model: {}",
                    item.getRegistryName(), failure.toString());
            return original;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void summarize(ModelBakeEvent event) {
        if(SOURCES.isEmpty()) return;
        org.apache.logging.log4j.LogManager.getLogger("hbm").info("Native item particles resolved by source: {}", SOURCES);
    }

    private static TextureAtlasSprite source(Item item, TextureAtlasSprite sprite, String name) {
        SOURCES.merge(name, 1, Integer::sum);
        return sprite;
    }

    /** The stitched material sprite of a bound model texture; null keeps the warning honest. */
    private static TextureAtlasSprite boundSprite(TextureMap atlas, Item item) {
        ResourceLocation texture = BOUND.get(item);
        if(texture == null || !hasSourcePng(texture)) return null;
        return stitchedSprite(atlas, MaterialTextureAtlasSprite.spriteLocation(texture).toString());
    }

    private static TextureAtlasSprite atlasSprite(TextureMap atlas, ResourceLocation location) {
        if(location == null || !hasSourcePng(pngForAtlasSprite(location))) return null;
        return stitchedSprite(atlas, location.toString());
    }

    /** A registered entry alone does not prove the sprite was stitched and
     * uploaded; the post-stitch lookup must return that exact sprite. */
    private static TextureAtlasSprite stitchedSprite(TextureMap atlas, String name) {
        TextureAtlasSprite registered = atlas.getTextureExtry(name);
        TextureAtlasSprite uploaded = atlas.getAtlasSprite(name);
        if(registered == null || uploaded != registered || uploaded == atlas.getMissingSprite()) return null;
        return uploaded;
    }

    /** A directly held sprite counts only when the atlas actually uploaded it. */
    private static TextureAtlasSprite uploadedSprite(TextureMap atlas, TextureAtlasSprite sprite) {
        if(sprite == null || sprite == atlas.getMissingSprite()) return null;
        return atlas.getAtlasSprite(sprite.getIconName()) == sprite ? sprite : null;
    }

    /** Post-stitch sprite of a legacy icon; null when it never reached the atlas. */
    public static TextureAtlasSprite spriteOf(com.hbm.render.icon.IIcon icon) {
        if(icon == null) return null;
        TextureMap atlas = Minecraft.getMinecraft().getTextureMapBlocks();
        if(icon instanceof com.hbm.render.icon.WrappedIcon)
            return uploadedSprite(atlas, ((com.hbm.render.icon.WrappedIcon) icon).getSprite());
        if(icon instanceof LegacyTextureAtlasSprite)
            return uploadedSprite(atlas, ((LegacyTextureAtlasSprite) icon).getSprite());
        if(icon instanceof TextureAtlasSprite)
            return uploadedSprite(atlas, (TextureAtlasSprite) icon);
        return stitchedSprite(atlas, icon.getIconName());
    }

    private static ResourceLocation pngForAtlasSprite(ResourceLocation atlasLocation) {
        return new ResourceLocation(atlasLocation.getNamespace(), "textures/" + atlasLocation.getPath() + ".png");
    }

    /** A sprite entry with no backing PNG loads as missing texture data; never present it as resolved. */
    private static boolean hasSourcePng(ResourceLocation pngLocation) {
        try(IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(pngLocation)) {
            return true;
        } catch(Exception absent) {
            return false;
        }
    }

    private static TextureAtlasSprite blockIconSprite(ItemBlock item, TextureMap atlas) {
        net.minecraft.block.Block block = item.getBlock();
        if(block instanceof com.hbm.blocks.BlockBase) {
            TextureAtlasSprite sprite = iconSprite(((com.hbm.blocks.BlockBase) block).getIcon(1, 0), atlas);
            if(sprite != null) return sprite;
        }
        TextureAtlasSprite stitched = iconSprite(com.hbm.render.icon.PlainCubeLegacyIcons.get(block), atlas);
        if(stitched != null) return stitched;
        stitched = atlasSprite(atlas, bindingAtlasLocation(LegacyBlockTextures.atlasName(block)));
        if(stitched != null) return stitched;
        String plain = PlainCubeTextures.bindings().get(block);
        if(plain == null) return null;
        ResourceLocation legacy = new ResourceLocation(plain);
        String path = legacy.getPath();
        if(!path.startsWith("blocks/")) path = "blocks/" + path;
        return atlasSprite(atlas, new ResourceLocation(legacy.getNamespace(), path));
    }

    private static ResourceLocation bindingAtlasLocation(String binding) {
        if(binding == null) return null;
        return new ResourceLocation(binding);
    }

    private static TextureAtlasSprite iconSprite(com.hbm.render.icon.IIcon icon, TextureMap atlas) {
        if(icon == null) return null;
        TextureAtlasSprite direct = null;
        if(icon instanceof com.hbm.render.icon.WrappedIcon) direct = ((com.hbm.render.icon.WrappedIcon) icon).getSprite();
        else if(icon instanceof LegacyTextureAtlasSprite) direct = ((LegacyTextureAtlasSprite) icon).getSprite();
        else if(icon instanceof TextureAtlasSprite) direct = (TextureAtlasSprite) icon;
        if(direct != null) return uploadedSprite(atlas, direct);
        return stitchedSprite(atlas, icon.getIconName());
    }
}
