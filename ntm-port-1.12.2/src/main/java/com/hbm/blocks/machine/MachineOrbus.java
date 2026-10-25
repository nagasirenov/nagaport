package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.storage.TileEntityMachineOrbus;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class MachineOrbus extends BlockDummyable implements IPersistentInfoProvider {

	public MachineOrbus(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		if(meta >= 12) return new TileEntityMachineOrbus();
		if(meta >= 6) return new TileEntityProxyCombo(false, false, true);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {4, 0, 2, 1, 2, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {

			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;

			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			return true;
		} else if(player.isSneaking()){
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;

			TileEntityMachineOrbus kyleEntity = (TileEntityMachineOrbus) world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

			if(kyleEntity != null) {
			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof IItemFluidIdentifier) {
				FluidType type = ((IItemFluidIdentifier) com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem()).getType(world, pos[0], pos[1], pos[2], com.hbm.util.NativeHeldItem.getMainHandNullable(player));

				kyleEntity.tank.setTankType(type);
				kyleEntity.markDirty();
				player.sendMessage(new TextComponentString("Changed type to ").setStyle(new Style().setColor(TextFormatting.YELLOW)).appendSibling(new TextComponentTranslation(type.getConditionalName())).appendSibling(new TextComponentString("!")));
				}
			}

			return true;
			} else {
			return true;
		}
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;

		ForgeDirection d2 = dir.getRotation(ForgeDirection.UP);
		dir = dir.getOpposite();

		for(int i = 0; i < 5; i += 4) {
			this.makeExtra(world, x, y + i, z);
			this.makeExtra(world, x + dir.offsetX, y + i, z + dir.offsetZ);
			this.makeExtra(world, x + d2.offsetX, y + i, z + d2.offsetZ);
			this.makeExtra(world, x + dir.offsetX + d2.offsetX, y + i, z + dir.offsetZ + d2.offsetZ);
		}
	}


		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		return IPersistentNBT.getDrops(world, x, y, z, this);
	}

	@Override
	public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List list, boolean ext) {
		FluidTank tank = new FluidTank(Fluids.NONE, 0);
		tank.readFromNBT(persistentTag, "tank");
		list.add(TextFormatting.YELLOW + "" + tank.getFill() + "/" + tank.getMaxFill() + "mB " + tank.getTankType().getLocalizedName());
	}
}
