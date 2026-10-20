package com.hbm.render.item.nativeitem;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

/**
 * Particle-atlas view of a real, static mesh material. Mesh renderers continue to
 * bind the original PNG; neither their source pixels nor their UVs are changed.
 *
 * Forge 2768's ordinary loader rejects rectangular static images. Its stitcher
 * can rotate rectangular holders without rotating uploaded pixels, and its
 * mipmap generator assumes each level has an even row stride. A square canvas
 * aligned to the configured mip level satisfies all three native contracts.
 * The original rectangle is copied unchanged, with its last row/column extended
 * only into unused particle-atlas space. This is not a replacement item icon.
 */
public final class MaterialTextureAtlasSprite extends TextureAtlasSprite {
    private final ResourceLocation texture;
    private final int mipmapLevels;
    private int sourceWidth;
    private int sourceHeight;

    public MaterialTextureAtlasSprite(ResourceLocation texture, int mipmapLevels) {
        super(spriteLocation(texture).toString());
        if (mipmapLevels < 0 || mipmapLevels > 30) {
            throw new IllegalArgumentException("Invalid material mip level: " + mipmapLevels);
        }
        this.texture = texture;
        this.mipmapLevels = mipmapLevels;
    }

    /** Converts an existing full material PNG location to its atlas lookup key. */
    public static ResourceLocation spriteLocation(ResourceLocation texture) {
        String path = texture.getPath();
        if (!path.startsWith("textures/") || !path.endsWith(".png")) {
            throw new IllegalArgumentException("Expected a material textures/...png resource: " + texture);
        }
        // Do not take ownership of an existing block/item sprite for this PNG.
        return new ResourceLocation(texture.getNamespace(), "native_material/" + path.substring(9, path.length() - 4));
    }

    /** One custom sprite per material, even when multiple native items share it. */
    public static MaterialTextureAtlasSprite register(TextureMap map, ResourceLocation texture) {
        String name = spriteLocation(texture).toString();
        TextureAtlasSprite existing = map.getTextureExtry(name);
        if (existing != null) {
            if (!(existing instanceof MaterialTextureAtlasSprite)) {
                throw new IllegalStateException("Material atlas entry already has another loader: " + name);
            }
            return (MaterialTextureAtlasSprite) existing;
        }
        MaterialTextureAtlasSprite sprite = new MaterialTextureAtlasSprite(texture, map.getMipmapLevels());
        if (!map.setTextureEntry(sprite)) {
            throw new IllegalStateException("Unable to register material atlas entry: " + name);
        }
        return sprite;
    }

    public int getSourceWidth() { return sourceWidth; }
    public int getSourceHeight() { return sourceHeight; }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location,
            Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
        // Replace rather than append frames on resource reload.
        setFramesTextureData(Collections.emptyList());
        frameCounter = tickCounter = sourceWidth = sourceHeight = width = height = 0;
        interpolatedFrameData = null;
        try (IResource resource = manager.getResource(texture)) {
            BufferedImage image = ImageIO.read(resource.getInputStream());
            if (image == null) throw new IOException("Resource is not a readable PNG");
            sourceWidth = image.getWidth();
            sourceHeight = image.getHeight();
            long alignment = 1L << mipmapLevels;
            long side = ((Math.max(sourceWidth, sourceHeight) + alignment - 1) / alignment) * alignment;
            if (side * side > Integer.MAX_VALUE) throw new IOException("Material atlas canvas is too large");
            width = height = (int) side;
            int[] pixels = new int[width * height];
            image.getRGB(0, 0, sourceWidth, sourceHeight, pixels, 0, width);
            for (int y = 0; y < sourceHeight; y++) {
                Arrays.fill(pixels, y * width + sourceWidth, (y + 1) * width,
                        pixels[y * width + sourceWidth - 1]);
            }
            for (int y = sourceHeight; y < height; y++) {
                System.arraycopy(pixels, (sourceHeight - 1) * width, pixels, y * width, width);
            }
            int[][] frame = new int[mipmapLevels + 1][];
            frame[0] = pixels;
            setFramesTextureData(Collections.singletonList(frame));
        } catch (IOException e) {
            // TextureMap catches RuntimeException and records the broken texture.
            throw new IllegalStateException("Unable to load mesh material " + texture, e);
        }
        // false means CONTINUE native generateMipmaps and Stitcher.addSprite.
        // true means SKIP this sprite in Forge's custom-loader contract.
        return false;
    }
}
