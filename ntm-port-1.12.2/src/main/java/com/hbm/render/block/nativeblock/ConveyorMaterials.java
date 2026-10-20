package com.hbm.render.block.nativeblock;

import java.util.Arrays;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Exact atlas materials selected by the original conveyor icon callbacks. */
public final class ConveyorMaterials {
    public final TextureAtlasSprite belt, side, curveLeft, curveRight, concrete, iron, grate;
    public ConveyorMaterials(TextureAtlasSprite belt, TextureAtlasSprite side, TextureAtlasSprite curveLeft,
            TextureAtlasSprite curveRight, TextureAtlasSprite concrete, TextureAtlasSprite iron, TextureAtlasSprite grate) {
        this.belt = belt; this.side = side; this.curveLeft = curveLeft; this.curveRight = curveRight;
        this.concrete = concrete; this.iron = iron; this.grate = grate;
    }
    public TextureAtlasSprite[] beltFaces(int metadata, boolean bendable) {
        int path = bendable ? metadata >= 6 && metadata <= 9 ? 1 : metadata >= 10 && metadata <= 13 ? 2 : 0 : 0;
        TextureAtlasSprite[] result = new TextureAtlasSprite[6];
        for(int face = 0; face < 6; face++) {
            if(path > 0 && face > 1
                    || (metadata == 2 || metadata == 3) && (face == 4 || face == 5)
                    || (metadata == 4 || metadata == 5) && (face == 2 || face == 3)) result[face] = side;
            else result[face] = path == 1 ? curveLeft : path == 2 ? curveRight : belt;
        }
        return result;
    }
    public static TextureAtlasSprite[] uniform(TextureAtlasSprite sprite) {
        TextureAtlasSprite[] faces = new TextureAtlasSprite[6];
        Arrays.fill(faces, sprite);
        return faces;
    }
}
