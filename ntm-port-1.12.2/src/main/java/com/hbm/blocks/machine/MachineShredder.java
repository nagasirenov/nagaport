package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMachineShredder;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class MachineShredder extends BlockContainer {

    private final Random field_149933_a = new Random();
	private static boolean keepInventory;
	
	@SideOnly(Side.CLIENT)
	private IIcon iconBottom;
	private IIcon iconTop;
	private IIcon iconSide;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconBottom = iconRegister.registerIcon(RefStrings.MODID + ":machine_shredder_bottom_alt");
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":machine_shredder_top_alt");
		this.iconSide = iconRegister.registerIcon(RefStrings.MODID + ":machine_shredder_side_alt");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":machine_shredder_front_alt");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		if(side == 0)
			return iconBottom;
		if(side == 1)
			return iconTop;
		if(side == 2 || side == 3)
			return blockIcon;
		if(side == 4 || side == 5)
			return iconSide;
		
		return null;
	}
	
	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_)
    {
        return Item.getItemFromBlock(ModBlocks.machine_shredder);
    }

	public MachineShredder(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityMachineShredder();
	}
	
	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			TileEntityMachineShredder entity = (TileEntityMachineShredder) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(entity != null)
			{
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void breakBlock(World p_149749_1_, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState) {
		int p_149749_2_ = nativePos.getX(); int p_149749_3_ = nativePos.getY(); int p_149749_4_ = nativePos.getZ();
		Block p_149749_5_ = nativeState.getBlock();
        if (!keepInventory)
        {
        	TileEntityMachineShredder tileentityfurnace = (TileEntityMachineShredder)p_149749_1_.getTileEntity(new net.minecraft.util.math.BlockPos(p_149749_2_, p_149749_3_, p_149749_4_));

            if (tileentityfurnace != null)
            {
                for (int i1 = 0; i1 < tileentityfurnace.getSizeInventory(); ++i1)
                {
                    ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);

                    if (itemstack != null)
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

                            // Capture identity before native depletion hides the item.
                            net.minecraft.item.Item spillItem = itemstack.getItem();
                            int spillMeta = itemstack.getItemDamage();
                            NBTTagCompound spillTag = itemstack.getTagCompound();
                            itemstack.shrink(j1);
                            EntityItem entityitem = new EntityItem(p_149749_1_, p_149749_2_ + f, p_149749_3_ + f1, p_149749_4_ + f2, new ItemStack(spillItem, j1, spillMeta));

                            if (spillTag != null)
                            {
                                entityitem.getItem().setTagCompound((NBTTagCompound)spillTag.copy());
                            }

                            float f3 = 0.05F;
                            entityitem.motionX = (float)this.field_149933_a.nextGaussian() * f3;
                            entityitem.motionY = (float)this.field_149933_a.nextGaussian() * f3 + 0.2F;
                            entityitem.motionZ = (float)this.field_149933_a.nextGaussian() * f3;
                            p_149749_1_.spawnEntity(entityitem);
                        }
                    }
                }

                p_149749_1_.updateComparatorOutputLevel(nativePos, p_149749_5_);
            }
        }

        super.breakBlock(p_149749_1_, nativePos, nativeState);
    }
}
