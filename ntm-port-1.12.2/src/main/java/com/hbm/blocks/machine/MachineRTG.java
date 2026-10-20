package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMachineRTG;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class MachineRTG extends BlockContainer {
	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);

	private static boolean keepInventory;
	private final Random field_149933_a = new Random();

    public MachineRTG(Material p_i45386_1_) {
		super(p_i45386_1_);
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
	}
    
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.ENTITYBLOCK_ANIMATED; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		
		if(this == ModBlocks.machine_rtg_grey)
			return new TileEntityMachineRTG();
		
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			if(this == ModBlocks.machine_rtg_grey) {
				TileEntityMachineRTG entity = (TileEntityMachineRTG) world.getTileEntity(pos);
				if(entity != null)
				{
					FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void breakBlock(World p_149749_1_, BlockPos pos, IBlockState state)
    {
        int p_149749_2_ = pos.getX(), p_149749_3_ = pos.getY(), p_149749_4_ = pos.getZ();
        Block p_149749_5_ = state.getBlock();
        if (!keepInventory)
        {
			if (this == ModBlocks.machine_rtg_grey) {
				TileEntityMachineRTG tileentityfurnace = (TileEntityMachineRTG) p_149749_1_.getTileEntity(pos);

				if (tileentityfurnace != null) {
					for (int i1 = 0; i1 < tileentityfurnace.getSizeInventory(); ++i1) {
						ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);

						if (itemstack != null && !itemstack.isEmpty()) {
							float f = this.field_149933_a.nextFloat() * 0.8F + 0.1F;
							float f1 = this.field_149933_a.nextFloat() * 0.8F + 0.1F;
							float f2 = this.field_149933_a.nextFloat() * 0.8F + 0.1F;

							while (!itemstack.isEmpty()) {
								int j1 = this.field_149933_a.nextInt(21) + 10;

								if (j1 > itemstack.getCount()) {
									j1 = itemstack.getCount();
								}

								// Preserve item identity/damage before the native source becomes EMPTY.
                                net.minecraft.item.Item droppedItem = itemstack.getItem();
                                int droppedDamage = itemstack.getItemDamage();
                                NBTTagCompound droppedTag = itemstack.getTagCompound();
                                itemstack.shrink(j1);
								EntityItem entityitem = new EntityItem(p_149749_1_, p_149749_2_ + f, p_149749_3_ + f1,
										p_149749_4_ + f2,
										new ItemStack(droppedItem, j1, droppedDamage));

								if (droppedTag != null) {
									entityitem.getItem()
											.setTagCompound(droppedTag.copy());
								}

								float f3 = 0.05F;
								entityitem.motionX = (float) this.field_149933_a.nextGaussian() * f3;
								entityitem.motionY = (float) this.field_149933_a.nextGaussian() * f3 + 0.2F;
								entityitem.motionZ = (float) this.field_149933_a.nextGaussian() * f3;
								p_149749_1_.spawnEntity(entityitem);
							}
						}
					}

					p_149749_1_.updateComparatorOutputLevel(pos, p_149749_5_);
				}
			}
		}

        super.breakBlock(p_149749_1_, pos, state);
    }
}
