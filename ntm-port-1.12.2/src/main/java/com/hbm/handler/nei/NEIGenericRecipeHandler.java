package com.hbm.handler.nei;

import java.util.ArrayList;
import java.util.List;

import com.hbm.config.ClientConfig;
import com.hbm.handler.jei.MachineRecipeCategory;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.interfaces.NotableComments;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipes.IOutput;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.lib.RefStrings;

import com.hbm.handler.jei.RecipeStackMatcher;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/** Native JEI categories for the ordered, configurable generic recipe sets. */
@NotableComments
public abstract class NEIGenericRecipeHandler extends MachineRecipeCategory {

    private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID, "textures/gui/nei/gui_nei.png");

    public final String displayName;
    public final GenericRecipes<?> recipeSet;
    public final ItemStack[] machines;

    public NEIGenericRecipeHandler(String displayName, GenericRecipes<?> recipeSet, Block... machines) {
        ItemStack[] machineStacks = new ItemStack[machines.length];
        for (int i = 0; i < machines.length; i++) machineStacks[i] = new ItemStack(machines[i]);
        this.displayName = displayName;
        this.recipeSet = recipeSet;
        this.machines = machineStacks;
    }

    public NEIGenericRecipeHandler(String displayName, GenericRecipes<?> recipeSet, ItemStack... machines) {
        this.displayName = displayName;
        this.recipeSet = recipeSet;
        this.machines = machines;
    }

    @Override public String getTitle() { return this.displayName; }
    @Override public ItemStack[] getMachines() { return this.machines; }

    public int getInputXOffset(GenericRecipe recipe, int inputCount) { return 0; }
    public int getOutputXOffset(GenericRecipe recipe, int outputCount) { return 0; }
    public int getMachineXOffset(GenericRecipe recipe) { return 0; }
    public ItemStack[] getMachines(GenericRecipe recipe) { return this.machines; }

    /** Resolve every query against the current ordered set and current secret-visibility setting. */
    @Override
    public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> result = new ArrayList<>();
        boolean hideSecrets = ClientConfig.NEI_HIDE_SECRETS.get();
        for (GenericRecipe recipe : this.recipeSet.recipeOrderedList) {
            if (hideSecrets && isSecret(recipe)) continue;
            if (focus == null || matchesFocus(recipe, focus)) result.add(createView(recipe));
        }
        return result;
    }

    private boolean isSecret(GenericRecipe recipe) {
        if (recipe.isPooled()) {
            for (String pool : recipe.getPools()) {
                if (pool.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) return true;
            }
        }
        if (recipe.inputItem != null) {
            for (AStack input : recipe.inputItem) {
                for (ItemStack stack : input.extractForNEI()) {
                    if (ModItems.excludeNEI.contains(stack.getItem())) return true;
                }
            }
        }
        if (recipe.outputItem != null) {
            for (IOutput output : recipe.outputItem) {
                for (ItemStack stack : output.getAllPossibilities()) {
                    if (ModItems.excludeNEI.contains(stack.getItem())) return true;
                }
            }
        }
        return false;
    }

    private boolean matchesFocus(GenericRecipe recipe, IFocus<ItemStack> focus) {
        ItemStack ingredient = focus.getValue();
        if (ingredient == null || ingredient.isEmpty()) return false;
        if (focus.getMode() == IFocus.Mode.OUTPUT) {
            if (recipe.outputItem != null) {
                for (IOutput output : recipe.outputItem) {
                    for (ItemStack stack : output.getAllPossibilities()) {
                        if (RecipeStackMatcher.matchesCrafting(stack, ingredient)) return true;
                    }
                }
            }
            if (recipe.outputFluid != null) {
                for (FluidStack fluid : recipe.outputFluid) {
                    if (areItemsAndMetaEqual(ItemFluidIcon.make(fluid), ingredient)) return true;
                }
            }
        } else {
            if (recipe.inputItem != null) {
                for (AStack input : recipe.inputItem) {
                    for (ItemStack stack : input.extractForNEI()) {
                        if (RecipeStackMatcher.matchesCrafting(stack, ingredient)) return true;
                    }
                }
            }
            if (recipe.inputFluid != null) {
                for (FluidStack fluid : recipe.inputFluid) {
                    if (areItemsAndMetaEqual(ItemFluidIcon.make(fluid), ingredient)) return true;
                }
            }
        }
        return false;
    }

    protected MachineRecipeView createView(GenericRecipe recipe) {
        List<ItemStack[]> inputs = new ArrayList<>();
        List<ItemStack[]> outputs = new ArrayList<>();
        if (recipe.inputItem != null) {
            for (AStack input : recipe.inputItem) inputs.add(input.extractForNEI().toArray(new ItemStack[0]));
        }
        if (recipe.inputFluid != null) {
            for (FluidStack fluid : recipe.inputFluid) inputs.add(new ItemStack[] {ItemFluidIcon.make(fluid)});
        }
        if (recipe.outputItem != null) {
            for (IOutput output : recipe.outputItem) outputs.add(output.getAllPossibilities());
        }
        if (recipe.outputFluid != null) {
            for (FluidStack fluid : recipe.outputFluid) outputs.add(new ItemStack[] {ItemFluidIcon.make(fluid)});
        }

        MachineRecipeView view = new MachineRecipeView(this, recipe, inputs.toArray(new ItemStack[0][]), outputs.toArray(new ItemStack[0][]));
        int inputOffset = getInputXOffset(recipe, inputs.size());
        int outputOffset = getOutputXOffset(recipe, outputs.size());
        int machineOffset = getMachineXOffset(recipe);
        int[][] inputPositions = getInputSlotPositions(inputs.size());
        int[][] outputPositions = getOutputSlotPositions(outputs.size());
        for (int i = 0; i < inputs.size(); i++) view.addInputSlot(i, inputPositions[i][0] + inputOffset, inputPositions[i][1]);
        for (int i = 0; i < outputs.size(); i++) view.addOutputSlot(i, outputPositions[i][0] + outputOffset, outputPositions[i][1]);

        if (recipe.isPooled()) {
            String[] pools = recipe.getPools();
            ItemStack[] blueprints = new ItemStack[pools.length];
            for (int i = 0; i < pools.length; i++) blueprints[i] = ItemBlueprints.make(pools[i]);
            view.addDecoration(blueprints, 75 + machineOffset, 10);
        }
        view.addDecoration(getMachines(recipe), 75 + machineOffset, recipe.isPooled() ? 38 : 31);
        return view;
    }

    /** Fluid searches intentionally ignore quantity/pressure NBT, matching the fluid identity only. */
    public static boolean areItemsAndMetaEqual(ItemStack first, ItemStack second) {
        return first != null && second != null && !first.isEmpty() && !second.isEmpty()
                && first.getItem() == second.getItem() && first.getItemDamage() == second.getItemDamage();
    }

    @Override
    public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        GenericRecipe recipe = (GenericRecipe) view.source;
        minecraft.getTextureManager().bindTexture(TEXTURE);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 5, 11, 166, 65, 256, 256);
        int inputOffset = getInputXOffset(recipe, view.inputs.length);
        int outputOffset = getOutputXOffset(recipe, view.outputs.length);
        for (int[] position : getInputSlotPositions(view.inputs.length)) {
            Gui.drawModalRectWithCustomSizedTexture(position[0] + inputOffset - 1, position[1] - 1, 5, 87, 18, 18, 256, 256);
        }
        for (int[] position : getOutputSlotPositions(view.outputs.length)) {
            Gui.drawModalRectWithCustomSizedTexture(position[0] + outputOffset - 1, position[1] - 1, 5, 87, 18, 18, 256, 256);
        }
        int machineX = 74 + getMachineXOffset(recipe);
        if (recipe.isPooled()) {
            Gui.drawModalRectWithCustomSizedTexture(machineX, 7, 77, 87, 18, 50, 256, 256);
        } else {
            Gui.drawModalRectWithCustomSizedTexture(machineX, 14, 59, 87, 18, 36, 256, 256);
        }
    }

	public int[][] getInputSlotPositions(int count) {

		if(count == 1) return new int[][] { {48, 24} };
		if(count == 2) return new int[][] { {30, 24}, {48, 24} };
		if(count == 3) return new int[][] { {12, 24}, {30, 24}, {48, 24} };
		if(count == 4) return new int[][] { {30, 15}, {48, 15}, {30, 33}, {48, 33} };
		if(count == 5) return new int[][] { {12, 15}, {30, 15}, {48, 15}, {12, 33}, {30, 33} };
		if(count == 6) return new int[][] { {12, 15}, {30, 15}, {48, 15}, {12, 33}, {30, 33}, {48, 33} };
		
		int[][] slots = new int[count][2];
		int cols = (count + 2) / 3;
		
		for(int i = 0; i < count; i++) {
			slots[i][0] = 12 + (i % cols) * 18 - (cols == 4 ? 18 : 0);
			slots[i][1] = 6 + (i / cols) * 18;
		}
		
		return slots;
	}
	
	public int[][] getOutputSlotPositions(int count) {
		switch(count) {
		case 1: return new int[][] {
			{102, 24}
		};
		case 2: return new int[][] {
			{102, 24},
			{120, 24}
		};
		case 3: return new int[][] {
			{102, 24},
			{120, 24},
			{138, 24}
		};
		case 4: return new int[][] {
			{102, 24 - 9},
			{120, 24 - 9},
			{102, 24 + 9},
			{120, 24 + 9}
		};
		case 5: return new int[][] {
			{102, 24 - 9}, {120, 24 - 9},
			{102, 24 + 9}, {120, 24 + 9},
			{138, 24},
		};
		case 6: return new int[][] {
			{102, 6}, {120, 6},
			{102, 24}, {120, 24},
			{102, 42}, {120, 42},
		};
		case 7: return new int[][] {
			{102, 6}, {120, 6},
			{102, 24}, {120, 24},
			{102, 42}, {120, 42},
			{138, 24},
		};
		case 8: return new int[][] {
			{102, 6}, {120, 6},
			{102, 24}, {120, 24},
			{102, 42}, {120, 42},
			{138, 24}, {138, 42},
		};
		}
		return new int[count][2];
	}

}
