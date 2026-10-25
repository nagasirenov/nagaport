package com.hbm.blocks.generic;

import java.util.List;
import javax.annotation.Nullable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

/** Native owner for the original two height-adjustable steel grates. */
public class BlockGrate extends Block implements ITooltipProvider {
	@Override
	public net.minecraft.block.Block setSoundType(net.minecraft.block.SoundType sound) {
		return super.setSoundType(sound);
	}

    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    public static final IUnlistedProperty<RenderSnapshot> RENDER = new IUnlistedProperty<RenderSnapshot>() {
        @Override public String getName() { return "grate_render"; }
        @Override public boolean isValid(RenderSnapshot value) { return value != null; }
        @Override public Class<RenderSnapshot> getType() { return RenderSnapshot.class; }
        @Override public String valueToString(RenderSnapshot value) { return value.pos.toString(); }
    };
    private final boolean wide;
    public BlockGrate(Material material, boolean wide) {
        super(material);
        this.wide = wide;
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
    }
    public boolean isWide() { return wide; }
    @Override protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty<?>[] {METADATA}, new IUnlistedProperty<?>[] {RENDER});
    }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    public float getY(int meta) { return meta == 9 ? -.125F : meta * .125F; }
    public static int renderShift(int meta) { return meta == 9 ? -1 : meta >= 8 ? 1 : 0; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        float y = getY(getMetaFromState(state));
        return new AxisAlignedBB(0, y, 0, 1, y + .125F - (wide ? .001F : 0), 1);
    }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoundingBox(state, world, pos);
    }
    @Override public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        int meta = getMetaFromState(state);
        return side == EnumFacing.UP && meta == 7 || side == EnumFacing.DOWN && meta == 0;
    }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        return isSideSolid(state, world, pos, side) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX,
            float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getStateFromMeta(side == EnumFacing.DOWN ? 7 : side == EnumFacing.UP ? 0 : (int)Math.floor(hitY * 8D));
    }
    private static AxisAlignedBB worldCollision(World world, BlockPos pos) {
        AxisAlignedBB box = world.getBlockState(pos).getCollisionBoundingBox(world, pos);
        return box == null ? null : box.offset(pos);
    }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        if(player.isSneaking()) {
            int meta = getMetaFromState(world.getBlockState(pos));
            if(meta == 0) {
                BlockPos below = pos.down();
                IBlockState other = world.getBlockState(below);
                AxisAlignedBB box = worldCollision(world, below);
                // Original intentionally asks the BELOW block's isAir at the ABOVE position.
                if(!other.getBlock().isAir(other, world, pos.up()) && (box == null || box.maxY - (double)pos.getY() < -.05))
                    world.setBlockState(pos, getStateFromMeta(9), 3);
            } else if(meta == 7) {
                BlockPos above = pos.up();
                IBlockState other = world.getBlockState(above);
                AxisAlignedBB box = worldCollision(world, above);
                if(!other.getBlock().isAir(other, world, above) && (box == null || box.minY - (double)above.getY() > .05))
                    world.setBlockState(pos, getStateFromMeta(8), 3);
            }
        }
    }
    @Override public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos fromPos) {
        if(world.isRemote) return;
        int meta = getMetaFromState(world.getBlockState(pos));
        boolean breakIt = false;
        if(meta == 9) {
            AxisAlignedBB box = worldCollision(world, pos.down());
            breakIt = !(box == null || box.maxY - (double)pos.getY() < -.05);
        } else if(meta == 8) {
            AxisAlignedBB box = worldCollision(world, pos.up());
            breakIt = !(box == null || box.minY - (double)(pos.getY() + 1) > .05);
        }
        if(breakIt) {
            dropBlockAsItem(world, pos, getStateFromMeta(0), 0);
            world.setBlockToAir(pos);
        }
    }
    @Override public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB query,
            List<AxisAlignedBB> boxes, @Nullable Entity entity, boolean actualState) {
        if(!wide || !(entity instanceof EntityItem || entity instanceof EntityXPOrb)) {
            super.addCollisionBoxToList(state, world, pos, query, boxes, entity, actualState);
            return;
        }
        int meta = getMetaFromState(world.getBlockState(pos));
        // Deliberately uses meta*.125, NOT getY(meta), including special metadata 9.
        if(entity.posY < pos.getY() + meta * .125D + .375) {
            entity.motionX = 0;
            entity.motionY = -.25;
            entity.motionZ = 0;
            entity.setPosition(entity.posX, entity.posY - .125, entity.posZ);
        }
    }
    @Override public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean extended) {
        list.add(I18nUtil.resolveKey(getTranslationKey() + ".desc"));
    }
    @Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return ((IExtendedBlockState)state).withProperty(RENDER, new RenderSnapshot(this, state, world, pos));
    }
    public static RenderSnapshot renderSnapshot(IBlockState state) {
        if(!(state instanceof IExtendedBlockState)) throw new IllegalArgumentException("Grate model requires its render snapshot");
        RenderSnapshot snapshot = ((IExtendedBlockState)state).getValue(RENDER);
        if(snapshot == null) throw new IllegalArgumentException("Grate model requires its render snapshot");
        return snapshot;
    }
    public static final class RenderSnapshot {
        public final IBlockAccess world;
        public final BlockPos pos, renderPos;
        public final IBlockState state;
        public final int visible;
        RenderSnapshot(BlockGrate block, IBlockState state, IBlockAccess world, BlockPos pos) {
            this.world = world;
            this.pos = pos.toImmutable();
            this.state = ((IExtendedBlockState)state).getClean();
            this.renderPos = pos.add(0, renderShift(block.getMetaFromState(state)), 0);
            AxisAlignedBB ownerBounds = block.getBoundingBox(state, world, pos);
            int faces = 0;
            for(EnumFacing side : EnumFacing.values()) {
                // Inherited 1.7 Block.shouldSideBeRendered used owner bounds, NOT the renderer's normalized bounds.
                boolean recessed = side == EnumFacing.DOWN && ownerBounds.minY > 0 || side == EnumFacing.UP && ownerBounds.maxY < 1;
                if(recessed || !world.getBlockState(renderPos.offset(side)).isOpaqueCube()) faces |= 1 << side.getIndex();
            }
            visible = faces;
        }
    }
}
