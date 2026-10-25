package com.hbm.main;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Supplies a real pre-load route for the TESR-only fusion coupler world block.
 * The block remains invisible to the chunk model renderer; its core is drawn by
 * RenderFusionCoupler and its inventory form is owned by the native item renderer.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class FusionCouplerModelHandler {

	private static final String ROUTE = "native_invisible/fusion_coupler";
	private static final ModelResourceLocation WORLD = new ModelResourceLocation(
			new ResourceLocation(RefStrings.MODID, ROUTE), "normal");
	private static final Loader LOADER = new Loader();
	private static boolean registered;

	private FusionCouplerModelHandler() { }

	static ModelResourceLocation emptyWorldLocation() { return WORLD; }

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void register(ModelRegistryEvent event) {
		if(!registered) {
			ModelLoaderRegistry.registerLoader(LOADER);
			registered = true;
		}

		final Block block = ModBlocks.fusion_coupler;
		if(block == null) throw new IllegalStateException("fusion_coupler is not registered before model routing");
		ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return WORLD;
			}
		});
	}

	private static String path(ResourceLocation location) {
		String path = location.getPath();
		if(path.startsWith("models/")) path = path.substring(7);
		if(path.startsWith("block/")) path = path.substring(6);
		return path;
	}

	private static final class Loader implements ICustomModelLoader {
		@Override public void onResourceManagerReload(IResourceManager resourceManager) { }

		@Override
		public boolean accepts(ResourceLocation location) {
			return RefStrings.MODID.equals(location.getNamespace()) && ROUTE.equals(path(location));
		}

		@Override
		public IModel loadModel(ResourceLocation location) {
			if(!accepts(location)) throw new IllegalArgumentException("Unknown invisible model route " + location);
			return new IModel() {
				@Override
				public IBakedModel bake(IModelState state, VertexFormat format,
						Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
					TextureAtlasSprite particle = bakedTextureGetter.apply(new ResourceLocation("minecraft", "missingno"));
					return new InvisibleBakedModel(particle);
				}
			};
		}
	}

	private static final class InvisibleBakedModel implements IBakedModel {
		private final TextureAtlasSprite particle;

		private InvisibleBakedModel(TextureAtlasSprite particle) {
			this.particle = particle;
		}

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			return Collections.emptyList();
		}

		@Override public boolean isAmbientOcclusion() { return false; }
		@Override public boolean isGui3d() { return false; }
		@Override public boolean isBuiltInRenderer() { return false; }
		@Override public TextureAtlasSprite getParticleTexture() { return particle; }
		@Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
		@Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
	}
}
