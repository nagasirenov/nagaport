package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityICFPress;
import com.hbm.util.i18n.I18nUtil;

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
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class MachineICFPress extends BlockContainer implements ITooltipProvider {

	protected IIcon blockIcon;
	
	@SideOnly(Side.CLIENT) private IIcon iconTop;

	public MachineICFPress() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityICFPress();
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":machine_icf_press_top");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":machine_icf_press_side");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : (side == 0 ? this.iconTop : this.blockIcon);
	}
	
	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state)
    {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        Block block = state.getBlock();

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		if(!(te instanceof ISidedInventory)) return;
		ISidedInventory tileentityfurnace = (ISidedInventory) te;
		if(tileentityfurnace != null) {
			for(int i1 = 0; i1 < tileentityfurnace.getSizeInventory(); ++i1) {
				ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);
				if(itemstack != null && !itemstack.isEmpty()) {
					float f = world.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
					while(itemstack.getCount() > 0) {
						int j1 = world.rand.nextInt(21) + 10;
						if(j1 > itemstack.getCount()) j1 = itemstack.getCount();
						// Capture before depletion: native empty stacks hide their item.
						net.minecraft.item.Item dropItem = itemstack.getItem();
						int dropMeta = itemstack.getItemDamage();
						NBTTagCompound dropTag = itemstack.getTagCompound();
						itemstack.shrink(j1);
						EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(dropItem, j1, dropMeta));
						if(dropTag != null) entityitem.getItem().setTagCompound(dropTag.copy());
						float f3 = 0.05F;
						entityitem.motionX = (float) world.rand.nextGaussian() * f3;
						entityitem.motionY = (float) world.rand.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = (float) world.rand.nextGaussian() * f3;
						world.spawnEntity(entityitem);
					}
				}
			}
			world.updateComparatorOutputLevel(pos, block);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		for(String s : I18nUtil.resolveKeyArray(((Block)this).getTranslationKey() + ".desc")) list.add(TextFormatting.YELLOW + s);
	}
}
