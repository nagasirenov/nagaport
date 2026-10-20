package com.hbm.blocks.generic;

import java.util.Random;

import api.hbm.fluid.IFluidStandardSender;
import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.MultipassContext;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.TileEntityLoadedBase;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockFissure extends BlockContainer implements IBlockMultiPass {

    @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return MultipassContext.stateContainer(this); }
    @Override public net.minecraft.block.state.IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(MultipassContext.METADATA, metadata & 15); }
    @Override public int getMetaFromState(net.minecraft.block.state.IBlockState state) { return state.getValue(MultipassContext.METADATA); }
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) { return net.minecraft.util.EnumBlockRenderType.MODEL; }
    @Override public net.minecraft.util.BlockRenderLayer getRenderLayer() { return net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public net.minecraft.block.state.IBlockState getExtendedState(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        return ((net.minecraftforge.common.property.IExtendedBlockState) state).withProperty(MultipassContext.PROPERTY, getMultipassContext(state, world, pos));
    }



	public BlockFissure() {
		super(Material.ROCK);
		this.setBlockUnbreakable();
		this.setResistance(1_000_000);
		this.setTickRandomly(true);
	}





	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, Random rand) {
        boolean crater = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)) != 0;
        net.minecraft.util.math.BlockPos above = pos.up();
        if(world.getBlockState(above).getBlock().isReplaceable(world, above)) world.setBlockState(above, (crater ? ModBlocks.rad_lava_block : ModBlocks.volcanic_lava_block).getDefaultState());
	}

	@Override
	public boolean shouldRenderItemMulti() {
		return true;
	}

	@Override
	public int getPasses() {
		return 2;
	}



	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityFissure();
	}

	public static class TileEntityFissure extends TileEntityLoadedBase implements IFluidStandardSender, net.minecraft.util.ITickable {

		public FluidTank lava = new FluidTank(Fluids.LAVA, 1_000);

		@Override
		public void update() {

			if(!this.world.isRemote) {
				lava.setFill(1_000);
				this.sendFluid(lava, this.world, this.pos.getX(), this.pos.getY() + 1, this.pos.getZ(), ForgeDirection.UP);
			}
		}

		@Override
		public boolean canConnect(FluidType type, ForgeDirection dir) {
			return dir == ForgeDirection.DOWN && type == Fluids.LAVA;
		}

		@Override public FluidTank[] getAllTanks() { return new FluidTank[] {lava}; }
		@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {lava}; }
	}
}
