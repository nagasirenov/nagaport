package com.hbm.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockRubberCacti;
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

/** Bakes the original rubber cactus OBJ for the 1.12 block and item model paths. */
@SideOnly(Side.CLIENT)
public final class RubberCactusObjModelHandler {

	private static final BlockRubberCacti BLOCK = (BlockRubberCacti) ModBlocks.spike_cacti;
	private static final ResourceLocation MODEL = new ResourceLocation(RefStrings.MODID, "plant_spike.obj");

	@SubscribeEvent
	public void onModelRegistry(ModelRegistryEvent event) {
		OBJLoader.INSTANCE.addDomain(RefStrings.MODID);

		Item item = Item.getItemFromBlock(BLOCK);
		ResourceLocation name = BLOCK.getRegistryName();
		if(item != null && name != null) {
			ModelResourceLocation inventory = new ModelResourceLocation(name, "inventory");
			for(int meta = 0; meta < BlockRubberCacti.EnumBushType.values().length; meta++) {
				ModelLoader.setCustomModelResourceLocation(item, meta, inventory);
			}
		}
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		ResourceLocation name = BLOCK.getRegistryName();
		if(name == null) return;

		BlockRubberCacti.EnumBushType[] types = BlockRubberCacti.EnumBushType.values();
		for(int meta = 0; meta < types.length; meta++) {
			String iconName = RefStrings.MODID + ":blocks/rubber_tall." + types[meta].getName();
			ModelResourceLocation worldLocation = new ModelResourceLocation(name, "type=" + types[meta].getName());
			event.getModelRegistry().putObject(worldLocation, bakeObj(iconName, DefaultVertexFormats.BLOCK, false));
		}

		String itemIconName = RefStrings.MODID + ":blocks/rubber_tall." + types[0].getName();
		IBakedModel itemModel = bakeObj(itemIconName, DefaultVertexFormats.ITEM, true);

		Item item = Item.getItemFromBlock(BLOCK);
		if(item != null && item.getRegistryName() != null) {
			event.getModelRegistry().putObject(new ModelResourceLocation(item.getRegistryName(), "inventory"), itemModel);
		}
	}

	private static IBakedModel bakeObj(String texture, VertexFormat format, boolean inventory) {
		try {
			IModel source = ModelLoaderRegistry.getModel(MODEL);
			ImmutableMap<String, String> textures = ImmutableMap.<String, String>builder()
					.put("#" + OBJModel.Material.DEFAULT_NAME, texture)
					.put("#" + OBJModel.Material.WHITE_NAME, texture)
					.build();
			IModel textured = source.process(ImmutableMap.of("flip-v", "true")).retexture(textures);
			IBakedModel mesh = textured.bake(textured.getDefaultState(), format, ModelLoader.defaultTextureGetter());
			return new RubberCactusMeshModel(mesh, inventory);
		} catch(Exception e) {
			throw new IllegalStateException("Unable to bake rubber cactus OBJ model: " + MODEL, e);
		}
	}

	private static final class RubberCactusMeshModel implements IBakedModel {
		private final List<BakedQuad> quads;
		private final TextureAtlasSprite particle;
		private final ItemOverrideList overrides;
		private final boolean ambientOcclusion;

		private RubberCactusMeshModel(IBakedModel mesh, boolean inventory) {
			this.overrides = mesh.getOverrides();
			this.ambientOcclusion = inventory && mesh.isAmbientOcclusion();

			List<BakedQuad> sourceQuads = collectAllQuads(mesh);
			List<BakedQuad> bakedQuads = new ArrayList<>(sourceQuads.size() * 2);
			if(inventory) {
				// The old item renderer emitted the unrotated mesh twice, centering
				// only X/Z around the block center.
				appendCopy(bakedQuads, sourceQuads, false);
				appendCopy(bakedQuads, sourceQuads, false);
			} else {
				// ObjUtil rotated once by -PI and once by zero around Y, then moved
				// only X/Z to the center of the block. The OBJ Y origin stays at 0.
				appendCopy(bakedQuads, sourceQuads, true);
				appendCopy(bakedQuads, sourceQuads, false);
			}
			this.quads = Collections.unmodifiableList(bakedQuads);
			// Keep particle lookup after OBJ mesh quad creation, matching Forge's
			// baked-model lifecycle for retextured OBJ materials.
			this.particle = mesh.getParticleTexture();
		}

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			return side == null ? quads : Collections.<BakedQuad>emptyList();
		}

		private static List<BakedQuad> collectAllQuads(IBakedModel mesh) {
			List<BakedQuad> result = new ArrayList<>();
			result.addAll(mesh.getQuads(null, null, 0L));
			for(EnumFacing facing : EnumFacing.values()) {
				result.addAll(mesh.getQuads(null, facing, 0L));
			}
			return result;
		}

		private static void appendCopy(List<BakedQuad> target, List<BakedQuad> source, boolean halfTurn) {
			for(BakedQuad quad : source) {
				target.add(transformQuad(quad, halfTurn));
			}
		}

		private static BakedQuad transformQuad(BakedQuad quad, boolean halfTurn) {
			VertexFormat format = quad.getFormat();
			int[] data = quad.getVertexData().clone();
			int stride = format.getIntegerSize();
			int positionOffset = -1;
			int normalOffset = format.hasNormal() ? format.getNormalOffset() / 4 : -1;
			for(int element = 0; element < format.getElementCount(); element++) {
				VertexFormatElement vertexElement = format.getElement(element);
				if(vertexElement.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
					positionOffset = format.getOffset(element) / 4;
				}
			}
			if(positionOffset < 0) return quad;

			for(int vertex = 0; vertex < 4; vertex++) {
				int index = vertex * stride + positionOffset;
				float x = Float.intBitsToFloat(data[index]);
				float z = Float.intBitsToFloat(data[index + 2]);
				if(halfTurn) {
					x = -x;
					z = -z;
				}
				data[index] = Float.floatToRawIntBits(x + 0.5F);
				data[index + 2] = Float.floatToRawIntBits(z + 0.5F);

				if(normalOffset >= 0 && halfTurn) {
					int normalIndex = vertex * stride + normalOffset;
					int normal = data[normalIndex];
					float nx = -(byte) normal / 127.0F;
					float ny = (byte) (normal >> 8) / 127.0F;
					float nz = -(byte) (normal >> 16) / 127.0F;
					data[normalIndex] = packNormal(nx, ny, nz);
				}
			}

			EnumFacing face = quad.getFace();
			if(halfTurn && face != null) {
				face = EnumFacing.getFacingFromVector(-face.getXOffset(), face.getYOffset(), -face.getZOffset());
			}
			return new BakedQuad(data, quad.getTintIndex(), face, quad.getSprite(),
					quad.shouldApplyDiffuseLighting(), format);
		}

		private static int packNormal(float x, float y, float z) {
			int nx = (byte) (x * 127.0F);
			int ny = (byte) (y * 127.0F);
			int nz = (byte) (z * 127.0F);
			return nx & 255 | (ny & 255) << 8 | (nz & 255) << 16;
		}

		@Override public boolean isAmbientOcclusion() { return ambientOcclusion; }
		@Override public boolean isGui3d() { return true; }
		@Override public boolean isBuiltInRenderer() { return false; }
		@Override public TextureAtlasSprite getParticleTexture() { return particle; }
		@Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
		@Override public ItemOverrideList getOverrides() { return overrides; }
	}
}
