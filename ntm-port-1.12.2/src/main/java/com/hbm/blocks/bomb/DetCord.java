package com.hbm.blocks.bomb;

import com.hbm.entity.item.EntityTNTPrimedBase;

import net.minecraft.util.math.MathHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.properties.IProperty;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import com.hbm.blocks.network.ConnectionState;
import com.hbm.lib.ForgeDirection;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class DetCord extends BlockDetonatable implements IDetConnectible {

	public DetCord(Material material) {
		super(material, 0, 0, 0, false, false);
	}
	
	@Override protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty<?>[0], new IUnlistedProperty<?>[] {ConnectionState.CONNECTIONS});
    }
    @Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        int mask = 0;
        EnumFacing[] sides = {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH};
        for(int i = 0; i < sides.length; i++) {
            BlockPos next = pos.offset(sides[i]);
            if(IDetConnectible.isConnectible(world, next.getX(), next.getY(), next.getZ(), ForgeDirection.getOrientation(sides[i].getOpposite().getIndex()))) mask |= 32 >> i;
        }
        return ((IExtendedBlockState)state).withProperty(ConnectionState.CONNECTIONS, mask);
    }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(world.isBlockPowered(new BlockPos(x, y, z))) {
			this.explode(world, x, y, z);
		}
	}

	public void explode(World world, int x, int y, int z) {
		if(!world.isRemote) {
			world.setBlockToAir(new BlockPos(x, y, z));
			world.createExplosion(null, x + 0.5, y + 0.5, z + 0.5, 1.5F, true);
		}
	}

	@Override
	public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
		explode(world, MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
	}
}
