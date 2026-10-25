package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachineAlkylation;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

public class MachineAlkylation extends BlockDummyable implements ILookOverlay {

	public MachineAlkylation(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12)
			return new TileEntityMachineAlkylation();
		if(meta >= extra)
			return new TileEntityProxyCombo(false, true, true);

		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {3, 0, 2, 2, 1, 1};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		x += dir.offsetX * o;
		z += dir.offsetZ * o;

		makeExtra(world, x + rot.offsetX, y, z + rot.offsetZ);
		makeExtra(world, x + rot.offsetX + dir.offsetX * 2, y, z + rot.offsetZ + dir.offsetZ * 2);
		makeExtra(world, x + rot.offsetX - dir.offsetX * 2, y, z + rot.offsetZ - dir.offsetZ * 2);
		makeExtra(world, x - rot.offsetX, y, z - rot.offsetZ);
		makeExtra(world, x - rot.offsetX + dir.offsetX * 2, y, z - rot.offsetZ + dir.offsetZ * 2);
		makeExtra(world, x - rot.offsetX - dir.offsetX * 2, y, z - rot.offsetZ - dir.offsetZ * 2);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);

		if(pos == null)
			return;

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

		if(!(te instanceof TileEntityMachineAlkylation))
			return;

		TileEntityMachineAlkylation alkylation = (TileEntityMachineAlkylation) te;

		List<String> text = new ArrayList<>();

		text.add((alkylation.power < alkylation.getMaxPower() / 20 ? TextFormatting.RED : TextFormatting.GREEN) + "Power: " + BobMathUtil.getShortNumber(alkylation.power) + "HE");

		for(int i = 0; i < alkylation.tanks.length; i++) {
			if(alkylation.tanks[i].getTankType() == Fluids.NONE) continue;
			text.add((i < 2 ? (TextFormatting.GREEN + "-> ") : (TextFormatting.RED + "<- ")) + TextFormatting.RESET + alkylation.tanks[i].getTankType().getLocalizedName() + ": " + alkylation.tanks[i].getFill() + "/" + alkylation.tanks[i].getMaxFill() + "mB");
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(!world.isRemote && !player.isSneaking()) {

			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof IItemFluidIdentifier) {
				int[] pos = this.findCore(world, x, y, z);

				if(pos == null)
					return false;

				TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

				if(!(te instanceof TileEntityMachineAlkylation))
					return false;

				TileEntityMachineAlkylation alkylation = (TileEntityMachineAlkylation) te;
				FluidType type = ((IItemFluidIdentifier) com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem()).getType(world, pos[0], pos[1], pos[2], com.hbm.util.NativeHeldItem.getMainHandNullable(player));
				alkylation.tanks[0].setTankType(type);
				alkylation.markDirty();
				player.sendMessage(new TextComponentString("Changed type to ").setStyle(new Style().setColor(TextFormatting.YELLOW)).appendSibling(new TextComponentTranslation(type.getConditionalName())).appendSibling(new TextComponentString("!")));

				return true;
			}
			return false;

		} else {
			return true;
		}
	}
}
