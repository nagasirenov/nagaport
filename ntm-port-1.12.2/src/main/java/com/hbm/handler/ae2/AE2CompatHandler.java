package com.hbm.handler.ae2;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

/** Keep the ordinary mod entry point free of AE2-typed fields and interfaces. */
public class AE2CompatHandler {
    public static void init() {
        if (Loader.isModLoaded("appliedenergistics2")) {
            registerHandler();
        }
    }

    @Optional.Method(modid = "appliedenergistics2")
    private static void registerHandler() {
        MinecraftForge.EVENT_BUS.register(new AE2StorageCapability.AttachHandler());
    }
}
