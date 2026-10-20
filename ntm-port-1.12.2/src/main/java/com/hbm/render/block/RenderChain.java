package com.hbm.render.block;

import java.util.*;
import com.hbm.render.block.nativeblock.StructuralGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Original ordered chain quads, baked directly into the native atlas format. */
public final class RenderChain {
    private RenderChain() { }
    public static List<BakedQuad> bake(int metadata, TextureAtlasSprite sprite) {
        List<double[]> vertices = new ArrayList<>();
        int l = metadata;
        double x = 0, y = 0, z = 0;
        double d0 = sprite.getMinU(), d1 = sprite.getMinV(), d2 = sprite.getMaxU(), d3 = sprite.getMaxV();
        double scale = 0, wallOffset = .05D;
		if(l == 0) {

			double minU = (double) sprite.getMinU();
			double minV = (double) sprite.getMinV();
			double maxU = (double) sprite.getMaxU();
			double maxV = (double) sprite.getMaxV();
			double minX = x;
			double maxX = x + 1;
			double minY = y;
			double ySize = 1;
			double minZ = z + 0;
			double maxZ = z + 1;
            vertices.add(new double[] {minX, minY + (double) ySize, minZ, minU, minV});
            vertices.add(new double[] {minX, minY + 0.0D, minZ, minU, maxV});
            vertices.add(new double[] {maxX, minY + 0.0D, maxZ, maxU, maxV});
            vertices.add(new double[] {maxX, minY + (double) ySize, maxZ, maxU, minV});
            vertices.add(new double[] {maxX, minY + (double) ySize, maxZ, minU, minV});
            vertices.add(new double[] {maxX, minY + 0.0D, maxZ, minU, maxV});
            vertices.add(new double[] {minX, minY + 0.0D, minZ, maxU, maxV});
            vertices.add(new double[] {minX, minY + (double) ySize, minZ, maxU, minV});
            vertices.add(new double[] {minX, minY + (double) ySize, maxZ, minU, minV});
            vertices.add(new double[] {minX, minY + 0.0D, maxZ, minU, maxV});
            vertices.add(new double[] {maxX, minY + 0.0D, minZ, maxU, maxV});
            vertices.add(new double[] {maxX, minY + (double) ySize, minZ, maxU, minV});
            vertices.add(new double[] {maxX, minY + (double) ySize, minZ, minU, minV});
            vertices.add(new double[] {maxX, minY + 0.0D, minZ, minU, maxV});
            vertices.add(new double[] {minX, minY + 0.0D, maxZ, maxU, maxV});
            vertices.add(new double[] {minX, minY + (double) ySize, maxZ, maxU, minV});
		}

		if(l == 5) {
            vertices.add(new double[] {(double) x + wallOffset, (double) (y + 1) + scale, (double) (z + 1) + scale, d0, d1});
            vertices.add(new double[] {(double) x + wallOffset, (double) (y + 0) - scale, (double) (z + 1) + scale, d0, d3});
            vertices.add(new double[] {(double) x + wallOffset, (double) (y + 0) - scale, (double) (z + 0) - scale, d2, d3});
            vertices.add(new double[] {(double) x + wallOffset, (double) (y + 1) + scale, (double) (z + 0) - scale, d2, d1});
            vertices.add(new double[] {(double) x + wallOffset, (double) (y + 0) - scale, (double) (z + 1) + scale, d0, d3});
            vertices.add(new double[] {(double) x + wallOffset, (double) (y + 1) + scale, (double) (z + 1) + scale, d0, d1});
            vertices.add(new double[] {(double) x + wallOffset, (double) (y + 1) + scale, (double) (z + 0) - scale, d2, d1});
            vertices.add(new double[] {(double) x + wallOffset, (double) (y + 0) - scale, (double) (z + 0) - scale, d2, d3});
		}

		if(l == 4) {
            vertices.add(new double[] {(double) (x + 1) - wallOffset, (double) (y + 0) - scale, (double) (z + 1) + scale, d2, d3});
            vertices.add(new double[] {(double) (x + 1) - wallOffset, (double) (y + 1) + scale, (double) (z + 1) + scale, d2, d1});
            vertices.add(new double[] {(double) (x + 1) - wallOffset, (double) (y + 1) + scale, (double) (z + 0) - scale, d0, d1});
            vertices.add(new double[] {(double) (x + 1) - wallOffset, (double) (y + 0) - scale, (double) (z + 0) - scale, d0, d3});
            vertices.add(new double[] {(double) (x + 1) - wallOffset, (double) (y + 1) + scale, (double) (z + 1) + scale, d2, d1});
            vertices.add(new double[] {(double) (x + 1) - wallOffset, (double) (y + 0) - scale, (double) (z + 1) + scale, d2, d3});
            vertices.add(new double[] {(double) (x + 1) - wallOffset, (double) (y + 0) - scale, (double) (z + 0) - scale, d0, d3});
            vertices.add(new double[] {(double) (x + 1) - wallOffset, (double) (y + 1) + scale, (double) (z + 0) - scale, d0, d1});
		}

		if(l == 3) {
            vertices.add(new double[] {(double) (x + 1) + scale, (double) (y + 0) - scale, (double) z + wallOffset, d2, d3});
            vertices.add(new double[] {(double) (x + 1) + scale, (double) (y + 1) + scale, (double) z + wallOffset, d2, d1});
            vertices.add(new double[] {(double) (x + 0) - scale, (double) (y + 1) + scale, (double) z + wallOffset, d0, d1});
            vertices.add(new double[] {(double) (x + 0) - scale, (double) (y + 0) - scale, (double) z + wallOffset, d0, d3});
            vertices.add(new double[] {(double) (x + 1) + scale, (double) (y + 1) + scale, (double) z + wallOffset, d2, d1});
            vertices.add(new double[] {(double) (x + 1) + scale, (double) (y + 0) - scale, (double) z + wallOffset, d2, d3});
            vertices.add(new double[] {(double) (x + 0) - scale, (double) (y + 0) - scale, (double) z + wallOffset, d0, d3});
            vertices.add(new double[] {(double) (x + 0) - scale, (double) (y + 1) + scale, (double) z + wallOffset, d0, d1});
		}

		if(l == 2) {
            vertices.add(new double[] {(double) (x + 1) + scale, (double) (y + 1) + scale, (double) (z + 1) - wallOffset, d0, d1});
            vertices.add(new double[] {(double) (x + 1) + scale, (double) (y + 0) - scale, (double) (z + 1) - wallOffset, d0, d3});
            vertices.add(new double[] {(double) (x + 0) - scale, (double) (y + 0) - scale, (double) (z + 1) - wallOffset, d2, d3});
            vertices.add(new double[] {(double) (x + 0) - scale, (double) (y + 1) + scale, (double) (z + 1) - wallOffset, d2, d1});
            vertices.add(new double[] {(double) (x + 1) + scale, (double) (y + 0) - scale, (double) (z + 1) - wallOffset, d0, d3});
            vertices.add(new double[] {(double) (x + 1) + scale, (double) (y + 1) + scale, (double) (z + 1) - wallOffset, d0, d1});
            vertices.add(new double[] {(double) (x + 0) - scale, (double) (y + 1) + scale, (double) (z + 1) - wallOffset, d2, d1});
            vertices.add(new double[] {(double) (x + 0) - scale, (double) (y + 0) - scale, (double) (z + 1) - wallOffset, d2, d3});
		}

        List<BakedQuad> result = new ArrayList<>();
        for(int i = 0; i < vertices.size(); i += 4)
            result.add(StructuralGeometry.chainFace(sprite, vertices.subList(i, i + 4)));
        return Collections.unmodifiableList(result);
    }
}
