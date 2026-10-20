package com.hbm.handler.nei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.hbm.handler.jei.MachineRecipeCategory;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.items.ModItems;
import com.hbm.util.InventoryUtil;
import com.hbm.handler.jei.RecipeStackMatcher;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/** Native JEI presentation of NTM's map-backed machine recipes. */
public abstract class NEIUniversalHandler extends MachineRecipeCategory {
    public final String display;
    public final ItemStack[] machine;
    public final HashMap<Object, Object> recipes;
    public HashMap<Object, Object> machineOverrides;

    @SuppressWarnings("unchecked")
    public NEIUniversalHandler(String display, ItemStack[] machine, HashMap recipes) {
        this.display = display;
        this.machine = machine;
        this.recipes = recipes;
    }
    @SuppressWarnings("unchecked")
    public NEIUniversalHandler(String display, HashMap recipes, HashMap machines) {
        this(display, (ItemStack[]) null, recipes);
        this.machineOverrides = machines;
    }
    public NEIUniversalHandler(String display, ItemStack machine, HashMap recipes) { this(display, new ItemStack[]{machine}, recipes); }
    public NEIUniversalHandler(String display, Item machine, HashMap recipes) { this(display, new ItemStack(machine), recipes); }
    public NEIUniversalHandler(String display, Block machine, HashMap recipes) { this(display, new ItemStack(machine), recipes); }

    @Override public String getTitle() { return display; }
    @Override public ItemStack[] getMachines() { return machine == null ? new ItemStack[0] : machine; }

    protected ItemStack[][] recipeInputs(Object key) { return InventoryUtil.extractObject(key); }
    protected boolean acceptsFocus(IFocus<ItemStack> focus) { return true; }
    protected boolean matchesOutput(ItemStack stack, ItemStack focus) { return RecipeStackMatcher.matchesCrafting(stack, focus); }
    protected boolean matchesInput(ItemStack stack, ItemStack focus) { return RecipeStackMatcher.matchesCrafting(stack, focus); }

    /** Query each time: source maps and focus-dependent heat/NBT rules are not frozen at startup. */
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> result = new ArrayList<>();
        if(!acceptsFocus(focus)) return result;
        outer: for(Entry<Object, Object> recipe : recipes.entrySet()) {
            ItemStack[][] inputs = recipeInputs(recipe.getKey());
            ItemStack[][] outputs = InventoryUtil.extractObject(recipe.getValue());
            for(ItemStack[] alternatives : inputs) for(ItemStack stack : alternatives)
                if(stack.getItem() == ModItems.item_secret) continue outer;
            for(ItemStack[] alternatives : outputs) for(ItemStack stack : alternatives)
                if(stack.getItem() == ModItems.item_secret) continue outer;
            boolean matches = focus == null;
            if(focus != null) {
                boolean output = focus.getMode() == IFocus.Mode.OUTPUT;
                for(ItemStack[] alternatives : output ? outputs : inputs) for(ItemStack stack : alternatives)
                    if(output ? matchesOutput(stack, focus.getValue()) : matchesInput(stack, focus.getValue())) matches = true;
            }
            if(matches) result.add(createView(recipe.getKey(), inputs, outputs));
        }
        return result;
    }

    protected MachineRecipeView createView(Object originalInput, ItemStack[][] inputs, ItemStack[][] outputs) {
        MachineRecipeView view = new MachineRecipeView(this, originalInput, inputs, outputs);
        int[][] inputPositions = getInputCoords(inputs.length);
        int[][] outputPositions = getOutputCoords(outputs.length);
        for(int i = 0; i < inputs.length; i++) view.addInputSlot(i, inputPositions[i][0], inputPositions[i][1]);
        for(int i = 0; i < outputs.length; i++) view.addOutputSlot(i, outputPositions[i][0], outputPositions[i][1]);
        Object override = machineOverrides == null ? null : machineOverrides.get(originalInput);
        List<ItemStack> decorations = new ArrayList<>();
        if(override != null) for(ItemStack[] variants : InventoryUtil.extractObject(override)) decorations.addAll(Arrays.asList(variants));
        else if(machine != null) decorations.addAll(Arrays.asList(machine));
        if(!decorations.isEmpty()) view.addDecoration(decorations.toArray(new ItemStack[0]), 75, 31);
        return view;
    }

    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        minecraft.getTextureManager().bindTexture(new ResourceLocation("hbm:textures/gui/nei/gui_nei.png"));
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 5, 11, 166, 65, 256, 256);
        for(int[] pos : getInputCoords(view.inputs.length)) Gui.drawModalRectWithCustomSizedTexture(pos[0] - 1, pos[1] - 1, 5, 87, 18, 18, 256, 256);
        for(int[] pos : getOutputCoords(view.outputs.length)) Gui.drawModalRectWithCustomSizedTexture(pos[0] - 1, pos[1] - 1, 5, 87, 18, 18, 256, 256);
        Gui.drawModalRectWithCustomSizedTexture(74, 14, 59, 87, 18, 36, 256, 256);
    }

	public static int[][] getInputCoords(int count) {
		
		switch(count) {
		case 1: return new int[][] {
			{48, 24}
		};
		case 2: return new int[][] {
			{48, 24},
			{30, 24}
		};
		case 3: return new int[][] {
			{48, 24},
			{30, 24},
			{12, 24}
		};
		case 4: return new int[][] {
			{48, 24 - 9},
			{30, 24 - 9},
			{48, 24 + 9},
			{30, 24 + 9}
		};
		case 5: return new int[][] {
			{48, 24 - 9},
			{30, 24 - 9},
			{12, 24},
			{48, 24 + 9},
			{30, 24 + 9},
		};
		case 6: return new int[][] {
			{48, 24 - 9},
			{30, 24 - 9},
			{12, 24 - 9},
			{48, 24 + 9},
			{30, 24 + 9},
			{12, 24 + 9}
		};
		case 7: return new int[][] {
			{48, 24 - 18},
			{30, 24 - 9},
			{12, 24 - 9},
			{48, 24},
			{30, 24 + 9},
			{12, 24 + 9},
			{48, 24 + 18}
		};
		case 8: return new int[][] {
			{48, 24 - 18},
			{30, 24 - 18},
			{12, 24 - 9},
			{48, 24},
			{30, 24},
			{12, 24 + 9},
			{48, 24 + 18},
			{30, 24 + 18}
		};
		case 9: return new int[][] {
			{48, 24 - 18},
			{30, 24 - 18},
			{12, 24 - 18},
			{48, 24},
			{30, 24},
			{12, 24},
			{48, 24 + 18},
			{30, 24 + 18},
			{12, 24 + 18}
		};
		}
		
		int[][] slots = new int[count][2];
		
		for(int i = 0; i < count; i++) {
			slots[i] = new int[] {i % 4 * 18, i / 4 * 18};
		}
		
		return slots;
	}
	
	public static int[][] getOutputCoords(int count) {
		
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
		
		int[][] slots = new int[count][2];
		
		for(int i = 0; i < count; i++) {
			slots[i] = new int[] {i % 4 * 18, i / 4 * 18};
		}
		
		return slots;
	}

}
