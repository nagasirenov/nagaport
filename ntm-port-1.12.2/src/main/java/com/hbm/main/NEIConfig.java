package com.hbm.main;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.hbm.handler.RecipeBrowserVisibility;
import com.hbm.lib.RefStrings;

/** Optional NEI item visibility adapter; native recipes are registered with JEI. */
public class NEIConfig implements IConfigureNEI {
    @Override public void loadConfig() { RecipeBrowserVisibility.configureHiddenItems(API::hideItem); }
    @Override public String getName() { return "Nuclear Tech NEI Plugin"; }
    @Override public String getVersion() { return RefStrings.VERSION; }
}
