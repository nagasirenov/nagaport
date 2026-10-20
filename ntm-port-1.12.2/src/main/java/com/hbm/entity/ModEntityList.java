package com.hbm.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraft.world.World;

public class ModEntityList {

	private static EntityData[] array = new EntityData[0];
	private static final Map<Integer, Class<? extends Entity>> map = new HashMap<Integer, Class<? extends Entity>>();


	public static void registerEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod) {
		registerEntity(entityClass, entityName, id, mod, 80, 3, true, -1, -1, false);
	}

	public static void registerEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int eggColor1, int eggColor2) {
		registerEntity(entityClass, entityName, id, mod, 80, 3, true, eggColor1, eggColor2, true);
	}

	public static void registerEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
		registerEntity(entityClass, entityName, id, mod, trackingRange, updateFrequency, sendsVelocityUpdates, -1, -1, false);
	}

	public static void registerEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, int eggColor1, int eggColor2) {
		registerEntity(entityClass, entityName, id, mod, trackingRange, updateFrequency, sendsVelocityUpdates, eggColor1, eggColor2, true);
	}

	private static void registerEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, int eggColor1, int eggColor2, boolean hasEgg) {
        if(id < 0 || getData(id) != null)
            throw new IllegalArgumentException("ID " + id + " is already being used or invalid!");
        ResourceLocation key = registryKey(entityClass, entityName);
        if(map.containsValue(entityClass) || ForgeRegistries.ENTITIES.containsKey(key))
            throw new IllegalArgumentException("Entity class or registry key already registered: " + key);
        // Native Forge stores this display/translation name verbatim; retain the
        // original entity.hbm.<name>.name localization keys.
        // Egg presence historically depends on color1, independently of the creation gate.
        if(eggColor1 != -1)
            EntityRegistry.registerModEntity(key, entityClass, RefStrings.MODID + "." + entityName, id, mod, trackingRange,
                    updateFrequency, sendsVelocityUpdates, eggColor1, eggColor2);
        else
            EntityRegistry.registerModEntity(key, entityClass, RefStrings.MODID + "." + entityName, id, mod, trackingRange,
                    updateFrequency, sendsVelocityUpdates);

        if(!key.equals(EntityList.getKey(entityClass))
                || ForgeRegistries.ENTITIES.getValue(key).getEntityClass() != entityClass)
            throw new IllegalStateException("Native entity registration failed: " + key);

		if(id >= array.length) {
			EntityData[] newArray = new EntityData[id + 5];
			System.arraycopy(array, 0, newArray, 0, array.length);
			array = newArray;
		}


		array[id] = new EntityData(entityName, key, id, eggColor1, eggColor2, hasEgg);
		map.put(id, entityClass);

	}

	public static String getName(int id) {
		EntityData data = getData(id);
		if(data == null)
			return null;

		return RefStrings.MODID + "." + data.name;
	}

	public static EntityData getData(int id) {
		if(id < 0 || id >= array.length)
			return null;

		return array[id];
	}

	public static boolean hasEntitiesWithEggs() {
		for(EntityData data : array) {
			if(data != null && data.hasEgg) return true;
		}

		return false;
	}

	public static Entity createEntityByID(int id, World world) {
		EntityData data = getData(id);

		if(data == null || !data.hasEgg)
			return null;

        return EntityList.createEntityByIDFromName(data.registryName, world);
	}

	public static EntityData[] getDatasWithEggs() {
		List<EntityData> list = new LinkedList<EntityData>();
		for(Integer id : map.keySet()) {
			EntityData data = getData(id);
			if(data != null && data.hasEgg)
				list.add(data);
		}
		return list.toArray(new EntityData[list.size()]);
	}

    // Two pairs historically share display names. Keep those names and local
    // IDs, but give each class its own native persistence/network identity.
    static ResourceLocation registryKey(Class<? extends Entity> entityClass, String name) {
        if("com.hbm.entity.cart.EntityMinecartDestroyer".equals(entityClass.getName())
                && "entity_ntm_cart_crate".equals(name)) name = "entity_ntm_cart_destroyer";
        if("com.hbm.entity.effect.EntityCloudSolinium".equals(entityClass.getName())
                && "entity_cloud_rainbow".equals(name)) name = "entity_cloud_solinium";
        return new ResourceLocation(RefStrings.MODID, name);
    }

    public static void registerEntityEgg(Class<? extends Entity> entity, int primaryColor, int secondaryColor) {
        ResourceLocation key = EntityList.getKey(entity);
        if(key == null) throw new IllegalArgumentException("Entity must be registered before its egg: " + entity);
        EntityRegistry.registerEgg(key, primaryColor, secondaryColor);
    }

    public static ItemStack getEggFromEntity(Entity entity) {
        ResourceLocation key = entity == null ? null : EntityList.getKey(entity);
        if(key == null || !EntityList.ENTITY_EGGS.containsKey(key)) return ItemStack.EMPTY;
        ItemStack egg = new ItemStack(Items.SPAWN_EGG);
        ItemMonsterPlacer.applyEntityIdToItemStack(egg, key);
        return egg;
    }

	public static class EntityData {

		public final String name;
        public final ResourceLocation registryName;
		public final int id, eggColor1, eggColor2;
		public final boolean hasEgg;

		EntityData(String name, ResourceLocation registryName, int id, int eggColor1, int eggColor2, boolean hasEgg) {
			this.name = name;
            this.registryName = registryName;
			this.id = id;
			this.eggColor1 = eggColor1;
			this.eggColor2 = eggColor2;
			this.hasEgg = hasEgg;
		}
	}

}