package com.hbm.handler.nei;


import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIMachineGasCent;
import com.hbm.inventory.recipes.GasCentrifugeRecipes;
import com.hbm.inventory.recipes.MachineRecipes;
import com.hbm.lib.RefStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import com.hbm.handler.jei.MachineRecipeCategory;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.handler.jei.RecipeStackMatcher;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
/** Native JEI category for the original GasCentrifugeRecipeHandler recipe source. */
public class GasCentrifugeRecipeHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_centrifuge_gas.png");
    private IDrawable background;
    @Override public String getUid() { return "gascentprocessing"; }
    @Override public String getTitle() {
		return "Gas Centrifuge";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.machine_gascent)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);
        energy = helper.createAnimatedDrawable(helper.createDrawable(texture, 176, 0, 16, 34), 480, IDrawableAnimated.StartDirection.TOP, true);
        normal = helper.createAnimatedDrawable(helper.createDrawable(texture, 208, 0, 44, 37), 150, IDrawableAnimated.StartDirection.LEFT, false);
        fast = helper.createAnimatedDrawable(helper.createDrawable(texture, 208, 0, 44, 37), 80, IDrawableAnimated.StartDirection.LEFT, false);
    }
    @Override public void registerGuiAreas(IModRegistry registry) { registry.addRecipeClickArea(GUIMachineGasCent.class, (70 - 5) + 5, (36 - 11) + 11, 36, 12, getUid()); }
    @Override public boolean isNavigationArea(int x, int y) {
        return (new java.awt.Rectangle(79 - 5, 26 - 11, 44, 40).contains(x, y));
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {
			Map<Object, Object[]> recipes = GasCentrifugeRecipes.getGasCentrifugeRecipes();
			for(Map.Entry<Object, Object[]> recipe : recipes.entrySet()) {
				views.add(createView((ItemStack) recipe.getKey(), (ItemStack[]) recipe.getValue()[0], (boolean) recipe.getValue()[1], (int) recipe.getValue()[2]));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();

		Map<Object, Object[]> recipes = GasCentrifugeRecipes.getGasCentrifugeRecipes();
		for(Map.Entry<Object, Object[]> recipe : recipes.entrySet()) {
			if(RecipeStackMatcher.matchesType(((ItemStack[]) recipe.getValue()[0])[0], result) || RecipeStackMatcher.matchesType(((ItemStack[]) recipe.getValue()[0])[1], result)
					|| RecipeStackMatcher.matchesType(((ItemStack[]) recipe.getValue()[0])[2], result) || RecipeStackMatcher.matchesType(((ItemStack[]) recipe.getValue()[0])[3], result))
				views.add(createView((ItemStack) recipe.getKey(), (ItemStack[]) recipe.getValue()[0], (boolean) recipe.getValue()[1], (int) recipe.getValue()[2]));
		}

        } else {
            ItemStack ingredient = focus.getValue();

		Map<Object, Object[]> recipes = GasCentrifugeRecipes.getGasCentrifugeRecipes();
		for(Map.Entry<Object, Object[]> recipe : recipes.entrySet()) {
			if(compareFluidStacks(ingredient, (ItemStack) recipe.getKey()))
				views.add(createView((ItemStack) recipe.getKey(), (ItemStack[]) recipe.getValue()[0], (boolean) recipe.getValue()[1], (int) recipe.getValue()[2]));
		}

        }
        return views;
    }
    private MachineRecipeView createView(ItemStack input, ItemStack[] outputs, boolean highSpeed, int count) {
        ItemStack shown = input.copy(); shown.setCount(1);
        ItemStack[][] results = new ItemStack[outputs.length][];
        for(int i = 0; i < outputs.length; i++) results[i] = new ItemStack[]{outputs[i]};
        MachineRecipeView view = new MachineRecipeView(this, new Object[]{highSpeed, count}, new ItemStack[][]{{shown}}, results).addInputSlot(0, 47, 24);
        for(int i = 0; i < outputs.length; i++) view.addOutputSlot(i, i % 2 == 0 ? 129 : 147, i < 2 ? 15 : 33);
        List<ItemStack> batteries = new ArrayList<>();
        for(ItemStack battery : MachineRecipes.instance().getBatteries()) batteries.add(battery);
        if(!batteries.isEmpty()) view.addDecoration(batteries.toArray(new ItemStack[0]), 3, 42);
        return view;
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        Object[] data = (Object[]) view.source;
        boolean highSpeed = (Boolean) data[0];
        if(highSpeed) {
            minecraft.getTextureManager().bindTexture(texture);
            Gui.drawModalRectWithCustomSizedTexture(25, 24, 192, 0, 16, 16, 256, 256);
        }
        energy.draw(minecraft, 3, 6);
        (highSpeed ? fast : normal).draw(minecraft, 74, 17);
        String text = data[1] + " G. Cents";
        minecraft.fontRenderer.drawString(text, 50 - minecraft.fontRenderer.getStringWidth(text) / 2, 10, 65280);
    }
    private IDrawableAnimated energy, normal, fast;
    private boolean compareFluidStacks(ItemStack a, ItemStack b) { return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage(); }
}
