package com.hbm.handler.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

/** A native JEI wrapper with logical ingredients separate from visual decorations. */
public class MachineRecipeView implements IRecipeWrapper {

    public final Object source;

    /** Defensive snapshots for machine-specific rendering, not the stored ingredients. */
    public final ItemStack[][] inputs;
    public final ItemStack[][] outputs;

    private final MachineRecipeCategory category;
    private final List<List<ItemStack>> inputIngredients;
    private final List<List<ItemStack>> outputIngredients;
    private final List<Slot> slots = new ArrayList<>();
    private final List<IGuiIngredient<ItemStack>> displayedInputs;
    private final List<IGuiIngredient<ItemStack>> displayedOutputs;

    public MachineRecipeView(MachineRecipeCategory category, Object source, ItemStack[][] inputs, ItemStack[][] outputs) {
        this.category = Objects.requireNonNull(category, "category");
        this.source = source;
        this.inputIngredients = snapshotIngredients(inputs);
        this.outputIngredients = snapshotIngredients(outputs);
        this.inputs = copyMatrix(inputIngredients);
        this.outputs = copyMatrix(outputIngredients);
        this.displayedInputs = new ArrayList<>(Collections.nCopies(inputIngredients.size(), null));
        this.displayedOutputs = new ArrayList<>(Collections.nCopies(outputIngredients.size(), null));
    }

    public MachineRecipeCategory getCategory() {
        return category;
    }

    /** Coordinates describe the visible 16x16 item, excluding JEI's one-pixel padding. */
    public MachineRecipeView addInputSlot(int index, int x, int y) {
        checkIndex(index, inputIngredients.size(), "input");
        slots.add(new Slot(SlotKind.INPUT, index, x, y, Collections.emptyList()));
        return this;
    }

    /** Coordinates describe the visible 16x16 item, excluding JEI's one-pixel padding. */
    public MachineRecipeView addOutputSlot(int index, int x, int y) {
        checkIndex(index, outputIngredients.size(), "output");
        slots.add(new Slot(SlotKind.OUTPUT, index, x, y, Collections.emptyList()));
        return this;
    }

    /**
     * Add a rotating, clickable item display without declaring a recipe input or
     * output. JEI handles its rendering, tooltip, focus and ingredient shortcuts.
     */
    public MachineRecipeView addDecoration(ItemStack[] variants, int x, int y) {
        slots.add(new Slot(SlotKind.DECORATION, -1, x, y, snapshotAlternatives(variants)));
        return this;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, copyIngredients(inputIngredients));
        ingredients.setOutputLists(VanillaTypes.ITEM, copyIngredients(outputIngredients));
    }

    void initializeSlots(IGuiItemStackGroup itemStacks) {
        Collections.fill(displayedInputs, null);
        Collections.fill(displayedOutputs, null);
        for (int guiIndex = 0; guiIndex < slots.size(); guiIndex++) {
            Slot slot = slots.get(guiIndex);
            boolean input = slot.kind == SlotKind.INPUT;
            itemStacks.init(guiIndex, input, slot.x - 1, slot.y - 1);
            List<ItemStack> variants;
            if (input) {
                variants = inputIngredients.get(slot.index);
            } else if (slot.kind == SlotKind.OUTPUT) {
                variants = outputIngredients.get(slot.index);
            } else {
                // A GUI-only slot, never passed to IIngredients or default recipe indexing.
                variants = slot.decoration;
            }
            itemStacks.set(guiIndex, copyAlternatives(variants));
            IGuiIngredient<ItemStack> ingredient = itemStacks.getGuiIngredients().get(guiIndex);
            if (input) {
                displayedInputs.set(slot.index, ingredient);
            } else if (slot.kind == SlotKind.OUTPUT) {
                displayedOutputs.set(slot.index, ingredient);
            }
        }
    }

    /** Return a copy of the variant JEI is currently displaying, including focus lock. */
    public ItemStack displayedInput(int index) {
        return displayedIngredient(index, displayedInputs, inputIngredients);
    }

    /** Return a copy of the variant JEI is currently displaying, including focus lock. */
    public ItemStack displayedOutput(int index) {
        return displayedIngredient(index, displayedOutputs, outputIngredients);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        category.drawRecipe(this, minecraft, mouseX, mouseY);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return category.getRecipeTooltips(this, mouseX, mouseY);
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        return category.handleClick(this, minecraft, mouseX, mouseY, mouseButton);
    }

    private static ItemStack displayedIngredient(int index, List<IGuiIngredient<ItemStack>> displayed,
                                                 List<List<ItemStack>> ingredients) {
        checkIndex(index, ingredients.size(), "ingredient");
        IGuiIngredient<ItemStack> guiIngredient = displayed.get(index);
        if (guiIngredient != null) {
            ItemStack ingredient = guiIngredient.getDisplayedIngredient();
            return ingredient == null || ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.copy();
        }
        List<ItemStack> variants = ingredients.get(index);
        return variants.isEmpty() ? ItemStack.EMPTY : variants.get(0).copy();
    }

    private static void checkIndex(int index, int size, String kind) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Invalid " + kind + " slot " + index + " of " + size);
        }
    }

    private static List<List<ItemStack>> snapshotIngredients(@Nullable ItemStack[][] ingredients) {
        if (ingredients == null || ingredients.length == 0) {
            return Collections.emptyList();
        }
        List<List<ItemStack>> result = new ArrayList<>(ingredients.length);
        for (ItemStack[] alternatives : ingredients) {
            result.add(snapshotAlternatives(alternatives));
        }
        return Collections.unmodifiableList(result);
    }

    private static List<ItemStack> snapshotAlternatives(@Nullable ItemStack[] ingredients) {
        if (ingredients == null || ingredients.length == 0) {
            return Collections.emptyList();
        }
        List<ItemStack> result = new ArrayList<>(ingredients.length);
        for (ItemStack stack : ingredients) {
            if (stack != null && !stack.isEmpty()) {
                result.add(stack.copy());
            }
        }
        return Collections.unmodifiableList(result);
    }

    private static List<ItemStack> copyAlternatives(List<ItemStack> ingredients) {
        List<ItemStack> result = new ArrayList<>(ingredients.size());
        for (ItemStack stack : ingredients) {
            result.add(stack.copy());
        }
        return Collections.unmodifiableList(result);
    }

    private static List<List<ItemStack>> copyIngredients(List<List<ItemStack>> ingredients) {
        List<List<ItemStack>> result = new ArrayList<>(ingredients.size());
        for (List<ItemStack> alternatives : ingredients) {
            result.add(copyAlternatives(alternatives));
        }
        return Collections.unmodifiableList(result);
    }

    private static ItemStack[][] copyMatrix(List<List<ItemStack>> ingredients) {
        ItemStack[][] result = new ItemStack[ingredients.size()][];
        for (int index = 0; index < ingredients.size(); index++) {
            result[index] = copyAlternatives(ingredients.get(index)).toArray(new ItemStack[0]);
        }
        return result;
    }

    private enum SlotKind {
        INPUT, OUTPUT, DECORATION
    }

    private static final class Slot {
        private final SlotKind kind;
        private final int index;
        private final int x;
        private final int y;
        private final List<ItemStack> decoration;

        private Slot(SlotKind kind, int index, int x, int y, List<ItemStack> decoration) {
            this.kind = kind;
            this.index = index;
            this.x = x;
            this.y = y;
            this.decoration = decoration;
        }
    }
}
