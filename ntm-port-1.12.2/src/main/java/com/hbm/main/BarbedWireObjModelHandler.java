package com.hbm.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BarbedWire;
import com.hbm.lib.RefStrings;

import net.minecraft.block.state.IBlockState;
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

/** Bakes the shared original barbed-wire OBJ for all six block identities. */
@SideOnly(Side.CLIENT)
public final class BarbedWireObjModelHandler {

	private static final ResourceLocation MODEL = new ResourceLocation(RefStrings.MODID, "models/blocks/barbed_wire.obj");
	private static final BarbedWire[] BLOCKS = {
		(BarbedWire) ModBlocks.barbed_wire,
		(BarbedWire) ModBlocks.barbed_wire_fire,
		(BarbedWire) ModBlocks.barbed_wire_poison,
		(BarbedWire) ModBlocks.barbed_wire_acid,
		(BarbedWire) ModBlocks.barbed_wire_wither,
		(BarbedWire) ModBlocks.barbed_wire_ultradeath
	};

	@SubscribeEvent
	public void onModelRegistry(ModelRegistryEvent event) {
		OBJLoader.INSTANCE.addDomain(RefStrings.MODID);

		for(BarbedWire block : BLOCKS) {
			ResourceLocation name = block.getRegistryName();
			Item item = Item.getItemFromBlock(block);
			if(name == null || item == null || item.getRegistryName() == null) continue;

			ModelResourceLocation inventory = new ModelResourceLocation(name, "inventory");
			// Orientation is block state, not an inventory texture/model variant.
			for(int metadata = 0; metadata <= 5; metadata++) {
				ModelLoader.setCustomModelResourceLocation(item, metadata, inventory);
			}
		}
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		for(BarbedWire block : BLOCKS) {
			ResourceLocation name = block.getRegistryName();
			if(name == null) continue;

			// Inventory icons use metadata zero; all current BarbedWire metadata values
			// resolve to this identity's same icon, as in the legacy block callback.
			String texture = block.getIcon(0, 0).getIconName();
			IBakedModel blockMesh = bakeObj(texture, DefaultVertexFormats.BLOCK);
			List<BakedQuad> blockSource = collectAllQuads(blockMesh);
			List<List<BakedQuad>> orientedQuads = new ArrayList<>(6);
			for(int orientation = 0; orientation <= 5; orientation++) {
				List<BakedQuad> quads = new ArrayList<>(blockSource.size());
				for(BakedQuad quad : blockSource) {
					quads.add(transformWorldQuad(quad, orientation));
				}
				orientedQuads.add(Collections.unmodifiableList(quads));
			}
			// Query the particle only after the retextured OBJ's quads have been built.
			TextureAtlasSprite blockParticle = blockMesh.getParticleTexture();
			ItemOverrideList blockOverrides = blockMesh.getOverrides();
			for(int orientation = 0; orientation <= 5; orientation++) {
				ModelResourceLocation stateModel = new ModelResourceLocation(name, "orientation=" + orientation);
				event.getModelRegistry().putObject(stateModel,
						new BarbedWireBakedModel(orientedQuads.get(orientation), blockParticle, blockOverrides, false, false));
			}

			IBakedModel itemMesh = bakeObj(texture, DefaultVertexFormats.ITEM);
			List<BakedQuad> itemSource = collectAllQuads(itemMesh);
			List<BakedQuad> itemQuads = new ArrayList<>(itemSource.size());
			for(BakedQuad quad : itemSource) itemQuads.add(transformItemQuad(quad));
			// Keep the inventory mesh unrotated and look up its particle after baking.
			TextureAtlasSprite itemParticle = itemMesh.getParticleTexture();
			ModelResourceLocation inventory = new ModelResourceLocation(name, "inventory");
			event.getModelRegistry().putObject(inventory,
					new BarbedWireBakedModel(Collections.unmodifiableList(itemQuads), itemParticle,
							itemMesh.getOverrides(), true, itemMesh.isAmbientOcclusion()));
		}
	}

	private static IBakedModel bakeObj(String texture, VertexFormat format) {
		try {
			IModel source = ModelLoaderRegistry.getModel(MODEL);
			ImmutableMap<String, String> textures = ImmutableMap.<String, String>builder()
					.put("#" + OBJModel.Material.DEFAULT_NAME, texture)
					.put("#" + OBJModel.Material.WHITE_NAME, texture)
					.build();
			IModel textured = source.process(ImmutableMap.of("flip-v", "true")).retexture(textures);
			return textured.bake(textured.getDefaultState(), format, ModelLoader.defaultTextureGetter());
		} catch(Exception e) {
			throw new IllegalStateException("Unable to bake barbed-wire OBJ model for " + texture + ": " + MODEL, e);
		}
	}

	private static List<BakedQuad> collectAllQuads(IBakedModel mesh) {
		List<BakedQuad> result = new ArrayList<>();
		result.addAll(mesh.getQuads(null, null, 0L));
		for(EnumFacing facing : EnumFacing.values()) result.addAll(mesh.getQuads(null, facing, 0L));
		return result;
	}

