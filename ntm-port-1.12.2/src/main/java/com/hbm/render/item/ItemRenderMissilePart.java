package com.hbm.render.item;

import org.lwjgl.opengl.GL11;

import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import com.hbm.render.util.MissilePart;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import com.hbm.render.item.nativeitem.context.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

public class ItemRenderMissilePart extends NativeOriginalItemRenderer {
	
	MissilePart part;

	public ItemRenderMissilePart() { super(true, false); }
	
	public ItemRenderMissilePart(MissilePart part) {
        super(true, false);
		this.part = part;
	}
	
    @Override public boolean handlesPerspective(ItemStack item, TransformType type) { return part != null && part.renderInventoryModel && part.model != null && part.texture != null; }

	@Override
	protected void drawModel(ItemStack item, NativeItemContext context) {
		
		if(part == null)
			return;

		GL11.glPushMatrix();
		
		switch(context.transform) {
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			GL11.glTranslated(0.5, 0, 0);
			
		case NONE:
		case HEAD:
		case FIXED:
		case GROUND:
			double s = 0.4;
			GL11.glScaled(s, s, s);
			
			Minecraft.getMinecraft().renderEngine.bindTexture(part.texture);
			part.model.renderAll();
			
			break;
			
		case GUI:
			
			double height = part.guiheight;
			
			if(height == 0D)
				height = 4D;
			
			double size = 10;
			double scale = size / height;
			
			GL11.glTranslated(height / 2 * scale, 0, 0);
			GL11.glRotated(135, 0, 0, 1);
			GL11.glRotated(145, 1, 0, 0);
			
			if(part.type == PartType.WARHEAD) {
				GL11.glTranslated(0, height / 8 * scale, 0);
			}
			
			if(part.type == PartType.FUSELAGE) {
				GL11.glTranslated(0, (height / 4 - 0.5) * scale, 0);
			}
			
			GL11.glTranslated(3.5, 14, 0);
			GL11.glScaled(-scale, -scale, -scale);

			GL11.glRotatef(animationTimeMillis() / 25 % 360, 0, -1, 0);
			Minecraft.getMinecraft().renderEngine.bindTexture(part.texture);
			part.model.renderAll();
			
			break;
		default: break;
		}
		
		GL11.glPopMatrix();
	}
}
