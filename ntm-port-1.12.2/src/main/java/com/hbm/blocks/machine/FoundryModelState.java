package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.machine.TileEntityFoundryBase;
import com.hbm.tileentity.machine.TileEntityFoundryOutlet;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

/** Immutable render-only snapshot, rebuilt from the current chunk/tile view, never a cached tile reference. */
public final class FoundryModelState {
    public final int neighbors, outlets, amount, capacity, color;
    public final boolean material, filter, closed;
    public FoundryModelState(int neighbors, int outlets, int amount, int capacity, int color, boolean material, boolean filter, boolean closed) {
        this.neighbors=neighbors;this.outlets=outlets;this.amount=amount;this.capacity=capacity;this.color=color;
        this.material=material;this.filter=filter;this.closed=closed;
    }
    public static final IUnlistedProperty<FoundryModelState> CONTENTS = new IUnlistedProperty<FoundryModelState>() {
        @Override public String getName() { return "foundry_contents"; }
        @Override public boolean isValid(FoundryModelState value) { return value != null; }
        @Override public Class<FoundryModelState> getType() { return FoundryModelState.class; }
        @Override public String valueToString(FoundryModelState value) { return value.toString(); }
    };
    public static BlockStateContainer create(Block block) { return new ExtendedBlockState(block,new IProperty<?>[]{FoundryBlock.METADATA},new IUnlistedProperty<?>[]{CONTENTS}); }
    public static IBlockState extend(IBlockState state, IBlockAccess world, BlockPos pos) {
        int neighbors=0,outlets=0;Block block=state.getBlock();
        for(EnumFacing side:EnumFacing.values()) {
            BlockPos next=pos.offset(side);IBlockState adjacent=world.getBlockState(next);Block neighbor=adjacent.getBlock();
            boolean connect = block instanceof FoundryChannel
                    ? ((FoundryChannel)block).canConnectTo(world,pos.getX(),pos.getY(),pos.getZ(),ForgeDirection.getOrientation(side.getIndex()))
                    : neighbor == ModBlocks.foundry_tank;
            if(connect) neighbors |= 1 << side.getIndex();
            // Slagtaps deliberately did not open a tank wall in the original renderer.
            if(neighbor == ModBlocks.foundry_outlet && neighbor.getMetaFromState(adjacent) == side.getIndex()) outlets |= 1 << side.getIndex();
        }
        TileEntity tile=world.getTileEntity(pos);TileEntityFoundryBase foundry=tile instanceof TileEntityFoundryBase?(TileEntityFoundryBase)tile:null;
        TileEntityFoundryOutlet outlet=tile instanceof TileEntityFoundryOutlet?(TileEntityFoundryOutlet)tile:null;
        boolean material=foundry!=null && foundry.type!=null;
        FoundryModelState data=new FoundryModelState(neighbors,outlets,foundry==null?0:foundry.amount,foundry==null?0:foundry.getCapacity(),material?foundry.type.moltenColor:0,material,outlet!=null&&outlet.filter!=null,outlet!=null&&outlet.isClosed());
        return ((IExtendedBlockState)state).withProperty(CONTENTS,data);
    }
    public static FoundryModelState get(IBlockState state) {
        if(!(state instanceof IExtendedBlockState)) throw new IllegalArgumentException("Foundry world geometry requires its native snapshot");
        FoundryModelState data=((IExtendedBlockState)state).getValue(CONTENTS);
        if(data==null) throw new IllegalArgumentException("Foundry contents snapshot is missing");
        return data;
    }
    public boolean connects(EnumFacing side) { return (neighbors & 1 << side.getIndex()) != 0; }
    public boolean outlet(EnumFacing side) { return (outlets & 1 << side.getIndex()) != 0; }
}
