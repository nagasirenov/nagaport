package com.hbm.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hbm.main.MainRegistry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/**
 * Persistent registry references for custom NBT.
 *
 * <p>1.7.10's numeric block/item ids were assigned at runtime and are not a stable
 * identity across sessions or mod sets. This helper writes the registry name and
 * only reads the registry name. A numeric-only tag is treated as a legacy,
 * unresolved identity: the raw tag is never interpreted through the current
 * runtime registry and callers are expected to preserve it with
 * {@link #preserveLegacy(NBTTagCompound, NBTTagCompound, String)}.</p>
 */
public final class PersistentRegistryRef {

	public static final int SCHEMA_VERSION = 2;
	public static final String SCHEMA_KEY = "hbmRefSchema";
	public static final String NAME_SUFFIX = "_name";

	private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

	private PersistentRegistryRef() {
	}

	public static String nameKey(String key) {
		return key + NAME_SUFFIX;
	}

	public static boolean hasName(NBTTagCompound nbt, String key) {
		return nbt != null && nbt.hasKey(nameKey(key), 8);
	}

	public static boolean hasLegacyNumeric(NBTTagCompound nbt, String key) {
		return nbt != null && !hasName(nbt, key) && nbt.hasKey(key);
	}

	public static boolean writeItem(NBTTagCompound nbt, String key, Item item) {
		if(nbt == null || item == null) return false;

		ResourceLocation name = item.getRegistryName();
		if(name == null) {
			warn("item", key, "item has no registry name");
			return false;
		}

		nbt.setString(nameKey(key), name.toString());
		nbt.setByte(SCHEMA_KEY, (byte) SCHEMA_VERSION);
		return true;
	}

	public static Item readItem(NBTTagCompound nbt, String key) {
		if(nbt == null) return null;

		if(nbt.hasKey(nameKey(key), 8)) {
			return readItemName(nbt, nameKey(key));
		}

		if(nbt.hasKey(key)) {
			warn("item", key, "legacy numeric id " + nbt.getInteger(key) + " is unresolved; raw tag preserved");
		}
		return null;
	}

	/** Reads an item from an already-named string key (used by legacy schemas such as "name"). */
	public static Item readItemName(NBTTagCompound nbt, String nameKey) {
		if(nbt == null || !nbt.hasKey(nameKey, 8)) return null;

		String name = nbt.getString(nameKey);
		Item item = Item.getByNameOrId(name);
		if(item == null) warn("item", nameKey, "registry name '" + name + "' is not registered");
		return item;
	}

	public static boolean writeBlock(NBTTagCompound nbt, String key, Block block) {
		if(nbt == null || block == null) return false;

		ResourceLocation name = block.getRegistryName();
		if(name == null) {
			warn("block", key, "block has no registry name");
			return false;
		}

		nbt.setString(nameKey(key), name.toString());
		nbt.setByte(SCHEMA_KEY, (byte) SCHEMA_VERSION);
		return true;
	}

	public static Block readBlock(NBTTagCompound nbt, String key) {
		if(nbt == null) return null;

		if(nbt.hasKey(nameKey(key), 8)) {
			String name = nbt.getString(nameKey(key));
			Block block = Block.getBlockFromName(name);
			if(block == null) warn("block", key, "registry name '" + name + "' is not registered");
			return block;
		}

		if(nbt.hasKey(key)) {
			warn("block", key, "legacy numeric id " + nbt.getInteger(key) + " is unresolved; raw tag preserved");
		}
		return null;
	}

	/**
	 * Copies only the given legacy keys from the tag that was read into the tag
	 * being written when no registry name was written for them. This keeps old
	 * saves byte-preserving instead of silently dropping or remapping ids.
	 */
	public static void preserveLegacy(NBTTagCompound source, NBTTagCompound target, String... keys) {
		if(source == null || target == null || keys == null) return;

		for(String key : keys) {
			if(key == null || target.hasKey(nameKey(key), 8)) continue;
			if(source.hasKey(key)) target.setTag(key, source.getTag(key).copy());
		}
	}

	public static NBTTagCompound legacyOnly(NBTTagCompound source, String... keys) {
		if(source == null || keys == null) return null;

		NBTTagCompound out = new NBTTagCompound();
		for(String key : keys) {
			if(key != null && source.hasKey(key) && !source.hasKey(nameKey(key), 8)) {
				out.setTag(key, source.getTag(key).copy());
			}
		}
		return out.getSize() == 0 ? null : out;
	}

	private static void warn(String type, String key, String detail) {
		String message = "Persistent " + type + " reference '" + key + "': " + detail;
		if(WARNED.add(message)) {
			MainRegistry.logger.warn(message);
		}
	}
}
