package com.hbm.registry;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Mod-local compatibility bridge for the removed 1.7.10 GameRegistry block/item APIs.
 *
 * <p>Registration calls keep their 1.7.10 call-site timing: names are captured and
 * ItemBlocks are constructed immediately, while the actual objects are queued for
 * Forge's 1.12.2 Block and Item registry events. The two event queues are drained
 * once, in Block-then-Item order, only after the preInit tables have completed.</p>
 *
 * <p>The legacy registry name rule is also retained. Forge 1.7.10 prefixed the
 * supplied name with the active mod id unless its existing namespace already
 * matched that mod id; it did not remove an unlocalized-name prefix such as
 * {@code tile.}. Consequently the 1.12.2 ResourceLocation path is the original
 * supplied name (including any dots), with the mod id used as its namespace.</p>
 */
public final class GameRegistry {

	private enum Phase {
		NEW,
		QUEUING,
		TABLES_COMPLETE,
		BLOCKS_FLUSHING,
		BLOCKS_FLUSHED,
		ITEMS_FLUSHING,
		ITEMS_FLUSHED
	}

	private static final List<BlockRegistration> BLOCKS = new ArrayList<BlockRegistration>();
	private static final List<ItemRegistration> ITEMS = new ArrayList<ItemRegistration>();
	private static final Set<ResourceLocation> BLOCK_NAMES = new HashSet<ResourceLocation>();
	private static final Set<ResourceLocation> ITEM_NAMES = new HashSet<ResourceLocation>();
	private static final EventHandler EVENT_HANDLER = new EventHandler();
	private static Phase phase = Phase.NEW;

	private GameRegistry() { }

	/** Begin collecting the legacy ModBlocks/ModItems preInit table registrations. */
	public static synchronized void beginRegistrationTables() {
		if (phase != Phase.NEW) {
			throw new IllegalStateException("Legacy block/item registration tables were already started");
		}
		MinecraftForge.EVENT_BUS.register(EVENT_HANDLER);
		phase = Phase.QUEUING;
	}

	/** Mark both preInit registration tables complete before Forge fires registry events. */
	public static synchronized void finishRegistrationTables() {
		if (phase != Phase.QUEUING) {
			throw new IllegalStateException("Legacy block/item registration tables cannot be completed in phase " + phase);
		}
		phase = Phase.TABLES_COMPLETE;
	}

	/** Legacy two-argument overload; it creates the standard ItemBlock. */
	public static Block registerBlock(Block block, String name) {
		return registerBlock(block, ItemBlock.class, name);
	}

	/** Legacy three-argument overload; the ItemBlock is constructed at this call site. */
	public static Block registerBlock(Block block, Class<? extends ItemBlock> itemClass, String name) {
		return registerBlock(block, itemClass, name, new Object[0]);
	}

	/**
	 * Legacy constructor-argument overload. As in Forge 1.7.10, the selected public
	 * constructor takes Block followed by the exact runtime classes of the extra args.
	 */
	public static Block registerBlock(Block block, Class<? extends ItemBlock> itemClass, String name, Object... itemCtorArgs) {
		if (block == null) {
			throw new IllegalArgumentException("registerBlock: block cannot be null");
		}

		ResourceLocation registryName = legacyRegistryName(name);
		ItemBlock itemBlock = itemClass == null ? null : constructItemBlock(block, itemClass, itemCtorArgs);

		synchronized (GameRegistry.class) {
			requireQueuing("registerBlock");
			addUniqueName(BLOCK_NAMES, registryName, "block");
			BLOCKS.add(new BlockRegistration(block, registryName));
			if (itemBlock != null) {
				addUniqueName(ITEM_NAMES, registryName, "ItemBlock/item");
				ITEMS.add(new ItemRegistration(itemBlock, registryName));
			}
		}
		return block;
	}

	/** Deprecated 1.7.10 compatibility overload; the legacy modId argument was ignored. */
	@Deprecated
	public static Block registerBlock(Block block, Class<? extends ItemBlock> itemClass, String name,
			String modId, Object... itemCtorArgs) {
		return registerBlock(block, itemClass, name, itemCtorArgs);
	}

	/** Legacy two-argument item registration overload. */
	public static void registerItem(Item item, String name) {
		registerItem(item, name, null);
	}

	/** Legacy overload; the 1.7.10 modId argument was deprecated and unused. */
	public static Item registerItem(Item item, String name, String modId) {
		if (item == null) {
			throw new IllegalArgumentException("registerItem: item cannot be null");
		}
		ResourceLocation registryName = legacyRegistryName(name);
		synchronized (GameRegistry.class) {
			requireQueuing("registerItem");
			addUniqueName(ITEM_NAMES, registryName, "item");
			ITEMS.add(new ItemRegistration(item, registryName));
		}
		return item;
	}

