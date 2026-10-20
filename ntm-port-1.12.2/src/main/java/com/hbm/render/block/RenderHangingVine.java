package com.hbm.render.block;
import java.util.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
/** The fixed vine cross has the same DOWN coordinates and reversed back-face UVs as coral. */
public final class RenderHangingVine {
 private RenderHangingVine() { }
 public static List<double[]> vertices(TextureAtlasSprite icon) { return RenderCoral.vertices(icon,0,0,0,1,EnumFacing.DOWN); }
}
