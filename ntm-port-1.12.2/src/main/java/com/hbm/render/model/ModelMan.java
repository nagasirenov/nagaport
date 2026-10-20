package com.hbm.render.model;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;

public class ModelMan extends ModelArmorBase {

	public ModelMan() {
		super(0);

		this.head = new ModelRendererObj(ResourceManager.player_manly_af, "Head");
		this.body = new ModelRendererObj(ResourceManager.player_manly_af, "Body");
		this.leftArm = new ModelRendererObj(ResourceManager.player_manly_af, "LeftArm").setRotationPoint(5.0F, 2.0F, 0.0F);
		this.rightArm = new ModelRendererObj(ResourceManager.player_manly_af, "RightArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
		this.leftLeg = new ModelRendererObj(ResourceManager.player_manly_af, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightLeg = new ModelRendererObj(ResourceManager.player_manly_af, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);

		GL11.glPushMatrix();
		GlStateManager.enableLighting();
		bindTexture(ResourceManager.player_manly_tex);
		this.head.render(scaleFactor);
		this.body.render(scaleFactor);
		this.leftArm.render(scaleFactor);
		this.rightArm.render(scaleFactor);
		this.leftLeg.render(scaleFactor);
		this.rightLeg.render(scaleFactor);
		GL11.glPopMatrix();
	}

	public void render(Entity par1Entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, RenderPlayer render) {

		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, par1Entity);

		this.head.copyRotationFrom(render.getMainModel().bipedHead);
		this.body.copyRotationFrom(render.getMainModel().bipedBody);
		this.leftArm.copyRotationFrom(render.getMainModel().bipedLeftArm);
		this.rightArm.copyRotationFrom(render.getMainModel().bipedRightArm);
		this.leftLeg.copyRotationFrom(render.getMainModel().bipedLeftLeg);
		this.rightLeg.copyRotationFrom(render.getMainModel().bipedRightLeg);

		GL11.glPushMatrix();
		GlStateManager.enableLighting();
		bindTexture(ResourceManager.player_manly_tex);
		this.head.render(scaleFactor);
		this.body.render(scaleFactor);
		this.leftArm.render(scaleFactor);
		this.rightArm.render(scaleFactor);
		this.leftLeg.render(scaleFactor);
		this.rightLeg.render(scaleFactor);
		GL11.glPopMatrix();
	}
}
