package com.hbm.render.model;

import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;

public class ModelArmorBase extends ModelBiped {

	public int type;
	public ModelRendererObj head;
	public ModelRendererObj body;
	public ModelRendererObj leftArm;
	public ModelRendererObj rightArm;
	public ModelRendererObj leftLeg;
	public ModelRendererObj rightLeg;
	public ModelRendererObj leftFoot;
	public ModelRendererObj rightFoot;

	public ModelArmorBase(int type) {
		this.type = type;

		// Generate null defaults to prevent major breakage from using incomplete models
		this.head = new ModelRendererObj(null);
		this.body = new ModelRendererObj(null);
		this.leftArm = new ModelRendererObj(null).setRotationPoint(5.0F, 2.0F, 0.0F);
		this.rightArm = new ModelRendererObj(null).setRotationPoint(-5.0F, 2.0F, 0.0F);
		this.leftLeg = new ModelRendererObj(null).setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightLeg = new ModelRendererObj(null).setRotationPoint(-1.9F, 12.0F, 0.0F);
		this.leftFoot = new ModelRendererObj(null).setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightFoot = new ModelRendererObj(null).setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {

		boolean calculateRotations = true;

		Render<?> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);
		if(render instanceof RenderPlayer) {
			RenderPlayer renderPlayer = (RenderPlayer) render;
			this.copyPropertiesFromBiped(renderPlayer.getMainModel());
			calculateRotations = false;
			
		} else if(render instanceof RenderBiped) {
			RenderBiped<?> renderBiped = (RenderBiped<?>) render;
			this.copyPropertiesFromBiped((ModelBiped) renderBiped.getMainModel());
			calculateRotations = false;
		}

		/// FALLBACK ///
		if(calculateRotations) {

			this.isSneak = entity.isSneaking();
			this.isRiding = entity.isRiding();

			if(this.isSneak) {
				this.rightFoot.offsetZ = this.rightLeg.offsetZ = 4.0F;
				this.leftFoot.offsetZ = this.leftLeg.offsetZ = 4.0F;
				this.rightFoot.offsetY = this.rightLeg.offsetY = -3.0F;
				this.leftFoot.offsetY = this.leftLeg.offsetY = -3.0F;
			} else {
				this.rightFoot.offsetZ = this.rightLeg.offsetZ = 0.1F;
				this.leftFoot.offsetZ = this.leftLeg.offsetZ = 0.1F;
				this.rightFoot.offsetY = this.rightLeg.offsetY = 0.0F;
				this.leftFoot.offsetY = this.leftLeg.offsetY = 0.0F;
			}
			
			this.head.rotateAngleY = netHeadYaw / (180F / (float) Math.PI);
			this.head.rotateAngleX = headPitch / (180F / (float) Math.PI);
			this.rightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
			this.leftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
			this.rightArm.rotateAngleZ = 0.0F;
			this.leftArm.rotateAngleZ = 0.0F;

			if(this.isRiding) {
				this.rightArm.rotateAngleX -= (float) Math.PI / 5F;
				this.leftArm.rotateAngleX -= (float) Math.PI / 5F;
			}

			if(heldItemWeight(this.leftArmPose) != 0) {
				this.leftArm.rotateAngleX = this.leftArm.rotateAngleX * 0.5F - ((float) Math.PI / 10F) * (float) heldItemWeight(this.leftArmPose);
			}

			if(heldItemWeight(this.rightArmPose) != 0) {
				this.rightArm.rotateAngleX = this.rightArm.rotateAngleX * 0.5F - ((float) Math.PI / 10F) * (float) heldItemWeight(this.rightArmPose);
			}

			this.rightArm.rotateAngleY = 0.0F;
			this.leftArm.rotateAngleY = 0.0F;
			float f6;
			float f7;

			if(this.swingProgress > -9990.0F) {
				f6 = this.swingProgress;
				this.body.rotateAngleY = MathHelper.sin(MathHelper.sqrt(f6) * (float) Math.PI * 2.0F) * 0.2F;
				this.rightArm.rotationPointZ = MathHelper.sin(this.body.rotateAngleY) * 5.0F;
				this.rightArm.rotationPointX = -MathHelper.cos(this.body.rotateAngleY) * 5.0F;
				this.leftArm.rotationPointZ = -MathHelper.sin(this.body.rotateAngleY) * 5.0F;
				this.leftArm.rotationPointX = MathHelper.cos(this.body.rotateAngleY) * 5.0F;
				this.rightArm.rotateAngleY += this.body.rotateAngleY;
				this.leftArm.rotateAngleY += this.body.rotateAngleY;
				this.leftArm.rotateAngleX += this.body.rotateAngleY;
				f6 = 1.0F - this.swingProgress;
				f6 *= f6;
				f6 *= f6;
				f6 = 1.0F - f6;
				f7 = MathHelper.sin(f6 * (float) Math.PI);
				float f8 = MathHelper.sin(this.swingProgress * (float) Math.PI) * -(this.head.rotateAngleX - 0.7F) * 0.75F;
				this.rightArm.rotateAngleX = (float) ((double) this.rightArm.rotateAngleX - ((double) f7 * 1.2D + (double) f8));
				this.rightArm.rotateAngleY += this.body.rotateAngleY * 2.0F;
				this.rightArm.rotateAngleZ = MathHelper.sin(this.swingProgress * (float) Math.PI) * -0.4F;
			}

			if(this.isSneak) {
				this.body.rotateAngleX = 0.5F;
				this.rightArm.rotateAngleX += 0.4F;
				this.leftArm.rotateAngleX += 0.4F;
				this.head.offsetY = 1.0F;
			} else {
				this.body.rotateAngleX = 0.0F;
				this.head.offsetY = 0.0F;
			}

			this.rightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.leftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.rightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
			this.leftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;

			if(this.rightArmPose == ArmPose.BOW_AND_ARROW || this.leftArmPose == ArmPose.BOW_AND_ARROW) {
				f6 = 0.0F;
				f7 = 0.0F;
				this.rightArm.rotateAngleZ = 0.0F;
				this.leftArm.rotateAngleZ = 0.0F;
				this.rightArm.rotateAngleY = -(0.1F - f6 * 0.6F) + this.head.rotateAngleY;
				this.leftArm.rotateAngleY = 0.1F - f6 * 0.6F + this.head.rotateAngleY + 0.4F;
				this.rightArm.rotateAngleX = -((float) Math.PI / 2F) + this.head.rotateAngleX;
				this.leftArm.rotateAngleX = -((float) Math.PI / 2F) + this.head.rotateAngleX;
				this.rightArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
				this.leftArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
				this.rightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
				this.leftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
				this.rightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
				this.leftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
			}
		}
	}

