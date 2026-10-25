package com.hbm.render.item.weapon.sedna;

import com.hbm.render.item.nativeitem.context.NativeItemContext;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

import org.lwjgl.opengl.GL11;

import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class ItemRenderAberrator extends ItemRenderWeaponBase {

	@Override
	protected float getTurnMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.25F; }

	@Override
	public float getViewFOV(ItemStack stack, float fov) {
		float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		return  fov * (1 - aimingProgress * 0.33F);
	}

	@Override
	public void setupFirstPerson(ItemStack stack) {
		GL11.glTranslated(0, 0, 1);

		float offset = 0.8F;
		standardAimingTransform(stack,
				-1.0F * offset, -1.25F * offset, 1.25F * offset,
				0, -5.25 / 8D, 0.125);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.aberrator_tex);
		double scale = 0.25D;
		GL11.glScaled(scale, scale, scale);

		double[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
		double[] rise = HbmAnimations.getRelevantTransformation("RISE");
		double[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
		double[] slide = HbmAnimations.getRelevantTransformation("SLIDE");
		double[] bullet = HbmAnimations.getRelevantTransformation("BULLET");
		double[] hammer = HbmAnimations.getRelevantTransformation("HAMMER");
		double[] roll = HbmAnimations.getRelevantTransformation("ROLL");
		double[] mag = HbmAnimations.getRelevantTransformation("MAG");
		double[] magroll = HbmAnimations.getRelevantTransformation("MAGROLL");
		double[] sight = HbmAnimations.getRelevantTransformation("SIGHT");

		GL11.glTranslated(0, rise[1], 0);

		GL11.glTranslated(0, 1, -2.25);
		GL11.glRotated(equip[0], 1, 0, 0);
		GL11.glTranslated(0, -1, 2.25);

		GL11.glTranslated(0, -1, -4);
		GL11.glRotated(recoil[0], 1, 0, 0);
		GL11.glTranslated(0, 1, 4);

		GL11.glTranslated(0, 1, 0);
		GL11.glRotated(roll[2], 0, 0, 1);
		GL11.glTranslated(0, -1, 0);

		GL11.glShadeModel(GL11.GL_SMOOTH);

		ResourceManager.aberrator.renderPart("Gun");

		GL11.glPushMatrix();
		GL11.glTranslated(0, 2.4375, -1.9375);
		GL11.glRotated(sight[0], 1, 0, 0);
		GL11.glTranslated(0, -2.4375, 1.9375);
		ResourceManager.aberrator.renderPart("Sight");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslated(mag[0], mag[1], mag[2]);

		GL11.glTranslated(0, 1, 0);
		GL11.glRotated(magroll[2], 0, 0, 1);
		GL11.glTranslated(0, -1, 0);

		ResourceManager.aberrator.renderPart("Magazine");
		GL11.glTranslated(bullet[0], bullet[1], bullet[2]);
		ResourceManager.aberrator.renderPart("Bullet");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslated(0, 0, slide[2]);
		ResourceManager.aberrator.renderPart("Slide");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslated(0, 1.25, -3.625);
		GL11.glRotated(-45 + hammer[0], 1, 0, 0);
		GL11.glTranslated(0, -1.25, 3.625);
		ResourceManager.aberrator.renderPart("Hammer");
		GL11.glPopMatrix();

		double smokeScale = 0.5;

		GL11.glPushMatrix();
		GL11.glTranslated(0, 2, 4);
		GL11.glRotated(recoil[0], -1, 0, 0);
		GL11.glRotated(roll[2], 0, 0, -1);
		GL11.glRotated(90, 0, 1, 0);
		GL11.glScaled(smokeScale, smokeScale, smokeScale);
		this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.5D);
		GL11.glPopMatrix();

		GL11.glShadeModel(GL11.GL_FLAT);

		GL11.glPushMatrix();
		GL11.glTranslated(0, 2, 4);
		GL11.glRotated(90, 0, 1, 0);
		GL11.glRotated(90 * gun.shotRand, 1, 0, 0);
		GL11.glScaled(0.75, 0.75, 0.75);
		this.renderMuzzleFlash(gun.lastShot[0], 75, 7.5);
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslated(0, 2, -1.5);
		GL11.glScaled(0.5, 0.5, 0.5);
		this.renderFireball(gun.lastShot[0]);
		GL11.glPopMatrix();

		Minecraft.getMinecraft().getTextureManager().bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);
		net.minecraft.client.renderer.texture.TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:items/golden_sword");

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		float minU = icon.getMinU();
		float maxU = icon.getMaxU();
		float minV = icon.getMinV();
		float maxV = icon.getMaxV();
		GL11.glTranslated(0, 2, 4.5);
		GL11.glRotated(roll[2], 0, 0, -1);
		GL11.glRotated(recoil[0], -1, 0, 0);
		GL11.glRotated(equip[0], -1, 0, 0);
		GL11.glRotated(System.currentTimeMillis() / 50D % 360D, 0, 0, 1);

		float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		aimingProgress = Math.min(1F, aimingProgress * 2);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();
		GL11.glPushMatrix();
		int amount = 16;
		for(int i = 0; i < amount; i++) {
			GL11.glPushMatrix();
			GL11.glTranslated(0, -1.5 - aimingProgress, 0);
			GL11.glRotated(90 * aimingProgress, 1, 0, 0);
			GL11.glRotated(-45, 0, 0, 1);
			tessBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
			float tessNormal0X = 0F, tessNormal0Y = 1F, tessNormal0Z = 0F;
			tessBuffer.pos(-0.5, -0.5F, -0.5).tex(maxU, maxV).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
			tessBuffer.pos(0.5F, -0.5F, -0.5).tex(minU, maxV).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
			tessBuffer.pos(0.5F, 0.5F, -0.5).tex(minU, minV).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
			tessBuffer.pos(-0.5, 0.5F, -0.5).tex(maxU, minV).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
			tess.draw();
			GL11.glPopMatrix();
			GL11.glRotated(360D / amount, 0, 0, 1);
		}
		GL11.glPopMatrix();
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		GL11.glTranslated(0, -1, 4);
		double scale = 1.5D;
		GL11.glScaled(scale, scale, scale);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 2.5D;
		GL11.glScaled(scale, scale, scale);
		GL11.glRotated(25, 1, 0, 0);
		GL11.glRotated(45, 0, 1, 0);
		GL11.glTranslated(-0.5,-1, 0);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -12.5D;
		GL11.glScaled(scale, scale, scale);
		GL11.glRotated(90, 0, 1, 0);
		GL11.glTranslated(0,-1, 0.5);
	}

	@Override
	public void renderOther(ItemStack stack, NativeItemContext context) {

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0F);
		GL11.glEnable(GL11.GL_ALPHA_TEST);

		GL11.glShadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.aberrator_tex);
		ResourceManager.aberrator.renderPart("Gun");
		ResourceManager.aberrator.renderPart("Hammer");
		ResourceManager.aberrator.renderPart("Magazine");
		ResourceManager.aberrator.renderPart("Slide");
		ResourceManager.aberrator.renderPart("Sight");
		GL11.glShadeModel(GL11.GL_FLAT);

		if(isThirdPerson(context.transform)) {
			EntityLivingBase ent = context.holder;
			long shot;
			double shotRand = 0;
			if(ent == Minecraft.getMinecraft().player) {
				ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
				shot = gun.lastShot[0];
				shotRand = gun.shotRand;
			} else {
				shot = ItemRenderWeaponBase.flashMap.getOrDefault(ent, (long) -1);
				if(shot < 0) return;
			}

			GL11.glPushMatrix();
			GL11.glTranslated(0, 2, 4);
			GL11.glRotated(90, 0, 1, 0);
			GL11.glRotated(90 * shotRand, 1, 0, 0);
			GL11.glScaled(0.75, 0.75, 0.75);
			this.renderMuzzleFlash(shot, 75, 7.5);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glTranslated(0, 2, -1.5);
			GL11.glScaled(0.5, 0.5, 0.5);
			this.renderFireball(shot);
			GL11.glPopMatrix();
		}
	}

	public static void renderFireball(long lastShot) {
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();

		int flash = 150;

		if(System.currentTimeMillis() - lastShot < flash) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glPushMatrix();

			double fire = (System.currentTimeMillis() - lastShot) / (double) flash;
			double height = 5 * fire;
			double length = 10 * fire;
			double offset = 1 * fire;
			double lengthOffset = -1.125;
			Minecraft.getMinecraft().getTextureManager().bindTexture(flash_plume);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

			buffer.pos(height, -offset, 0).tex(0, 1).lightmap(240, 240).color(255, 255, 255, 255).endVertex();
			buffer.pos(-height, -offset, 0).tex(1, 1).lightmap(240, 240).color(255, 255, 255, 255).endVertex();
			buffer.pos(-height, -offset + length, -lengthOffset).tex(1, 0).lightmap(240, 240).color(255, 255, 255, 255).endVertex();
			buffer.pos(height, -offset + length, -lengthOffset).tex(0 ,0).lightmap(240, 240).color(255, 255, 255, 255).endVertex();

			buffer.pos(height, -offset, 0).tex(0, 1).lightmap(240, 240).color(255, 255, 255, 255).endVertex();
			buffer.pos(-height, -offset, 0).tex(1, 1).lightmap(240, 240).color(255, 255, 255, 255).endVertex();
			buffer.pos(-height, -offset + length, lengthOffset).tex(1, 0).lightmap(240, 240).color(255, 255, 255, 255).endVertex();
			buffer.pos(height, -offset + length, lengthOffset).tex(0 ,0).lightmap(240, 240).color(255, 255, 255, 255).endVertex();

			tess.draw();
			GL11.glPopMatrix();
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
	}
}
