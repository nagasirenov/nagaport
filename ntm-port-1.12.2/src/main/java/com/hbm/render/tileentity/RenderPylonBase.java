package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.config.ClientConfig;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.network.TileEntityPylonBase;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class RenderPylonBase extends TileEntitySpecialRenderer {

	/**
	 * The closest we have to a does-all solution. It will figure out if it needs to draw multiple lines,
	 * iterate through all the mounting points, try to find the matching mounting points and then draw the lines.
	 * @param pyl
	 * @param x
	 * @param y
	 * @param z
	 */
	public void renderLinesGeneric(TileEntityPylonBase pyl, double x, double y, double z) {

		this.bindTexture(pyl.color == 0 ? ResourceManager.wire_tex : ResourceManager.wire_greyscale_tex);

		pyl.getConnected().forEach(wire -> {
			TileEntity tile = com.hbm.world.WorldBlockState.getTileEntity(pyl.getWorld(), wire[0], wire[1], wire[2]);

			if(tile instanceof TileEntityPylonBase) {
				TileEntityPylonBase pylon = (TileEntityPylonBase) tile;

				Vec3d[] m1 = pyl.getMountPos();
				Vec3d[] m2 = pylon.getMountPos();

				int lineCount = Math.min(m1.length, m2.length);

				for(int line = 0; line < lineCount; line++) {

					Vec3d first = m1[line % m1.length];
					int secondIndex = line % m2.length;

					/*
					 * hacky hacky hack
					 * this will shift the mount point order by 2 to prevent wires from crossing
					 * when meta 12 and 15 pylons are connected. this isn't a great solution
					 * and there's still ways to cross the wires in an ugly way but for now
					 * it should be enough.
					 */
					if(lineCount == 4 && (
							(pyl.getBlockMetadata() - 10 == 5 && pylon.getBlockMetadata() - 10 == 2) ||
							(pyl.getBlockMetadata() - 10 == 2 && pylon.getBlockMetadata() - 10 == 5))) {

						secondIndex += 2;
						secondIndex %= m2.length;
					}

					Vec3d second = m2[secondIndex];

					double sX = second.x + pylon.getPos().getX() - pyl.getPos().getX();
					double sY = second.y + pylon.getPos().getY() - pyl.getPos().getY();
					double sZ = second.z + pylon.getPos().getZ() - pyl.getPos().getZ();

					renderLine(pyl.getWorld(), pyl, x, y, z,
							first.x,
							first.y,
							first.z,
							first.x + (sX - first.x) * 0.5,
							first.y + (sY - first.y) * 0.5,
							first.z + (sZ - first.z) * 0.5);
				}
			}
		});
	}

	/**
	 * Renders half a line
	 * First coords: the relative render position
	 * Second coords: the pylon's mounting point
	 * Third coords: the midway point exactly between the mounting points. The "hang" doesn't need to be accounted for, it's calculated in here.
	 * @param world
	 * @param pyl
	 * @param x
	 * @param y
	 * @param z
	 * @param x0
	 * @param y0
	 * @param z0
	 * @param x1
	 * @param y1
	 * @param z1
	 */
	public void renderLine(World world, TileEntityPylonBase pyl, double x, double y, double z, double x0, double y0, double z0, double x1, double y1, double z1) {

		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		float count = 10;
		Tessellator tess = Tessellator.getInstance();

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		Vec3d delta = new Vec3d(x0 - x1, y0 - y1, z0 - z1);

		double girth = 0.03125D;
		double hyp = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
		double yaw = Math.atan2(delta.x, delta.z);
		double pitch = Math.atan2(delta.y, hyp);
		double rotator = Math.PI * 0.5D;
		double newPitch = pitch + rotator;
		double newYaw = yaw + rotator;
		double iZ = Math.cos(yaw) * Math.cos(newPitch) * girth;
		double iX = Math.sin(yaw) * Math.cos(newPitch) * girth;
		double iY = Math.sin(newPitch) * girth;
		double jZ = Math.cos(newYaw) * girth;
		double jX = Math.sin(newYaw) * girth;

		int col = pyl.color == 0 ? 0xffffff : pyl.color;
		int r = col >> 16 & 255;
		int g = col >> 8 & 255;
		int bl = col & 255;

		if(!ClientConfig.RENDER_CABLE_HANG.get()) {
			int brightness = world.getCombinedLight(new net.minecraft.util.math.BlockPos(MathHelper.floor(pyl.getPos().getX() + x0), MathHelper.floor(pyl.getPos().getY() + y0), MathHelper.floor(pyl.getPos().getZ() + z0)), 0);
			int skyLight = brightness >> 16 & 65535;
			int blockLight = brightness & 65535;
			drawLineSegment(buf, x0, y0, z0, x1, y1, z1, iX, iY, iZ, jX, jZ, r, g, bl, skyLight, blockLight);
		} else {

			double hang = Math.min(delta.length() / 15D, 2.5D);

			for(float j = 0; j < count; j++) {

				float k = j + 1;

				double sagJ = Math.sin(j / count * Math.PI * 0.5) * hang;
				double sagK = Math.sin(k / count * Math.PI * 0.5) * hang;
				double sagMean = (sagJ + sagK) / 2D;

				double deltaX = x1 - x0;
				double deltaY = y1 - y0;
				double deltaZ = z1 - z0;

				double ja = j + 0.5D;
				double ix = pyl.getPos().getX() + x0 + deltaX / (double)(count) * ja;
				double iy = pyl.getPos().getY() + y0 + deltaY / (double)(count) * ja - sagMean;
				double iz = pyl.getPos().getZ() + z0 + deltaZ / (double)(count) * ja;

				int brightness = world.getCombinedLight(new net.minecraft.util.math.BlockPos(MathHelper.floor(ix), MathHelper.floor(iy), MathHelper.floor(iz)), 0);
				int skyLight = brightness >> 16 & 65535;
				int blockLight = brightness & 65535;

				drawLineSegment(buf,
						x0 + (deltaX * j / count),
						y0 + (deltaY * j / count) - sagJ,
						z0 + (deltaZ * j / count),
						x0 + (deltaX * k / count),
						y0 + (deltaY * k / count) - sagK,
						z0 + (deltaZ * k / count),
						iX, iY, iZ, jX, jZ, r, g, bl, skyLight, blockLight);
			}
		}
		tess.draw();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glPopMatrix();
	}

	/**
	 * Draws a single segment from the first to the second 3D coordinate.
	 * Not fantastic but it looks good enough.
	 * Possible enhancement: remove the draw calls and put those around the drawLineSegment calls for better-er performance
	 * @param x
	 * @param y
	 * @param z
	 * @param a
	 * @param b
	 * @param c
	 */
	public void drawLineSegment(BufferBuilder buf, double x, double y, double z, double a, double b, double c, double iX, double iY, double iZ, double jX, double jZ, int r, int g, int bl, int skyLight, int blockLight) {

		double deltaX = a - x;
		double deltaY = b - y;
		double deltaZ = c - z;
		double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
		int wrap = (int) Math.ceil(length * 8);

		if(deltaX + deltaZ < 0) {
			wrap *= -1;
			jZ *= -1;
			jX *= -1;
		}

		buf.pos(x + iX, y + iY, z + iZ).tex(0, 0).color(r, g, bl, 255).lightmap(skyLight, blockLight).endVertex();
		buf.pos(x - iX, y - iY, z - iZ).tex(0, 1).color(r, g, bl, 255).lightmap(skyLight, blockLight).endVertex();
		buf.pos(a - iX, b - iY, c - iZ).tex(wrap, 1).color(r, g, bl, 255).lightmap(skyLight, blockLight).endVertex();
		buf.pos(a + iX, b + iY, c + iZ).tex(wrap, 0).color(r, g, bl, 255).lightmap(skyLight, blockLight).endVertex();
		buf.pos(x + jX, y, z + jZ).tex(0, 0).color(r, g, bl, 255).lightmap(skyLight, blockLight).endVertex();
		buf.pos(x - jX, y, z - jZ).tex(0, 1).color(r, g, bl, 255).lightmap(skyLight, blockLight).endVertex();
		buf.pos(a - jX, b, c - jZ).tex(wrap, 1).color(r, g, bl, 255).lightmap(skyLight, blockLight).endVertex();
		buf.pos(a + jX, b, c + jZ).tex(wrap, 0).color(r, g, bl, 255).lightmap(skyLight, blockLight).endVertex();
	}

	public static final int LINE_COLOR = 0xBB3311;
}
