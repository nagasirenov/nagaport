package com.hbm.render.block.nativeblock;

import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.machine.rbmk.RBMKRenderState.Profile;
import com.hbm.blocks.machine.rbmk.RBMKRenderState.Family;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

/** Exact per-owner atlas suffixes, including the original ReaSim DOWN-before-pipe precedence. */
public final class RBMKMaterials {
    public final Profile profile;
    private final Function<ResourceLocation, TextureAtlasSprite> sprites;
    public RBMKMaterials(Profile profile, Function<ResourceLocation, TextureAtlasSprite> sprites) { this.profile = profile; this.sprites = sprites; }
    public static Collection<ResourceLocation> textures(Profile p) {
        List<ResourceLocation> result = new ArrayList<>();
        for(String suffix : new String[] {"_side", "_top"}) result.add(new ResourceLocation(p.texture + suffix));
        if(!p.ownLid) for(String suffix : new String[] {"_cover_top", "_cover_side", "_glass_top", "_glass_side"}) result.add(new ResourceLocation(p.texture + suffix));
        if(p.family == Family.CONTROL) for(String suffix : new String[] {"_pipe_top", "_pipe_side"}) result.add(new ResourceLocation(p.texture + suffix));
        if(p.family == Family.ROD) for(String suffix : new String[] {"_inner", "_fuel"}) result.add(new ResourceLocation(p.texture + suffix));
        if(p.specialBottom) result.add(new ResourceLocation(p.texture + "_bottom"));
        return Collections.unmodifiableList(result);
    }
    public TextureAtlasSprite texture(String suffix) { return sprites.apply(new ResourceLocation(profile.texture + suffix)); }
    public TextureAtlasSprite face(int side, int lid, boolean pipes) {
        if(lid == 0 && profile.specialBottom && side == 0) return texture("_bottom");
        if(pipes) return texture(side < 2 ? "_pipe_top" : "_pipe_side");
        if(!profile.ownLid && lid != 0) return texture((lid == 1 ? "_cover" : "_glass") + (side < 2 ? "_top" : "_side"));
        return texture(side < 2 ? "_top" : "_side");
    }
}
