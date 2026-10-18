package com.hbm.render.entity.item;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.cart.EntityMinecartBogie;
import com.hbm.entity.item.EntityMagnusCartus;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class RenderMagnusCartus extends Render {

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1) {
		EntityMagnusCartus cart = (EntityMagnusCartus) entity;
		Entity e = entity.worldObj.getEntityByID(cart.getBogieID());
		EntityMinecartBogie bogie = null;
		
		if(e instanceof EntityMinecartBogie) {
			bogie = (EntityMinecartBogie) e;
		}
		
		if(bogie == null)
			return;
		
		Vec3d vec = new Vec3d(cart.posX - bogie.posX, cart.posY - bogie.posY, cart.posZ - bogie.posZ);
		
		GL11.glPushMatrix();
		GL11.glTranslated(x - vec.x * 0.5, y - vec.y * 0.5, z - vec.z * 0.5);
		GL11.glScaled(1.2, 1.2, 1.2);
		GL11.glRotated(Math.atan2(vec.x, vec.z) / Math.PI * 180D - 90, 0, 1, 0);
		this.bindTexture(this.getEntityTexture(cart));
		ResourceManager.b29.renderAll();
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ResourceManager.b29_0_tex;
	}
}
