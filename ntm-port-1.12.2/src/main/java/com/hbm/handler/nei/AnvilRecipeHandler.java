package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.NTMAnvil;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.gui.GUIAnvil;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilConstructionRecipe;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilOutput;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.OverlayType;
import com.hbm.lib.RefStrings;
import com.hbm.util.ItemStackUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import com.hbm.handler.jei.MachineRecipeCategory;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.handler.jei.RecipeStackMatcher;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
/** Native JEI category for the original AnvilRecipeHandler recipe source. */
public class AnvilRecipeHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_anvil.png");
    private IDrawable background;
    @Override public String getUid() { return "ntmAnvil"; }
    @Override public String getTitle() {
		return "Anvil";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.anvil_iron),
				new ItemStack(ModBlocks.anvil_lead),
				new ItemStack(ModBlocks.anvil_steel),
				new ItemStack(ModBlocks.anvil_desh),
				new ItemStack(ModBlocks.anvil_saturnite),
				new ItemStack(ModBlocks.anvil_ferrouranium),
				new ItemStack(ModBlocks.anvil_bismuth_bronze),
				new ItemStack(ModBlocks.anvil_arsenic_bronze),
				new ItemStack(ModBlocks.anvil_schrabidate),
				new ItemStack(ModBlocks.anvil_dnt),
				new ItemStack(ModBlocks.anvil_osmiridium),
				new ItemStack(ModBlocks.anvil_murky)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);

    }
    @Override public void registerGuiAreas(IModRegistry registry) { registry.addRecipeClickArea(GUIAnvil.class, (11) + 5, (42) + 11, 36, 18, getUid()); registry.addRecipeClickArea(GUIAnvil.class, (65) + 5, (42) + 11, 36, 18, getUid()); }
    @Override public boolean isNavigationArea(int x, int y) {
        return (false);
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {
			List<AnvilConstructionRecipe> recipes = AnvilRecipes.getConstruction();

			for(AnvilConstructionRecipe recipe : recipes) {
				views.add(createView(recipe));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();


		List<AnvilConstructionRecipe> recipes = AnvilRecipes.getConstruction();

		for(AnvilConstructionRecipe recipe : recipes) {

			for(AnvilOutput out : recipe.output) {
				if(RecipeStackMatcher.matchesCrafting(out.stack, result)) {
					views.add(createView(recipe));
					break;
				}
			}
		}

        } else {
            ItemStack ingredient = focus.getValue();


		List<AnvilConstructionRecipe> recipes = AnvilRecipes.getConstruction();

		for(AnvilConstructionRecipe recipe : recipes) {

			outer:
			for(AStack in : recipe.input) {

				List<ItemStack> stacks = in.extractForNEI();
				for(ItemStack stack : stacks) {
					if(RecipeStackMatcher.matchesCrafting(stack, ingredient)) {
						views.add(createView(recipe));
						break outer;
					}
				}
			}
		}

        }
        return views;
    }
    private MachineRecipeView createView(AnvilConstructionRecipe recipe) {
        ItemStack[][] inputs = new ItemStack[recipe.input.size()][];
        ItemStack[][] outputs = new ItemStack[recipe.output.size()][];
        for(int i = 0; i < inputs.length; i++) inputs[i] = recipe.input.get(i).extractForNEI().toArray(new ItemStack[0]);
        for(int i = 0; i < outputs.length; i++) {
            AnvilOutput output = recipe.output.get(i);
            ItemStack stack = output.stack.copy();
            if(output.chance != 1) ItemStackUtil.addTooltipToStack(stack, TextFormatting.RED + "" + (((int)(output.chance * 1000)) / 10D) + "%");
            outputs[i] = new ItemStack[]{stack};
        }
        int inLine = 1, outLine = 1, inX, inY, outX, outY, anvilX;
        if(inputs.length == 1 && outputs.length == 1) {
            inX = 48; inY = 24; outX = 102; outY = 24; anvilX = 75;
        } else if(inputs.length == 1 && outputs.length > 1) {
            outLine = 6; inX = 12; inY = 24; outX = 48; outY = 6; anvilX = 30;
        } else if(inputs.length > 1 && outputs.length == 1) {
            inLine = 6; inX = 12; inY = 6; outX = 138; outY = 24; anvilX = 120;
        } else {
            inLine = 4; outLine = 4; inX = 3; inY = 6; outX = 93; outY = 6; anvilX = 75;
        }
        MachineRecipeView view = new MachineRecipeView(this, recipe, inputs, outputs);
        for(int i = 0; i < inputs.length; i++) view.addInputSlot(i, inX + 18 * (i % inLine), inY + 18 * (i / inLine));
        for(int i = 0; i < outputs.length; i++) view.addOutputSlot(i, outX + 18 * (i % outLine), outY + 18 * (i / outLine));
        view.addDecoration(com.hbm.handler.jei.RecipeItemAlternatives.from(NTMAnvil.getAnvilsFromTier(recipe.tierLower)), anvilX, 31);
        return view;
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        minecraft.getTextureManager().bindTexture(texture);
        if(view.inputs.length == 1 && view.outputs.length == 1) {
            drawPatch(47, 23, 113, 105, 18, 18); drawPatch(101, 23, 113, 105, 18, 18); drawPatch(74, 14, 149, 96, 18, 36);
        } else if(view.inputs.length == 1 && view.outputs.length > 1) {
            drawPatch(11, 23, 113, 105, 18, 18); drawPatch(47, 5, 5, 87, 108, 54); drawPatch(29, 14, 185, 96, 18, 36);
        } else if(view.inputs.length > 1 && view.outputs.length == 1) {
            drawPatch(11, 5, 5, 87, 108, 54); drawPatch(137, 23, 113, 105, 18, 18); drawPatch(119, 14, 167, 96, 18, 36);
        } else {
            drawPatch(2, 5, 5, 87, 72, 54); drawPatch(92, 5, 5, 87, 72, 54); drawPatch(74, 14, 131, 96, 18, 36);
        }
    }
    private static void drawPatch(int x, int y, int u, int v, int width, int height) {
        Gui.drawModalRectWithCustomSizedTexture(x, y, u, v, width, height, 256, 256);
    }
    private final java.util.Map<mezz.jei.api.gui.IRecipeLayout, AnvilConstructionRecipe> displayedRecipes = new java.util.WeakHashMap<>();
    @Override public void setRecipe(mezz.jei.api.gui.IRecipeLayout layout, MachineRecipeView view, mezz.jei.api.ingredients.IIngredients ingredients) {
        super.setRecipe(layout, view, ingredients);
        displayedRecipes.put(layout, (AnvilConstructionRecipe) view.source);
    }
    public AnvilConstructionRecipe getConstructionRecipe(mezz.jei.api.gui.IRecipeLayout layout) { return displayedRecipes.get(layout); }
}
