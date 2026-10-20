package com.hbm.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Adds the original NTM chest entries to the target version's vanilla pools. */
public final class VanillaLootHandler {

    private static final class Addition {
        final Item item;
        final int weight;
        Addition(Item item, int weight) {
            this.item = item;
            this.weight = weight;
        }
    }

    private final Map<ResourceLocation, List<Addition>> entries = new HashMap<>();

    public void addItem(ResourceLocation table, Item item, int weight) {
        // Called in preInit, before deferred RegistryEvent item registration.
        // Registry names are only resolved when the table loads after startup.
        entries.computeIfAbsent(table, key -> new ArrayList<>()).add(new Addition(item, weight));
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        List<Addition> additions = entries.get(event.getName());
        if (additions == null) return;
        LootPool pool = targetPool(event);
        for (Addition addition : additions) {
            // Every pristine entry has min=max=1, metadata=0 and no NBT.
            String name = addition.item.getRegistryName().toString();
            pool.addEntry(new LootEntryItem(addition.item, addition.weight, 0, new LootFunction[0], new LootCondition[0], name));
        }
    }

    private static LootPool targetPool(LootTableLoadEvent event) {
        // Forge names the first unnamed vanilla pool "main". Retain its rolls
        // and other pools. Bonus chest's first two pools guarantee tools; its
        // fourth pool contains the resources with which NTM loot should compete.
        String name = LootTableList.CHESTS_SPAWN_BONUS_CHEST.equals(event.getName()) ? "pool3" : "main";
        LootPool pool = event.getTable().getPool(name);
        if (pool == null) throw new IllegalStateException("Missing loot pool " + name + " in " + event.getName());
        return pool;
    }
}
