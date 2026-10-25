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
import com.hbm.blocks.BlockBase;
import com.hbm.blocks.BlockEnums.LightstoneType;
import com.hbm.blocks.PlainCubeTextures;
import com.hbm.blocks.generic.BlockGenericStairs;
import com.hbm.blocks.generic.BlockLightstone;
import com.hbm.lib.RefStrings;
import com.hbm.render.LegacyTexturePath;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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

/** Native vanilla stair geometry for the 20 BlockGenericStairs families that have no JSON models. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class LegacyStairModelHandler {

	private static final String PREFIX = "native_legacy_stair/";
	private static final Set<String> TARGETS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			"tile.concrete_smooth_stairs",
			"tile.concrete_stairs",
			"tile.concrete_asbestos_stairs",
			"tile.ducrete_smooth_stairs",
			"tile.ducrete_stairs",
			"tile.brick_concrete_stairs",
			"tile.brick_concrete_mossy_stairs",
			"tile.brick_concrete_cracked_stairs",
			"tile.brick_concrete_broken_stairs",
			"tile.brick_ducrete_stairs",
			"tile.reinforced_stone_stairs",
			"tile.reinforced_brick_stairs",
			"tile.brick_obsidian_stairs",
			"tile.brick_light_stairs",
			"tile.brick_compound_stairs",
			"tile.brick_asbestos_stairs",
			"tile.brick_fire_stairs",
			"tile.asphalt_stairs",
			"tile.lightstone_tile_stairs",
			"tile.lightstone_bricks_stairs")));

	private static final Map<String, StairRoute> ROUTES = new HashMap<>();
	private static final Loader LOADER = new Loader();
	private static boolean registered;

	private LegacyStairModelHandler() { }

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void register(ModelRegistryEvent event) {
		if(!registered) {
			ModelLoaderRegistry.registerLoader(LOADER);
			registered = true;
		}
		ROUTES.clear();

		Set<String> found = new HashSet<>();
		for(Object[] recipe : BlockGenericStairs.recipeGen) {
			if(recipe == null || recipe.length < 3 || !(recipe[0] instanceof Block) ||
					!(recipe[1] instanceof Integer) || !(recipe[2] instanceof BlockGenericStairs)) continue;
			Block source = (Block) recipe[0];
			int sourceMeta = (Integer) recipe[1];
			BlockGenericStairs stair = (BlockGenericStairs) recipe[2];
			ResourceLocation name = stair.getRegistryName();
			if(name == null || !RefStrings.MODID.equals(name.getNamespace()) || !TARGETS.contains(name.getPath())) continue;

			found.add(name.getPath());
			final ResourceLocation stairName = name;
			final MaterialSet material = material(source, sourceMeta);
			for(IBlockState state : stair.getBlockState().getValidStates()) {
				String route = worldRoute(stairName, state);
				ROUTES.put(route, new StairRoute(material, state));
			}

			ModelLoader.setCustomStateMapper(stair, new StateMapperBase() {
				@Override
				protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
					return location(worldRoute(stairName, state), "normal");
				}
			});

			Item item = Item.getItemFromBlock(stair);
			if(!(item instanceof ItemBlock) || ((ItemBlock) item).getBlock() != stair)
				throw new IllegalStateException("Missing stair ItemBlock " + stairName);
			String itemRoute = PREFIX + stairName.getPath() + "/item";
			ROUTES.put(itemRoute, new StairRoute(material, null));
			final ModelResourceLocation inventory = location(itemRoute, "inventory");
			ModelLoader.setCustomModelResourceLocation(item, 0, inventory);
			ModelLoader.setCustomMeshDefinition(item, stack -> inventory);
		}

		if(found.size() != TARGETS.size()) {
			Set<String> missing = new HashSet<>(TARGETS);
			missing.removeAll(found);
			throw new IllegalStateException("Missing generic stair registrations: " + missing);
		}
	}

	private static ModelResourceLocation location(String route, String variant) {
		return new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, route), variant);
	}

	private static String worldRoute(ResourceLocation name, IBlockState state) {
		return PREFIX + name.getPath() + "/world/" + state.getValue(BlockStairs.FACING).getName() + "/" +
				state.getValue(BlockStairs.HALF).getName() + "/" + state.getValue(BlockStairs.SHAPE).getName();
	}

	private static MaterialSet material(Block source, int metadata) {
		String plain = PlainCubeTextures.bindings().get(source);
		if(plain != null) {
			String texture = LegacyTexturePath.blockAtlasLocation(plain).toString();
			return new MaterialSet(texture, texture, texture);
		}

		if(source instanceof BlockLightstone) {
			LightstoneType[] values = LightstoneType.values();
			int index = Math.floorMod(metadata, values.length);
			String base = ((BlockLightstone) source).getTextureMultiName(values[index]);
			String side = LegacyTexturePath.blockAtlasLocation(base).toString();
			String cap = index >= 3 ? LegacyTexturePath.blockAtlasLocation(base + ".top").toString() : side;
			return new MaterialSet(cap, cap, side);
		}

		if(source instanceof BlockBase) {
			String legacy = ((BlockBase) source).getTextureName();
			if(legacy == null || legacy.trim().isEmpty())
				throw new IllegalStateException("Generic stair source has no legacy texture name: " + source.getRegistryName());
			String texture = LegacyTexturePath.blockAtlasLocation(legacy).toString();
			return new MaterialSet(texture, texture, texture);
		}

		throw new IllegalStateException("Unsupported generic stair source " + source.getRegistryName() + " (" + source.getClass().getName() + ")");
	}

	private static String path(ResourceLocation location) {
		String path = location.getPath();
		if(path.startsWith("models/")) path = path.substring(7);
		if(path.startsWith("block/")) path = path.substring(6);
		if(path.startsWith("item/")) path = path.substring(5);
		return path;
	}

	private static IModel stairModel(StairRoute route) throws Exception {
		BlockStairs.EnumShape shape = route.state == null ? BlockStairs.EnumShape.STRAIGHT : route.state.getValue(BlockStairs.SHAPE);
		String parent;
		switch(shape) {
			case INNER_LEFT:
			case INNER_RIGHT:
				parent = "inner_stairs";
				break;
			case OUTER_LEFT:
			case OUTER_RIGHT:
				parent = "outer_stairs";
				break;
			default:
				parent = "stairs";
		}

		IModel delegate = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/" + parent)).retexture(
				ImmutableMap.<String, String>builder()
						.put("bottom", route.material.bottom)
						.put("top", route.material.top)
						.put("side", route.material.side)
						.build());
		if(route.state != null && uvLock(route.state)) delegate = delegate.uvlock(true);
		ModelRotation rotation = route.state == null ? ModelRotation.X0_Y0 : rotation(route.state);
		return new RotatedModel(delegate, rotation);
	}

	private static ModelRotation rotation(IBlockState state) {
		int y = rotationY(state);
		int x = state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP ? 180 : 0;
		return ModelRotation.getModelRotation(x, y);
	}

	private static int rotationY(IBlockState state) {
		EnumFacing facing = state.getValue(BlockStairs.FACING);
		int y;
		switch(facing) {
			case SOUTH: y = 90; break;
			case WEST: y = 180; break;
			case NORTH: y = 270; break;
			default: y = 0;
		}
		BlockStairs.EnumShape shape = state.getValue(BlockStairs.SHAPE);
		boolean top = state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP;
		if(top) {
			if(shape == BlockStairs.EnumShape.INNER_RIGHT || shape == BlockStairs.EnumShape.OUTER_RIGHT) y = (y + 90) % 360;
		} else if(shape == BlockStairs.EnumShape.INNER_LEFT || shape == BlockStairs.EnumShape.OUTER_LEFT) {
			y = (y + 270) % 360;
		}
		return y;
	}

	private static boolean uvLock(IBlockState state) {
		return state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP || rotationY(state) != 0;
	}

	private static final class Loader implements ICustomModelLoader {
		@Override public void onResourceManagerReload(IResourceManager resourceManager) { }

		@Override
		public boolean accepts(ResourceLocation location) {
			return RefStrings.MODID.equals(location.getNamespace()) && path(location).startsWith(PREFIX);
		}

		@Override
		public IModel loadModel(ResourceLocation location) throws Exception {
			StairRoute route = ROUTES.get(path(location));
			if(route == null) throw new IllegalArgumentException("Unknown legacy stair route " + location);
			return stairModel(route);
		}
	}

	private static final class RotatedModel implements IModel {
		private final IModel delegate;
		private final ModelRotation rotation;

		private RotatedModel(IModel delegate, ModelRotation rotation) {
			this.delegate = delegate;
			this.rotation = rotation;
		}

		@Override public Collection<ResourceLocation> getDependencies() { return delegate.getDependencies(); }
		@Override public Collection<ResourceLocation> getTextures() { return delegate.getTextures(); }

		@Override
		public IBakedModel bake(IModelState state, VertexFormat format,
				Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
			return delegate.bake(rotation, format, bakedTextureGetter);
		}
	}

	private static final class StairRoute {
		private final MaterialSet material;
		private final IBlockState state;

		private StairRoute(MaterialSet material, IBlockState state) {
			this.material = material;
			this.state = state;
		}
	}

	private static final class MaterialSet {
		private final String bottom;
		private final String top;
		private final String side;

		private MaterialSet(String bottom, String top, String side) {
			this.bottom = bottom;
			this.top = top;
			this.side = side;
		}
	}
}
