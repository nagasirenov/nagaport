package com.hbm.render.block;

import java.util.*;
import com.hbm.render.block.nativeblock.UniversalCuboidGeometry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/** Native baked-model consumer of the original universal owner geometry. */
public final class RenderISBRHUniversal implements IBakedModel {
    private final ISBRHUniversal owner;
    private final IBlockState fallback;
    private final boolean inventory;
    private final TextureAtlasSprite sprite;
    private final ItemCameraTransforms transforms;
    public RenderISBRHUniversal(Block block, int metadata, boolean inventory, TextureAtlasSprite sprite, ItemCameraTransforms transforms) {
        this.owner=(ISBRHUniversal)block;this.fallback=block.getStateFromMeta(metadata);this.inventory=inventory;this.sprite=sprite;this.transforms=transforms;
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        IBlockState actual=state==null?fallback:state;
        List<BakedQuad> result=new ArrayList<>();
        if(inventory || owner.renderAllFaces()) {
            if(side==null) return UniversalCuboidGeometry.bake(owner.getRenderBoxes(actual,inventory),sprite,inventory,inventory?owner.getInventoryYOffset(actual):0);
            return Collections.emptyList();
        }
        // Sandbags retain the original boundary-face culling, unlike the other
        // owners whose original shouldSideBeRendered explicitly returned true.
        for(AxisAlignedBB box:owner.getRenderBoxes(actual,false)) for(EnumFacing face:EnumFacing.values()) {
            boolean boundary=face==EnumFacing.DOWN?box.minY<=0:face==EnumFacing.UP?box.maxY>=1:
                face==EnumFacing.NORTH?box.minZ<=0:face==EnumFacing.SOUTH?box.maxZ>=1:
                face==EnumFacing.WEST?box.minX<=0:box.maxX>=1;
            if(side==(boundary?face:null)) result.add(UniversalCuboidGeometry.face(box,sprite,false,0,face));
        }
        return result;
    }
    @Override public boolean isAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return sprite; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return transforms; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
