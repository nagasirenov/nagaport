package com.hbm.render.item.weapon.sedna;

import com.hbm.render.item.nativeitem.context.NativeItemContext;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.Project;

import com.hbm.config.ClientConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.SmokeNode;
import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import com.hbm.render.item.nativeitem.context.ContextItemRenderer;

public abstract class ItemRenderWeaponBase extends ContextItemRenderer {

    private static final net.minecraft.client.renderer.vertex.VertexFormat WEAPON_FLASH_FORMAT = new net.minecraft.client.renderer.vertex.VertexFormat()
            .addElement(net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_3F)
            .addElement(net.minecraft.client.renderer.vertex.DefaultVertexFormats.COLOR_4UB)
            .addElement(net.minecraft.client.renderer.vertex.DefaultVertexFormats.TEX_2F)
            .addElement(net.minecraft.client.renderer.vertex.DefaultVertexFormats.TEX_2S)
            .addElement(net.minecraft.client.renderer.vertex.DefaultVertexFormats.NORMAL_3B)
            .addElement(net.minecraft.client.renderer.vertex.DefaultVertexFormats.PADDING_1B);
    private static final net.minecraft.client.renderer.vertex.VertexFormat WEAPON_SMOKE_FORMAT = new net.minecraft.client.renderer.vertex.VertexFormat()
            .addElement(net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_3F)
            .addElement(net.minecraft.client.renderer.vertex.DefaultVertexFormats.COLOR_4UB)
            .addElement(net.minecraft.client.renderer.vertex.DefaultVertexFormats.NORMAL_3B)
            .addElement(net.minecraft.client.renderer.vertex.DefaultVertexFormats.PADDING_1B);

	public static final ResourceLocation flash_plume = new ResourceLocation(RefStrings.MODID, "textures/models/weapons/lilmac_plume.png");
	public static final ResourceLocation laser_flash = new ResourceLocation(RefStrings.MODID, "textures/models/weapons/laser_flash.png");
	
	public static float interp;
	public static HashMap<EntityLivingBase, Long> flashMap = new HashMap<EntityLivingBase, Long>();

	public boolean isAkimbo(EntityLivingBase entity) { return false; }
	public boolean isLeftHanded() { return false; }

