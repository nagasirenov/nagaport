package com.hbm.handler.nei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hbm.handler.jei.RecipeStackMatcher;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.jei.MachineRecipeCategory;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsSatellite;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.lib.WeightedRandomChestContent;
import com.hbm.saveddata.satellites.SatelliteMiner;
import com.hbm.util.ItemStackUtil;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

/** Native JEI view of the original satellite cargo pools. */
public class SatelliteHandler extends MachineRecipeCategory {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            RefStrings.MODID + ":textures/gui/nei/gui_nei_anvil.png");

    @Override public String getUid() { return "ntmSatellite"; }
    @Override public String getTitle() { return "Satellite"; }
    @Override public ItemStack[] getMachines() {
        return new ItemStack[] {new ItemStack(ModBlocks.sat_dock)};
    }

    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if (focus != null && focus.getMode() == IFocus.Mode.INPUT) {
            // Usage lookup intentionally ignores metadata/NBT and uses the
            // explicit pool constants rather than the cargo lookup below.
            Item item = focus.getValue().getItem();
            if (item == ModItems.sat_miner) {
                views.add(createView(item, ItemPool.getPool(ItemPoolsSatellite.POOL_SAT_MINER)));
            } else if (item == ModItems.sat_lunar_miner) {
                views.add(createView(item, ItemPool.getPool(ItemPoolsSatellite.POOL_SAT_LUNAR)));
            }
            return views;
        }
        for (Item satelliteItem : new Item[] {ModItems.sat_miner, ModItems.sat_lunar_miner}) {
            String poolName = SatelliteMiner.getCargoForItem(satelliteItem);
            if (poolName == null) continue;
            WeightedRandomChestContent[] pool = ItemPool.getPool(poolName);
            if (focus == null) {
                views.add(createView(satelliteItem, pool));
            } else {
                for (WeightedRandomChestContent poolEntry : pool) {
                    if (RecipeStackMatcher.matchesCrafting(poolEntry.theItemId, focus.getValue())) {
                        views.add(createView(satelliteItem, pool));
                        break; // One recipe per satellite, even for repeated matching entries.
                    }
                }
            }
        }
        return views;
    }

    private MachineRecipeView createView(Item poolItem, WeightedRandomChestContent[] poolEntries) {
        List<ItemStack> outputs = new ArrayList<>();
        int weight = Arrays.stream(poolEntries).mapToInt(poolEntry -> poolEntry.itemWeight).sum();
        for (WeightedRandomChestContent poolEntry : poolEntries) {
            ItemStack stack = poolEntry.theItemId.copy();
            float chance = 100F * poolEntry.itemWeight / weight;
            ItemStackUtil.addTooltipToStack(stack, TextFormatting.RED + "" + ((int)(chance * 10F) / 10F) + "%");
            outputs.add(stack);
        }

        // Preserve eighteen rotating output positions and duplicate cargo entries.
        // JEI cycles each position's alternatives without the old null padding.
        ItemStack[][] slots = new ItemStack[Math.min(outputs.size(), 18)][];
        for (int i = 0; i < slots.length; i++) {
            List<ItemStack> alternatives = new ArrayList<>();
            for (int j = i; j < outputs.size(); j += 18) alternatives.add(outputs.get(j));
            slots[i] = alternatives.toArray(new ItemStack[0]);
        }
        MachineRecipeView view = new MachineRecipeView(this, poolItem,
                new ItemStack[][] {{new ItemStack(poolItem)}}, slots);
        view.addInputSlot(0, 12, 24);
        for (int i = 0; i < slots.length; i++) view.addOutputSlot(i, 48 + 18 * (i % 6), 6 + 18 * (i / 6));
        view.addDecoration(new ItemStack[] {new ItemStack(ModBlocks.sat_dock)}, 30, 31);
        return view;
    }

    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        minecraft.getTextureManager().bindTexture(TEXTURE);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 5, 11, 166, 65, 256, 256);
        Gui.drawModalRectWithCustomSizedTexture(11, 23, 113, 105, 18, 18, 256, 256);
        Gui.drawModalRectWithCustomSizedTexture(47, 5, 5, 87, 108, 54, 256, 256);
        Gui.drawModalRectWithCustomSizedTexture(29, 14, 131, 96, 18, 36, 256, 256);
    }

    @Override public boolean isNavigationArea(int x, int y) {
        // No original transfer rectangles or GUI areas. Item navigation,
        // including the dock decoration, belongs to JEI's native item slots.
        return false;
    }
}
