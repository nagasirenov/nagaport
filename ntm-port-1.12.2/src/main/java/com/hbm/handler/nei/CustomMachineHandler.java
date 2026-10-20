package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import codechicken.lib.gui.GuiDraw;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.CustomMachineConfigJSON;
import com.hbm.config.CustomMachineConfigJSON.MachineConfiguration;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.CustomMachineRecipes;
import com.hbm.inventory.recipes.CustomMachineRecipes.CustomMachineRecipe;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.lib.RefStrings;
import com.hbm.util.ItemStackUtil;
import com.hbm.util.Tuple.Pair;
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
/** Native JEI category for the original CustomMachineHandler recipe source. */
public class CustomMachineHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_custom.png");
    private IDrawable background;
    @Override public String getUid() { return "ntm_" + conf.unlocalizedName; }
    @Override public String getTitle() {
		return conf.localizedName;
	 }
    @Override public ItemStack[] getMachines() { return new ItemStack[0]; }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);
        background = helper.createBlankDrawable(166, 84);
    }
    @Override public void registerGuiAreas(IModRegistry registry) {  }
    @Override public boolean isNavigationArea(int x, int y) {
        return new java.awt.Rectangle(65, 23, 36, 18).contains(x, y);
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {

			List<CustomMachineRecipe> recipes = CustomMachineRecipes.recipes.get(conf.recipeKey);

			if(recipes != null) for(CustomMachineRecipe recipe : recipes) {
				views.add(createView(recipe));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();


		List<CustomMachineRecipe> recipes = CustomMachineRecipes.recipes.get(conf.recipeKey);

		if(recipes != null) outer:for(CustomMachineRecipe recipe : recipes) {

			for(Pair<ItemStack, Float> stack : recipe.outputItems) {

				if(RecipeStackMatcher.matchesCrafting(stack.getKey(), result)) {
					views.add(createView(recipe));
					continue outer;
				}
			}

			for(FluidStack fluid : recipe.outputFluids) {
				ItemStack drop = ItemFluidIcon.make(fluid);

				if(compareFluidStacks(result, drop)) {
					views.add(createView(recipe));
					continue outer;
				}
			}
		}

        } else {
            ItemStack ingredient = focus.getValue();


		List<CustomMachineRecipe> recipes = CustomMachineRecipes.recipes.get(conf.recipeKey);

		if(recipes != null) outer:for(CustomMachineRecipe recipe : recipes) {

			for(AStack stack : recipe.inputItems) {

				List<ItemStack> stacks = stack.extractForNEI();

				for(ItemStack sta : stacks) {
					if(RecipeStackMatcher.matchesCrafting(ingredient, sta)) {
						views.add(createView(recipe));
						continue outer;
					}
				}
			}

			for(FluidStack fluid : recipe.inputFluids) {
				ItemStack drop = ItemFluidIcon.make(fluid);

				if(compareFluidStacks(ingredient, drop)) {
					views.add(createView(recipe));
					continue outer;
				}
			}
		}

        }
        return views;
    }
    private MachineRecipeView createView(CustomMachineRecipe recipe) {
        List<ItemStack[]> inputs = new ArrayList<>();
        List<ItemStack[]> outputs = new ArrayList<>();
        for(int i = 0; i < Math.min(recipe.inputFluids.length, 3); i++) inputs.add(new ItemStack[]{ItemFluidIcon.make(recipe.inputFluids[i])});
        for(int i = 0; i < Math.min(recipe.inputItems.length, 6); i++) inputs.add(recipe.inputItems[i].extractForNEI().toArray(new ItemStack[0]));
        for(int i = 0; i < Math.min(recipe.outputFluids.length, 3); i++) outputs.add(new ItemStack[]{ItemFluidIcon.make(recipe.outputFluids[i])});
        for(int i = 0; i < Math.min(recipe.outputItems.length, 6); i++) {
            Pair<ItemStack, Float> pair = recipe.outputItems[i];
            ItemStack output = pair.getKey().copy();
            if(pair.getValue() != 1) ItemStackUtil.addTooltipToStack(output, TextFormatting.RED + "" + (((int)(pair.getValue() * 1000)) / 10D) + "%");
            outputs.add(new ItemStack[]{output});
        }
        MachineRecipeView view = new MachineRecipeView(this, recipe, inputs.toArray(new ItemStack[0][]), outputs.toArray(new ItemStack[0][]));
        int slot = 0;
        for(int i = 0; i < Math.min(recipe.inputFluids.length, 3); i++) view.addInputSlot(slot++, 12 + i * 18, 6);
        for(int i = 0; i < Math.min(recipe.inputItems.length, 6); i++) view.addInputSlot(slot++, 12 + (i % 3) * 18, i < 3 ? 24 : 42);
        slot = 0;
        for(int i = 0; i < Math.min(recipe.outputFluids.length, 3); i++) view.addOutputSlot(slot++, 102 + i * 18, 6);
        for(int i = 0; i < Math.min(recipe.outputItems.length, 6); i++) view.addOutputSlot(slot++, 102 + (i % 3) * 18, i < 3 ? 24 : 42);
        view.addDecoration(new ItemStack[]{new ItemStack(ModBlocks.custom_machine, 1, 100 + CustomMachineConfigJSON.niceList.indexOf(conf))}, 75, 42);
        return view;
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        minecraft.getTextureManager().bindTexture(texture);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 5, 11, 166, 65, 256, 256);
        CustomMachineRecipe recipe = (CustomMachineRecipe) view.source;
        if(recipe.radiationAmount != 0) {
            String radiation = "Radiation:" + recipe.radiationAmount;
            minecraft.fontRenderer.drawString(radiation, 160 - minecraft.fontRenderer.getStringWidth(radiation), 63, 0x08FF00);
        }
        if(recipe.pollutionAmount != 0) {
            String pollution = recipe.pollutionType + ":" + recipe.pollutionAmount;
            minecraft.fontRenderer.drawString(pollution, 160 - minecraft.fontRenderer.getStringWidth(pollution), 75, 0x404040);
        }
        if(conf.fluxMode) {
            String flux = "Flux:" + recipe.flux;
            minecraft.fontRenderer.drawString(flux, 83 - minecraft.fontRenderer.getStringWidth(flux) / 2, 16, 0x08FF00);
        }
        if(conf.maxHeat > 0 && recipe.heat > 0) {
            String heat = "Heat:" + recipe.heat;
            minecraft.fontRenderer.drawString(heat, 83 - minecraft.fontRenderer.getStringWidth(heat) / 2, 8, 0xFF0000);
        }
    }
    public final MachineConfiguration conf;
    public CustomMachineHandler(MachineConfiguration configuration) { this.conf = configuration; }
    public static boolean compareFluidStacks(ItemStack a, ItemStack b) { return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage(); }
}
