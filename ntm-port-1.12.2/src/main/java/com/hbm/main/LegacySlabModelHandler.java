package com.hbm.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.PlainCubeTextures;
import com.hbm.blocks.generic.BlockLightstone;
import com.hbm.blocks.generic.BlockMultiSlab;
import com.hbm.blocks.generic.BlockMultiSlabMeta;
import com.hbm.blocks.BlockEnums.LightstoneType;
import com.hbm.lib.RefStrings;
import com.hbm.render.LegacyTexturePath;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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

/** Native vanilla slab geometry for the BlockMultiSlab families that have no JSON models.
 * Single slabs reuse the vanilla bottom/top slab parents; double slabs are the plain
 * full cube of their material, matching the original ISBRH geometry. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class LegacySlabModelHandler {

	private static final String PREFIX = "native_legacy_slab/";
	private static final Set<String> TARGETS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			"tile.concrete_slab",
			"tile.concrete_double_slab",
			"tile.concrete_brick_slab",
			"tile.concrete_brick_double_slab",
			"tile.brick_slab",
			"tile.brick_double_slab",
			"tile.stones_slab",
			"tile.stones_double_slab")));

	private static final Map<String, SlabRoute> ROUTES = new HashMap<>();
	private static final Loader LOADER = new Loader();
	private static boolean registered;

	private LegacySlabModelHandler() { }

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void register(ModelRegistryEvent event) {
		if(!registered) {
			ModelLoaderRegistry.registerLoader(LOADER);
			registered = true;
		}
		ROUTES.clear();

		Set<String> found = new HashSet<>();
		for(Block block : net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.getValuesCollection()) {
			ResourceLocation name = block.getRegistryName();
			if(!(block instanceof BlockMultiSlab) || name == null || !RefStrings.MODID.equals(name.getNamespace())
					|| !TARGETS.contains(name.getPath())) continue;
			BlockMultiSlab slab = (BlockMultiSlab) block;
			if(slab.slabMaterials == null || slab.slabMaterials.length == 0)
				throw new IllegalStateException("Slab family without materials: " + name);
			found.add(name.getPath());

			final ResourceLocation slabName = name;
			final boolean isDouble = slab.isDouble();
			for(IBlockState state : slab.getBlockState().getValidStates()) {
				int variant = state.getValue(BlockMultiSlab.MATERIAL);
				String route = PREFIX + name.getPath() + "/world/" + variant + "/" +
						state.getValue(net.minecraft.block.BlockSlab.HALF).getName();
				ROUTES.put(route, new SlabRoute(material(slab, variant), isDouble,
						state.getValue(net.minecraft.block.BlockSlab.HALF) == net.minecraft.block.BlockSlab.EnumBlockHalf.TOP));
			}

			ModelLoader.setCustomStateMapper(slab, new StateMapperBase() {
				@Override
				protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
					int variant = state.getValue(BlockMultiSlab.MATERIAL);
					return location(PREFIX + slabName.getPath() + "/world/" + variant + "/" +
							state.getValue(net.minecraft.block.BlockSlab.HALF).getName(), "normal");
				}
			});

			Item item = Item.getItemFromBlock(slab);
			if(!(item instanceof ItemBlock) || ((ItemBlock) item).getBlock() != slab)
				throw new IllegalStateException("Missing slab ItemBlock " + slabName);
			for(int variant = 0; variant < 8; variant++) {
				String itemRoute = PREFIX + name.getPath() + "/item/" + variant;
				ROUTES.put(itemRoute, new SlabRoute(material(slab, variant), isDouble, false));
				ModelLoader.setCustomModelResourceLocation(item, variant,
						location(itemRoute, "inventory"));
			}
		}

		if(found.size() != TARGETS.size()) {
			Set<String> missing = new HashSet<>(TARGETS);
			missing.removeAll(found);
			throw new IllegalStateException("Missing generic slab registrations: " + missing);
		}
	}

	private static ModelResourceLocation location(String route, String variant) {
		return new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, route), variant);
	}

	/** Texture set for one slab material, resolved exactly like the stairs handler. */
	private static MaterialSet material(BlockMultiSlab slab, int variant) {
		int index = variant % slab.slabMaterials.length;
		Block source = slab.slabMaterials[index];
		int sourceMeta = slab instanceof BlockMultiSlabMeta ? ((BlockMultiSlabMeta) slab).metas[index] : index;

		String plain = PlainCubeTextures.bindings().get(source);
		if(plain != null) {
			String texture = LegacyTexturePath.blockAtlasLocation(plain).toString();
			return new MaterialSet(texture);
		}

		if(source instanceof BlockLightstone) {
			LightstoneType[] values = LightstoneType.values();
			LightstoneType type = values[Math.floorMod(sourceMeta, values.length)];
			String base = ((BlockLightstone) source).getTextureMultiName(type);
			return new MaterialSet(LegacyTexturePath.blockAtlasLocation(base).toString());
		}

		if(source instanceof com.hbm.blocks.BlockBase) {
			String legacy = ((com.hbm.blocks.BlockBase) source).getTextureName();
			if(legacy == null || legacy.trim().isEmpty())
				throw new IllegalStateException("Slab material has no legacy texture name: " + source.getRegistryName());
			return new MaterialSet(LegacyTexturePath.blockAtlasLocation(legacy).toString());
		}

		throw new IllegalStateException("Unsupported slab material " + source.getRegistryName() +
				" (" + source.getClass().getName() + ")");
	}

	private static String path(ResourceLocation location) {
		String path = location.getPath();
		if(path.startsWith("models/")) path = path.substring(7);
		return path;
	}

	private static IModel slabModel(SlabRoute route) throws Exception {
		if(route.isDouble) {
			// A double slab is the full material cube, as the original rendered it.
			return ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/cube")).retexture(
					ImmutableMap.<String, String>builder()
							.put("particle", route.material.side)
							.put("down", route.material.bottom)
							.put("up", route.material.top)
							.put("north", route.material.side)
							.put("south", route.material.side)
							.put("west", route.material.side)
							.put("east", route.material.side)
							.build());
		}
		String parent = route.top ? "upper_slab" : "half_slab";
		return ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/" + parent)).retexture(
				ImmutableMap.<String, String>builder()
						.put("bottom", route.material.bottom)
						.put("top", route.material.top)
						.put("side", route.material.side)
						.build());
	}

	private static final class Loader implements ICustomModelLoader {
		@Override public void onResourceManagerReload(IResourceManager resourceManager) { }

		@Override
		public boolean accepts(ResourceLocation location) {
			return RefStrings.MODID.equals(location.getNamespace()) && path(location).startsWith(PREFIX);
		}

		@Override
		public IModel loadModel(ResourceLocation location) throws Exception {
			SlabRoute route = ROUTES.get(path(location));
			if(route == null) throw new IllegalArgumentException("Unknown legacy slab route " + location);
			return slabModel(route);
		}
	}

	private static final class SlabRoute {
		private final MaterialSet material;
		private final boolean isDouble;
		private final boolean top;

		private SlabRoute(MaterialSet material, boolean isDouble, boolean top) {
			this.material = material;
			this.isDouble = isDouble;
			this.top = top;
		}
	}

	private static final class MaterialSet {
		private final String bottom;
		private final String top;
		private final String side;

		private MaterialSet(String texture) {
			this.bottom = texture;
			this.top = texture;
			this.side = texture;
		}
	}
}
