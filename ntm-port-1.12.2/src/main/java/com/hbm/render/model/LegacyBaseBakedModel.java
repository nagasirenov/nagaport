package com.hbm.render.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.lwjgl.util.vector.Vector3f;

import com.hbm.blocks.BlockBase;
import com.hbm.items.ItemEnumMulti;
import com.hbm.render.icon.IIcon;
import com.hbm.render.item.nativeitem.NativeItemParticles;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Baked full-cube models backed by the sprites registered through NTM's
 * legacy icon callbacks, plus flat enum-subtype item models.
 */
@SideOnly(Side.CLIENT)
public final class LegacyBaseBakedModel implements IBakedModel {

	private static final FaceBakery FACE_BAKERY = new FaceBakery();

	private final BlockBase block;
	private final Item item;
	private final int metadata;
	private final boolean inventoryCube;
	private final TextureAtlasSprite itemSprite;
	private final TextureAtlasSprite particleSprite;
	private final boolean threeDimensional;
	private final Map<Integer, BakedQuad[]> blockQuadCache = new ConcurrentHashMap<>();
	private final List<BakedQuad> flatItemQuads;

	private LegacyBaseBakedModel(BlockBase block, Item item, int metadata, boolean inventoryCube,
			TextureAtlasSprite itemSprite, TextureAtlasSprite particleSprite, boolean threeDimensional) {
		this.block = block;
		this.item = item;
		this.metadata = metadata;
		this.inventoryCube = inventoryCube;
		this.itemSprite = itemSprite;
		this.particleSprite = particleSprite;
		this.threeDimensional = threeDimensional;
		this.flatItemQuads = itemSprite == null || threeDimensional
				? Collections.<BakedQuad>emptyList() : makeFlatItemQuads(itemSprite);
	}

	public static LegacyBaseBakedModel forBlock(BlockBase block) {
		return new LegacyBaseBakedModel(block, null, 0, false, null,
				resolve(block.getIcon(1, 0)), true);
	}

	public static LegacyBaseBakedModel forBlockItem(BlockBase block, Item item, int metadata) {
		TextureAtlasSprite particle = resolve(block.getIcon(1, metadata));
		return new LegacyBaseBakedModel(block, item, metadata, true, null, particle, true);
	}

	public static LegacyBaseBakedModel forFlatItem(Item item, int metadata, TextureAtlasSprite sprite) {
		return new LegacyBaseBakedModel(null, item, metadata, false, sprite, sprite, false);
	}

	public static LegacyBaseBakedModel forEnumItem(ItemEnumMulti item, int metadata) {
		return forFlatItem(item, metadata, resolve(item.getIconFromDamage(metadata)));
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		if(block == null) {
			if(itemSprite == null || side != null) return Collections.emptyList();
			return flatItemQuads;
		}

		int meta = inventoryCube || state == null ? metadata : block.getMetaFromState(state);
		BakedQuad[] quads = blockQuadCache.get(meta);
		if(quads == null) {
			quads = makeCubeQuads(meta);
			BakedQuad[] previous = blockQuadCache.putIfAbsent(meta, quads);
			if(previous != null) quads = previous;
		}

		if(side != null) {
			BakedQuad quad = quads[side.ordinal()];
			return quad == null ? Collections.<BakedQuad>emptyList() : Collections.singletonList(quad);
		}
		if(!inventoryCube) return Collections.emptyList();

		List<BakedQuad> result = new ArrayList<>(6);
		for(BakedQuad quad : quads) if(quad != null) result.add(quad);
		return result;
	}

	private BakedQuad[] makeCubeQuads(int meta) {
		BakedQuad[] quads = new BakedQuad[EnumFacing.values().length];
		for(EnumFacing facing : EnumFacing.values()) {
			TextureAtlasSprite sprite = resolve(block.getIcon(facing.getIndex(), meta));
			if(sprite != null) {
				quads[facing.ordinal()] = bakeFace(facing, sprite, 0.0F, 0.0F, 0.0F,
					16.0F, 16.0F, 16.0F, facing, true);
			}
		}
		return quads;
	}

	private static List<BakedQuad> makeFlatItemQuads(TextureAtlasSprite sprite) {
		List<BakedQuad> quads = new ArrayList<>(2);
		quads.add(bakeFace(EnumFacing.NORTH, sprite, 0.0F, 0.0F, 7.5F,
			16.0F, 16.0F, 8.5F, null, false));
		quads.add(bakeFace(EnumFacing.SOUTH, sprite, 0.0F, 0.0F, 7.5F,
			16.0F, 16.0F, 8.5F, null, false));
		return Collections.unmodifiableList(quads);
	}

	private static BakedQuad bakeFace(EnumFacing facing, TextureAtlasSprite sprite,
			float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
			EnumFacing cullFace, boolean shade) {
		BlockPartFace face = new BlockPartFace(cullFace, 0, sprite.getIconName(),
				new BlockFaceUV(new float[] {0.0F, 0.0F, 16.0F, 16.0F}, 0));
		return FACE_BAKERY.makeBakedQuad(new Vector3f(minX, minY, minZ),
				new Vector3f(maxX, maxY, maxZ), face, sprite, facing,
				ModelRotation.X0_Y0, null, true, shade);
	}

	private static TextureAtlasSprite resolve(IIcon icon) {
		TextureAtlasSprite sprite = NativeItemParticles.spriteOf(icon, "LegacyBaseBakedModel");
		return sprite != null ? sprite : net.minecraft.client.Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return threeDimensional;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return particleSprite;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return inventoryCube ? BlockItemTransforms.BLOCK : ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}

	public BlockBase getBlock() {
		return block;
	}

	public Item getItem() {
		return item;
	}

	public int getMetadata() {
		return metadata;
	}
}
