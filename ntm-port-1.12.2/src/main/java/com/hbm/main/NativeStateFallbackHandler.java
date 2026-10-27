package com.hbm.main;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.lwjgl.util.vector.Vector3f;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.generic.BlockRotatablePillar;
import com.hbm.lib.RefStrings;
import com.hbm.render.icon.IIcon;
import com.hbm.render.item.nativeitem.NativeItemParticles;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
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
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Last-resort native routes for NTM blocks whose states fall through every
 * specialized handler and whose default blockstate JSON does not exist. Each
 * covered block gets one icon-driven model built from its legacy icons, so
 * states keep distinct textures instead of failing their lookups. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class NativeStateFallbackHandler {

	private static final String PREFIX = "native_fallback/";
	private static final java.util.Set<String> CANONICAL_INVENTORY_ITEMBLOCKS = Collections.unmodifiableSet(
			new java.util.HashSet<>(java.util.Arrays.asList(
					"tile.fallout",
					"tile.waste_leaves",
					"tile.rubber_leaves",
					"tile.pet_leaves",
					"tile.leaves_layer",
					"tile.foam_layer",
					"tile.sand_boron_layer",
					"tile.vinyl_vine",
					"tile.oil_spill",
					"tile.flesh_block",
					"tile.charred_flesh_block",
					"tile.carbonized_flesh_block",
					"tile.ladder_sturdy",
					"tile.ladder_iron",
					"tile.ladder_gold",
					"tile.ladder_aluminium",
					"tile.ladder_copper",
					"tile.ladder_titanium",
					"tile.ladder_lead",
					"tile.ladder_cobalt",
					"tile.ladder_steel",
					"tile.ladder_tungsten",
					"tile.laythe_seagrass",
					"tile.laythe_glowgrass",
					"tile.trapdoor_steel",
					"tile.primed_log",
					"tile.eu_log",
					"tile.bf_log",
					"tile.lattice_log",
					"tile.pvc_log",
					"tile.vinyl_log")));
	private static final FaceBakery FACE_BAKERY = new FaceBakery();
	private static final Map<String, FallbackBlock> ROUTES = new HashMap<>();
	private static final Map<String, IBlockState> PARTICLE_STATES = new HashMap<>();
	private static final Loader LOADER = new Loader();
	private static boolean registered;

	private NativeStateFallbackHandler() { }

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void register(ModelRegistryEvent event) {
		if(!registered) {
			ModelLoaderRegistry.registerLoader(LOADER);
			registered = true;
		}
		ROUTES.clear();
		PARTICLE_STATES.clear();

		Map<?, ?> existing = stateMappers();
		int blocks = 0, states = 0, blockItems = 0;
		for(Block block : net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.getValuesCollection()) {
			ResourceLocation name = block.getRegistryName();
			if(name == null || !RefStrings.MODID.equals(name.getNamespace())) continue;

			Item item = Item.getItemFromBlock(block);
			boolean canonicalInventory = CANONICAL_INVENTORY_ITEMBLOCKS.contains(name.getPath());
			if(canonicalInventory && item instanceof ItemBlock && ((ItemBlock) item).getBlock() == block) {
				// These legacy ItemBlocks have real item JSON, but Forge does not infer
				// an ItemModelMesher route from a blockstate "inventory" variant. Force
				// the canonical item key before the blockstate-owned world path exits.
				ModelResourceLocation inventory = new ModelResourceLocation(name, "inventory");
				ModelBakery.registerItemVariants(item, inventory);
				ModelLoader.setCustomMeshDefinition(item, stack -> inventory);
				blockItems++;
			}

			if(BakeOwnedModelLoader.hasBlockstate(name)) continue;
			FallbackBlock adapter = adapterFor(block);
			if(adapter == null) continue;

			// Compose with whatever mapper already exists; only replace routes
			// that still point at a default-style blockstate JSON which is absent.
			IStateMapper previous = existing == null ? null : (IStateMapper) existing.get(block.delegate);
			Map<IBlockState, ModelResourceLocation> routes = previous == null
					? defaultRoutes(block)
					: new HashMap<>(previous.putStateModelLocations(block));
			boolean touched = false;
			String routeKey = PREFIX + name.getPath().replace('#', '_') + "/block";
			int stateIndex = 0;
			for(IBlockState state : block.getBlockState().getValidStates()) {
				ModelResourceLocation current = routes.get(state);
				if(current != null && !current.getPath().equals(name.getPath())) {
					stateIndex++;
					continue;
				}
				String stateRouteKey = routeKey + "/state_" + stateIndex++;
				ROUTES.put(stateRouteKey, adapter);
				PARTICLE_STATES.put(stateRouteKey, state);
				routes.put(state, location(stateRouteKey));
				touched = true;
				states++;
			}
			if(touched) {
				final Map<IBlockState, ModelResourceLocation> published = routes;
				ModelLoader.setCustomStateMapper(block, owner -> new HashMap<>(published));
				blocks++;
			}

			// Inventory coverage is independent of the world reroute above: a prior
			// world owner (for example INVISIBLE container states routed to empty
			// models) must not leave the ItemBlock without a real per-face model.
			if(item instanceof ItemBlock && ((ItemBlock) item).getBlock() == block
					&& !canonicalInventory
					&& !BakeOwnedModelLoader.hasInventoryAsset(name)
					&& !hasCustomRoute(item)) {
				String itemKey = PREFIX + name.getPath().replace('#', '_') + "/item";
				ROUTES.put(itemKey, adapter);
				ModelResourceLocation inventory = new ModelResourceLocation(
						new ResourceLocation(RefStrings.MODID, itemKey), "inventory");
				ModelBakery.registerItemVariants(item, inventory);
				ModelLoader.setCustomMeshDefinition(item, stack -> inventory);
				blockItems++;
			}
		}
		// Final inventory pass: every remaining NTM item without an asset or a
		// custom route gets a tracked load-time route, so its variant lookup no
		// longer fails while its real bake owner (if any) takes it from here.
		int items = 0;
		for(Item item : net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.getValuesCollection()) {
			ResourceLocation itemName = item.getRegistryName();
			if(itemName == null || !RefStrings.MODID.equals(itemName.getNamespace())) continue;
			if(BakeOwnedModelLoader.hasInventoryAsset(itemName)) continue;
			// Items that own meta or mesh routes still fail their default variant
			// lookup; a tracked placeholder quiets it without touching ownership.
			boolean owned = hasCustomRoute(item);
			// Registry names may contain reserved characters (the "#undef" statue);
			// sanitize the route path while the mesh definition keeps it valid.
			ModelResourceLocation location = new ModelResourceLocation(
					new ResourceLocation(itemName.getNamespace(), itemName.getPath().replace('#', '_')), "inventory");
			BakeOwnedModelLoader.registerMissing(location, owned ? "DefaultVariantQuiet" : "ItemRouteFallback");
			if(!owned) com.hbm.render.item.RouteFallbackModelRepair.track(location, item);
			ModelBakery.registerItemVariants(item, location);
			if(!owned) ModelLoader.setCustomMeshDefinition(item, stack -> location);
			items++;
		}
		MainRegistry.logger.info("Native state fallback: {} blocks, {} states, {} block items, {} items rerouted", blocks, states, blockItems, items);
	}

	private static ModelResourceLocation location(String route) {
		return new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, route), "normal");
	}

	private static FallbackBlock adapterFor(Block block) {
		if(block instanceof BlockBase) return new BlockBaseAdapter((BlockBase) block);
		if(block instanceof BlockRotatablePillar) return new PillarAdapter((BlockRotatablePillar) block);
		java.lang.reflect.Method legacy = declaredIconLookup(block);
		if(legacy != null) return new DeclaredIconAdapter(block, legacy);
		if(block instanceof com.hbm.blocks.generic.DecoBlockAlt) return new StatueAdapter();
		// Bound plain blocks (vanilla superclasses, no own icon lookup) render as
		// cubes of their stitched bind icon. The stitch runs after this pass, so
		// the adapter resolves the icon lazily.
		if(com.hbm.blocks.LegacyBlockTextures.texture(block) != null
				|| com.hbm.blocks.PlainCubeTextures.bindings().containsKey(block))
			return new BoundAdapter(block);
		return null;
	}

	private static Map<IBlockState, ModelResourceLocation> defaultRoutes(Block block) {
		return new StateMap.Builder().build()
				.putStateModelLocations(block);
	}

	private static Map<?, ?> stateMappers() {
		try {
			Field field = ModelLoader.class.getDeclaredField("customStateMappers");
			field.setAccessible(true);
			return (Map<?, ?>) field.get(null);
		} catch(ReflectiveOperationException failure) {
			throw new IllegalStateException("Cannot read Forge state mapper registry", failure);
		}
	}

	private static boolean hasCustomRoute(Item item) {
		try {
			Field meshes = ModelLoader.class.getDeclaredField("customMeshDefinitions");
			Field models = ModelLoader.class.getDeclaredField("customModels");
			meshes.setAccessible(true);
			models.setAccessible(true);
			if(((Map<?, ?>) meshes.get(null)).containsKey(item.delegate)) return true;
			for(Object key : ((Map<?, ?>) models.get(null)).keySet())
				if(((org.apache.commons.lang3.tuple.Pair<?, ?>) key).getLeft() == item.delegate) return true;
			return false;
		} catch(ReflectiveOperationException failure) {
			throw new IllegalStateException("Cannot verify item model ownership", failure);
		}
	}

	/** Per-state legacy icon view used by the fallback models. */
	private interface FallbackBlock {
		boolean isCross();
		IIcon icon(EnumFacing side, IBlockState state);
		Block block();
	}

	private static final class BlockBaseAdapter implements FallbackBlock {
		private final BlockBase block;
		private final boolean cross;

		BlockBaseAdapter(BlockBase block) {
			this.block = block;
			this.cross = legacyRenderType(block) == 1 || isPlantBlock(block);
		}

		@Override public boolean isCross() { return cross; }
		@Override public Block block() { return block; }

		@Override
		public IIcon icon(EnumFacing side, IBlockState state) {
			return block.getIcon(side.getIndex(), state.getBlock().getMetaFromState(state));
		}
	}

	private static final class PillarAdapter implements FallbackBlock {
		private final BlockRotatablePillar block;

		PillarAdapter(BlockRotatablePillar block) { this.block = block; }

		@Override public boolean isCross() { return false; }
		@Override public Block block() { return block; }

		@Override
		public IIcon icon(EnumFacing side, IBlockState state) {
			return side == EnumFacing.UP ? block.getLegacyTopIcon() : block.getLegacySideIcon();
		}
	}

	/** Blocks that declare the legacy two-argument icon lookup themselves,
	 * e.g. the Forge fluid blocks with their own still/flowing icon fields. */
	private static final class DeclaredIconAdapter implements FallbackBlock {
		private final Block block;
		private final java.lang.reflect.Method lookup;

		DeclaredIconAdapter(Block block, java.lang.reflect.Method lookup) {
			this.block = block;
			this.lookup = lookup;
		}

		@Override public boolean isCross() { return isPlantBlock(block); }
		@Override public Block block() { return block; }

		@Override
		public IIcon icon(EnumFacing side, IBlockState state) {
			try {
				return (IIcon) lookup.invoke(block, side.getIndex(), block.getMetaFromState(state));
			} catch(ReflectiveOperationException failure) {
				return null;
			}
		}
	}

	/** The ELB statue block: INVISIBLE TESR world rendering, "code" icon. */
	private static final class StatueAdapter implements FallbackBlock {
		@Override public boolean isCross() { return false; }
		@Override public Block block() { return com.hbm.blocks.ModBlocks.statue_elb_f; }

		@Override
		public IIcon icon(EnumFacing side, IBlockState state) {
			return com.hbm.blocks.generic.DecoBlockAlt.getLegacyBlockIcon();
		}
	}

	/** Bound vanilla-superclass blocks: one stitched bind icon for all faces. */
	private static final class BoundAdapter implements FallbackBlock {
		private final Block block;

		BoundAdapter(Block block) {
			this.block = block;
		}

		@Override public boolean isCross() { return isPlantBlock(block); }
		@Override public Block block() { return block; }

		@Override
		public IIcon icon(EnumFacing side, IBlockState state) {
			IIcon icon = com.hbm.render.icon.PlainCubeLegacyIcons.get(this.block);
			if(icon == null) icon = com.hbm.blocks.generic.DecoBlockAlt.getLegacyBlockIcon();
			return icon;
		}
	}

	/** Finds a getIcon(int, int) declared on the block class itself. */
	private static java.lang.reflect.Method declaredIconLookup(Block block) {
		for(Class<?> current = block.getClass(); current != null && current != Block.class
				&& current != BlockBase.class; current = current.getSuperclass()) {
			try {
				return current.getDeclaredMethod("getIcon", int.class, int.class);
			} catch(NoSuchMethodException ignored) { }
		}
		return null;
	}

	private static boolean isPlantBlock(Block block) {
		return block instanceof net.minecraftforge.common.IPlantable
				|| block instanceof net.minecraft.block.IGrowable
				|| block instanceof com.hbm.blocks.generic.BlockNTMFlower
				|| block instanceof com.hbm.blocks.generic.BlockDeadPlant
				|| block instanceof com.hbm.blocks.generic.BlockTallPlant
				|| block instanceof com.hbm.blocks.generic.BlockTallPlantWater
				|| block instanceof com.hbm.blocks.generic.BlockRubberPlant
				|| block instanceof com.hbm.blocks.generic.BlockStalagmite
				|| block instanceof com.hbm.blocks.BlockWaterPlant
				|| block instanceof com.hbm.blocks.BlockKelp
				|| block instanceof com.hbm.blocks.BlockCrop
				|| block instanceof com.hbm.blocks.BlockNTSapling
				|| block.getDefaultState().getMaterial() == net.minecraft.block.material.Material.PLANTS
				|| block.getDefaultState().getMaterial() == net.minecraft.block.material.Material.VINE;
	}

	private static int legacyRenderType(Block block) {
		for(Class<?> current = block.getClass(); current != null && current != Block.class;
				current = current.getSuperclass()) {
			try {
				java.lang.reflect.Method method = current.getDeclaredMethod("getRenderType");
				if(method.getReturnType() != int.class) continue;
				method.setAccessible(true);
				Object value = method.invoke(block);
				return value instanceof Integer ? (Integer) value : 0;
			} catch(ReflectiveOperationException ignored) { }
		}
		return 0;
	}

	private static final class Loader implements ICustomModelLoader {
		@Override public void onResourceManagerReload(IResourceManager resourceManager) {
			FallbackModel.clearCaches();
		}

		@Override
		public boolean accepts(ResourceLocation location) {
			if(!RefStrings.MODID.equals(location.getNamespace())) return false;
			String path = location.getPath();
			if(path.startsWith("models/")) path = path.substring(7);
			return path.startsWith(PREFIX);
		}

		@Override
		public IModel loadModel(ResourceLocation location) {
			String path = location.getPath();
			if(path.startsWith("models/")) path = path.substring(7);
			FallbackBlock adapter = ROUTES.get(path);
			if(adapter == null) throw new IllegalArgumentException("Unknown native fallback route " + location);
			return new FallbackModel(adapter, path.endsWith("/item"), PARTICLE_STATES.get(path));
		}
	}

	/** Baked cube (or cross) model resolved from the block's per-state legacy icons. */
	private static final class FallbackModel implements IBakedModel, IModel {
		private static final Map<FallbackModel, Map<Integer, List<BakedQuad>>> QUAD_CACHE = new ConcurrentHashMap<>();
		private static final Map<FallbackModel, Map<Integer, List<BakedQuad>>> CROSS_CACHE = new ConcurrentHashMap<>();

		static void clearCaches() {
			QUAD_CACHE.clear();
			CROSS_CACHE.clear();
		}

		private final FallbackBlock adapter;
		private final boolean inventory;
		private final IBlockState particleState;

		FallbackModel(FallbackBlock adapter, boolean inventory, IBlockState particleState) {
			this.adapter = adapter;
			this.inventory = inventory;
			this.particleState = particleState;
		}

		private Map<Integer, List<BakedQuad>> quads() {
			return QUAD_CACHE.computeIfAbsent(this, key -> new ConcurrentHashMap<>());
		}

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			IBlockState resolved = state != null ? state : adapter.block().getDefaultState();
			int meta = resolved.getBlock().getMetaFromState(resolved);
			if(adapter.isCross()) {
				if(side != null) return Collections.emptyList();
				Map<Integer, List<BakedQuad>> modelCache = CROSS_CACHE.computeIfAbsent(this, key -> new ConcurrentHashMap<>());
				return modelCache.computeIfAbsent(meta, key -> inventory
						? makeFlatItemQuads(resolve(adapter.icon(EnumFacing.NORTH, resolved)))
						: crossQuads(adapter.icon(EnumFacing.NORTH, resolved)));
			}
			if(side == null) {
				List<BakedQuad> all = new ArrayList<>(6);
				for(EnumFacing facing : EnumFacing.values()) {
					BakedQuad quad = quads().computeIfAbsent(meta << 3 | facing.ordinal(),
							key -> Collections.singletonList(bakeFace(facing, resolve(adapter.icon(facing, resolved)))))
							.get(0);
					if(quad != null) all.add(quad);
				}
				return all;
			}
			List<BakedQuad> sideQuads = quads().computeIfAbsent(meta << 3 | side.ordinal(),
					key -> Collections.singletonList(bakeFace(side, resolve(adapter.icon(side, resolved)))));
			return sideQuads.get(0) == null ? Collections.<BakedQuad>emptyList() : sideQuads;
		}

		private static List<BakedQuad> makeFlatItemQuads(TextureAtlasSprite sprite) {
			if(sprite == null) return Collections.emptyList();
			BlockPartFace face = new BlockPartFace(null, 0, sprite.getIconName(),
					new BlockFaceUV(new float[] {0.0F, 0.0F, 16.0F, 16.0F}, 0));
			List<BakedQuad> quads = new ArrayList<>(2);
			quads.add(FACE_BAKERY.makeBakedQuad(new Vector3f(0.0F, 0.0F, 7.5F),
					new Vector3f(16.0F, 16.0F, 8.5F), face, sprite, EnumFacing.NORTH,
					ModelRotation.X0_Y0, null, true, false));
			quads.add(FACE_BAKERY.makeBakedQuad(new Vector3f(0.0F, 0.0F, 7.5F),
					new Vector3f(16.0F, 16.0F, 8.5F), face, sprite, EnumFacing.SOUTH,
					ModelRotation.X0_Y0, null, true, false));
			return Collections.unmodifiableList(quads);
		}

		private static List<BakedQuad> crossQuads(IIcon icon) {
			TextureAtlasSprite sprite = resolve(icon);
			if(sprite == null) return Collections.emptyList();
			List<BakedQuad> quads = new ArrayList<>(4);
			BlockPartFace face = new BlockPartFace(null, 0, sprite.getIconName(),
					new BlockFaceUV(new float[] {0.0F, 0.0F, 16.0F, 16.0F}, 0));
			Vector3f from1 = new Vector3f(0.8F, 0.0F, 8.0F);
			Vector3f to1 = new Vector3f(15.2F, 16.0F, 8.0F);
			Vector3f from2 = new Vector3f(8.0F, 0.0F, 0.8F);
			Vector3f to2 = new Vector3f(8.0F, 16.0F, 15.2F);
			net.minecraft.client.renderer.block.model.BlockPartRotation rot =
					new net.minecraft.client.renderer.block.model.BlockPartRotation(new Vector3f(0.5F, 0.5F, 0.5F),
							net.minecraft.util.EnumFacing.Axis.Y, 45.0F, true);
			quads.add(FACE_BAKERY.makeBakedQuad(from1, to1, face, sprite, EnumFacing.NORTH,
					ModelRotation.X0_Y0, rot, true, true));
			quads.add(FACE_BAKERY.makeBakedQuad(from1, to1, face, sprite, EnumFacing.SOUTH,
					ModelRotation.X0_Y0, rot, true, true));
			quads.add(FACE_BAKERY.makeBakedQuad(from2, to2, face, sprite, EnumFacing.WEST,
					ModelRotation.X0_Y0, rot, true, true));
			quads.add(FACE_BAKERY.makeBakedQuad(from2, to2, face, sprite, EnumFacing.EAST,
					ModelRotation.X0_Y0, rot, true, true));
			return Collections.unmodifiableList(quads);
		}

		private static BakedQuad bakeFace(EnumFacing facing, TextureAtlasSprite sprite) {
			if(sprite == null) return null;
			BlockPartFace face = new BlockPartFace(facing, 0, sprite.getIconName(),
					new BlockFaceUV(new float[] {0.0F, 0.0F, 16.0F, 16.0F}, 0));
			return FACE_BAKERY.makeBakedQuad(new Vector3f(0.0F, 0.0F, 0.0F),
					new Vector3f(16.0F, 16.0F, 16.0F), face, sprite, facing,
					ModelRotation.X0_Y0, null, true, true);
		}

		private static TextureAtlasSprite resolve(IIcon icon) {
			TextureAtlasSprite sprite = NativeItemParticles.spriteOf(icon, "NativeStateFallbackHandler");
			return sprite != null ? sprite : net.minecraft.client.Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
		}

		@Override public boolean isAmbientOcclusion() { return true; }
		@Override public boolean isGui3d() { return !adapter.isCross(); }
		@Override public boolean isBuiltInRenderer() { return false; }
		@Override public TextureAtlasSprite getParticleTexture() {
			if(particleState == null)
				return resolve(adapter.icon(EnumFacing.NORTH, adapter.block().getDefaultState()));
			IBlockState resolved = particleState;
			TextureAtlasSprite sprite = NativeItemParticles.spriteOf(adapter.icon(EnumFacing.NORTH, resolved));
			if(sprite != null) return sprite;
			for(EnumFacing face : EnumFacing.values()) {
				if(face == EnumFacing.NORTH) continue;
				sprite = NativeItemParticles.spriteOf(adapter.icon(face, resolved));
				if(sprite != null) return sprite;
			}
			return net.minecraft.client.Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
		}
		@Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
		private final ItemOverrideList overrides = new ItemOverrideList(Collections.emptyList()) {
			@Override
			public IBakedModel handleItemState(IBakedModel originalModel, net.minecraft.item.ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.EntityLivingBase entity) {
				if(stack.isEmpty()) return originalModel;
				int meta = stack.getItemDamage();
				return getBakedModelForMeta(meta);
			}
		};

		private final Map<Integer, IBakedModel> metaModels = new ConcurrentHashMap<>();

		public IBakedModel getBakedModelForMeta(int meta) {
			return metaModels.computeIfAbsent(meta, m -> new MetaBakedModel(this, m));
		}

		@Override public ItemOverrideList getOverrides() { return overrides; }

		@Override public java.util.Collection<ResourceLocation> getDependencies() { return Collections.emptyList(); }
		@Override public java.util.Collection<ResourceLocation> getTextures() { return Collections.emptyList(); }
		@Override public IModelState getDefaultState() { return TRSRTransformation.identity(); }
		@Override
		public IBakedModel bake(IModelState state, VertexFormat format,
				Function<ResourceLocation, TextureAtlasSprite> sprites) {
			return this;
		}
	}

	private static final class MetaBakedModel implements IBakedModel {
		private final FallbackModel parent;
		private final int meta;

		MetaBakedModel(FallbackModel parent, int meta) {
			this.parent = parent;
			this.meta = meta;
		}

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			IBlockState metaState = state != null ? state : parent.adapter.block().getStateFromMeta(meta);
			return parent.getQuads(metaState, side, rand);
		}

		@Override public boolean isAmbientOcclusion() { return parent.isAmbientOcclusion(); }
		@Override public boolean isGui3d() { return parent.isGui3d(); }
		@Override public boolean isBuiltInRenderer() { return false; }
		@Override public TextureAtlasSprite getParticleTexture() { return parent.getParticleTexture(); }
		@Override public ItemCameraTransforms getItemCameraTransforms() { return parent.getItemCameraTransforms(); }
		@Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
	}
}
