package com.hbm.render.item.weapon;

import org.lwjgl.opengl.GL11;

import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import com.hbm.render.item.nativeitem.context.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

public class ItemRenderShim extends NativeOriginalItemRenderer {
	
	public ItemRenderShim() { super(false, true); }

	@Override
	protected void drawModel(ItemStack item, NativeItemContext context) {
		GL11.glPushMatrix();
		switch(context.transform) {
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			if(item.getItem() == ModItems.stopsign || item.getItem() == ModItems.sopsign || item.getItem() == ModItems.chernobylsign) {
				GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
				GL11.glTranslatef(-1.0F, -1.5F, 0.0F);
			}
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case NONE:
		case HEAD:
		case FIXED:
		case GROUND:
				if(item.getItem() == ModItems.shimmer_sledge)
					Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.shimmer_sledge_tex);
				if(item.getItem() == ModItems.shimmer_axe)
					Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.shimmer_axe_tex);
				if(item.getItem() == ModItems.stopsign)
					Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.stopsign_tex);
				if(item.getItem() == ModItems.sopsign)
					Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.sopsign_tex);
				if(item.getItem() == ModItems.chernobylsign)
					Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.chernobylsign_tex);
				
				if(item.getItem() == ModItems.shimmer_sledge || item.getItem() == ModItems.shimmer_axe) {
					GL11.glRotatef(-135.0F, 0.0F, 0.0F, 1.0F);
					GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
					GL11.glScalef(1.5F, 1.5F, 1.5F);
					GL11.glTranslatef(0.45F, -0.3F, 0.0F);
				}
				
				if(item.getItem() == ModItems.stopsign || item.getItem() == ModItems.sopsign || item.getItem() == ModItems.chernobylsign) {
					GL11.glRotatef(45.0F, 0.0F, 0.0F, 1.0F);
					GL11.glScalef(0.35F, 0.35F, 0.35F);
					GL11.glTranslatef(2.0F, -2.0F, 0.0F);
					GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
				}
				
				if(item.getItem() == ModItems.shimmer_sledge)
					ResourceManager.shimmer_sledge.renderAll();
				if(item.getItem() == ModItems.shimmer_axe)
					ResourceManager.shimmer_axe.renderAll();
				if(item.getItem() == ModItems.stopsign || item.getItem() == ModItems.sopsign || item.getItem() == ModItems.chernobylsign)
					ResourceManager.stopsign.renderAll();
				
		default: break;
		}
		GL11.glPopMatrix();
	}
}
