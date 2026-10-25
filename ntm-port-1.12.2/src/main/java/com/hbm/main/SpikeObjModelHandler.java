package com.hbm.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Bakes the original spike OBJ as a block and inventory model. */
@SideOnly(Side.CLIENT)
public final class SpikeObjModelHandler {

	private static final String TEXTURE = RefStrings.MODID + ":blocks/spikes";
	private static final ResourceLocation MODEL = new ResourceLocation(RefStrings.MODID, "blocks/spikes.obj");

	@SubscribeEvent
	public void onModelRegistry(ModelRegistryEvent event) {
		OBJLoader.INSTANCE.addDomain(RefStrings.MODID);

		Item item = Item.getItemFromBlock(ModBlocks.spikes);
		if(item != null && item.getRegistryName() != null) {
			ModelLoader.setCustomModelResourceLocation(item, 0,
					new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		ResourceLocation name = ModBlocks.spikes.getRegistryName();
		if(name == null) return;

		IBakedModel blockModel = bakeModel(DefaultVertexFormats.BLOCK);
		event.getModelRegistry().putObject(new ModelResourceLocation(name, "normal"), blockModel);

		Item item = Item.getItemFromBlock(ModBlocks.spikes);
		if(item != null && item.getRegistryName() != null) {
			event.getModelRegistry().putObject(
					new ModelResourceLocation(item.getRegistryName(), "inventory"),
					bakeModel(DefaultVertexFormats.ITEM, true));
		}
	}

	private static IBakedModel bakeModel(VertexFormat format) {
		return bakeModel(format, false);
	}

	private static IBakedModel bakeModel(VertexFormat format, boolean inventory) {
		try {
			IModel source = ModelLoaderRegistry.getModel(MODEL);
			ImmutableMap<String, String> textures = ImmutableMap.<String, String>builder()
					.put("#" + OBJModel.Material.DEFAULT_NAME, TEXTURE)
					.put("#" + OBJModel.Material.WHITE_NAME, TEXTURE)
					.build();
			IModel textured = source.process(ImmutableMap.of("flip-v", "true")).retexture(textures);
			IBakedModel mesh = textured.bake(textured.getDefaultState(), format,
					ModelLoader.defaultTextureGetter());
			return new SpikeBakedModel(mesh, inventory);
		} catch(Exception e) {
			throw new IllegalStateException("Unable to bake the spike OBJ model: " + MODEL, e);
		}
	}

	/** Recreates ObjUtil's centering and world-only per-face color multiplier with baked quads. */
	private static final class SpikeBakedModel implements IBakedModel {
		private final List<BakedQuad> quads;
		private final TextureAtlasSprite particle;
		private final ItemOverrideList overrides;
		private final boolean ambientOcclusion;

		private SpikeBakedModel(IBakedModel mesh, boolean inventory) {
			this.overrides = mesh.getOverrides();
			this.ambientOcclusion = inventory && mesh.isAmbientOcclusion();

			List<BakedQuad> baked = new ArrayList<>();
			for(BakedQuad quad : collectAllQuads(mesh)) {
				baked.add(centerAndShade(quad, inventory));
			}
			this.quads = Collections.unmodifiableList(baked);
			this.particle = mesh.getParticleTexture();
		}

		private static List<BakedQuad> collectAllQuads(IBakedModel mesh) {
			List<BakedQuad> result = new ArrayList<>();
			result.addAll(mesh.getQuads(null, null, 0L));
			for(EnumFacing facing : EnumFacing.values()) {
				result.addAll(mesh.getQuads(null, facing, 0L));
			}
			return result;
		}

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			return side == null ? quads : Collections.<BakedQuad>emptyList();
		}

		private static BakedQuad centerAndShade(BakedQuad quad, boolean inventory) {
			VertexFormat format = quad.getFormat();
			int[] data = quad.getVertexData().clone();
			int stride = format.getIntegerSize();
			int positionOffset = -1;
			int colorOffset = -1;

			for(int element = 0; element < format.getElementCount(); element++) {
				VertexFormatElement vertexElement = format.getElement(element);
				if(vertexElement.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
					positionOffset = format.getOffset(element) / 4;
				} else if(vertexElement.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
					colorOffset = format.getOffset(element) / 4;
				}
			}
			if(positionOffset < 0) return quad;

			int color = 0;
			if(!inventory && colorOffset >= 0) {
				color = legacyFaceColor(data, positionOffset, stride, quad.getFace());
			}
			for(int vertex = 0; vertex < 4; vertex++) {
				int index = vertex * stride + positionOffset;
				float x = Float.intBitsToFloat(data[index]);
				float z = Float.intBitsToFloat(data[index + 2]);
				// The original world renderer centers X/Z; inventory rendering then
				// centers the same mesh in all three axes after its Y -0.5 translate.
				data[index] = Float.floatToRawIntBits(x + 0.5F);
				data[index + 2] = Float.floatToRawIntBits(z + 0.5F);
				if(!inventory && colorOffset >= 0) data[vertex * stride + colorOffset] = color;
			}

			return new BakedQuad(data, quad.getTintIndex(), quad.getFace(), quad.getSprite(),
					inventory && quad.shouldApplyDiffuseLighting(), format);
		}

		private static int legacyFaceColor(int[] data, int positionOffset, int stride, EnumFacing fallback) {
			float x0 = Float.intBitsToFloat(data[positionOffset]);
			float y0 = Float.intBitsToFloat(data[positionOffset + 1]);
			float z0 = Float.intBitsToFloat(data[positionOffset + 2]);
			float x1 = Float.intBitsToFloat(data[stride + positionOffset]);
			float y1 = Float.intBitsToFloat(data[stride + positionOffset + 1]);
			float z1 = Float.intBitsToFloat(data[stride + positionOffset + 2]);
			float x2 = Float.intBitsToFloat(data[stride * 2 + positionOffset]);
			float y2 = Float.intBitsToFloat(data[stride * 2 + positionOffset + 1]);
			float z2 = Float.intBitsToFloat(data[stride * 2 + positionOffset + 2]);

			float ax = x1 - x0;
			float ay = y1 - y0;
			float az = z1 - z0;
			float bx = x2 - x0;
			float by = y2 - y0;
			float bz = z2 - z0;
			float nx = ay * bz - az * by;
			float ny = az * bx - ax * bz;
			float nz = ax * by - ay * bx;
			float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
			if(length > 0.0F) {
				nx /= length;
				ny /= length;
				nz /= length;
			} else if(fallback != null) {
				nx = fallback.getXOffset();
				ny = fallback.getYOffset();
				nz = fallback.getZOffset();
			}

			float brightness = (ny + 0.7F) * 0.9F - Math.abs(nx) * 0.1F + Math.abs(nz) * 0.1F;
			if(brightness < 0.45F) brightness = 0.45F;
			int component = (int) (brightness * 255.0F) & 255;
			return 0xFF000000 | component << 16 | component << 8 | component;
		}

		@Override public boolean isAmbientOcclusion() { return ambientOcclusion; }
		@Override public boolean isGui3d() { return true; }
		@Override public boolean isBuiltInRenderer() { return false; }
		@Override public TextureAtlasSprite getParticleTexture() { return particle; }
		@Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
		@Override public ItemOverrideList getOverrides() { return overrides; }
	}
}
