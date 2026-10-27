package com.hbm.render.icon;

import java.awt.image.BufferedImage;
import java.io.IOException;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import java.util.function.Function;

/**
 * TODO: using this with a missing texture for some reason crashes the game
 * TexMan's mip map levels seem to be -1 for some reason, run debugger
 * 
 * @author hbm
 */
@SideOnly(Side.CLIENT)
public class TextureAtlasSpriteMutatable extends TextureAtlasSprite {
	
	private RGBMutator mutator;
	public String basePath = "textures/items";
	private int mipmap = 0;

	public TextureAtlasSpriteMutatable(String iconName, RGBMutator mutator) {
		super(iconName);
		this.mutator = mutator;
	}
	
	public TextureAtlasSpriteMutatable setBlockAtlas() {
		this.mipmap = Minecraft.getMinecraft().gameSettings.mipmapLevels;
		this.basePath = "textures/blocks";
		return this;
	}

	@Override
	public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
		return true;
	}

	@Override
	public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
		// Forge wraps the icon name as textures/<name>.png before handing it to
		// custom loaders; the base atlas name lives on the sprite itself.
		String path = this.getIconName();
		int suffix = path.indexOf('-');
		if(suffix >= 0) path = path.substring(0, suffix);
		// The icon name may carry its own namespace; keep it and drop the
		// wrapper's, so the source path never doubles the prefix.
		ResourceLocation source;
		int domain = path.indexOf(':');
		String domainStr = (domain >= 0) ? path.substring(0, domain) : location.getNamespace();
		if(domainStr == null || domainStr.isEmpty() || "minecraft".equals(domainStr)) {
			domainStr = com.hbm.lib.RefStrings.MODID;
		}
		String subPath = (domain >= 0) ? path.substring(domain + 1) : path;
		source = new ResourceLocation(domainStr, subPath);
		// Legacy registry-style names ("tile.slag") keep their art under the plain
		// atlas name ("slag"); strip the old prefix the same way the legacy icon
		// register does, never by rewriting the resource itself.
		String sourcePath = source.getPath();
		boolean legacyBlockName = sourcePath.startsWith("tile.");
		if(legacyBlockName) sourcePath = sourcePath.substring(5);
		else if(sourcePath.startsWith("item.")) sourcePath = sourcePath.substring(5);
		if(legacyBlockName && "textures/items".equals(this.basePath)) {
			// ItemAutogen can still use the original block texture base (tile.slag);
			// its generated variants must read textures/blocks/slag.png, not items/.
			this.basePath = "textures/blocks";
		}
		source = new ResourceLocation(source.getNamespace(), sourcePath);
		ResourceLocation sourceTexture = completeResourceLocation(source, 0);

		try {
			PngSizeInfo sizeInfo;
			try(IResource sizeResource = manager.getResource(sourceTexture)) {
				sizeInfo = PngSizeInfo.makeFromResource(sizeResource);
			}
			loadSprite(sizeInfo, false);
			try(IResource frameResource = manager.getResource(sourceTexture)) {
				int effectiveMipmap = Math.max(1, Minecraft.getMinecraft().gameSettings.mipmapLevels + 1);
				loadSpriteFrames(frameResource, effectiveMipmap);
			}
			mutateFrames();
			// Forge 2768's TextureMap exits before stitching the sprite when a
			// custom loader returns true; false hands the loaded frames to the
			// ordinary stitcher, which is what generated sprites need.
			return false;
		} catch(IOException e) {
			throw new IllegalStateException("Unable to load generated atlas source " + sourceTexture, e);
		}
	}

	private void mutateFrames() {
		if(mutator == null || framesTextureData.isEmpty()) return;

		int frameCount = framesTextureData.size();
		for(int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
			int[][] frame = framesTextureData.get(frameIndex);
			if(frame == null) continue;

			for(int level = 0; level < frame.length; level++) {
				int[] pixels = frame[level];
				if(pixels == null) continue;
				int frameWidth = Math.max(1, width >> level);
				int frameHeight = Math.max(1, height >> level);
				BufferedImage image = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
				image.setRGB(0, 0, frameWidth, frameHeight, pixels, 0, frameWidth);
				mutator.mutate(image, frameIndex, frameCount);
				frame[level] = image.getRGB(0, 0, frameWidth, frameHeight, pixels, 0, frameWidth);
			}
		}
	}
	
	private ResourceLocation completeResourceLocation(ResourceLocation loc, int mipmap) {
		return mipmap == 0 ? new ResourceLocation(loc.getNamespace(), String.format("%s/%s%s", new Object[] { this.basePath, loc.getPath(), ".png" }))
				: new ResourceLocation(loc.getNamespace(), String.format("%s/mipmaps/%s.%d%s", new Object[] { this.basePath, loc.getPath(), Integer.valueOf(mipmap), ".png" }));
	}
}
