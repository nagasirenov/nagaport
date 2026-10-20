package com.hbm.render.entity.effect;

import org.lwjgl.opengl.GL11;
import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import com.hbm.render.mesh.ReloadableObjMesh;
import com.hbm.render.mesh.ObjMeshRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderEMPBlast extends Render<EntityEMPBlast> {

	private static final ResourceLocation ringModelRL = new ResourceLocation(/*"/assets/" + */RefStrings.MODID, "models/Ring.obj");
	private final ReloadableObjMesh ringModel;
    private final ResourceLocation ringTexture;
    
    public RenderEMPBlast(RenderManager renderManager) {
        this(renderManager, loadRingModel());
    }

    // PreInit must sample the model before RenderManager exists; its native factory supplies that manager later.
    // The ordinary constructor still eagerly loads one fresh mesh for each renderer, as the original did.
    public static ReloadableObjMesh loadRingModel() {
        return new ReloadableObjMesh(Minecraft.getMinecraft().getResourceManager(), ringModelRL, false);
    }

    public RenderEMPBlast(RenderManager renderManager, ReloadableObjMesh ringModel) {
        super(renderManager);
        this.ringModel = ringModel;
        ringTexture = new ResourceLocation(RefStrings.MODID, "textures/models/EMPBlast.png");
    }

	@Override
	public void doRender(EntityEMPBlast p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
		this.renderRing(p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
	}
	
	public void renderRing(EntityEMPBlast p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {

		GL11.glPushMatrix();
        GL11.glTranslatef((float)p_76986_2_, (float)p_76986_4_, (float)p_76986_6_);
        // Preserve the original state behavior: lighting and culling remain disabled after this draw.
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glScalef(p_76986_1_.scale, 1F, p_76986_1_.scale);
        
        bindTexture(ringTexture);
        // Ring.obj is a single fully attributed triangle part; retain plain Forge flat normals/UV inset.
        ObjMeshRenderer.drawParts(ringModel.getMesh().parts, ObjMeshRenderer.Shading.LEGACY_FLAT, 0);
        GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityEMPBlast p_110775_1_) {
		return null;
	}
}
