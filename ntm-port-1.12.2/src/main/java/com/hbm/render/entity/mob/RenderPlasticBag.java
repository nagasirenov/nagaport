package com.hbm.render.entity.mob;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import com.hbm.render.mesh.ReloadableObjMesh;
import com.hbm.render.mesh.ObjMeshRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderPlasticBag extends Render<Entity> {
	
	// Preserve the original eager, static singleton snapshot rather than a mesh per renderer.
	private static final ReloadableObjMesh model = new ReloadableObjMesh(Minecraft.getMinecraft().getResourceManager(),
			new ResourceLocation(RefStrings.MODID, "models/mobs/plasticbag.obj"), false);
	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID, "textures/entity/plasticbag.png");

	// Called at the original PreInit registration slot, before the native factory receives RenderManager.
	public static ReloadableObjMesh getModel() {
		return model;
	}

	public RenderPlasticBag(RenderManager renderManager) {
		super(renderManager);
		this.shadowOpaque = 0.0F;
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1) {
		
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		// Preserve the original culling-state leak; do not silently restore it during the API migration.
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * f1 + 90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * f1 - 90, 0.0F, 0.0F, 1.0F);
		
		this.bindEntityTexture(entity);
		ObjMeshRenderer.drawParts(model.getMesh().parts, ObjMeshRenderer.Shading.LEGACY_FLAT, 0);
		
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return texture;
	}
}
