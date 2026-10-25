package com.hbm.render.icon;

import java.util.IdentityHashMap;
import java.util.Map;

import com.hbm.blocks.LegacyBlockTextures;
import com.hbm.blocks.PlainCubeTextures;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Client-side legacy icon view of explicit compatibility texture bindings. */
@SideOnly(Side.CLIENT)
public final class PlainCubeLegacyIcons {

	private static final Map<Block, IIcon> ICONS = new IdentityHashMap<>();

	private PlainCubeLegacyIcons() { }

	public static void register(IIconRegister register) {
		ICONS.clear();
		registerBindings(register, PlainCubeTextures.bindings());
		registerBindings(register, LegacyBlockTextures.bindings());
	}

	private static void registerBindings(IIconRegister register, Map<Block, String> bindings) {
		for(Map.Entry<Block, String> entry : bindings.entrySet()) {
			Block block = entry.getKey();
			if(hasOwnIconCallback(block)) continue; // its registerBlockIcons owns the names
			ResourceLocation legacy = new ResourceLocation(entry.getValue());
			String path = legacy.getPath();
			if(!path.startsWith("blocks/")) path = "blocks/" + path;
			String atlasName = new ResourceLocation(legacy.getNamespace(), path).toString();
			if(block instanceof net.minecraft.block.BlockDoor) {
				// The original door blocks registered per-half icons from the base;
				// the plain base name has no file and is never requested on 1.7.
				IIcon lower = register.registerIcon(new ResourceLocation(legacy.getNamespace(), path + "_lower").toString());
				register.registerIcon(new ResourceLocation(legacy.getNamespace(), path + "_upper").toString());
				ICONS.put(block, lower);
				continue;
			}
			ICONS.put(block, register.registerIcon(atlasName));
		}
	}

	/** True when the block class itself declares the legacy icon callback; the
	 * framework default on BlockBase does not count as an owner. */
	private static boolean hasOwnIconCallback(Block block) {
		Class<?> type = block.getClass();
		while(type != null && type != Object.class) {
			if(type == com.hbm.blocks.BlockBase.class) return false;
			try {
				type.getDeclaredMethod("registerBlockIcons", IIconRegister.class);
				return true;
			} catch(NoSuchMethodException ignored) { }
			type = type.getSuperclass();
		}
		return false;
	}

	public static IIcon get(Block block) {
		return ICONS.get(block);
	}
}