    @Override public final javax.vecmath.Matrix4f localTransform(ItemStack stack, NativeItemContext context) {
        // Caller compensation belongs to the native baked model. Setup methods
        // execute in the actual draw, not in a matrix-capture approximation.
        return new com.hbm.render.item.nativeitem.ItemTransformMatrix().build();
    }
    protected static boolean isThirdPerson(TransformType type) {
        return type == TransformType.THIRD_PERSON_LEFT_HAND || type == TransformType.THIRD_PERSON_RIGHT_HAND;
    }
    @Override protected void draw(ItemStack item, NativeItemContext context) {
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_CULL_FACE);
            switch(context.transform) {
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND: setupFirstPerson(item); renderFirstPerson(item); break;
            case THIRD_PERSON_LEFT_HAND:
            case THIRD_PERSON_RIGHT_HAND:
                if(!isLeftHanded()) { setupThirdPerson(item); renderEquipped(item, context); }
                break;
            case GUI: setupInv(item); renderInv(item); break;
            case GROUND:
            case FIXED:
            case HEAD:
            case NONE: setupEntity(item); renderEntity(item); break;
            default: throw new IllegalArgumentException("Unsupported native weapon transform: " + context.transform);
            }
        } finally { GL11.glPopMatrix(); }
    }

	// Explicit native holder, including remote players, is supplied by item overrides.
	public void renderEquipped(ItemStack stack, NativeItemContext context) { renderOther(stack, context); }
	public void renderEquippedAkimbo(ItemStack stack, EntityLivingBase entity) { renderOther(stack, new NativeItemContext(TransformType.THIRD_PERSON_LEFT_HAND, entity)); }
	public void renderInv(ItemStack stack) { renderOther(stack, new NativeItemContext(TransformType.GUI, null)); }
	public void renderEntity(ItemStack stack) { renderOther(stack, new NativeItemContext(TransformType.GROUND, null)); }

    /** Draw one real cached native hand, preserving the original custom projection
     * and animation body. The caller owns depth clearing and the single overlay. */
    public void renderNativeHand(ItemStack stack, float partialTicks, net.minecraft.util.EnumHand hand,
            net.minecraft.util.EnumHandSide side) {
        float previousInterpolation = interp;
        interp = partialTicks;
        try(com.hbm.render.item.nativeitem.context.NativeProjectionScope scope = new com.hbm.render.item.nativeitem.context.NativeProjectionScope()) {
            applyHandProjection(stack, partialTicks, hand);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            if(side == net.minecraft.util.EnumHandSide.LEFT) {
                GL11.glScalef(-1, 1, 1);
                net.minecraft.client.renderer.GlStateManager.cullFace(net.minecraft.client.renderer.GlStateManager.CullFace.FRONT);
            }
            setupTransformsAndRender(stack, side == net.minecraft.util.EnumHandSide.LEFT
                    ? TransformType.FIRST_PERSON_LEFT_HAND : TransformType.FIRST_PERSON_RIGHT_HAND);
        } finally { interp = previousInterpolation; }
    }
    public void applyHandProjection(ItemStack stack, float partialTicks, net.minecraft.util.EnumHand hand) {
        Minecraft mc = Minecraft.getMinecraft();
        float farPlaneDistance = mc.gameSettings.renderDistanceChunks * 16;
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        Project.gluPerspective(getFOVModifier(stack, partialTicks, ClientConfig.GUN_MODEL_FOV.get(), hand),
                (float)mc.displayWidth / (float)mc.displayHeight, .05F, farPlaneDistance * 2);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

	private float getFOVModifier(ItemStack stack, float interp, boolean useFOVSetting, net.minecraft.util.EnumHand hand) {
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityLivingBase entityplayer = (EntityLivingBase) mc.getRenderViewEntity();
		float fov = getBaseFOV(hand == net.minecraft.util.EnumHand.MAIN_HAND ? entityplayer.getHeldItemMainhand() : stack);

		if(useFOVSetting) fov = mc.gameSettings.fovSetting;

		if(entityplayer.getHealth() <= 0.0F) {
			float f2 = (float) entityplayer.deathTime + interp;
			fov /= (1.0F - 500.0F / (f2 + 500.0F)) * 2.0F + 1.0F;
		}

		net.minecraft.block.state.IBlockState block = ActiveRenderInfo.getBlockStateAtEntityViewpoint(mc.world, entityplayer, interp);
		if(block.getMaterial() == Material.WATER) fov = fov * 60.0F / 70.0F;

		return fov;
	}

	protected float getBaseFOV(ItemStack stack) { return 70F; }
	public float getViewFOV(ItemStack stack, float fov) { return  fov; }
	protected float getSwayMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 0.1F : 0.5F; }
	protected float getSwayPeriod(ItemStack stack) { return 0.75F; }
	protected float getTurnMagnitude(ItemStack stack) { return 2.75F; }
	
	protected void setupTransformsAndRender(ItemStack stack, TransformType transform) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;

		float swayMagnitude = getSwayMagnitude(stack);
		float swayPeriod = getSwayPeriod(stack);
		float turnMagnitude = getTurnMagnitude(stack);

		//lighting setup (item lighting changes based on player rotation)
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * interp;
		float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * interp;
		
		GL11.glPushMatrix();
		GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();
		
		//floppyness
		EntityPlayerSP entityplayersp = (EntityPlayerSP) player;
		float armPitch = entityplayersp.prevRenderArmPitch + (entityplayersp.renderArmPitch - entityplayersp.prevRenderArmPitch) * interp;
		float armYaw = entityplayersp.prevRenderArmYaw + (entityplayersp.renderArmYaw - entityplayersp.prevRenderArmYaw) * interp;
		GL11.glRotatef((player.rotationPitch - armPitch) * 0.1F * turnMagnitude, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef((player.rotationYaw - armYaw) * 0.1F * turnMagnitude, 0.0F, 1.0F, 0.0F);

		//brightness setup
		int brightness = mc.world.getCombinedLight(new net.minecraft.util.math.BlockPos(player.posX, player.posY, player.posZ), 0);
		int j = brightness % 65536;
		int k = brightness / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j / 1.0F, (float) k / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		//color setup
		int color = mc.getItemColors().colorMultiplier(stack, 0);
		float r = (float) (color >> 16 & 255) / 255.0F;
		float g = (float) (color >> 8 & 255) / 255.0F;
		float b = (float) (color & 255) / 255.0F;
		GL11.glColor4f(r, g, b, 1.0F);

		GL11.glPushMatrix();
		
		//swing
		/*float swing = player.getSwingProgress(interp);
		float swingZ = MathHelper.sin(swing * (float) Math.PI);
		float swingX = MathHelper.sin(MathHelper.sqrt(swing) * (float) Math.PI);
		GL11.glTranslatef(-swingX * 0.4F, MathHelper.sin(MathHelper.sqrt(swing) * (float) Math.PI * 2.0F) * 0.2F, -swingZ * 0.2F);

		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		float swingYaw = MathHelper.sin(swing * swing * (float) Math.PI);
		float swingPitchRoll = MathHelper.sin(MathHelper.sqrt(swing) * (float) Math.PI);
		GL11.glRotatef(-swingYaw * 20.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-swingPitchRoll * 20.0F, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef(-swingPitchRoll * 80.0F, 1.0F, 0.0F, 0.0F);*/

		GL11.glEnable(GL12.GL_RESCALE_NORMAL); //!
		
		GL11.glRotated(180, 0, 1, 0);

		//viewbob
		if(mc.getRenderViewEntity() instanceof EntityPlayer) {
			EntityPlayer entityplayer = (EntityPlayer) mc.getRenderViewEntity();
			float distanceDelta = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
			float distanceInterp = -(entityplayer.distanceWalkedModified + distanceDelta * interp);
			float camYaw = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * interp;
			float camPitch = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * interp;
			GL11.glTranslatef(MathHelper.sin(distanceInterp * (float) Math.PI * swayPeriod) * camYaw * 0.5F * swayMagnitude, -Math.abs(MathHelper.cos(distanceInterp * (float) Math.PI * swayPeriod) * camYaw) * swayMagnitude, 0.0F);
			GL11.glRotatef(MathHelper.sin(distanceInterp * (float) Math.PI * swayPeriod) * camYaw * 3.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(Math.abs(MathHelper.cos(distanceInterp * (float) Math.PI * swayPeriod - 0.2F) * camYaw) * 5.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(camPitch, 1.0F, 0.0F, 0.0F);
		}
		
		this.render(stack, new NativeItemContext(transform, player));

		GL11.glPopMatrix();

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
	}
	
	public void setupFirstPerson(ItemStack stack) {
		GL11.glTranslated(0, 0, 1);
		
		if(Minecraft.getMinecraft().player.isSneaking()) {
			GL11.glTranslated(0, -3.875 / 8D, 0);
		} else {
			float offset = 0.8F;
			GL11.glRotated(180, 0, 1, 0);
			GL11.glTranslatef(1.0F * offset, -0.75F * offset, -0.5F * offset);
			GL11.glRotated(180, 0, 1, 0);
		}
	}
	
	public void setupThirdPerson(ItemStack stack) {
		double scale = 0.125D;
		GL11.glScaled(scale, scale, scale);
		
		GL11.glRotatef(15.0F, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef(12.5F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(15.0F, 1.0F, 0.0F, 0.0F);
		
		GL11.glTranslated(3.5, 0, 0);

	}
	
	public void setupThirdPersonAkimbo(ItemStack stack) {
		double scale = 0.125D;
		GL11.glScaled(scale, scale, scale);
		
		GL11.glRotatef(15.0F, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef(12.5F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(10.0F, 1.0F, 0.0F, 0.0F);
		
		GL11.glTranslated(5, 0, 0);

	}
	
	public void setupInv(ItemStack stack) {
		GL11.glAlphaFunc(GL11.GL_GREATER, 0F);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		
		GL11.glScaled(1, 1, -1);
		GL11.glTranslated(8, 8, 0);
		GL11.glRotated(225, 0, 0, 1);
		GL11.glRotated(90, 0, 1, 0);
	}
	
	public void setupEntity(ItemStack stack) {
		double scale = 0.125D;
		GL11.glScaled(scale, scale, scale);
		GL11.glRotated(-90, 0, 1, 0);
	}
	
	public void setupModTable(ItemStack stack) {
		double scale = -5D;
		GL11.glScaled(scale, scale, scale);
		GL11.glRotated(90, 0, 1, 0);
	}
	
	public void renderModTable(ItemStack stack, int index) {
		renderOther(stack, new NativeItemContext(TransformType.GUI, null));
	}

	public abstract void renderFirstPerson(ItemStack stack);
	public void renderOther(ItemStack stack, NativeItemContext context) { }
	
	public static void standardAimingTransform(ItemStack stack, double sX, double sY, double sZ, double aX, double aY, double aZ) {
		float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		double x = sX + (aX - sX) * aimingProgress;
		double y = sY + (aY - sY) * aimingProgress;
		double z = sZ + (aZ - sZ) * aimingProgress;
		GL11.glTranslated(x, y, z);
	}
	
	public static void renderSmokeNodes(List<SmokeNode> nodes, double scale) {
		Tessellator tess = Tessellator.getInstance();
        net.minecraft.client.renderer.BufferBuilder buffer = tess.getBuffer();
		
		if(nodes.size() > 1) {

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0F);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glDepthMask(false);

			buffer.begin(GL11.GL_QUADS, WEAPON_SMOKE_FORMAT);
            float alpha = 1;

			
			for(int i = 0; i < nodes.size() - 1; i++) {
				SmokeNode node = nodes.get(i);
				SmokeNode past = nodes.get(i + 1);
				
				alpha = (float) node.alpha;
				buffer.pos(node.forward, node.lift, node.side).color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();
				alpha = 0F;
				buffer.pos(node.forward, node.lift, node.side + node.width * scale).color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();
				alpha = 0F;
				buffer.pos(past.forward, past.lift, past.side + past.width * scale).color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();
				alpha = (float) past.alpha;
				buffer.pos(past.forward, past.lift, past.side).color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();
				
				alpha = (float) node.alpha;
				buffer.pos(node.forward, node.lift, node.side).color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();
				alpha = 0F;
				buffer.pos(node.forward, node.lift, node.side - node.width * scale).color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();
				alpha = 0F;
				buffer.pos(past.forward, past.lift, past.side - past.width * scale).color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();
				alpha = (float) past.alpha;
				buffer.pos(past.forward, past.lift, past.side).color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();
			}
			tess.draw();
			
			GL11.glDepthMask(true);
			GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.1F);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
		}
	}
	
	public static void renderMuzzleFlash(long lastShot) {
		renderMuzzleFlash(lastShot, 75, 15);
	}
	
	public static void renderMuzzleFlash(long lastShot, int duration, double l) {
		Tessellator tess = Tessellator.getInstance();
        net.minecraft.client.renderer.BufferBuilder buffer = tess.getBuffer();
		
		int flash = duration;
		
		if(System.currentTimeMillis() - lastShot < flash) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glPushMatrix();
			
			double fire = (System.currentTimeMillis() - lastShot) / (double) flash;
			
			double width = 6 * fire;
			double length = l * fire;
			double inset = 2;
			Minecraft.getMinecraft().getTextureManager().bindTexture(flash_plume);
			buffer.begin(GL11.GL_QUADS, WEAPON_FLASH_FORMAT);
			



			buffer.pos(0,  -width,  - inset).color(255, 255, 255, 255).tex( 1,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  width,  - inset).color(255, 255, 255, 255).tex( 0,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0.1,  width,  length - inset).color(255, 255, 255, 255).tex( 0 , 0).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0.1,  -width,  length - inset).color(255, 255, 255, 255).tex( 1,  0).lightmap(0, 240).normal(0, 1, 0).endVertex();

			buffer.pos(0,  width,  inset).color(255, 255, 255, 255).tex( 0,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  -width,  inset).color(255, 255, 255, 255).tex( 1,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0.1,  -width,  -length + inset).color(255, 255, 255, 255).tex( 1,  0).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0.1,  width,  -length + inset).color(255, 255, 255, 255).tex( 0 , 0).lightmap(0, 240).normal(0, 1, 0).endVertex();

			buffer.pos(0,  - inset,  width).color(255, 255, 255, 255).tex( 0,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  - inset,  -width).color(255, 255, 255, 255).tex( 1,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0.1,  length - inset,  -width).color(255, 255, 255, 255).tex( 1,  0).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0.1,  length - inset,  width).color(255, 255, 255, 255).tex( 0 , 0).lightmap(0, 240).normal(0, 1, 0).endVertex();

			buffer.pos(0,  inset,  -width).color(255, 255, 255, 255).tex( 1,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  inset,  width).color(255, 255, 255, 255).tex( 0,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0.1,  -length + inset,  width).color(255, 255, 255, 255).tex( 0 , 0).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0.1,  -length + inset,  -width).color(255, 255, 255, 255).tex( 1,  0).lightmap(0, 240).normal(0, 1, 0).endVertex();
			
			tess.draw();
			GL11.glPopMatrix();
			GL11.glDepthMask(true);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
	}
	
	public static void renderGapFlash(long lastShot) {
		Tessellator tess = Tessellator.getInstance();
        net.minecraft.client.renderer.BufferBuilder buffer = tess.getBuffer();
		
		int flash = 75;
		
		if(System.currentTimeMillis() - lastShot < flash) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glPushMatrix();
			
			double fire = (System.currentTimeMillis() - lastShot) / (double) flash;
			
			double height = 4 * fire;
			double length = 15 * fire;
			double lift = 3 * fire;
			double offset = 1 * fire;
			double lengthOffset = 0.125;
			Minecraft.getMinecraft().getTextureManager().bindTexture(flash_plume);
			buffer.begin(GL11.GL_QUADS, WEAPON_FLASH_FORMAT);


			
			
			buffer.pos(0,  -height,  -offset).color(255, 255, 255, 255).tex( 1,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  height,  -offset).color(255, 255, 255, 255).tex( 0,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  height + lift,  length - offset).color(255, 255, 255, 255).tex( 0 , 0).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  -height + lift,  length - offset).color(255, 255, 255, 255).tex( 1,  0).lightmap(0, 240).normal(0, 1, 0).endVertex();

			buffer.pos(0,  height,  offset).color(255, 255, 255, 255).tex( 0,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  -height,  offset).color(255, 255, 255, 255).tex( 1,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  -height + lift,  -length + offset).color(255, 255, 255, 255).tex( 1,  0).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  height + lift,  -length + offset).color(255, 255, 255, 255).tex( 0 , 0).lightmap(0, 240).normal(0, 1, 0).endVertex();

			buffer.pos(0,  -height,  -offset).color(255, 255, 255, 255).tex( 1,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  height,  -offset).color(255, 255, 255, 255).tex( 0,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(lengthOffset,  height,  length - offset).color(255, 255, 255, 255).tex( 0 , 0).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(lengthOffset,  -height,  length - offset).color(255, 255, 255, 255).tex( 1,  0).lightmap(0, 240).normal(0, 1, 0).endVertex();

			buffer.pos(0,  height,  offset).color(255, 255, 255, 255).tex( 0,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  -height,  offset).color(255, 255, 255, 255).tex( 1,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(lengthOffset,  -height,  -length + offset).color(255, 255, 255, 255).tex( 1,  0).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(lengthOffset,  height,  -length + offset).color(255, 255, 255, 255).tex( 0 , 0).lightmap(0, 240).normal(0, 1, 0).endVertex();
			
			tess.draw();
			GL11.glPopMatrix();
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
	}
	
	public static void renderLaserFlash(long lastShot, int flash, double scale, int color) {
		Tessellator tess = Tessellator.getInstance();
        net.minecraft.client.renderer.BufferBuilder buffer = tess.getBuffer();
		
		if(System.currentTimeMillis() - lastShot < flash) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glPushMatrix();
			
			double fire = (System.currentTimeMillis() - lastShot) / (double) flash;
			
			double size = 4 * fire * scale;
			
			Minecraft.getMinecraft().getTextureManager().bindTexture(laser_flash);
			buffer.begin(GL11.GL_QUADS, WEAPON_FLASH_FORMAT);



			
			
			buffer.pos(0,  -size,  -size).color(((color >> 16) & 255), ((color >> 8) & 255), (color & 255), 255).tex( 1,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  size,  -size).color(((color >> 16) & 255), ((color >> 8) & 255), (color & 255), 255).tex( 0,  1).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  size,  size).color(((color >> 16) & 255), ((color >> 8) & 255), (color & 255), 255).tex( 0 , 0).lightmap(0, 240).normal(0, 1, 0).endVertex();
			buffer.pos(0,  -size,  size).color(((color >> 16) & 255), ((color >> 8) & 255), (color & 255), 255).tex( 1,  0).lightmap(0, 240).normal(0, 1, 0).endVertex();
			
			tess.draw();
			GL11.glPopMatrix();
			GL11.glDepthMask(true);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
	}
}
