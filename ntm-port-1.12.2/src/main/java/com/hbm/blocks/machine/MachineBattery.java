package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.machine.storage.TileEntityMachineBattery;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class MachineBattery extends BlockContainer implements ILookOverlay, IPersistentInfoProvider, ITooltipProvider {

	private final Random field_149933_a = new Random();
	private static boolean keepInventory;
	public long maxPower;

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconFront;

	public MachineBattery(Material p_i45386_1_, long maxPower) {
		super(p_i45386_1_);
		this.maxPower = maxPower;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		if(this == ModBlocks.machine_battery) {
			this.iconFront = iconRegister.registerIcon(RefStrings.MODID + ":battery_front_alt");
			this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":battery_top");
			this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":battery_side_alt");
		}
		if(this == ModBlocks.machine_battery_potato) {
			this.iconFront = iconRegister.registerIcon(RefStrings.MODID + ":battery_potato_front");
			this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":battery_potato_top");
			this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":battery_potato_side");
		}
		if(this == ModBlocks.machine_lithium_battery) {
			this.iconFront = iconRegister.registerIcon(RefStrings.MODID + ":battery_lithium_front");
			this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":battery_lithium_top");
			this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":battery_lithium_side");
		}
		if(this == ModBlocks.machine_schrabidium_battery) {
			this.iconFront = iconRegister.registerIcon(RefStrings.MODID + ":battery_schrabidium_front");
			this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":battery_schrabidium_top");
			this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":battery_schrabidium_side");
		}
		if(this == ModBlocks.machine_dineutronium_battery) {
			this.iconFront = iconRegister.registerIcon(RefStrings.MODID + ":battery_dineutronium_front");
			this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":battery_dineutronium_top");
			this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":battery_dineutronium_side");
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {

		if(side == 0 || side == 1)
			return iconTop;

		return metadata == 0 && side == 3 ? this.iconFront : (side == metadata ? this.iconFront : this.blockIcon);
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return Item.getItemFromBlock(this);
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		this.setDefaultDirection(world, x, y, z);
	}

	private void setDefaultDirection(World world, int x, int y, int z) {
		if(!world.isRemote) {
			Block block1 = com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock();
			Block block2 = com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock();
			Block block3 = com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock();
			Block block4 = com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock();

			byte b0 = 3;

			if(block1.func_149730_j() && !block2.func_149730_j()) {
				b0 = 3;
			}
			if(block2.func_149730_j() && !block1.func_149730_j()) {
				b0 = 2;
			}
			if(block3.func_149730_j() && !block4.func_149730_j()) {
				b0 = 5;
			}
			if(block4.func_149730_j() && !block3.func_149730_j()) {
				b0 = 4;
			}

			world.setBlockMetadataWithNotify(x, y, z, b0, 2);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0) {
			world.setBlockMetadataWithNotify(x, y, z, 2, 2);
		}
		if(i == 1) {
			world.setBlockMetadataWithNotify(x, y, z, 5, 2);
		}
		if(i == 2) {
			world.setBlockMetadataWithNotify(x, y, z, 3, 2);
		}
		if(i == 3) {
			world.setBlockMetadataWithNotify(x, y, z, 4, 2);
		}

		if(itemStack.hasDisplayName()) {
			((TileEntityMachineBattery) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z))).setCustomName(itemStack.getDisplayName());
		}

		IPersistentNBT.restoreData(world, x, y, z, itemStack);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityMachineBattery();
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			TileEntityMachineBattery entity = (TileEntityMachineBattery) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(entity != null) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void breakBlock(World p_149749_1_, int p_149749_2_, int p_149749_3_, int p_149749_4_, Block p_149749_5_, int p_149749_6_) {
		if(!keepInventory) {
			TileEntity tile = p_149749_1_.getTileEntity(new net.minecraft.util.math.BlockPos(p_149749_2_, p_149749_3_, p_149749_4_));

			if(tile instanceof TileEntityMachineBattery) {
				TileEntityMachineBattery battery = (TileEntityMachineBattery) tile;

				for(int i1 = 0; i1 < battery.getSizeInventory(); ++i1) {
					ItemStack itemstack = battery.getStackInSlot(i1);

					if(itemstack != null && !itemstack.isEmpty()) {
						float f = this.field_149933_a.nextFloat() * 0.8F + 0.1F;
						float f1 = this.field_149933_a.nextFloat() * 0.8F + 0.1F;
						float f2 = this.field_149933_a.nextFloat() * 0.8F + 0.1F;

						while(itemstack.getCount() > 0) {
							int j1 = this.field_149933_a.nextInt(21) + 10;

							if(j1 > itemstack.getCount()) {
								j1 = itemstack.getCount();
							}

							// Capture before depletion: native empty stacks hide their item.
							net.minecraft.item.Item dropItem = itemstack.getItem();
							int dropMeta = itemstack.getItemDamage();
							NBTTagCompound dropTag = itemstack.getTagCompound();
							itemstack.shrink(j1);
							EntityItem entityitem = new EntityItem(p_149749_1_, p_149749_2_ + f, p_149749_3_ + f1, p_149749_4_ + f2, new ItemStack(dropItem, j1, dropMeta));

							if(dropTag != null) {
								entityitem.getItem().setTagCompound(dropTag.copy());
							}

							float f3 = 0.05F;
							entityitem.motionX = (float) this.field_149933_a.nextGaussian() * f3;
							entityitem.motionY = (float) this.field_149933_a.nextGaussian() * f3 + 0.2F;
							entityitem.motionZ = (float) this.field_149933_a.nextGaussian() * f3;
							p_149749_1_.spawnEntity(entityitem);
						}
					}
				}

				p_149749_1_.func_147453_f(p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_);
			}
		}

		super.breakBlock(p_149749_1_, p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_, p_149749_6_);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(!(te instanceof TileEntityMachineBattery))
			return;

		TileEntityMachineBattery battery = (TileEntityMachineBattery) te;

		List<String> text = new ArrayList();
		text.add(BobMathUtil.getShortNumber(battery.getPower()) + " / " + BobMathUtil.getShortNumber(battery.getMaxPower()) + "HE");

		double percent = (double) battery.getPower() / (double) battery.getMaxPower();
		int charge = (int) Math.floor(percent * 10_000D);
		int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int)(0xFF * percent) << 8);

		text.add("&[" + color + "&]" + (charge / 100D) + "%");

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public boolean hasComparatorInputOverride(net.minecraft.block.state.IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(net.minecraft.block.state.IBlockState state, World world, net.minecraft.util.math.BlockPos blockPos) {
		int x = blockPos.getX();
		int y = blockPos.getY();
		int z = blockPos.getZ();

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(!(te instanceof TileEntityMachineBattery))
			return 0;

		TileEntityMachineBattery battery = (TileEntityMachineBattery) te;
		return battery.getComparatorPower();
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		return IPersistentNBT.getDrops(world, x, y, z, this);
	}

	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {

		if(!player.capabilities.isCreativeMode) {
			harvesters.set(player);
			this.dropBlockAsItem(world, x, y, z, meta, 0);
			harvesters.set(null);
		}
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
		player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
		player.addExhaustion(0.025F);
	}

	@Override
	public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.YELLOW + "" + BobMathUtil.getShortNumber(persistentTag.getLong("power")) + "/" + BobMathUtil.getShortNumber(this.maxPower) + "HE");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.GOLD + "Stores up to "+ BobMathUtil.getShortNumber(this.maxPower) + "HE");
		list.add(TextFormatting.GOLD + "Charge speed: "+ BobMathUtil.getShortNumber(this.maxPower / 200) + "HE");
		list.add(TextFormatting.GOLD + "Discharge speed: "+ BobMathUtil.getShortNumber(this.maxPower / 600) + "HE");
	}

}
