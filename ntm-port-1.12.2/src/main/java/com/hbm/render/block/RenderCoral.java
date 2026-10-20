package com.hbm.render.block;
import java.util.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
/** Original coral coordinate, winding and UV equations, now native quad input. */
public final class RenderCoral {
 private RenderCoral() { }
 public static List<double[]> vertices(TextureAtlasSprite icon,double x,double y,double z,double height,EnumFacing dir) {
  List<double[]> vertices=new ArrayList<>();
		double minU = icon.getMinU();
		double minV = icon.getMinV();
		double maxU = icon.getMaxU();
		double maxV = icon.getMaxV();
		
		double factor = 0.45D * height;
		double minY = 0;
		double maxY = height;
		double minX = 0.5D - factor;
		double maxX = 0.5D + factor;
		double minZ = 0.5D - factor;
		double maxZ = 0.5D + factor;

		double x0, x1, x2, x3, x4, x5, x6, x7;
		double y0, y1, y2, y3, y4, y5, y6, y7;
		double z0, z1, z2, z3, z4, z5, z6, z7;
		
		x0 = minX; x1 = minX; x2 = maxX; x3 = maxX;
		y0 = minY; y1 = maxY; y2 = maxY; y3 = minY;
		z0 = minZ; z1 = minZ; z2 = maxZ; z3 = maxZ;

		x4 = maxX; x5 = maxX; x6 = minX; x7 = minX;
		y4 = minY; y5 = maxY; y6 = maxY; y7 = minY;
		z4 = minZ; z5 = minZ; z6 = maxZ; z7 = maxZ;

		switch(dir) {
		case UP:
			y0 = 1 - y0; y1 = 1 - y1; y2 = 1 - y2; y3 = 1 - y3;
			y4 = 1 - y4; y5 = 1 - y5; y6 = 1 - y6; y7 = 1 - y7;
			break;
		case NORTH:
			y0 = minZ; y1 = minZ; y2 = maxZ; y3 = maxZ;
			z0 = minY; z1 = maxY; z2 = maxY; z3 = minY;
			y4 = minZ; y5 = minZ; y6 = maxZ; y7 = maxZ;
			z4 = minY; z5 = maxY; z6 = maxY; z7 = minY;
			break;
		case SOUTH:
			y0 = minZ; y1 = minZ; y2 = maxZ; y3 = maxZ;
			z0 = 1 - minY; z1 = 1 - maxY; z2 = 1 - maxY; z3 = 1 - minY;
			y4 = minZ; y5 = minZ; y6 = maxZ; y7 = maxZ;
			z4 = 1 - minY; z5 = 1 - maxY; z6 = 1 - maxY; z7 = 1 - minY;
			break;
		case WEST:
			y0 = minX; y1 = minX; y2 = maxX; y3 = maxX;
			x0 = minY; x1 = maxY; x2 = maxY; x3 = minY;
			y4 = maxX; y5 = maxX; y6 = minX; y7 = minX;
			x4 = minY; x5 = maxY; x6 = maxY; x7 = minY;
			break;
		case EAST:
			y0 = minX; y1 = minX; y2 = maxX; y3 = maxX;
			x0 = 1 - minY; x1 = 1 - maxY; x2 = 1 - maxY; x3 = 1 - minY;
			y4 = maxX; y5 = maxX; y6 = minX; y7 = minX;
			x4 = 1 - minY; x5 = 1 - maxY; x6 = 1 - maxY; x7 = 1 - minY;
			break;
		default:
			break;
		}

		vertices.add(new double[]{x + x0, y + y0, z + z0, maxU, maxV}); 
		vertices.add(new double[]{x + x1, y + y1, z + z1, maxU, minV});
		vertices.add(new double[]{x + x2, y + y2, z + z2, minU, minV});
		vertices.add(new double[]{x + x3, y + y3, z + z3, minU, maxV});

		vertices.add(new double[]{x + x3, y + y3, z + z3, maxU, maxV});
		vertices.add(new double[]{x + x2, y + y2, z + z2, maxU, minV});
		vertices.add(new double[]{x + x1, y + y1, z + z1, minU, minV});
		vertices.add(new double[]{x + x0, y + y0, z + z0, minU, maxV}); 

		vertices.add(new double[]{x + x4, y + y4, z + z4, maxU, maxV}); 
		vertices.add(new double[]{x + x5, y + y5, z + z5, maxU, minV});
		vertices.add(new double[]{x + x6, y + y6, z + z6, minU, minV});
		vertices.add(new double[]{x + x7, y + y7, z + z7, minU, maxV});

		vertices.add(new double[]{x + x7, y + y7, z + z7, maxU, maxV});
		vertices.add(new double[]{x + x6, y + y6, z + z6, maxU, minV});
		vertices.add(new double[]{x + x5, y + y5, z + z5, minU, minV});
		vertices.add(new double[]{x + x4, y + y4, z + z4, minU, maxV}); 
 return vertices;
 }
}
