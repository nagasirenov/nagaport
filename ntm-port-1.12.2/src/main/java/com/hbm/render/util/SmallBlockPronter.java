package com.hbm.render.util;

import org.lwjgl.opengl.GL11;

import com.hbm.interfaces.Spaghetti;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.hbm.render.icon.IIcon;
import com.hbm.lib.ForgeDirection;
import com.hbm.blocks.BlockBase;

public class SmallBlockPronter {

	static float pixel = 1F/16F;

	/**
	 * Bind the required texture yourself bruh     <- ding dong, 2023 bob speaking, fuck this guy
	 * @param loc
	 * @param x
	 * @param y
	 * @param z
	 */
	@Deprecated @Spaghetti("this is horseshit")
	public static void renderSmolBlockAt(float x, float y, float z) {
		GL11.glPushMatrix();
		GL11.glTranslatef(x - 1, y - 1, z);

		Tessellator tesseract = Tessellator.getInstance();
		BufferBuilder tesseractBuffer = tesseract.getBuffer();
		tesseractBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tesseractBuffer.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 0).endVertex();
			tesseractBuffer.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 0).endVertex();
			tesseractBuffer.pos(11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 1).endVertex();
			tesseractBuffer.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 1).endVertex();
			tesseract.draw();

			tesseractBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tesseractBuffer.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(1, 0).endVertex();
			tesseractBuffer.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 0).endVertex();
			tesseractBuffer.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 1).endVertex();
			tesseractBuffer.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(1, 1).endVertex();
			tesseract.draw();

			tesseractBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tesseractBuffer.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(1, 0).endVertex();
			tesseractBuffer.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(0, 0).endVertex();
			tesseractBuffer.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(0, 1).endVertex();
			tesseractBuffer.pos(11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(1, 1).endVertex();
			tesseract.draw();

			tesseractBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tesseractBuffer.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 0).endVertex();
			tesseractBuffer.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(0, 0).endVertex();
			tesseractBuffer.pos(11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(0, 1).endVertex();
			tesseractBuffer.pos(11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 1).endVertex();
			tesseract.draw();

			tesseractBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tesseractBuffer.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(1, 0).endVertex();
			tesseractBuffer.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(0, 0).endVertex();
			tesseractBuffer.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 1).endVertex();
			tesseractBuffer.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 1).endVertex();
			tesseract.draw();

			tesseractBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tesseractBuffer.pos(11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(1, 0).endVertex();
			tesseractBuffer.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(0, 0).endVertex();
			tesseractBuffer.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 1).endVertex();
			tesseractBuffer.pos(11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 1).endVertex();
		tesseract.draw();
		GL11.glPopMatrix();

	}

	public static void startDrawing() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.75F);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);
		Tessellator.getInstance().getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
	}

	public static void draw() {
		Tessellator.getInstance().draw();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	public static void drawSmolBlockAt(Block b, int meta, float x, float y, float z) {
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		FaceUv top = face(b, meta, ForgeDirection.UP), bottom = face(b, meta, ForgeDirection.DOWN);
		FaceUv north = face(b, meta, ForgeDirection.NORTH), south = face(b, meta, ForgeDirection.SOUTH);
		FaceUv east = face(b, meta, ForgeDirection.EAST), west = face(b, meta, ForgeDirection.WEST);
		float lo = 11 * pixel / 2, hi = 1 - lo;
		quad(buffer, south, x+hi,y+hi,z+hi, x+lo,y+hi,z+hi, x+lo,y+lo,z+hi, x+hi,y+lo,z+hi);
		quad(buffer, east, x+hi,y+hi,z+lo, x+hi,y+hi,z+hi, x+hi,y+lo,z+hi, x+hi,y+lo,z+lo);
		quad(buffer, north, x+lo,y+hi,z+lo, x+hi,y+hi,z+lo, x+hi,y+lo,z+lo, x+lo,y+lo,z+lo);
		quad(buffer, west, x+lo,y+hi,z+hi, x+lo,y+hi,z+lo, x+lo,y+lo,z+lo, x+lo,y+lo,z+hi);
		quad(buffer, top, x+hi,y+hi,z+lo, x+lo,y+hi,z+lo, x+lo,y+hi,z+hi, x+hi,y+hi,z+hi);
		quad(buffer, bottom, x+lo,y+lo,z+lo, x+hi,y+lo,z+lo, x+hi,y+lo,z+hi, x+lo,y+lo,z+hi);
	}

	private static FaceUv face(Block block, int metadata, ForgeDirection direction) {
		if(block instanceof BlockBase) {
			IIcon icon = ((BlockBase) block).getIcon(direction.ordinal(), metadata);
			if(icon == null) throw new IllegalArgumentException("No NTM icon for " + block + " metadata " + metadata + " face " + direction);
			return new FaceUv(icon.getMinU(), icon.getMaxU(), icon.getMinV(), icon.getMaxV());
		}
		net.minecraft.block.state.IBlockState state = block.getStateFromMeta(metadata);
		String name = NativeBlockMaterial.faceSprite(Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state), state, direction.toEnumFacing());
		if(name == null) throw new IllegalArgumentException("No unique baked-model sprite for " + block + " metadata " + metadata + " face " + direction);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name);
		return new FaceUv(sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
	}

	private static void quad(BufferBuilder b, FaceUv uv, float... xyz) {
		float[][] tex = {{uv.maxU,uv.minV},{uv.minU,uv.minV},{uv.minU,uv.maxV},{uv.maxU,uv.maxV}};
		for(int i = 0; i < 4; i++) b.pos(xyz[i*3],xyz[i*3+1],xyz[i*3+2]).tex(tex[i][0],tex[i][1]).normal(0F,1F,0F).endVertex();
	}

	private static final class FaceUv {
		private final float minU, maxU, minV, maxV;
		private FaceUv(float minU, float maxU, float minV, float maxV) { this.minU=minU; this.maxU=maxU; this.minV=minV; this.maxV=maxV; }
	}
}
