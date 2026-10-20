package com.hbm.render.item.weapon;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.render.model.ModelSword;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import com.hbm.render.item.nativeitem.context.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

public class ItemRenderRedstoneSword extends NativeOriginalItemRenderer {
	
	protected ModelSword swordModel;
	
	public ItemRenderRedstoneSword() { super(false, false);
		swordModel = new ModelSword();
	}

	@Override
	protected void drawModel(ItemStack item, NativeItemContext context) {
		switch(context.transform) {
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
		case NONE:
		case HEAD:
		case FIXED:
		case GROUND:
			GL11.glPushMatrix();
				Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(RefStrings.MODID +":textures/models/modelswordredstone.png"));
				GL11.glRotatef(-135.0F, 0.0F, 0.0F, 1.0F);
				GL11.glTranslatef(-0.8F, 0.4F, -0.1F);
				swordModel.render(context.holder, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			GL11.glPopMatrix();
		default: break;
		}
	}

}