	private static ItemBlock constructItemBlock(Block block, Class<? extends ItemBlock> itemClass, Object[] itemCtorArgs) {
		if (itemCtorArgs == null) {
			throw new IllegalArgumentException("ItemBlock constructor arguments cannot be null");
		}
		try {
			Class<?>[] parameterTypes = new Class<?>[itemCtorArgs.length + 1];
			Object[] constructorArgs = new Object[itemCtorArgs.length + 1];
			parameterTypes[0] = Block.class;
			constructorArgs[0] = block;
			for (int i = 0; i < itemCtorArgs.length; i++) {
				Object argument = itemCtorArgs[i];
				parameterTypes[i + 1] = argument.getClass();
				constructorArgs[i + 1] = argument;
			}
			Constructor<? extends ItemBlock> constructor = itemClass.getConstructor(parameterTypes);
			return constructor.newInstance(constructorArgs);
		} catch (Exception exception) {
			throw new IllegalStateException("Could not construct ItemBlock " + itemClass.getName()
					+ " for block " + block, exception);
		}
	}

	private static ResourceLocation legacyRegistryName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Registry name cannot be null");
		}
		int separator = name.lastIndexOf(':');
		String namespace = separator < 0 ? "" : name.substring(0, separator);
		if (RefStrings.MODID.equals(namespace)) {
			return new ResourceLocation(name);
		}
		// Mirrors GameData.addPrefix: prefix the whole supplied name if its namespace differs.
		return new ResourceLocation(RefStrings.MODID, name);
	}

	private static void requireQueuing(String operation) {
		if (phase != Phase.QUEUING) {
			throw new IllegalStateException(operation + " called outside registration-table collection (phase " + phase + ")");
		}
	}

	private static void addUniqueName(Set<ResourceLocation> names, ResourceLocation name, String registry) {
		if (!names.add(name)) {
			throw new IllegalStateException("Duplicate legacy " + registry + " registry name: " + name);
		}
	}

	private static final class BlockRegistration {
		private final Block block;
		private final ResourceLocation name;

		private BlockRegistration(Block block, ResourceLocation name) {
			this.block = block;
			this.name = name;
		}
	}

	private static final class ItemRegistration {
		private final Item item;
		private final ResourceLocation name;

		private ItemRegistration(Item item, ResourceLocation name) {
			this.item = item;
			this.name = name;
		}
	}

	public static final class EventHandler {
		private EventHandler() { }

		@SubscribeEvent
		public void registerBlocks(RegistryEvent.Register<Block> event) {
			synchronized (GameRegistry.class) {
				if (phase != Phase.TABLES_COMPLETE) {
					throw new IllegalStateException("Block registry event fired before tables completed or more than once (phase " + phase + ")");
				}
				phase = Phase.BLOCKS_FLUSHING;
			}
			for (BlockRegistration registration : BLOCKS) {
				if (registration.block.getRegistryName() == null) {
					registration.block.setRegistryName(registration.name);
				} else if (!registration.name.equals(registration.block.getRegistryName())) {
					throw new IllegalStateException("Block " + registration.block + " already has registry name "
							+ registration.block.getRegistryName() + ", expected legacy name " + registration.name);
				}
				event.getRegistry().register(registration.block);
			}
			synchronized (GameRegistry.class) {
				phase = Phase.BLOCKS_FLUSHED;
			}
		}

		@SubscribeEvent
		public void registerItems(RegistryEvent.Register<Item> event) {
			synchronized (GameRegistry.class) {
				if (phase != Phase.BLOCKS_FLUSHED) {
					throw new IllegalStateException("Item registry event fired before the Block registry was flushed or more than once (phase " + phase + ")");
				}
				phase = Phase.ITEMS_FLUSHING;
			}
			for (ItemRegistration registration : ITEMS) {
				if (registration.item.getRegistryName() == null) {
					registration.item.setRegistryName(registration.name);
				} else if (!registration.name.equals(registration.item.getRegistryName())) {
					throw new IllegalStateException("Item " + registration.item + " already has registry name "
							+ registration.item.getRegistryName() + ", expected legacy name " + registration.name);
				}
				event.getRegistry().register(registration.item);
			}
			synchronized (GameRegistry.class) {
				phase = Phase.ITEMS_FLUSHED;
			}
		}
	}
}
