package com.hbm.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockChargeBase;
import com.hbm.lib.RefStrings;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
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

/** Bakes the two legacy charge OBJ meshes as ordinary 1.12 block/item models. */
@SideOnly(Side.CLIENT)
public final class ChargeObjModelHandler {

	private static final BlockChargeBase[] CHARGES = {
		// ModBlocks exposes these as Block, but initializes them with these subclasses.
		(BlockChargeBase) ModBlocks.charge_dynamite,
		(BlockChargeBase) ModBlocks.charge_c4
	};

	@SubscribeEvent
	public void onModelRegistry(ModelRegistryEvent event) {
		OBJLoader.INSTANCE.addDomain(RefStrings.MODID);

		for(BlockChargeBase block : CHARGES) {
			ModelLoader.setCustomStateMapper(block,
					new StateMap.Builder().ignore(BlockChargeBase.ORIENTATION).build());

			Item item = Item.getItemFromBlock(block);
			ResourceLocation name = block.getRegistryName();
			if(item != null && name != null) {
				ModelResourceLocation inventory = new ModelResourceLocation(name, "inventory");
				for(int meta = 0; meta <= 15; meta++) {
					ModelLoader.setCustomModelResourceLocation(item, meta, inventory);
				}
			}
		}
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		for(BlockChargeBase block : CHARGES) {
			ResourceLocation name = block.getRegistryName();
			if(name == null) continue;

			String modelName = name.getPath().startsWith("tile.") ? name.getPath().substring("tile.".length()) : name.getPath();
			// OBJLoader resolves resources beneath assets/<domain>/models itself.
			IBakedModel blockMesh = bakeObj(block, "blocks/" + modelName + ".obj", DefaultVertexFormats.BLOCK);
			IBakedModel itemMesh = bakeObj(block, "blocks/" + modelName + ".obj", DefaultVertexFormats.ITEM);
			ModelResourceLocation blockLocation = new ModelResourceLocation(name, "normal");
			ModelResourceLocation itemLocation = new ModelResourceLocation(name, "inventory");

			event.getModelRegistry().putObject(blockLocation, new ChargeBakedModel(blockMesh, false));
			event.getModelRegistry().putObject(itemLocation, new ChargeBakedModel(itemMesh, true));
		}
	}

	private static IBakedModel bakeObj(BlockChargeBase block, String path, VertexFormat format) {
		try {
			ResourceLocation location = new ResourceLocation(RefStrings.MODID, path);
			IModel source = ModelLoaderRegistry.getModel(location);
			// These legacy sprites are wrappers; calling their inherited Minecraft method
			// through the pre-1.12 IIcon bridge can fail after ForgeGradle reobfuscation.
			String iconName = RefStrings.MODID + ":blocks/" + block.getRegistryName().getPath().replaceFirst("^tile\\.", "");
			ImmutableMap<String, String> texture = ImmutableMap.<String, String>builder()
					.put("#" + OBJModel.Material.DEFAULT_NAME, iconName)
					.put("#" + OBJModel.Material.WHITE_NAME, iconName)
					.build();
			IModel textured = source.process(ImmutableMap.of("flip-v", "true")).retexture(texture);
			return textured.bake(textured.getDefaultState(), format, ModelLoader.defaultTextureGetter());
		} catch(Exception e) {
			throw new IllegalStateException("Unable to bake charge OBJ model for " + block.getRegistryName() + ": " + path, e);
		}
	}

	/** Applies the original Tessellator/OpenGL rotations to Forge-baked OBJ quads. */
	private static final class ChargeBakedModel implements IBakedModel {
		private final IBakedModel mesh;
		private final boolean inventory;
		private final TextureAtlasSprite particle;
		private final Map<Integer, List<BakedQuad>> transformed = new HashMap<>();
		private List<BakedQuad> inventoryQuads;

		private ChargeBakedModel(IBakedModel mesh, boolean inventory) {
			this.mesh = mesh;
			this.inventory = inventory;
			collectAllQuads(null);
			this.particle = mesh.getParticleTexture();
		}

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			// The OBJ is an assembled mesh, not six independently cullable block faces.
			if(side != null) return Collections.emptyList();
			if(inventory) {
				if(inventoryQuads == null) inventoryQuads = transformMesh(state, true, 0);
				return inventoryQuads;
			}

			int meta = state == null ? 0 : state.getValue(BlockChargeBase.ORIENTATION);
			List<BakedQuad> quads = transformed.get(meta);
			if(quads == null) {
				quads = transformMesh(state, false, meta);
				transformed.put(meta, quads);
			}
			return quads;
		}

