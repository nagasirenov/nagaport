package com.hbm.particle;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Native owned-draw format for particles that supply both lightmap and normals. */
@SideOnly(Side.CLIENT)
public final class ParticleVertexFormats {
    public static final VertexFormat LIT_TEXTURED = new VertexFormat(DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
            .addElement(DefaultVertexFormats.NORMAL_3B).addElement(DefaultVertexFormats.PADDING_1B);
    private ParticleVertexFormats() { }
}
