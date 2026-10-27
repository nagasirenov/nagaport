package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityDroneCrate;
import com.hbm.util.i18n.I18nUtil;

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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DroneCrate extends BlockContainer implements ILookOverlay, ITooltipProvider {

	protected IIcon blockIcon;

	private static Random rand = new Random();

	@SideOnly(Side.CLIENT) private IIcon iconTop;
	@SideOnly(Side.CLIENT) private IIcon iconBottom;

	public DroneCrate() {
		super(Material.IRON);
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityDroneCrate();
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		this.blockIcon = reg.registerIcon(RefStrings.MODID + ":drone_crate_side");
		this.iconTop = reg.registerIcon(RefStrings.MODID + ":drone_crate_top");
		this.iconBottom = reg.registerIcon(RefStrings.MODID + ":drone_crate_bottom");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : (side == 0 ? this.iconBottom : this.blockIcon);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.drone_linker) return false;

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
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		TileEntityDroneCrate tileentityfurnace = (TileEntityDroneCrate) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			if(tileentityfurnace != null) {
				for(int i1 = 0; i1 < tileentityfurnace.getSizeInventory(); ++i1) {
					ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);

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
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		addStandardInfo(stack, player, list, ext);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		TileEntityDroneCrate tile = (TileEntityDroneCrate) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		List<String> text = new ArrayList();

		if(tile.nextY != -1) {
			text.add("Next waypoint: " + tile.nextX + " / " + tile.nextY + " / " + tile.nextZ);
			ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
		}
	}
}
