package com.hbm.blocks.generic;

import com.hbm.items.armor.ArmorFSB;
import com.hbm.items.armor.ArmorFSBPowered;
import com.hbm.main.ModSoundEvents;
import api.hbm.energymk2.IBatteryItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class HEVBattery extends Block {
    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(6D / 16D, 0, 6D / 16D, 10D / 16D, 6D / 16D, 10D / 16D);
    public HEVBattery(Material material) {
        super(material);
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
    }
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
    @Override public IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(METADATA, metadata & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return BOUNDS; }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return BOUNDS; }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(world.isRemote) return true;
        if(player.isSneaking()) return false;
        if(ArmorFSB.hasFSBArmorIgnoreCharge(player) && player.inventory.armorInventory.get(3).getItem() instanceof ArmorFSBPowered) {
            for(ItemStack stack : player.inventory.armorInventory) {
                if(stack.isEmpty()) continue;
                if(stack.getItem() instanceof IBatteryItem) {
                    IBatteryItem battery = (IBatteryItem)stack.getItem();
                    long maxcharge = battery.getMaxCharge(stack);
                    long charge = battery.getCharge(stack);
                    long newcharge = Math.min(charge + 150000, maxcharge);
                    battery.setCharge(stack, newcharge);
                }
            }
            world.playSound(null, player.posX, player.posY, player.posZ, ModSoundEvents.ITEM_BATTERY, SoundCategory.PLAYERS, 1F, 1F);
            world.setBlockToAir(pos);
        }
        return true;
    }
}
