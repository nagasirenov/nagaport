package com.hbm.render.item;

import javax.vecmath.Matrix4f;
import org.lwjgl.input.Keyboard;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.items.ModItems;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.context.*;
import com.hbm.render.item.nativeitem.direct.PlainItemPerspectives;
import com.hbm.render.util.RenderItemStack;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

/** Shift-only GUI preview. Selection is derived from the actual stack on every
 * draw, not the last handleRenderType call or another template's cached icon. */
public class ItemRenderTemplate extends ContextItemRenderer implements NativeSelectiveItemRenderer {
    protected boolean isShiftHeld() { return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT); }
    protected ItemStack preview(ItemStack stack) {
        if(stack.isEmpty() || stack.getItem() != ModItems.crucible_template) return ItemStack.EMPTY;
        CrucibleRecipes.CrucibleRecipe recipe = CrucibleRecipes.indexMapping.get(stack.getItemDamage());
        if(recipe == null || recipe.icon == null || recipe.icon.isEmpty()) return ItemStack.EMPTY;
        // A self-referential recipe cannot recurse into its own GUI preview.
        if(recipe.icon.getItem() == stack.getItem() && recipe.icon.getMetadata() == stack.getMetadata()) return ItemStack.EMPTY;
        return recipe.icon;
    }
    @Override public boolean handlesPerspective(ItemStack stack, TransformType type) {
        return type == TransformType.GUI && isShiftHeld() && !preview(stack).isEmpty();
    }
    @Override public Matrix4f localTransform(ItemStack stack, NativeItemContext context) { return new ItemTransformMatrix().build(); }
    @Override public Matrix4f composePerspective(ItemStack stack, NativeItemContext context) {
        return PlainItemPerspectives.compose(context.transform, localTransform(stack, context));
    }
    @Override protected void draw(ItemStack stack, NativeItemContext context) {
        RenderHelper.enableGUIStandardItemLighting();
        ItemStack icon = preview(stack);
        // If a reload removes a recipe between selection and draw, render the
        // original ordinary icon, never recursively dispatch this template.
        if(icon.isEmpty()) com.hbm.render.util.NativeGuiItemDraw.baseIntoGui(RenderItemStack.renderer(), stack, 0, 0, true);
        else RenderItemStack.renderItemStack(0, 0, 1F, icon);
    }
}
