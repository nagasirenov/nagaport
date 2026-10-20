package com.hbm.handler.jei;

import java.util.ArrayList;
import java.util.List;
import com.hbm.handler.nei.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

/** Native recipe integration. The lookup plugin preserves NTM-specific focus predicates. */
@JEIPlugin
public final class NTMJeiPlugin implements IModPlugin {
    private final List<MachineRecipeCategory> categories = new ArrayList<>();

    @Override public void registerCategories(IRecipeCategoryRegistration registry) {
        categories.clear();
        categories.addAll(com.hbm.main.NEIRegistry.listAllHandlers());
        for(com.hbm.config.CustomMachineConfigJSON.MachineConfiguration configuration : com.hbm.config.CustomMachineConfigJSON.niceList)
            categories.add(new CustomMachineHandler(configuration));
        for(MachineRecipeCategory category : categories) {
            category.initialize(registry.getJeiHelpers().getGuiHelper());
            registry.addRecipeCategories(category);
        }
    }
    @Override public void register(IModRegistry registry) {
        registry.addRecipeRegistryPlugin(new NativeRecipeLookup(categories, registry.getIngredientRegistry()));
        com.hbm.handler.imc.IMCHandlerNHNEI.registerCatalysts(registry, categories);
        com.hbm.handler.RecipeBrowserVisibility.configureHiddenItems(stack -> registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(stack));
        for(MachineRecipeCategory category : categories) {
            category.registerGuiAreas(registry);
            if(category instanceof AnvilRecipeHandler) {
                AnvilOverlayHandler selection = new AnvilOverlayHandler((AnvilRecipeHandler) category, registry.getJeiHelpers().recipeTransferHandlerHelper());
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(selection);
                registry.getRecipeTransferRegistry().addRecipeTransferHandler(selection, category.getUid());
            }
        }
    }
    @Override public void onRuntimeAvailable(IJeiRuntime runtime) { MachineRecipeCategory.setRuntime(runtime); }
}
