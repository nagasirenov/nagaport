package com.hbm.blocks.network;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityDroneDock;
import com.hbm.tileentity.network.TileEntityDroneProvider;
import com.hbm.tileentity.network.TileEntityDroneRequester;
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

import java.util.List;
import java.util.Random;

public class DroneDock extends BlockContainer implements ITooltipProvider {

	@SideOnly(Side.CLIENT)
	private IIcon iconSide;
	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconBottom;

	public DroneDock() {
		super(Material.IRON);
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		// The original dock/crate blocks registered side, top and bottom faces.
		String base = this.getRegistryName().toString();
		if(base.startsWith("hbm:tile.")) base = "hbm:" + base.substring(9);
		this.iconSide = reg.registerIcon(base + "_side");
		this.iconTop = reg.registerIcon(base + "_top");
		this.iconBottom = reg.registerIcon(base + "_bottom");
	}

	/** Legacy side icon lookup; null until the atlas pass has run. */
	@SideOnly(Side.CLIENT)
	public IIcon getLegacyIcon(int side) {
		if(side == 0) return this.iconBottom;
		if(side == 1) return this.iconTop;
		return this.iconSide;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return getLegacyIcon(side);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		if(this == ModBlocks.drone_dock) return new TileEntityDroneDock();
		if(this == ModBlocks.drone_crate_provider) return new TileEntityDroneProvider();
		if(this == ModBlocks.drone_crate_requester) return new TileEntityDroneRequester();

		return null;
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
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}

	@Override
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		if(this == ModBlocks.drone_dock) this.dropContents(world, x, y, z, block, meta, 0, 9);
		if(this == ModBlocks.drone_crate_provider) this.dropContents(world, x, y, z, block, meta, 0, 9);
		if(this == ModBlocks.drone_crate_requester) this.dropContents(world, x, y, z, block, meta, 9, 18);
		super.breakBlock(world, pos, state);
	}

	private final Random rand = new Random();
	public void dropContents(World world, int x, int y, int z, Block block, int meta, int start, int end) {
		ISidedInventory sidedInventory = (ISidedInventory) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(sidedInventory != null) {

			for(int i1 = start; i1 < end; ++i1) {
				ItemStack stack = sidedInventory.getStackInSlot(i1);

				if(stack != null && !stack.isEmpty()) {
					float f = this.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

					while(stack.getCount() > 0) {
						int j1 = this.rand.nextInt(21) + 10;

						if(j1 > stack.getCount()) {
							j1 = stack.getCount();
						}

						// Capture before depletion: native empty stacks hide their item.
						net.minecraft.item.Item dropItem = stack.getItem();
						int dropMeta = stack.getItemDamage();
						NBTTagCompound dropTag = stack.getTagCompound();
						stack.shrink(j1);
						EntityItem entity = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(dropItem, j1, dropMeta));

						if(dropTag != null) {
							entity.getItem().setTagCompound(dropTag.copy());
						}

						float f3 = 0.05F;
						entity.motionX = (float) this.rand.nextGaussian() * f3;
						entity.motionY = (float) this.rand.nextGaussian() * f3 + 0.2F;
						entity.motionZ = (float) this.rand.nextGaussian() * f3;
						world.spawnEntity(entity);
					}
				}
			}

			world.updateComparatorOutputLevel(new net.minecraft.util.math.BlockPos(x, y, z), block);
		}
	}
}
