package com.hbm.handler.imc;

import com.hbm.handler.jei.MachineRecipeCategory;
import mezz.jei.api.IModRegistry;
import net.minecraft.item.ItemStack;

/** Registers the original machine/catalyst mappings through the supported 1.12 recipe API. */
public final class IMCHandlerNHNEI {
    private IMCHandlerNHNEI() { }
    public static void registerCatalysts(IModRegistry registry, Iterable<MachineRecipeCategory> categories) {
        for(MachineRecipeCategory category : categories)
            for(ItemStack machine : category.getMachines())
                if(machine != null && !machine.isEmpty()) registry.addRecipeCatalyst(machine, category.getUid());
    }
}