		private List<BakedQuad> transformMesh(IBlockState state, boolean item, int meta) {
			int yQuarter;
			int zQuarter;
			if(item) {
				// renderInventoryBlock: Y -90, Z -90, then translate Y +0.375.
				yQuarter = 3;
				zQuarter = 3;
			} else {
				yQuarter = meta == 2 ? 1 : meta == 3 ? 3 : meta == 4 ? 2 : 0;
				zQuarter = meta == 0 ? 2 : (meta == 2 || meta == 3 || meta == 4 || meta == 5) ? 1 : 0;
			}

			List<BakedQuad> result = new ArrayList<>();
			for(BakedQuad quad : collectAllQuads(state)) {
				result.add(transformQuad(quad, yQuarter, zQuarter, item));
			}
			return Collections.unmodifiableList(result);
		}

		private List<BakedQuad> collectAllQuads(IBlockState state) {
			List<BakedQuad> result = new ArrayList<>();
			result.addAll(mesh.getQuads(state, null, 0L));
			for(EnumFacing facing : EnumFacing.values()) {
				result.addAll(mesh.getQuads(state, facing, 0L));
			}
			return result;
		}

		private static BakedQuad transformQuad(BakedQuad quad, int yQuarter, int zQuarter, boolean item) {
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

			for(int vertex = 0; vertex < 4; vertex++) {
				int index = vertex * stride + positionOffset;
				// Forge's OBJ loader keeps the source's origin-centered [-0.5, 0.5]
				// coordinates; baked block/item meshes use the [0, 1] cell instead.
				float x = Float.intBitsToFloat(data[index]);
				float y = Float.intBitsToFloat(data[index + 1]);
				float z = Float.intBitsToFloat(data[index + 2]);
				if(item) y += 0.375F;
				float[] rotated = rotate(x, y, z, yQuarter, zQuarter, item);
				data[index] = Float.floatToRawIntBits(rotated[0] + 0.5F);
				data[index + 1] = Float.floatToRawIntBits(rotated[1] + 0.5F);
				data[index + 2] = Float.floatToRawIntBits(rotated[2] + 0.5F);

				if(normalOffset >= 0) {
					int normal = data[vertex * stride + normalOffset];
					float nx = (byte) normal / 127.0F;
					float ny = (byte) (normal >> 8) / 127.0F;
					float nz = (byte) (normal >> 16) / 127.0F;
					float[] rotatedNormal = rotate(nx, ny, nz, yQuarter, zQuarter, item);
					data[vertex * stride + normalOffset] = packNormal(rotatedNormal[0], rotatedNormal[1], rotatedNormal[2]);
				}
			}

			if(!item && colorOffset >= 0) {
				int color = legacyFaceColor(data, positionOffset, stride);
				for(int vertex = 0; vertex < 4; vertex++) {
					data[vertex * stride + colorOffset] = color;
				}
			}

			EnumFacing face = quad.getFace();
			if(face != null) {
				float[] normal = rotate(face.getXOffset(), face.getYOffset(), face.getZOffset(), yQuarter, zQuarter, item);
				face = EnumFacing.getFacingFromVector(normal[0], normal[1], normal[2]);
			}
			return new BakedQuad(data, quad.getTintIndex(), face, quad.getSprite(),
					item && quad.shouldApplyDiffuseLighting(), format);
		}

		/** Matches the legacy flat S_Face normal using the transformed face vertices. */
		private static int legacyFaceColor(int[] data, int positionOffset, int stride) {
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
			}

			float brightness = (ny + 0.7F) * 0.9F - Math.abs(nx) * 0.1F + Math.abs(nz) * 0.1F;
			if(brightness < 0.45F) brightness = 0.45F;
			int component = (int) (brightness * 255.0F) & 255;
			return 0xFF000000 | component << 16 | component << 8 | component;
		}

		private static float[] rotate(float x, float y, float z, int yQuarter, int zQuarter, boolean item) {
			// ObjUtil's Vec3NT Z rotation has the opposite sign from OpenGL's matrix rotation.
			for(int i = 0; i < zQuarter; i++) {
				float nextX = item ? -y : y;
				y = item ? x : -x;
				x = nextX;
			}
			for(int i = 0; i < yQuarter; i++) {
				float nextX = z;
				z = -x;
				x = nextX;
			}
			return new float[] {x, y, z};
		}

		private static int packNormal(float x, float y, float z) {
			int nx = (byte) (x * 127.0F);
			int ny = (byte) (y * 127.0F);
			int nz = (byte) (z * 127.0F);
			return nx & 255 | (ny & 255) << 8 | (nz & 255) << 16;
		}

		@Override public boolean isAmbientOcclusion() { return inventory && mesh.isAmbientOcclusion(); }
		@Override public boolean isGui3d() { return mesh.isGui3d(); }
		@Override public boolean isBuiltInRenderer() { return false; }
		@Override public TextureAtlasSprite getParticleTexture() { return particle; }
		@Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
		@Override public ItemOverrideList getOverrides() { return mesh.getOverrides(); }
	}
}
