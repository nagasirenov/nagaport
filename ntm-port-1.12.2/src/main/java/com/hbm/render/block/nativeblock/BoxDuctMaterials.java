package com.hbm.render.block.nativeblock;

import java.util.*;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

/** Original box-duct material selection, including the cable's distinct connected junction ends. */
public final class BoxDuctMaterials {
    public enum Kind { FLUID, EXHAUST, CABLE }
    public final Kind kind;
    private final TextureAtlasSprite[][] straight, end, curve, junction;
    public final TextureAtlasSprite particle;
    public BoxDuctMaterials(Kind kind, Function<ResourceLocation, TextureAtlasSprite> sprites) {
        this.kind = Objects.requireNonNull(kind);
        String[] skins = skins(kind);
        straight = new TextureAtlasSprite[skins.length][1]; end = new TextureAtlasSprite[skins.length][kind == Kind.CABLE ? 5 : 1];
        curve = new TextureAtlasSprite[skins.length][4]; junction = new TextureAtlasSprite[skins.length][kind == Kind.CABLE ? 1 : 5];
        for(int m = 0; m < skins.length; m++) {
            straight[m][0] = sprites.apply(texture(skins[m], "straight"));
            for(int n = 0; n < end[m].length; n++) end[m][n] = sprites.apply(texture(skins[m], "end" + (kind == Kind.CABLE ? "_" + n : "")));
            for(int n = 0; n < 4; n++) curve[m][n] = sprites.apply(texture(skins[m], new String[]{"curve_tl", "curve_tr", "curve_bl", "curve_br"}[n]));
            for(int n = 0; n < junction[m].length; n++) junction[m][n] = sprites.apply(texture(skins[m], "junction" + (kind == Kind.CABLE ? "" : "_" + n)));
        }
        particle = sprites.apply(new ResourceLocation("hbm", "blocks/fluid_duct_box"));
    }
    private static String[] skins(Kind kind) { return kind == Kind.FLUID ? new String[]{"silver", "copper", "white"} : new String[]{kind == Kind.EXHAUST ? "exhaust" : "cable"}; }
    private static ResourceLocation texture(String skin, String part) { return new ResourceLocation("hbm", "blocks/boxduct_" + skin + "_" + part); }
    public static Collection<ResourceLocation> textures(Kind kind) {
        List<ResourceLocation> result = new ArrayList<>();
        for(String skin : skins(kind)) {
            for(String part : new String[]{"straight", "curve_tl", "curve_tr", "curve_bl", "curve_br"}) result.add(texture(skin, part));
            if(kind == Kind.CABLE) { for(int n=0;n<5;n++) result.add(texture(skin,"end_"+n)); result.add(texture(skin,"junction")); }
            else { result.add(texture(skin,"end")); for(int n=0;n<5;n++) result.add(texture(skin,"junction_"+n)); }
        }
        result.add(new ResourceLocation("hbm", "blocks/fluid_duct_box"));
        return Collections.unmodifiableList(result);
    }
    public TextureAtlasSprite face(int metadata, int mask, int side, boolean inventory) {
        int m = kind == Kind.FLUID ? Math.abs(metadata % 3) : 0;
        TextureAtlasSprite line = straight[m][0];
        if(inventory) return side == 2 || side == 3 ? end[m][kind == Kind.CABLE ? metadata : 0] : line;
        boolean pX=(mask&32)!=0,nX=(mask&16)!=0,pY=(mask&8)!=0,nY=(mask&4)!=0,pZ=(mask&2)!=0,nZ=(mask&1)!=0;
        int count = Integer.bitCount(mask);
        if((mask&15)==0 && mask>0) return side==4||side==5 ? end[m][kind==Kind.CABLE?metadata:0] : line;
        if((mask&60)==0 && mask>0) return side==2||side==3 ? end[m][kind==Kind.CABLE?metadata:0] : line;
        if((mask&51)==0 && mask>0) return side==0||side==1 ? end[m][kind==Kind.CABLE?metadata:0] : line;
        boolean connected = side==0&&nY || side==1&&pY || side==2&&nZ || side==3&&pZ || side==4&&nX || side==5&&pX;
        if((kind==Kind.CABLE || count==2) && connected) return end[m][kind==Kind.CABLE?metadata:0];
        if(count==2) {
            if(side==1&&nY || side==0&&pY || side==3&&nZ || side==2&&pZ || side==5&&nX || side==4&&pX) return line;
            if(nY&&pZ) return curve[m][side==4?3:2];
            if(nY&&nZ) return curve[m][side==5?3:2];
            if(nY&&pX) return curve[m][side==3?3:2];
            if(nY&&nX) return curve[m][side==2?3:2];
            if(pY&&pZ) return curve[m][side==4?1:0];
            if(pY&&nZ) return curve[m][side==5?1:0];
            if(pY&&pX) return curve[m][side==3?1:0];
            if(pY&&nX) return curve[m][side==2?1:0];
            if(pX&&nZ) return curve[m][1];
            if(pX&&pZ) return curve[m][3];
            if(nX&&nZ) return curve[m][0];
            if(nX&&pZ) return curve[m][2];
        }
        return junction[m][kind==Kind.CABLE?0:metadata/3];
    }
}
