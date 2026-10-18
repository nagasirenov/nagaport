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
		String path = location.getPath();
		int suffix = path.indexOf('-');
		if(suffix >= 0) path = path.substring(0, suffix);
		ResourceLocation source = new ResourceLocation(location.getNamespace(), path);
		ResourceLocation sourceTexture = completeResourceLocation(source, 0);

		try(IResource resource = manager.getResource(sourceTexture)) {
			loadSprite(PngSizeInfo.makeFromResource(resource), false);
			loadSpriteFrames(resource, mipmap);
			mutateFrames();
			return true;
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
