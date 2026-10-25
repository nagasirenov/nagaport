package com.hbm.blocks.generic;

import com.hbm.blocks.machine.BlockPillar;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockWriting extends BlockPillar {

	public BlockWriting(Material mat, String top) {
		super(mat, top);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(world.isRemote) {
			return true;

		} else if(!player.isSneaking()) {

			Style red = new Style().setColor(TextFormatting.RED);
			player.sendMessage(new TextComponentString("You should not have come here.").setStyle(red));
			player.sendMessage(new TextComponentString("This is not a place of honor. No great deed is commemorated here.").setStyle(red));
			player.sendMessage(new TextComponentString("Nothing of value is here.").setStyle(red));
			player.sendMessage(new TextComponentString("What is here is dangerous and repulsive.").setStyle(red));
			player.sendMessage(new TextComponentString("We considered ourselves a powerful culture. We harnessed the hidden fire, and used it for our own purposes.").setStyle(red));
			player.sendMessage(new TextComponentString("Then we saw the fire could burn within living things, unnoticed until it destroyed them.").setStyle(red));
			player.sendMessage(new TextComponentString("And we were afraid.").setStyle(red));
			player.sendMessage(new TextComponentString("We built great tombs to hold the fire for one hundred thousand years, after which it would no longer kill.").setStyle(red));
			player.sendMessage(new TextComponentString("If this place is opened, the fire will not be isolated from the world, and we will have failed to protect you.").setStyle(red));
			player.sendMessage(new TextComponentString("Leave this place and never come back.").setStyle(red));
			return true;

		} else {
			return false;
		}
	}
}