	private static BakedQuad transformWorldQuad(BakedQuad quad, int orientation) {
		VertexFormat format = quad.getFormat();
		int[] data = quad.getVertexData().clone();
		int stride = format.getIntegerSize();
		int positionOffset = -1;
		int colorOffset = -1;
		int normalOffset = format.hasNormal() ? format.getNormalOffset() / 4 : -1;
		for(int element = 0; element < format.getElementCount(); element++) {
			VertexFormatElement vertexElement = format.getElement(element);
			if(vertexElement.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
				positionOffset = format.getOffset(element) / 4;
			} else if(vertexElement.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
				colorOffset = format.getOffset(element) / 4;
			}
		}
		if(positionOffset < 0) return quad;

		int negativeQuarterTurns = orientation < 4 ? 1 : 2;
		for(int vertex = 0; vertex < 4; vertex++) {
			int index = vertex * stride + positionOffset;
			float x = Float.intBitsToFloat(data[index]);
			float y = Float.intBitsToFloat(data[index + 1]);
			float z = Float.intBitsToFloat(data[index + 2]);
			float[] rotated = rotateY(x, z, negativeQuarterTurns);
			// The source mesh is origin-centered in X/Z; legacy rendering translated
			// those axes by +0.5 while leaving its original Y coordinate untouched.
			data[index] = Float.floatToRawIntBits(rotated[0] + 0.5F);
			data[index + 2] = Float.floatToRawIntBits(rotated[1] + 0.5F);

			if(normalOffset >= 0) {
				int normalIndex = vertex * stride + normalOffset;
				int packed = data[normalIndex];
				float nx = (byte) packed / 127.0F;
				float ny = (byte) (packed >> 8) / 127.0F;
				float nz = (byte) (packed >> 16) / 127.0F;
				float[] rotatedNormal = rotateY(nx, nz, negativeQuarterTurns);
				data[normalIndex] = packNormal(rotatedNormal[0], ny, rotatedNormal[1]);
			}
		}

		EnumFacing face = rotateFace(quad.getFace(), negativeQuarterTurns);
		if(colorOffset >= 0) {
			int color = legacyFaceColor(data, positionOffset, stride, face);
			for(int vertex = 0; vertex < 4; vertex++) data[vertex * stride + colorOffset] = color;
		}
		// World quads already carry ObjUtil's directional shade, so disable the
		// normal diffuse/AO multipliers that would otherwise shade them a second time.
		return new BakedQuad(data, quad.getTintIndex(), face, quad.getSprite(), false, format);
	}

	private static BakedQuad transformItemQuad(BakedQuad quad) {
		VertexFormat format = quad.getFormat();
		int[] data = quad.getVertexData().clone();
		int stride = format.getIntegerSize();
		int positionOffset = -1;
		for(int element = 0; element < format.getElementCount(); element++) {
			if(format.getElement(element).getUsage() == VertexFormatElement.EnumUsage.POSITION) {
				positionOffset = format.getOffset(element) / 4;
			}
		}
		if(positionOffset < 0) return quad;

		for(int vertex = 0; vertex < 4; vertex++) {
			int index = vertex * stride + positionOffset;
			float x = Float.intBitsToFloat(data[index]);
			float z = Float.intBitsToFloat(data[index + 2]);
			// RenderItem centers baked geometry by (-0.5, -0.5, -0.5). Shift
			// origin-centered OBJ X/Z into that space and leave its Y unchanged.
			data[index] = Float.floatToRawIntBits(x + 0.5F);
			data[index + 2] = Float.floatToRawIntBits(z + 0.5F);
		}
		return new BakedQuad(data, quad.getTintIndex(), quad.getFace(), quad.getSprite(),
				quad.shouldApplyDiffuseLighting(), format);
	}

	private static float[] rotateY(float x, float z, int negativeQuarterTurns) {
		for(int turn = 0; turn < negativeQuarterTurns; turn++) {
			float nextX = -z;
			z = x;
			x = nextX;
		}
		return new float[] {x, z};
	}

	private static EnumFacing rotateFace(EnumFacing face, int negativeQuarterTurns) {
		if(face == null) return null;
		float x = face.getXOffset();
		float z = face.getZOffset();
		float[] rotated = rotateY(x, z, negativeQuarterTurns);
		return EnumFacing.getFacingFromVector(rotated[0], face.getYOffset(), rotated[1]);
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

	private static int packNormal(float x, float y, float z) {
		int nx = (byte) (x * 127.0F);
		int ny = (byte) (y * 127.0F);
		int nz = (byte) (z * 127.0F);
		return nx & 255 | (ny & 255) << 8 | (nz & 255) << 16;
	}

	private static final class BarbedWireBakedModel implements IBakedModel {
		private final List<BakedQuad> quads;
		private final TextureAtlasSprite particle;
		private final ItemOverrideList overrides;
		private final boolean inventory;
		private final boolean ambientOcclusion;

		private BarbedWireBakedModel(List<BakedQuad> quads, TextureAtlasSprite particle,
				ItemOverrideList overrides, boolean inventory, boolean ambientOcclusion) {
			this.quads = quads;
			this.particle = particle;
			this.overrides = overrides;
			this.inventory = inventory;
			this.ambientOcclusion = ambientOcclusion;
		}

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			// Keep the complete OBJ in the unculled bucket so the vanilla flat block
			// renderer supplies the block-position packed light, like the old ISBRH.
			return side == null ? quads : Collections.<BakedQuad>emptyList();
		}

		@Override public boolean isAmbientOcclusion() { return inventory && ambientOcclusion; }
		@Override public boolean isGui3d() { return true; }
		@Override public boolean isBuiltInRenderer() { return false; }
		@Override public TextureAtlasSprite getParticleTexture() { return particle; }
		@Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
		@Override public ItemOverrideList getOverrides() { return overrides; }
	}
}