	/** Original fallback weights: holding = 1, blocking = 3; bow aiming is applied below. */
	private static int heldItemWeight(ArmPose pose) {
		return pose == ArmPose.EMPTY ? 0 : pose == ArmPose.BLOCK ? 3 : 1;
	}

	protected static void bindTexture(ResourceLocation location) {
		Minecraft.getMinecraft().renderEngine.bindTexture(location);
	}

	private void copyPropertiesFromBiped(ModelBiped modelBiped) {

		this.head.copyRotationFrom(modelBiped.bipedHead);
		this.body.copyRotationFrom(modelBiped.bipedBody);
		this.leftArm.copyRotationFrom(modelBiped.bipedLeftArm);
		this.rightArm.copyRotationFrom(modelBiped.bipedRightArm);
		this.leftLeg.copyRotationFrom(modelBiped.bipedLeftLeg);
		this.rightLeg.copyRotationFrom(modelBiped.bipedRightLeg);
		this.leftFoot.copyRotationFrom(modelBiped.bipedLeftLeg);
		this.rightFoot.copyRotationFrom(modelBiped.bipedRightLeg);
		// compat crap
		this.leftArmPose = modelBiped.leftArmPose;
		this.rightArmPose = modelBiped.rightArmPose;
		this.isSneak = modelBiped.isSneak;
		this.isRiding = modelBiped.isRiding;
	}
}
