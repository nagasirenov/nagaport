package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.lwjgl.util.vector.Vector3f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Six native cull-face quads, with the original world/item texture selectors kept separate. */
public final class ThermalFurnaceBakedModel implements IBakedModel {
    public enum Family { BRICK, RTG, RTG_DIFURNACE, DIFURNACE }
    private final BakedQuad[] faces = new BakedQuad[6];
    private final TextureAtlasSprite particle;
    public ThermalFurnaceBakedModel(Family family, boolean active, boolean inventory, int metadata,
            boolean covered, Function<String, TextureAtlasSprite> sprites) {
        FaceBakery bakery = new FaceBakery();
        for(EnumFacing side : EnumFacing.values()) {
            TextureAtlasSprite sprite = sprites.apply(texture(family, active, inventory, covered, metadata, side));
            BlockPartFace face = new BlockPartFace(side, -1, sprite.getIconName(), new BlockFaceUV(side == EnumFacing.DOWN ? new float[] {0, 16, 16, 0} : new float[] {0, 0, 16, 16}, 0));
            faces[side.getIndex()] = bakery.makeBakedQuad(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16),
                    face, sprite, side, ModelRotation.X0_Y0, null, true, true);
        }
        // Use the original context-free side-zero texture for this metadata.
        particle = sprites.apply(texture(family, active, true, false, metadata, EnumFacing.DOWN));
    }
    public static String texture(Family family, boolean active, boolean inventory, boolean covered, int metadata, EnumFacing face) {
        int side = face.getIndex();
        boolean front = metadata == 0 && side == 3 || side == metadata;
        String name;
        switch(family) {
            case BRICK:
                name = front ? "machine_furnace_brick_front_" + (active ? "on" : "off")
                        : side == 1 ? "machine_furnace_brick_top" : side == 0 ? "machine_furnace_brick_bottom" : "machine_furnace_brick_side";
                break;
            case RTG:
                name = front ? "machine_rtg_furnace_" + (active ? "on" : "off") + "_alt"
                        : side <= 1 ? "machine_rtg_furnace_base_alt" : "machine_rtg_furnace_side_alt";
                break;
            case RTG_DIFURNACE:
                name = front ? "rtg_difurnace_front_" + (active ? "on" : "off")
                        : side == 1 ? "rtg_difurnace_top_" + (active ? "on" : "off") : "machine_rtg_furnace_side_alt";
                break;
            case DIFURNACE:
                if(!inventory && side == 0) name = "brick_fire";
                else if(!inventory && covered) name = front ? "difurnace_front_" + (active ? "on" : "off") + "_tall"
                        : side == 1 ? "brick_fire" : "difurnace_side_tall";
                else name = front ? "difurnace_front_" + (active ? "on" : "off") + "_alt"
                        : side == 1 ? "difurnace_top_" + (active ? "on" : "off") + "_alt" : "difurnace_side_alt";
                break;
            default: throw new AssertionError(family);
        }
        return "hbm:blocks/" + name;
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        // RenderItem asks every face as well as null: never duplicate inventory quads.
        return side == null ? Collections.emptyList() : Collections.singletonList(faces[side.getIndex()]);
    }
    @Override public boolean isAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return particle; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
