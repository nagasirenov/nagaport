package com.hbm.main;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.hbm.lib.RefStrings;
import com.hbm.render.icon.LegacyIconRegister;
import com.hbm.render.icon.IIconRegister;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/** Invokes original block/item icon callbacks during the 1.12 atlas pre-stitch. */
@SideOnly(Side.CLIENT)
public final class LegacyIconRegistrationHandler {

	private final Map<Class<?>, Method> blockCallbacks = new HashMap<>();
	private final Map<Class<?>, Method> itemCallbacks = new HashMap<>();

	@SubscribeEvent
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		TextureMap map = event.getMap();
		LegacyIconRegister register = new LegacyIconRegister(map);
		int blockCount = registerBlocks(register);
		int itemCount = registerItems(register);
		MainRegistry.logger.debug("Invoked legacy icon callbacks for {} NTM blocks and {} NTM items", blockCount, itemCount);
	}

	private int registerBlocks(LegacyIconRegister register) {
		int callbacks = 0;
		for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
			if(!isNTM(block.getRegistryName())) continue;
			Method callback = findCallback(blockCallbacks, block.getClass(), "registerBlockIcons");
			if(callback != null) {
				invoke(callback, block, register);
				callbacks++;
			}
		}
		return callbacks;
	}

	private int registerItems(LegacyIconRegister register) {
		int callbacks = 0;
		for(Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
			if(!isNTM(item.getRegistryName())) continue;
			Method callback = findCallback(itemCallbacks, item.getClass(), "registerIcons");
			if(callback != null) {
				invoke(callback, item, register);
				callbacks++;
			}
		}
		return callbacks;
	}

	private static boolean isNTM(ResourceLocation registryName) {
		return registryName != null && RefStrings.MODID.equals(registryName.getNamespace());
	}

	private static Method findCallback(Map<Class<?>, Method> cache, Class<?> type, String methodName) {
		if(cache.containsKey(type)) return cache.get(type);
		Method callback;
		try {
			callback = type.getMethod(methodName, IIconRegister.class);
		} catch(NoSuchMethodException e) {
			callback = null;
		}
		cache.put(type, callback);
		return callback;
	}

	private static void invoke(Method callback, Object target, LegacyIconRegister register) {
		try {
			callback.invoke(target, register);
		} catch(IllegalAccessException e) {
			throw new IllegalStateException("Unable to access legacy icon callback " + callback, e);
		} catch(InvocationTargetException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw new IllegalStateException("Legacy icon callback failed: " + callback, cause);
		}
	}
}
