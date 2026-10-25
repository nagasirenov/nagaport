package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.Random;

import com.hbm.config.CustomMachineConfigJSON;
import com.hbm.config.CustomMachineConfigJSON.MachineConfiguration;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityCustomMachine;

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
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BlockCustomMachine extends BlockContainer {

	@SideOnly(Side.CLIENT)
	private IIcon iconFront;
	protected IIcon blockIcon;

	public BlockCustomMachine() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCustomMachine();
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconFront = iconRegister.registerIcon(RefStrings.MODID + ":cm_terminal_front");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":cm_terminal_side");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		if(metadata >= 100) return side == 3 ? this.iconFront : this.blockIcon;
		return metadata == 0 && side == 3 ? this.iconFront : (side == metadata ? this.iconFront : this.blockIcon);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {

			TileEntityCustomMachine tile = (TileEntityCustomMachine) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			if(tile != null) {

				if(tile.checkStructure()) {
					FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
				} else if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.wand_s) {
					tile.buildStructure();
				}
			}
			return true;
		}

		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack stack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 2);
		if(i == 1) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 2);
		if(i == 2) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 2);
		if(i == 3) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 2);

		TileEntityCustomMachine tile = (TileEntityCustomMachine) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tile != null) {
			int id = stack.getItemDamage() - 100;

			if(id >= 0 && id < CustomMachineConfigJSON.customMachines.size()) {

				MachineConfiguration config = CustomMachineConfigJSON.niceList.get(id);

				if(config != null) {
					tile.machineType = config.unlocalizedName;
					tile.init();
					tile.markChanged();
				}
			}
		}
	}


		@Override
	public void onBlockHarvested(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.entity.player.EntityPlayer player) {
		int meta = state.getBlock().getMetaFromState(state);
		onBlockHarvested(worldIn, pos.getX(), pos.getY(), pos.getZ(), meta, player);
	}

public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {

		if(!player.capabilities.isCreativeMode) {
			harvesters.set(player);
			this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), 0);
			harvesters.set(null);
		}
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, @javax.annotation.Nullable net.minecraft.tileentity.TileEntity te, ItemStack stack) {
		player.addStat(StatList.getBlockStats(this));
		player.addExhaustion(0.025F);
	}


		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {

		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		Item item = getItemDropped(this.getStateFromMeta(metadata), world.rand, fortune);
		if(item != null) {

			TileEntityCustomMachine tile = (TileEntityCustomMachine) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			if(tile != null) {
				ItemStack stack = new ItemStack(item, 1, CustomMachineConfigJSON.niceList.indexOf(tile.config) + 100);
				ret.add(stack);
			}
		}

		return ret;
	}

	@Override
	public ItemStack getPickBlock(net.minecraft.block.state.IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		TileEntityCustomMachine tile = (TileEntityCustomMachine) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tile != null && tile.machineType != null && !tile.machineType.isEmpty()) {
			ItemStack stack = new ItemStack(this, 1, CustomMachineConfigJSON.niceList.indexOf(tile.config) + 100);
			return stack;
		}

		return super.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);

		ISidedInventory sided = (ISidedInventory) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		Random rand = world.rand;

		if(sided != null) {
			for(int i1 = 0; i1 < sided.getSizeInventory(); ++i1) {

				if(i1 >= 10 && i1 <= 15)
					continue; // do NOT drop the filters

				ItemStack itemstack = sided.getStackInSlot(i1);

				if(itemstack != null && !itemstack.isEmpty()) {
					float f = rand.nextFloat() * 0.8F + 0.1F;
					float f1 = rand.nextFloat() * 0.8F + 0.1F;
					float f2 = rand.nextFloat() * 0.8F + 0.1F;

					while(itemstack.getCount() > 0) {
						int j1 = rand.nextInt(21) + 10;

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
						entityitem.motionX = (float) rand.nextGaussian() * f3;
						entityitem.motionY = (float) rand.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = (float) rand.nextGaussian() * f3;
						world.spawnEntity(entityitem);
					}
				}
			}

			world.updateComparatorOutputLevel(new net.minecraft.util.math.BlockPos(x, y, z), block);
		}

		super.breakBlock(world, pos, state);
	}
}
