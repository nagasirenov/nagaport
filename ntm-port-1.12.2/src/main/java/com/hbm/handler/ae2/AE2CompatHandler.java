package com.hbm.handler.ae2;

import appeng.api.AEApi;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

public class AE2CompatHandler {
    public static void init() {
        if (Loader.isModLoaded("appliedenergistics2")) {
            registerHandler();
        }
    }

    @Optional.Method(modid = "appliedenergistics2")
    private static void registerHandler() {
        AEApi.instance().registries().externalStorage().addExternalStorageInterface(new MSUExternalStorageHandler());
		AEApi.instance().registries().externalStorage().addExternalStorageInterface(new AFLExternalStorageHandler());
    }
}
