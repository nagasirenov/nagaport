package com.hbm.render.block.ct;

import java.lang.reflect.Field;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.*;

/**
 * CT's original nine-point interpolation, applied to the native full-face corner solver.
 * Fragment vertices must not independently sample the native smooth solver. This is scoped
 * to the actual Forge lighter, restores its parent in finally and retains no render context.
 * Whole 1.7/1.12 corner-solver equality, vanilla's optional pipe-bypassing fallback and damage
 * retexturing are separate open acceptance gates, not approved visual substitutions.
 */
public final class CTLightQuad extends BakedQuad {
    private static final Field PARENT = parentField();
    private final BakedQuad fragment, fullFace;
    private final int quadrant;
    private static final int[][] GRID = {{1,0,3,4},{2,1,4,5},{4,3,6,7},{5,4,7,8}};
    public CTLightQuad(BakedQuad fragment, BakedQuad fullFace, int quadrant) {
        super(fragment.getVertexData(), fragment.getTintIndex(), fragment.getFace(), fragment.getSprite(),
                fragment.shouldApplyDiffuseLighting(), fragment.getFormat());
        this.fragment=fragment; this.fullFace=fullFace; this.quadrant=quadrant;
    }
    private static Field parentField() {
        try { Field field=QuadGatheringTransformer.class.getDeclaredField("parent"); field.setAccessible(true); return field; }
        catch(ReflectiveOperationException error) { throw new ExceptionInInitializerError(error); }
    }
    @Override public void pipe(IVertexConsumer consumer) {
        if(!(consumer instanceof VertexLighterFlat)) { fragment.pipe(consumer); return; }
        VertexLighterFlat lighter=(VertexLighterFlat)consumer;
        try {
            IVertexConsumer parent=(IVertexConsumer)PARENT.get(lighter);
            Capture capture=new Capture(parent.getVertexFormat());
            try {
                lighter.setParent(capture);
                fullFace.pipe(lighter);
            } finally { lighter.setParent(parent); }
            final Light[] grid=capture.grid();
            // Keep the fragment's own position/UV/material and ordinary metadata. Only
            // already-lit color/light coordinates come from the original CT interpolation.
            fragment.pipe(new VertexTransformer(parent) {
                int vertex;
                @Override public void put(int element,float... values) {
                    VertexFormatElement attribute=getVertexFormat().getElement(element);
                    Light light=grid[GRID[quadrant][vertex]];
                    if(attribute.getUsage()==VertexFormatElement.EnumUsage.COLOR) values=light.color.clone();
                    else if(attribute.getUsage()==VertexFormatElement.EnumUsage.UV && attribute.getIndex()==1)
                        values=new float[]{(light.brightness&65535)/32767F,(light.brightness>>>16&65535)/32767F,0,1};
                    super.put(element,values);
                    if(element==getVertexFormat().getElementCount()-1) vertex++;
                }
            });
        } catch(IllegalAccessException error) { throw new IllegalStateException("Cannot access native CT light consumer",error); }
    }
    private static final class Capture implements IVertexConsumer {
        final VertexFormat format;
        final Light[] corners={new Light(),new Light(),new Light(),new Light()};
        int vertex;
        Capture(VertexFormat format) { this.format=format; }
        @Override public VertexFormat getVertexFormat() { return format; }
        @Override public void setQuadTint(int tint) { }
        @Override public void setQuadOrientation(EnumFacing face) { }
        @Override public void setApplyDiffuseLighting(boolean diffuse) { }
        @Override public void setTexture(TextureAtlasSprite sprite) { }
        @Override public void put(int element,float... values) {
            VertexFormatElement attribute=format.getElement(element);
            if(attribute.getUsage()==VertexFormatElement.EnumUsage.COLOR) corners[vertex].color=values.clone();
            if(attribute.getUsage()==VertexFormatElement.EnumUsage.UV && attribute.getIndex()==1)
                corners[vertex].brightness=(Math.round(values[0]*32767F)&65535)|(Math.round(values[1]*32767F)&65535)<<16;
            if(element==format.getElementCount()-1) vertex++;
        }
        Light[] grid() {
            if(vertex!=4) throw new IllegalStateException("Native CT corner solver emitted "+vertex+" vertices");
            Light tl=corners[1],tr=corners[0],bl=corners[2],br=corners[3];
            return new Light[]{tl,Light.average(tl,tr),tr,Light.average(tl,bl),Light.average(tl,tr,bl,br),Light.average(tr,br),bl,Light.average(bl,br),br};
        }
    }
    private static final class Light {
        float[] color={1,1,1,1}; int brightness;
        static Light average(Light... values) {
            Light result=new Light(); result.color=new float[4];
            for(Light value:values) {
                for(int channel=0;channel<4;channel++) result.color[channel]+=value.color[channel];
                result.brightness+=value.brightness;
            }
            for(int channel=0;channel<4;channel++) result.color[channel]/=values.length;
            result.brightness/=values.length;
            return result;
        }
    }
}
