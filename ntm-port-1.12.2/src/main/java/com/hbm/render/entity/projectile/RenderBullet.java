package com.hbm.render.entity.projectile;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.entity.projectile.IBulletBase;
import com.hbm.handler.BulletConfiguration;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.model.ModelBullet;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.render.util.RenderSparks;
import com.hbm.render.util.NativeMachineItemRenderer;
import com.hbm.util.Tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

/** State writes also update the native cache, but remain unconditional for
 * existing raw-GL callers. Do not remove the raw write before its cache update. */
@Deprecated
public class RenderBullet extends Render<EntityBulletBaseNT> {

	private static final VertexFormat TRAIL_FORMAT = new VertexFormat()
			.addElement(DefaultVertexFormats.POSITION_3F).addElement(DefaultVertexFormats.COLOR_4UB)
			.addElement(DefaultVertexFormats.NORMAL_3B).addElement(DefaultVertexFormats.PADDING_1B);
	private final NativeMachineItemRenderer items = new NativeMachineItemRenderer();

	private final ModelBullet bullet;

	public RenderBullet(RenderManager renderManager) {
		super(renderManager);
		bullet = new ModelBullet();
	}

	@Override
	public void doRender(EntityBulletBaseNT bullet, double x, double y, double z, float f0, float f1) {
		
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		GL11.glRotatef(bullet.prevRotationYaw + (bullet.rotationYaw - bullet.prevRotationYaw) * f1 - 90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(bullet.prevRotationPitch + (bullet.rotationPitch - bullet.prevRotationPitch) * f1 + 180, 0.0F, 0.0F, 1.0F);
		GL11.glScalef(1.5F, 1.5F, 1.5F);

		int style = bullet.getRenderStyle();
		int trail = bullet.getRenderTrail();
		
		if(style != BulletConfiguration.STYLE_BLADE)
			GL11.glRotatef(new Random(bullet.getEntityId()).nextInt(90) - 45, 1.0F, 0.0F, 0.0F);
		
		GL11.glEnable(GL11.GL_CULL_FACE); GlStateManager.enableCull();
		
		switch(style) {
			case BulletConfiguration.STYLE_NONE: break;
			case BulletConfiguration.STYLE_NORMAL: renderBullet(trail); break;
			case BulletConfiguration.STYLE_PISTOL: renderPistol(trail); break;
			case BulletConfiguration.STYLE_BOLT: renderDart(trail, bullet.getEntityId()); break;
			case BulletConfiguration.STYLE_FLECHETTE: renderFlechette();; break;
			case BulletConfiguration.STYLE_FOLLY: renderBullet(trail); break;
			case BulletConfiguration.STYLE_PELLET: renderBuckshot(); break;
			case BulletConfiguration.STYLE_ROCKET: renderRocket(trail); break;
			case BulletConfiguration.STYLE_GRENADE: renderGrenade(trail); break;
			case BulletConfiguration.STYLE_ORB: renderOrb(trail); break;
			case BulletConfiguration.STYLE_METEOR: renderMeteor(trail); break;
			case BulletConfiguration.STYLE_APDS: renderAPDS(); break;
			case BulletConfiguration.STYLE_BLADE: renderBlade(); break;
			case BulletConfiguration.STYLE_TAU: renderTau(bullet, trail, f1); break;
			case BulletConfiguration.STYLE_LEADBURSTER: renderLeadburster(bullet, f1); break;
			case BulletConfiguration.STYLE_WAR: renderTracerV2(); break;

			default: renderBullet(trail); break;
		}
		
		
		GL11.glPopMatrix();
	}

	private void renderBullet(int type) {

		if (type == 2) {
			bindTexture(new ResourceLocation(RefStrings.MODID + ":textures/models/emplacer.png"));
			bullet.renderAll(0.0625F);
		} else if (type == 1) {
			bindTexture(new ResourceLocation(RefStrings.MODID + ":textures/models/tau.png"));
			bullet.renderAll(0.0625F);
		} else if (type == 0) {
			
			GL11.glScaled(0.5, 0.5, 0.5);
			GL11.glRotated(90, 0, 0, 1);
			GL11.glRotated(90, 0, 1, 0);

			GL11.glShadeModel(GL11.GL_SMOOTH); GlStateManager.shadeModel(GL11.GL_SMOOTH);
			bindTexture(ResourceManager.bullet_rifle_tex);
			ResourceManager.projectiles.renderPart("BulletRifle");
			GL11.glShadeModel(GL11.GL_FLAT); GlStateManager.shadeModel(GL11.GL_FLAT);
		}
		
	}
	
	private void renderPistol(int type) {
		
		GL11.glScaled(0.5, 0.5, 0.5);
		GL11.glRotated(90, 0, 0, 1);
		GL11.glRotated(90, 0, 1, 0);

		GL11.glShadeModel(GL11.GL_SMOOTH); GlStateManager.shadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.bullet_pistol_tex);
		ResourceManager.projectiles.renderPart("BulletPistol");
		GL11.glShadeModel(GL11.GL_FLAT); GlStateManager.shadeModel(GL11.GL_FLAT);
		
	}
	
