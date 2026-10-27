package com.hbm.blocks.machine;

import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMachineAutocrafter;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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

import java.util.Random;

public class MachineAutocrafter extends BlockContainer {

	protected IIcon blockIcon;

	@SideOnly(Side.CLIENT) private IIcon iconTop;
	@SideOnly(Side.CLIENT) private IIcon iconBottom;

	public MachineAutocrafter() {
		super(Material.IRON);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":machine_autocrafter_top");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":machine_autocrafter_side");
		this.iconBottom = iconRegister.registerIcon(RefStrings.MODID + ":machine_autocrafter_bottom");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : (side == 0 ? this.iconBottom : this.blockIcon);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMachineAutocrafter();
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			TileEntity entity = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(entity instanceof TileEntityMachineAutocrafter) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			}
			return true;
		} else {
			return false;
		}
	}

	private final Random rand = new Random();

	@Override
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		ISidedInventory tile = (ISidedInventory) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tile != null) {

			for(int i1 = 10; i1 < tile.getSizeInventory(); ++i1) {
				ItemStack itemstack = tile.getStackInSlot(i1);

				if(itemstack != null && !itemstack.isEmpty()) {
					float f = this.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

					while(itemstack.getCount() > 0) {
						int j1 = this.rand.nextInt(21) + 10;

						if(j1 > itemstack.getCount()) {
							j1 = itemstack.getCount();
						}

						// Capture before depletion: native empty stacks hide their item.
						net.minecraft.item.Item dropItem = itemstack.getItem();
						int dropMeta = itemstack.getItemDamage();
						NBTTagCompound dropTag = itemstack.getTagCompound();
						itemstack.shrink(j1);
						EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(dropItem, j1, dropMeta));

						if(dropTag != null) {
							entityitem.getItem().setTagCompound(dropTag.copy());
						}

						float f3 = 0.05F;
						entityitem.motionX = (float) this.rand.nextGaussian() * f3;
						entityitem.motionY = (float) this.rand.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = (float) this.rand.nextGaussian() * f3;
						world.spawnEntity(entityitem);
					}
				}
			}

			world.updateComparatorOutputLevel(new net.minecraft.util.math.BlockPos(x, y, z), block);
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}
}
