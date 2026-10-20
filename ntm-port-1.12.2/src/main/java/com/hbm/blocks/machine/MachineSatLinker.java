package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMachineSatLinker;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class MachineSatLinker extends BlockContainer {

    private final Random field_149933_a = new Random();
	private static boolean keepInventory;
	
	@SideOnly(Side.CLIENT)
	private IIcon iconTop;

	public MachineSatLinker(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityMachineSatLinker();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":machine_satlinker_top");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":machine_satlinker_side");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : (side == 0 ? this.iconTop : this.blockIcon);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			TileEntityMachineSatLinker entity = (TileEntityMachineSatLinker) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
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
	public void breakBlock(World p_149749_1_, int p_149749_2_, int p_149749_3_, int p_149749_4_, Block p_149749_5_, int p_149749_6_)
    {
        if (!keepInventory)
        {
        	ISidedInventory tileentityfurnace = (ISidedInventory)p_149749_1_.getTileEntity(new net.minecraft.util.math.BlockPos(p_149749_2_, p_149749_3_, p_149749_4_));

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

                p_149749_1_.func_147453_f(p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_);
            }
        }

        super.breakBlock(p_149749_1_, p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_, p_149749_6_);
    }

}
