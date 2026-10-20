package com.hbm.main;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.IBlockSideRotation;
import com.hbm.blocks.bomb.BlockChargeC4;
import com.hbm.blocks.bomb.BlockChargeDynamite;
import com.hbm.blocks.rail.IRenderBlock;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.block.ItemBlockBase;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.ISBRHUniversal;
import com.hbm.render.block.ct.IBlockCT;
import com.hbm.render.model.LegacyBaseBakedModel;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Registers real atlas-backed baked models for the safe legacy base subset.
 * Only ItemBlockBase forms of eligible cubes and ItemEnumMulti subtype items
 * are mapped; other item implementations can depend on custom renderers or
 * layered behavior and remain untouched.
 */
@SideOnly(Side.CLIENT)
public final class LegacyBaseModelHandler {

	private final Map<ResourceLocation, BlockBase> eligibleBlocks = new HashMap<>();
	private final Map<ModelResourceLocation, ItemModelInfo> itemModels = new HashMap<>();

	@SubscribeEvent
	public void onModelRegistry(ModelRegistryEvent event) {
		eligibleBlocks.clear();
		itemModels.clear();

		for(Block block : net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.getValuesCollection()) {
			ResourceLocation name = block.getRegistryName();
			if(!isNTM(name) || !(block instanceof BlockBase) || !isEligibleCube((BlockBase) block)) continue;
			eligibleBlocks.put(name, (BlockBase) block);
		}

		Collection<Item> items = net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.getValuesCollection();
		for(Item item : items) {
			if(!isNTM(item.getRegistryName())) continue;

			if(item instanceof ItemBlockBase && item instanceof ItemBlock) {
				Block placedBlock = ((ItemBlock) item).getBlock();
				if(!(placedBlock instanceof BlockBase) || !eligibleBlocks.containsKey(placedBlock.getRegistryName())) continue;
				int count = placedBlock instanceof IBlockMulti ? ((IBlockMulti) placedBlock).getSubCount() : 16;
				registerItemModels(item, placedBlock, count, true);
			} else if(item instanceof ItemEnumMulti) {
				registerItemModels(item, null, ((ItemEnumMulti) item).getSubCount(), false);
			}
		}

		MainRegistry.logger.debug("Registered {} base-cube block models and {} base/item subtype models",
				eligibleBlocks.size(), itemModels.size());
	}

	private void registerItemModels(Item item, Block block, int requestedCount, boolean cube) {
		ResourceLocation name = item.getRegistryName();
		if(name == null) return;
		int count = Math.max(1, requestedCount);
		for(int meta = 0; meta < count; meta++) {
			String variant = meta == 0 ? "inventory" : "inventory_" + meta;
			ModelResourceLocation location = new ModelResourceLocation(name, variant);
			ModelLoader.setCustomModelResourceLocation(item, meta, location);
			itemModels.put(location, new ItemModelInfo(item, block, meta, cube));
		}
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		registerColorHandlers();

		for(ModelResourceLocation location : event.getModelRegistry().getKeys()) {
			if(isItemVariant(location)) continue;
			BlockBase block = eligibleBlocks.get(new ResourceLocation(location.getNamespace(), location.getPath()));
			if(block != null) {
				LegacyBaseBakedModel model = LegacyBaseBakedModel.forBlock(block);
				if(model.getParticleTexture() != null) event.getModelRegistry().putObject(location, model);
			}
		}

		for(Map.Entry<ModelResourceLocation, ItemModelInfo> entry : itemModels.entrySet()) {
			ItemModelInfo info = entry.getValue();
			LegacyBaseBakedModel model;
			if(info.cube) {
				model = LegacyBaseBakedModel.forBlockItem((BlockBase) info.block, info.item, info.metadata);
			} else if(info.item instanceof ItemEnumMulti) {
				model = LegacyBaseBakedModel.forEnumItem((ItemEnumMulti) info.item, info.metadata);
			} else {
				continue;
			}
			if(model.getParticleTexture() != null) event.getModelRegistry().putObject(entry.getKey(), model);
		}
	}

	private void registerColorHandlers() {
		Minecraft minecraft = Minecraft.getMinecraft();
		BlockColors blockColors = minecraft.getBlockColors();
		for(final BlockBase block : eligibleBlocks.values()) {
			blockColors.registerBlockColorHandler((state, world, pos, tintIndex) ->
					getBlockColor(block, state, world, pos), block);
		}

		ItemColors itemColors = minecraft.getItemColors();
		Set<Item> registeredItems = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
		for(ItemModelInfo info : itemModels.values()) {
			final Item item = info.item;
			if(!registeredItems.add(item)) continue;
			final Block block = info.block;
			if(block instanceof BlockBase) {
				itemColors.registerItemColorHandler((stack, tintIndex) ->
						getItemColor(item, stack, block, tintIndex), item);
			} else {
				itemColors.registerItemColorHandler((stack, tintIndex) ->
						getItemColor(item, stack, null, tintIndex), item);
			}
		}
	}

