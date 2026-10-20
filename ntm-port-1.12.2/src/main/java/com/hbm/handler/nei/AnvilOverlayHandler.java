package com.hbm.handler.nei;

import com.hbm.inventory.container.ContainerAnvil;
import com.hbm.inventory.gui.GUIAnvil;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilConstructionRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Native JEI transfer action selects an anvil recipe. It never crafts or moves items. */
public final class AnvilOverlayHandler implements IRecipeTransferHandler<ContainerAnvil> {
    private final AnvilRecipeHandler category;
    private final IRecipeTransferHandlerHelper helper;
    private GUIAnvil target;

    public AnvilOverlayHandler(AnvilRecipeHandler category, IRecipeTransferHandlerHelper helper) {
        this.category = category;
        this.helper = helper;
    }

    @SubscribeEvent
    public void onGuiOpened(GuiOpenEvent event) {
        if(event.getGui() instanceof GUIAnvil) target = (GUIAnvil) event.getGui();
        else if(event.getGui() == null) target = null;
    }

    @Override public Class<ContainerAnvil> getContainerClass() { return ContainerAnvil.class; }

    @Override public IRecipeTransferError transferRecipe(ContainerAnvil container, IRecipeLayout layout,
            EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        AnvilConstructionRecipe recipe = category.getConstructionRecipe(layout);
        if(target == null || target.inventorySlots != container || recipe == null) return helper.createInternalError();
        if(doTransfer) target.focusRecipe(recipe);
        return null;
    }
}
