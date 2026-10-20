package com.hbm.render.block.nativeblock;

import java.util.*;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

/** Original per-face foundry assets; no particle-sprite substitution. */
public final class FoundryMaterials {
    private final Map<String,TextureAtlasSprite> sprites=new HashMap<>();
    public FoundryMaterials(FoundryGeometry.Kind kind, Function<ResourceLocation,TextureAtlasSprite> getter) {
        for(Map.Entry<String,ResourceLocation> entry:textures(kind).entrySet()) sprites.put(entry.getKey(),Objects.requireNonNull(getter.apply(entry.getValue()),entry.getValue().toString()));
    }
    public TextureAtlasSprite get(String face) { return Objects.requireNonNull(sprites.get(face),face); }
    public static Map<String,ResourceLocation> textures(FoundryGeometry.Kind kind) {
        Map<String,ResourceLocation> result=new LinkedHashMap<>();put(result,"Particle","brick_fire");String base="foundry_"+kind.name().toLowerCase(Locale.ROOT);
        for(String face:new String[]{"Top","Side","Bottom","Inner"}) put(result,face,base+"_"+face.toLowerCase(Locale.ROOT));
        if(kind==FoundryGeometry.Kind.TANK) { put(result,"SideOutlet",base+"_side_outlet");put(result,"SideUpper",base+"_upper");put(result,"SideUpperOutlet",base+"_upper_outlet"); }
        if(kind==FoundryGeometry.Kind.TANK||kind==FoundryGeometry.Kind.CHANNEL)put(result,"Lava","lava_gray");
        if(kind==FoundryGeometry.Kind.OUTLET||kind==FoundryGeometry.Kind.SLAGTAP) {
            put(result,"Front",base+"_front");put(result,"Lock","foundry_outlet_lock");put(result,"Filter","foundry_outlet_filter");
        }
        return Collections.unmodifiableMap(result);
    }
    private static void put(Map<String,ResourceLocation> target,String key,String name) { target.put(key,new ResourceLocation("hbm","blocks/"+name)); }
}
