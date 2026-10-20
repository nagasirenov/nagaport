package com.hbm.crafting.nativeapi;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Builds recipe/ore tables at Forge's recipe event, after all Item registry events. */
public final class NtmCraftingTables {
    private static final List<Runnable> TABLES = new ArrayList<>();
    private static boolean subscribed;
    private static boolean started;
    private NtmCraftingTables() { }

    public static void schedule(Runnable table) {
        if (started) throw new IllegalStateException("NTM initial recipe tables already started");
        if (!subscribed) {
            MinecraftForge.EVENT_BUS.register(new NtmCraftingTables());
            subscribed = true;
        }
        TABLES.add(table);
    }

    @SubscribeEvent public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        if (started) throw new IllegalStateException("NTM initial recipe tables cannot be registered twice");
        started = true;
        for (Runnable table : TABLES) table.run();
        TABLES.clear();
    }
}