	private void renderBuckshot() {
		
		GL11.glScaled(0.5, 0.5, 0.5);
		GL11.glRotated(90, 0, 0, 1);
		GL11.glRotated(90, 0, 1, 0);
		
		GL11.glShadeModel(GL11.GL_SMOOTH); GlStateManager.shadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.buckshot_tex);
		ResourceManager.projectiles.renderPart("Buckshot");
		GL11.glShadeModel(GL11.GL_FLAT); GlStateManager.shadeModel(GL11.GL_FLAT);
	}
	
	private void renderRocket(int type) {
		
		GL11.glScaled(0.5, 0.5, 0.5);
		GL11.glRotated(90, 0, 0, 1);
		GL11.glRotated(90, 0, 1, 0);

		GL11.glShadeModel(GL11.GL_SMOOTH); GlStateManager.shadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.rocket_tex);
		ResourceManager.projectiles.renderPart("Rocket");
		GL11.glShadeModel(GL11.GL_FLAT); GlStateManager.shadeModel(GL11.GL_FLAT);
	}
	
	private void renderGrenade(int type) {

		GL11.glScalef(0.25F, 0.25F, 0.25F);
		GL11.glRotated(90, 0, 0, 1);
		GL11.glRotated(90, 0, 1, 0);

		GL11.glShadeModel(GL11.GL_SMOOTH); GlStateManager.shadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.grenade_tex);
		ResourceManager.projectiles.renderPart("Grenade");
		GL11.glShadeModel(GL11.GL_FLAT); GlStateManager.shadeModel(GL11.GL_FLAT);
	}
	
	private void renderOrb(int type) {

        GL11.glEnable(GL11.GL_CULL_FACE); GlStateManager.enableCull();
        GL11.glDisable(GL11.GL_LIGHTING); GlStateManager.disableLighting();
        GL11.glShadeModel(GL11.GL_SMOOTH); GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND); GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDepthMask(false); GlStateManager.depthMask(false);
		
		switch(type) {
		case 0:
			bindTexture(ResourceManager.tom_flame_tex);
			ResourceManager.sphere_uv.renderAll();
			GL11.glScalef(0.3F, 0.3F, 0.3F);
			ResourceManager.sphere_uv.renderAll();
			GL11.glScalef(1F/0.3F, 1F/0.3F, 1F/0.3F);
			for(int i = 0; i < 5; i++)
				RenderSparks.renderSpark((int) (System.currentTimeMillis() / 100 + 100 * i), 0, 0, 0, 0.5F, 2, 2, 0x8080FF, 0xFFFFFF);
			break;
			
		case 1:
			GL11.glScalef(0.5F, 0.5F, 0.5F);
			GL11.glDisable(GL11.GL_TEXTURE_2D); GlStateManager.disableTexture2D();
			GL11.glColor4f(0.5F, 0.0F, 0.0F, 0.5F); GlStateManager.color(0.5F, 0.0F, 0.0F, 0.5F);
			ResourceManager.sphere_uv.renderAll();
			GL11.glScalef(0.75F, 0.75F, 0.75F);
			ResourceManager.sphere_uv.renderAll();
			GL11.glScalef(1F/0.75F, 1F/0.75F, 1F/0.75F);
			GL11.glEnable(GL11.GL_TEXTURE_2D); GlStateManager.enableTexture2D();
			for(int i = 0; i < 3; i++)
				RenderSparks.renderSpark((int) (System.currentTimeMillis() / 100 + 100 * i), 0, 0, 0, 1F, 2, 3, 0xFF0000, 0xFF8080);
			break;
		}
		
		GL11.glDisable(GL11.GL_BLEND); GlStateManager.disableBlend();
        GL11.glEnable(GL11.GL_LIGHTING); GlStateManager.enableLighting();
        GL11.glDepthMask(true); GlStateManager.depthMask(true);

	}
	
	private void renderMeteor(int type) {

        GL11.glEnable(GL11.GL_CULL_FACE); GlStateManager.enableCull();
        GL11.glDisable(GL11.GL_LIGHTING); GlStateManager.disableLighting();

		switch(type) {
		case 0:
			bindTexture(new ResourceLocation(RefStrings.MODID + ":textures/blocks/block_meteor_molten.png")); break;
		case 1:
			bindTexture(new ResourceLocation("textures/blocks/obsidian.png")); break;
		}
		
		ResourceManager.meteor.renderAll();
		
        GL11.glEnable(GL11.GL_LIGHTING); GlStateManager.enableLighting();
	}
	
	private void renderFlechette() {
		
		GL11.glScaled(0.5, 0.5, 0.5);
		GL11.glRotated(90, 0, 0, 1);
		GL11.glRotated(90, 0, 1, 0);
		
		GL11.glShadeModel(GL11.GL_SMOOTH); GlStateManager.shadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.flechette_tex);
		ResourceManager.projectiles.renderPart("Flechette");
		GL11.glShadeModel(GL11.GL_FLAT); GlStateManager.shadeModel(GL11.GL_FLAT);
	}
	private void renderTracerV2() {
		
		GL11.glScaled(0.5, 0.5, 0.5);
		GL11.glRotated(90, 0, 0, 1);
		GL11.glRotated(90, 0, 1, 0);
		
		GL11.glShadeModel(GL11.GL_SMOOTH); GlStateManager.shadeModel(GL11.GL_SMOOTH);
		BeamPronter.prontBeam(new Vec3d(0, 5, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0xFF9000, 0xFF9000, 0, 1, 0F, 6, (float)0.2 * 0.2F, 256);
		GL11.glShadeModel(GL11.GL_FLAT); GlStateManager.shadeModel(GL11.GL_FLAT);
	}
	
	private void renderAPDS() {
		
		GL11.glScaled(2, 2, 2);
		GL11.glRotated(90, 0, 0, 1);
		GL11.glRotated(90, 0, 1, 0);
		
		GL11.glShadeModel(GL11.GL_SMOOTH); GlStateManager.shadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.flechette_tex);
		ResourceManager.projectiles.renderPart("Flechette");
		GL11.glShadeModel(GL11.GL_FLAT); GlStateManager.shadeModel(GL11.GL_FLAT);
	}
	
	private void renderDart(int style, int eID) {
		
		float red = 1F;
		float green = 1F;
		float blue = 1F;
		
		switch(style) {
		case BulletConfiguration.BOLT_LASER: red = 1F; green = 0F; blue = 0F; break;
		case BulletConfiguration.BOLT_NIGHTMARE: red = 1F; green = 1F; blue = 0F; break;
		case BulletConfiguration.BOLT_LACUNAE: red = 0.25F; green = 0F; blue = 0.75F; break;
		case BulletConfiguration.BOLT_WORM: red = 0F; green = 1F; blue = 0F; break;
		case BulletConfiguration.BOLT_GLASS_CYAN: red = 0F; green = 1F; blue = 1F; break;
		case BulletConfiguration.BOLT_GLASS_BLUE: red = 0F; green = 0F; blue = 1F; break;
		
		case BulletConfiguration.BOLT_ZOMG:
			Random rand = new Random(eID * eID);
			red = rand.nextInt(2) * 0.6F;
			green = rand.nextInt(2) * 0.6F;
			blue = rand.nextInt(2) * 0.6F;
			break;
		}
		
		GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D); GlStateManager.disableTexture2D();
        GL11.glDisable(GL11.GL_CULL_FACE); GlStateManager.disableCull();
        GL11.glDisable(GL11.GL_LIGHTING); GlStateManager.disableLighting();
        GL11.glShadeModel(GL11.GL_SMOOTH); GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND); GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDepthMask(false); GlStateManager.depthMask(false);

        GL11.glScalef(1F/4F, 1F/8F, 1F/8F);
        GL11.glScalef(-1, 1, 1);

        GL11.glScalef(2, 2, 2);
        
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();
		
		//front
		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(6, 0, 0).color(red, green, blue, 1F).endVertex();
		buffer.pos(3, -1, -1).color(red, green, blue, 0F).endVertex();
		buffer.pos(3, 1, -1).color(red, green, blue, 0F).endVertex();
		tess.draw();

		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(3, -1, 1).color(red, green, blue, 0F).endVertex();
		buffer.pos(6, 0, 0).color(red, green, blue, 1F).endVertex();
		buffer.pos(3, 1, 1).color(red, green, blue, 0F).endVertex();
		tess.draw();

		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(3, -1, -1).color(red, green, blue, 0F).endVertex();
		buffer.pos(6, 0, 0).color(red, green, blue, 1F).endVertex();
		buffer.pos(3, -1, 1).color(red, green, blue, 0F).endVertex();
		tess.draw();

		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(6, 0, 0).color(red, green, blue, 1F).endVertex();
		buffer.pos(3, 1, -1).color(red, green, blue, 0F).endVertex();
		buffer.pos(3, 1, 1).color(red, green, blue, 0F).endVertex();
		tess.draw();
		
		//mid
		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(6, 0, 0).color(red, green, blue, 1F).endVertex();
		buffer.pos(4, -0.5, -0.5).color(red, green, blue, 1F).endVertex();
		buffer.pos(4, 0.5, -0.5).color(red, green, blue, 1F).endVertex();
		tess.draw();

		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(4, -0.5, 0.5).color(red, green, blue, 1F).endVertex();
		buffer.pos(6, 0, 0).color(red, green, blue, 1F).endVertex();
		buffer.pos(4, 0.5, 0.5).color(red, green, blue, 1F).endVertex();
		tess.draw();

		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(4, -0.5, -0.5).color(red, green, blue, 1F).endVertex();
		buffer.pos(6, 0, 0).color(red, green, blue, 1F).endVertex();
		buffer.pos(4, -0.5, 0.5).color(red, green, blue, 1F).endVertex();
		tess.draw();

		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(6, 0, 0).color(red, green, blue, 1F).endVertex();
		buffer.pos(4, 0.5, -0.5).color(red, green, blue, 1F).endVertex();
		buffer.pos(4, 0.5, 0.5).color(red, green, blue, 1F).endVertex();
		tess.draw();
		
		//tail
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(4, 0.5, -0.5).color(red, green, blue, 1F).endVertex();
		buffer.pos(4, 0.5, 0.5).color(red, green, blue, 1F).endVertex();
		buffer.pos(0, 0.5, 0.5).color(red, green, blue, 0F).endVertex();
		buffer.pos(0, 0.5, -0.5).color(red, green, blue, 0F).endVertex();
		tess.draw();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(4, -0.5, -0.5).color(red, green, blue, 1F).endVertex();
		buffer.pos(4, -0.5, 0.5).color(red, green, blue, 1F).endVertex();
		buffer.pos(0, -0.5, 0.5).color(red, green, blue, 0F).endVertex();
		buffer.pos(0, -0.5, -0.5).color(red, green, blue, 0F).endVertex();
		tess.draw();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(4, -0.5, 0.5).color(red, green, blue, 1F).endVertex();
		buffer.pos(4, 0.5, 0.5).color(red, green, blue, 1F).endVertex();
		buffer.pos(0, 0.5, 0.5).color(red, green, blue, 0F).endVertex();
		buffer.pos(0, -0.5, 0.5).color(red, green, blue, 0F).endVertex();
		tess.draw();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(4, -0.5, -0.5).color(red, green, blue, 1F).endVertex();
		buffer.pos(4, 0.5, -0.5).color(red, green, blue, 1F).endVertex();
		buffer.pos(0, 0.5, -0.5).color(red, green, blue, 0F).endVertex();
		buffer.pos(0, -0.5, -0.5).color(red, green, blue, 0F).endVertex();
		tess.draw();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D); GlStateManager.enableTexture2D();
		GL11.glDisable(GL11.GL_BLEND); GlStateManager.disableBlend();
		GL11.glEnable(GL11.GL_LIGHTING); GlStateManager.enableLighting();
		GL11.glEnable(GL11.GL_CULL_FACE); GlStateManager.enableCull();
		GL11.glDepthMask(true); GlStateManager.depthMask(true);
		
		GL11.glPopMatrix();
	}
	
	private void renderBlade() {
		GL11.glPushMatrix();
		
		EntityItem dummy = new EntityItem(Minecraft.getMinecraft().world, 0, 0, 0, new ItemStack(ModItems.blade_titanium));
		dummy.getItem().setCount(1);
		dummy.hoverStart = 0.0F;

		GL11.glRotated(90, 0, 0, 1);
		GL11.glTranslated(0, 0.5, 0);
		GL11.glRotated(System.currentTimeMillis() % 360, 1, 0, 0);
		GL11.glTranslated(0, -0.5, 0);
		GL11.glRotated(90, 0, 1, 0);
		GL11.glScaled(1, 2, 1);
		items.renderFrame(dummy, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F,
				NativeMachineItemRenderer.Display.ENTITY_FRAME);
		
		GL11.glPopMatrix();
	}

	private void renderTau(Entity bullet, int trail, float interp) {
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		
		float scale = 0.125F;

		double pX = bullet.prevPosX + (bullet.posX - bullet.prevPosX) * interp;
		double pY = bullet.prevPosY + (bullet.posY - bullet.prevPosY) * interp;
		double pZ = bullet.prevPosZ + (bullet.posZ - bullet.prevPosZ) * interp;
		
		IBulletBase iface = (IBulletBase) bullet;
		
		if(iface.prevY() == 0) {
			iface.prevX(pX);
			iface.prevY(pY);
			iface.prevZ(pZ);
		}
		
		double deltaX = iface.prevX() - pX;
		double deltaY = iface.prevY() - pY;
		double deltaZ = iface.prevZ() - pZ;
		
		EntityPlayer player = Minecraft.getMinecraft().player;
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;

		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glTranslated(pX - dX, pY - dY, pZ - dZ);

		float r = 1F;
		float g = 0.5F;
		float b = 0F;
		
		if(trail == 1) {
			r = 1;
			g = 1;
			b = 1;
		}
		
		for(Pair<Vec3d, Double> pair : iface.nodes()) {
			double mult = 1D;
			pair.key = pair.getKey().add(deltaX * mult, deltaY * mult, deltaZ * mult);
		}
		
		buffer.begin(GL11.GL_QUADS, TRAIL_FORMAT);

		for(int i = 0; i < iface.nodes().size() - 1; i++) {
			final Pair<Vec3d, Double> node = iface.nodes().get(i), past = iface.nodes().get(i + 1);
			final Vec3d nodeLoc = node.getKey(), pastLoc = past.getKey();
			float nodeAlpha = node.getValue().floatValue();
			float pastAlpha = past.getValue().floatValue();
			
			double timeAlpha = Math.max(2D - bullet.ticksExisted * 0.2, 0D);
			nodeAlpha *= timeAlpha;
			pastAlpha *= timeAlpha;
			float outerAlpha = 0.25F;
			
			if(nodeAlpha == 0 && pastAlpha == 0) {
				break;
			}

			trailVertex(buffer, nodeLoc.x, nodeLoc.y, nodeLoc.z, r, g, b, nodeAlpha);
			trailVertex(buffer, nodeLoc.x, nodeLoc.y + scale, nodeLoc.z, r, g, b, nodeAlpha * outerAlpha);
				trailVertex(buffer, pastLoc.x, pastLoc.y + scale, pastLoc.z, r, g, b, pastAlpha * outerAlpha);
				trailVertex(buffer, pastLoc.x, pastLoc.y, pastLoc.z, r, g, b, pastAlpha);

			trailVertex(buffer, nodeLoc.x, nodeLoc.y, nodeLoc.z, r, g, b, nodeAlpha);
			trailVertex(buffer, nodeLoc.x, nodeLoc.y - scale, nodeLoc.z, r, g, b, nodeAlpha * outerAlpha);
			trailVertex(buffer, pastLoc.x, pastLoc.y - scale, pastLoc.z, r, g, b, pastAlpha * outerAlpha);
			trailVertex(buffer, pastLoc.x, pastLoc.y, pastLoc.z, r, g, b, pastAlpha);
		}
		GL11.glColor3f(1F, 1F, 1F); GlStateManager.color(1F, 1F, 1F);
		GL11.glDepthMask(true); GlStateManager.depthMask(true);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0F); GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
		GL11.glEnable(GL11.GL_BLEND); GlStateManager.enableBlend();
		GL11.glDisable(GL11.GL_TEXTURE_2D); GlStateManager.disableTexture2D();
		GL11.glDisable(GL11.GL_CULL_FACE); GlStateManager.disableCull();
		GL11.glShadeModel(GL11.GL_SMOOTH); GlStateManager.shadeModel(GL11.GL_SMOOTH);
		tessellator.draw();
		GL11.glShadeModel(GL11.GL_FLAT); GlStateManager.shadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_CULL_FACE); GlStateManager.enableCull();
		GL11.glEnable(GL11.GL_TEXTURE_2D); GlStateManager.enableTexture2D();
		GL11.glDisable(GL11.GL_BLEND); GlStateManager.disableBlend();
		GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.1F); GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.1F);
		
		iface.prevX(pX);
		iface.prevY(pY);
		iface.prevZ(pZ);
	}

	private static void trailVertex(BufferBuilder buffer, double x, double y, double z, float r, float g, float b, float alpha) {
		buffer.pos(x, y, z).color((int) (r * 255F), (int) (g * 255F), (int) (b * 255F),
				Math.max(0, Math.min(255, (int) (alpha * 255F)))).normal(0F, 1F, 0F).endVertex();
	}

	private void renderLeadburster(Entity bullet, float interp) {
		EntityBulletBaseNT bulletnt = (EntityBulletBaseNT) bullet;
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_LIGHTING); GlStateManager.enableLighting();
		GL11.glEnable(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL); GlStateManager.enableRescaleNormal();
		GL11.glRotated(90, 0, 0, -1);
		double scale = 0.05;
		GL11.glScaled(scale, scale, scale);
		bindTexture(ResourceManager.leadburster_tex);
		ResourceManager.leadburster.renderPart("Based");
		if(bulletnt.getStuckIn() != -1) {
			GL11.glRotated((bullet.ticksExisted + interp) * -18, 0, 1, 0);
		}
		ResourceManager.leadburster.renderPart("Based.001");
		ResourceManager.leadburster.renderPart("Backlight");
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBulletBaseNT p_110775_1_) {
		return new ResourceLocation(RefStrings.MODID + ":textures/models/bullet.png");
	}

}
