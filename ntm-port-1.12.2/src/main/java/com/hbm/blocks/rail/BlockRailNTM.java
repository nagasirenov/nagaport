package com.hbm.blocks.rail;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.rail.RailStandardSwitch.TileEntityRailSwitch;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

/** Native state/model boundary shared only by the original eleven custom rail owners. */
public abstract class BlockRailNTM extends BlockDummyable {
    public static final IUnlistedProperty<Integer> SWITCH = new IUnlistedProperty<Integer>() {
        @Override public String getName() { return "rail_switch"; }
        @Override public boolean isValid(Integer value) { return value != null && value >= 0 && value <= 2; }
        @Override public Class<Integer> getType() { return Integer.class; }
        @Override public String valueToString(Integer value) { return value.toString(); }
    };
    protected BlockRailNTM(Material material) { super(material); }
    @Override protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty<?>[] {METADATA}, new IUnlistedProperty<?>[] {SWITCH});
    }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        // No tile means no sign in the original renderer; 1/2 select straight/turn.
        int sign = tile instanceof TileEntityRailSwitch ? (((TileEntityRailSwitch)tile).isSwitched ? 2 : 1) : 0;
        return ((IExtendedBlockState)state).withProperty(SWITCH, sign);
    }
    public static int switchSign(IBlockState state) {
        if(!(state instanceof IExtendedBlockState)) return 0;
        Integer value = ((IExtendedBlockState)state).getValue(SWITCH);
        return value == null ? 0 : value;
    }
    protected static int metadata(IBlockAccess world, int x, int y, int z) {
        IBlockState state = world.getBlockState(new BlockPos(x, y, z));
        return state.getBlock().getMetaFromState(state);
    }
}
