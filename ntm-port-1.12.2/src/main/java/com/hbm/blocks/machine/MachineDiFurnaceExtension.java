package com.hbm.blocks.machine;

import com.hbm.blocks.IProxyController;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDiFurnace;
import com.hbm.util.Compat;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;

/** Original extension geometry is owned by the native atlas-backed baked model. */
public class MachineDiFurnaceExtension extends BlockContainer implements IProxyController {
    public MachineDiFurnaceExtension() { super(Material.ROCK); }
    @Override public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityProxyCombo().inventory().fluid();
    }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(!player.isSneaking()) {
            TileEntity tile = world.getTileEntity(pos.down());
            if(tile instanceof TileEntityDiFurnace) {
                FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY() - 1, pos.getZ());
                return true;
            }
        }
        return false;
    }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        // The old non-normal block was not a solid support surface for attachments.
        return BlockFaceShape.UNDEFINED;
    }
    @Override public TileEntity getCore(World world, int x, int y, int z) {
        TileEntity tile = Compat.getTileStandard(world, x, y - 1, z);
        return tile instanceof TileEntityDiFurnace ? tile : null;
    }
}
