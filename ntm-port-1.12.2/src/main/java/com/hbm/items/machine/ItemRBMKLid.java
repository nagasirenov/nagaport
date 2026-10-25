package com.hbm.items.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKNeutronNode;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKBase;

import com.hbm.util.fauxpointtwelve.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemRBMKLid extends Item {


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {

		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

		if(b instanceof RBMKBase) {
			RBMKBase rbmk = (RBMKBase) b;

			int[] pos = rbmk.findCore(world, x, y, z);

			if(pos == null)
				return false;

			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

			if(!(te instanceof TileEntityRBMKBase))
				return false;

			TileEntityRBMKBase tile = (TileEntityRBMKBase) te;

			if(tile.hasLid())
				return false;

			RBMKNeutronNode node = (RBMKNeutronNode) NeutronNodeWorld.getNode(world, new BlockPos(te));
			if(node != null)
				node.addLid();

			int meta = RBMKBase.DIR_NORMAL_LID.ordinal();

			if(this == ModItems.rbmk_lid_glass) {
				meta = RBMKBase.DIR_GLASS_LID.ordinal();
				net.minecraft.block.SoundType st = Blocks.GLASS.getSoundType();
				world.playSound(null, x + 0.5, y + 0.5, z + 0.5, st.getPlaceSound(), net.minecraft.util.SoundCategory.BLOCKS, (st.getVolume() + 1.0F) / 2.0F, st.getPitch() * 0.8F);
			} else {
				net.minecraft.block.SoundType st = ModBlocks.concrete_smooth.getSoundType();
				world.playSound(null, x + 0.5, y + 0.5, z + 0.5, st.getPlaceSound(), net.minecraft.util.SoundCategory.BLOCKS, (st.getVolume() + 1.0F) / 2.0F, st.getPitch() * 0.8F);
			}

			tile.explodeOnBroken = false;
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, pos[0], pos[1], pos[2], meta + RBMKBase.offset, 3);
			tile.explodeOnBroken = true;
			stack.shrink(1);

			return true;
		}

		return false;
	}
}
