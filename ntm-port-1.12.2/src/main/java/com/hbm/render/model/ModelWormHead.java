package com.hbm.render.model;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;
import com.hbm.render.mesh.ReloadableObjMesh;
import com.hbm.render.mesh.ObjMeshRenderer;

public class ModelWormHead extends ModelBase {

	// Plain Forge 1.7.10 WavefrontObject used flat normals and a 0.0005 UV inset, not HFR smoothing.
	// Keep eager static resource sampling; this model did not subscribe to HFR reloads.
	public static final ReloadableObjMesh head = new ReloadableObjMesh(Minecraft.getMinecraft().getResourceManager(),
			new ResourceLocation(RefStrings.MODID, "models/mobs/bot_prime_head.obj"), false);

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

		super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

		GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * scaleFactor - 90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * scaleFactor - 90, 0.0F, 0.0F, 1.0F);

		ObjMeshRenderer.drawParts(head.getMesh().parts, ObjMeshRenderer.Shading.LEGACY_FLAT, 0);
	}
}