	private static int getBlockColor(BlockBase block, IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos) {
		int meta = state == null ? 0 : block.getMetaFromState(state);
		Method multiplier = findLegacyMethod(block.getClass(), "colorMultiplier", net.minecraft.world.IBlockAccess.class,
				int.class, int.class, int.class);
		if(multiplier != null && world != null && pos != null) {
			Object color = invoke(multiplier, block, world, pos.getX(), pos.getY(), pos.getZ());
			if(color instanceof Integer) return (Integer) color;
		}
		return getLegacyRenderColor(block, meta);
	}

	private static int getItemColor(Item item, ItemStack stack, Block block, int tintIndex) {
		if(tintIndex != 0) return 0xFFFFFF;
		Method itemColor = findLegacyMethod(item.getClass(), "getColorFromItemStack", ItemStack.class, int.class);
		if(itemColor != null && (!(block instanceof BlockBase) || itemColor.getDeclaringClass() != ItemBlockBase.class)) {
			Object color = invoke(itemColor, item, stack, tintIndex);
			if(color instanceof Integer) return (Integer) color;
		}
		if(block instanceof BlockBase) return getLegacyRenderColor((BlockBase) block, stack.getMetadata());
		return 0xFFFFFF;
	}

	private static int getLegacyRenderColor(BlockBase block, int metadata) {
		Method method = findLegacyMethod(block.getClass(), "getRenderColor", int.class);
		if(method == null) return 0xFFFFFF;
		Object color = invoke(method, block, metadata);
		return color instanceof Integer ? (Integer) color : 0xFFFFFF;
	}

	private static Method findLegacyMethod(Class<?> type, String name, Class<?>... parameters) {
		for(Class<?> current = type; current != null && current != Block.class && current != Item.class; current = current.getSuperclass()) {
			try {
				Method method = current.getDeclaredMethod(name, parameters);
				method.setAccessible(true);
				return method;
			} catch(NoSuchMethodException ignored) { }
		}
		return null;
	}

	private static Object invoke(Method method, Object target, Object... arguments) {
		try {
			return method.invoke(target, arguments);
		} catch(IllegalAccessException e) {
			throw new IllegalStateException("Unable to access legacy color method " + method, e);
		} catch(InvocationTargetException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw new IllegalStateException("Legacy color method failed: " + method, cause);
		}
	}

	private static boolean isEligibleCube(BlockBase block) {
		if(block instanceof BlockChargeDynamite || block instanceof BlockChargeC4) return false;
		// Contextual icons, connected textures, multi-pass, side-rotated, rail, and
		// explicit ISBRH blocks remain on legacy rendering. Nonzero results from
		// the old zero-argument int method catch shared IDs and TESR-only (-1).
		if(block instanceof IBlockCT || hasContextualLegacyIcon(block) ||
				block instanceof IBlockMultiPass || block instanceof IBlockSideRotation ||
				block instanceof IRenderBlock || block instanceof ISBRHUniversal ||
				hasCustomLegacyRenderType(block)) return false;
		for(IBlockState state : block.getBlockState().getValidStates()) {
			if(block.getRenderType(state) != EnumBlockRenderType.MODEL || !block.isFullCube(state)) return false;
		}
		return true;
	}

	private static boolean hasContextualLegacyIcon(BlockBase block) {
		for(Class<?> current = block.getClass(); current != null &&
				current != BlockBase.class && current != Block.class; current = current.getSuperclass()) {
			for(Method method : current.getDeclaredMethods()) {
				if(!"getIcon".equals(method.getName())) continue;
				Class<?>[] parameters = method.getParameterTypes();
				if(parameters.length == 2 && parameters[0] == int.class && parameters[1] == int.class) continue;
				return true;
			}
		}
		return false;
	}

	private static boolean hasCustomLegacyRenderType(BlockBase block) {
		for(Class<?> current = block.getClass(); current != null && current != Block.class; current = current.getSuperclass()) {
			try {
				Method method = current.getDeclaredMethod("getRenderType");
				if(method.getReturnType() != int.class) continue;
				Object renderType = invoke(method, block);
				// Legacy ID 0 was the ordinary full-cube path. Every other numeric
				// value selects a special vanilla renderer, an ISBRH, or a TESR.
				return !(renderType instanceof Integer) || ((Integer) renderType).intValue() != 0;
			} catch(NoSuchMethodException ignored) {
			} catch(IllegalStateException ignored) {
				return true;
			}
		}
		return false;
	}

	private static boolean isItemVariant(ModelResourceLocation location) {
		String variant = location.getVariant();
		return variant != null && variant.startsWith("inventory");
	}

	private static boolean isNTM(ResourceLocation location) {
		return location != null && RefStrings.MODID.equals(location.getNamespace());
	}

	private static final class ItemModelInfo {
		private final Item item;
		private final Block block;
		private final int metadata;
		private final boolean cube;

		private ItemModelInfo(Item item, Block block, int metadata, boolean cube) {
			this.item = item;
			this.block = block;
			this.metadata = metadata;
			this.cube = cube;
		}
	}
}
