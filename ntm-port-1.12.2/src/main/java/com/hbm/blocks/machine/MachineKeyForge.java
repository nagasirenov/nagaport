package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMachineKeyForge;
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

public class MachineKeyForge extends BlockContainer {

	protected IIcon blockIcon;

    private final Random field_149933_a = new Random();
	private static boolean keepInventory;

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconBottom;

	public MachineKeyForge(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityMachineKeyForge();
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {

		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":machine_keyforge_top");
		this.iconBottom = iconRegister.registerIcon(RefStrings.MODID + ":machine_keyforge_bottom");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":machine_keyforge_side");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 0 ? this.iconBottom : (side == 1 ? this.iconTop : this.blockIcon);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			TileEntityMachineKeyForge entity = (TileEntityMachineKeyForge) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
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
	public void breakBlock(World p_149749_1_, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state)
    {
		int p_149749_2_ = pos.getX(), p_149749_3_ = pos.getY(), p_149749_4_ = pos.getZ();
		Block p_149749_5_ = state.getBlock();
		int p_149749_6_ = p_149749_5_.getMetaFromState(state);
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

                p_149749_1_.updateComparatorOutputLevel(new net.minecraft.util.math.BlockPos(p_149749_2_, p_149749_3_, p_149749_4_), p_149749_5_);
            }
        }

        super.breakBlock(p_149749_1_, pos, state);
    }
}
