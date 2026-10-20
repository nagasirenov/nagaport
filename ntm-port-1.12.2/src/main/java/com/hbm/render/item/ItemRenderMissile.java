package com.hbm.render.item;

import org.lwjgl.opengl.GL11;

import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.render.util.MissileMultipart;
import com.hbm.render.util.MissilePronter;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import com.hbm.render.item.nativeitem.context.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

public class ItemRenderMissile extends NativeOriginalItemRenderer {

	public ItemRenderMissile() { super(true, false); }
	
    @Override public boolean handlesPerspective(ItemStack item, TransformType type) { return MissileMultipart.loadFromStruct(ItemCustomMissile.getStruct(item)) != null; }

	@Override
	protected void drawModel(ItemStack item, NativeItemContext context) {

		MissileMultipart missile = MissileMultipart.loadFromStruct(ItemCustomMissile.getStruct(item));
        if(missile == null) return;
		
		GL11.glPushMatrix();
		
		switch(context.transform) {
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
		case NONE:
		case HEAD:
		case FIXED:
		case GROUND:
			
			double s = 0.2;
			GL11.glScaled(s, s, s);
			
			GL11.glTranslated(2, 0, 0);
			
			MissilePronter.prontMissile(missile, Minecraft.getMinecraft().renderEngine);
			
			break;
			
		case GUI:
			
			double height = missile.getHeight();
			
			if(height == 0D)
				height = 4D;
			
			double size = 20;
			double scale = size / height;
			
			GL11.glTranslated(height / 2 * scale, 0, 0);

			GL11.glRotated(135, 0, 0, 1);
			GL11.glRotated(215, 1, 0, 0);
			
			GL11.glTranslated(7, 14, 0);
			
			GL11.glScaled(-scale, -scale, -scale);

			GL11.glRotatef(animationTimeMillis() / 25 % 360, 0, -1, 0);
			MissilePronter.prontMissile(missile, Minecraft.getMinecraft().renderEngine);
			
			break;
		default: break;
		}
		
		GL11.glPopMatrix();
	}
}
