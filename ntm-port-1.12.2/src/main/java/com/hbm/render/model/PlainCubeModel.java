package com.hbm.render.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.common.model.IModelState;

/** Native cube material/model pipeline with the original plain-block face UV convention.
 * Vanilla 1.12 flips the DOWN V axis and contracts UVs; neither is original NTM art.
 * Only UV0 is replaced. Geometry, culling, color, normals, diffuse/AO and native
 * perspective matrices remain supplied by the real cube model and renderer.
 */
public final class PlainCubeModel implements IModel {
    private final IModel cube;
    public PlainCubeModel(IModel cube) { this.cube = cube; }
    @Override public Collection<ResourceLocation> getDependencies() { return cube.getDependencies(); }
    @Override public Collection<ResourceLocation> getTextures() { return cube.getTextures(); }
    @Override public IModelState getDefaultState() { return cube.getDefaultState(); }
    @Override public IBakedModel bake(IModelState state, VertexFormat format,
            Function<ResourceLocation, TextureAtlasSprite> textures) {
        return new Baked(cube.bake(state, format, textures));
    }
    private static final class Baked extends BakedModelWrapper<IBakedModel> {
        private final Map<EnumFacing, List<BakedQuad>> faces = new EnumMap<>(EnumFacing.class);
        private final List<BakedQuad> general;
        Baked(IBakedModel model) {
            super(model);
            for(EnumFacing face : EnumFacing.values()) faces.put(face, remap(model.getQuads(null, face, 0)));
            general = remap(model.getQuads(null, null, 0));
        }
        @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing face, long seed) {
            return face == null ? general : faces.get(face);
        }
        @Override public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType type) {
            // BakedModelWrapper's default returns the unwrapped model, losing UV parity.
            return Pair.of(this, originalModel.handlePerspective(type).getRight());
        }
        private static List<BakedQuad> remap(List<BakedQuad> source) {
            List<BakedQuad> result = new ArrayList<>(source.size());
            for(BakedQuad quad : source) {
                VertexFormat format = quad.getFormat();
                int position = -1, uv = -1;
                for(int e = 0; e < format.getElementCount(); e++) {
                    VertexFormatElement element = format.getElement(e);
                    if(element.getUsage() == VertexFormatElement.EnumUsage.POSITION) position = e;
                    if(element.getUsage() == VertexFormatElement.EnumUsage.UV && element.getIndex() == 0) uv = e;
                }
                if(position < 0 || uv < 0) throw new IllegalArgumentException("Plain cube requires position and UV0");
                int[] data = quad.getVertexData().clone();
                TextureAtlasSprite sprite = quad.getSprite();
                for(int v = 0; v < 4; v++) {
                    float[] xyz = new float[4], tex = new float[4];
                    LightUtil.unpack(data, xyz, format, v, position);
                    float u, w;
                    switch(quad.getFace()) {
                    case DOWN: case UP: u = xyz[0]; w = xyz[2]; break;
                    case NORTH: u = 1 - xyz[0]; w = 1 - xyz[1]; break;
                    case SOUTH: u = xyz[0]; w = 1 - xyz[1]; break;
                    case WEST: u = xyz[2]; w = 1 - xyz[1]; break;
                    case EAST: u = 1 - xyz[2]; w = 1 - xyz[1]; break;
                    default: throw new AssertionError(quad.getFace());
                    }
                    tex[0] = sprite.getInterpolatedU(16 * u);
                    tex[1] = sprite.getInterpolatedV(16 * w);
                    LightUtil.pack(tex, data, format, v, uv);
                }
                result.add(new BakedQuad(data, quad.getTintIndex(), quad.getFace(), sprite,
                        quad.shouldApplyDiffuseLighting(), format));
            }
            return Collections.unmodifiableList(result);
        }
    }
}
