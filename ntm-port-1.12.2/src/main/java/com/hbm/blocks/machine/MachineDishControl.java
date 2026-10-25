package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDishControl;
import com.hbm.util.ChatBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import java.util.List;

public class MachineDishControl extends BlockDummyable implements ITooltipProvider {

	public MachineDishControl(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int meta) {
		if(meta >= 12) return new TileEntityDishControl();
		return new TileEntityProxyCombo(false, false, false); // no need for extra atm, it's just two blocks
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 0, 0, 0, 0, 0, 1 };
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			// Find the controller TileEntity
			int[] pos = findCore(world, x, y, z);

			TileEntity e = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
			if(e instanceof TileEntityDishControl) {
				TileEntityDishControl entityDishControl = (TileEntityDishControl) e;

				// Check if a dish was not assigned
				if(!entityDishControl.isLinked) {
					// No StarDar linked
					player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
						.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
						.next("] ").color(TextFormatting.DARK_AQUA)
						.next("Dish not linked!").color(TextFormatting.RED).flush());

					return false;
				}

				// also check that you're still pointing at a stardar...
				Block block = com.hbm.world.WorldBlockState.read(world, entityDishControl.linkPosition[0], entityDishControl.linkPosition[1], entityDishControl.linkPosition[2]).getBlock();

				if(!(block instanceof MachineStardar)) {
					// Invalid link
					player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
						.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
						.next("] ").color(TextFormatting.DARK_AQUA)
						.next("Missing control target!").color(TextFormatting.RED).flush());

					return false;
				}

				// Get StarDar
				MachineStardar stardarBlock = (MachineStardar) com.hbm.world.WorldBlockState.read(world, entityDishControl.linkPosition[0],
					entityDishControl.linkPosition[1],
					entityDishControl.linkPosition[2]).getBlock();

				// Trigger the StarDar UI to open
				net.minecraft.util.math.BlockPos linkPos = new net.minecraft.util.math.BlockPos(entityDishControl.linkPosition[0], entityDishControl.linkPosition[1], entityDishControl.linkPosition[2]);
				return stardarBlock.onBlockActivated(world, linkPos, world.getBlockState(linkPos), player, nativeHand, nativeFacing, hitX, hitY, hitZ);
			}

			return false;

		} else {
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		addStandardInfo(stack, player, list, ext);
	}

}
