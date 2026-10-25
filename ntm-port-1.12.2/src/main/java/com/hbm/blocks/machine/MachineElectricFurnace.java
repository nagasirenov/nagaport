package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import java.util.Random;

public class MachineElectricFurnace extends BlockContainer {

    private final Random field_149933_a = new Random();
	private final boolean isActive;
	private static boolean keepInventory;

	/** Retain every legacy metadata value, including the non-horizontal texture cases. */
	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);

	public MachineElectricFurnace(boolean blockState) {
		super(Material.IRON);
		isActive = blockState;
		setDefaultState(this.blockState.getBaseState().withProperty(METADATA, 0));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, METADATA);
	}

	@Override
	public IBlockState getStateFromMeta(int metadata) {
		return getDefaultState().withProperty(METADATA, metadata);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(METADATA);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}


	@Override
	public Item getItemDropped(IBlockState state, Random p_149650_2_, int p_149650_3_)
    {
        return Item.getItemFromBlock(ModBlocks.machine_electric_furnace_off);
    }

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		this.setDefaultDirection(world, pos);
	}

	private void setDefaultDirection(World world, BlockPos pos) {
		if(!world.isRemote)
		{
			// func_149730_j used the cached opaque/full-block flag, not dynamic opacity.
			IBlockState block1 = world.getBlockState(pos.north());
			IBlockState block2 = world.getBlockState(pos.south());
			IBlockState block3 = world.getBlockState(pos.west());
			IBlockState block4 = world.getBlockState(pos.east());

			byte b0 = 3;

			if(block1.isFullBlock() && !block2.isFullBlock())
			{
				b0 = 3;
			}
			if(block2.isFullBlock() && !block1.isFullBlock())
			{
				b0 = 2;
			}
			if(block3.isFullBlock() && !block4.isFullBlock())
			{
				b0 = 5;
			}
			if(block4.isFullBlock() && !block3.isFullBlock())
			{
				b0 = 4;
			}

			world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, (int)b0), 2);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0)
		{
			world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, 2), 2);
		}
		if(i == 1)
		{
			world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, 5), 2);
		}
		if(i == 2)
		{
			world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, 3), 2);
		}
		if(i == 3)
		{
			world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, 4), 2);
		}

		if(itemStack.hasDisplayName())
		{
			((TileEntityMachineElectricFurnace)world.getTileEntity(pos)).setCustomName(itemStack.getDisplayName());
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			TileEntityMachineElectricFurnace entity = (TileEntityMachineElectricFurnace) world.getTileEntity(pos);
			if(entity != null)
			{
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityMachineElectricFurnace();
	}

	public static void updateBlockState(boolean isProcessing, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		int i = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
		TileEntity entity = world.getTileEntity(pos);
		keepInventory = true;

		if(isProcessing)
		{
			world.setBlockState(pos, ModBlocks.machine_electric_furnace_on.getDefaultState());
		}else{
			world.setBlockState(pos, ModBlocks.machine_electric_furnace_off.getDefaultState());
		}

		keepInventory = false;
		world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, i), 2);

		if(entity != null) {
			entity.validate();
			world.setTileEntity(pos, entity);
		}
	}

	@Override
	public void breakBlock(World p_149749_1_, BlockPos pos, IBlockState state)
    {
        int p_149749_2_ = pos.getX();
        int p_149749_3_ = pos.getY();
        int p_149749_4_ = pos.getZ();
        if (!keepInventory)
        {
        	TileEntityMachineElectricFurnace tileentityfurnace = (TileEntityMachineElectricFurnace)p_149749_1_.getTileEntity(pos);

            if (tileentityfurnace != null)
            {
                for (int i1 = 0; i1 < tileentityfurnace.getSizeInventory(); ++i1)
                {
                    ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);

                    if (itemstack != null && !itemstack.isEmpty())
                    {
                        float f = this.field_149933_a.nextFloat() * 0.8F + 0.1F;
                        float f1 = this.field_149933_a.nextFloat() * 0.8F + 0.1F;
                        float f2 = this.field_149933_a.nextFloat() * 0.8F + 0.1F;

                        while (itemstack.getCount() > 0)
                        {
                            int j1 = this.field_149933_a.nextInt(21) + 10;

                            if (j1 > itemstack.getCount())
                            {
                                j1 = itemstack.getCount();
                            }

                            // Capture before depletion: native empty stacks hide their item.
                            net.minecraft.item.Item dropItem = itemstack.getItem();
                            int dropMeta = itemstack.getItemDamage();
                            NBTTagCompound dropTag = itemstack.getTagCompound();
                            itemstack.shrink(j1);
                            EntityItem entityitem = new EntityItem(p_149749_1_, p_149749_2_ + f, p_149749_3_ + f1, p_149749_4_ + f2, new ItemStack(dropItem, j1, dropMeta));

                            if (dropTag != null)
                            {
                                entityitem.getItem().setTagCompound(dropTag.copy());
                            }

                            float f3 = 0.05F;
                            entityitem.motionX = (float)this.field_149933_a.nextGaussian() * f3;
                            entityitem.motionY = (float)this.field_149933_a.nextGaussian() * f3 + 0.2F;
                            entityitem.motionZ = (float)this.field_149933_a.nextGaussian() * f3;
                            p_149749_1_.spawnEntity(entityitem);
                        }
                    }
                }

                p_149749_1_.updateComparatorOutputLevel(pos, state.getBlock());
            }
        }

        super.breakBlock(p_149749_1_, pos, state);
    }

	@Override
	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World p_149734_1_, BlockPos pos, Random rand)
    {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        if (isActive)
        {
            int l = getMetaFromState(state);
            float f = x + 0.5F;
            float f1 = y + 0.0F + rand.nextFloat() * 6.0F / 16.0F;
            float f2 = z + 0.5F;
            float f3 = 0.52F;
            float f4 = rand.nextFloat() * 0.6F - 0.3F;

            if (l == 4)
            {
                p_149734_1_.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                p_149734_1_.spawnParticle(EnumParticleTypes.FLAME, f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
            }
            else if (l == 5)
            {
                p_149734_1_.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                p_149734_1_.spawnParticle(EnumParticleTypes.FLAME, f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
            }
            else if (l == 2)
            {
                p_149734_1_.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
                p_149734_1_.spawnParticle(EnumParticleTypes.FLAME, f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
            }
            else if (l == 3)
            {
                p_149734_1_.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
                p_149734_1_.spawnParticle(EnumParticleTypes.FLAME, f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
            }
        }
    }

	@SideOnly(Side.CLIENT)
    public ItemStack getItem(World world, BlockPos pos, IBlockState state)
    {
        return new ItemStack(ModBlocks.machine_electric_furnace_off);
    }
}
