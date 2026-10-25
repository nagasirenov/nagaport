package com.hbm.items.machine;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineDishControl;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDishControl;
import com.hbm.util.ChatBuilder;

import net.minecraftforge.fml.common.Mod;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemReactorSensor extends Item {


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_,
			float p_77648_8_, float p_77648_9_, float p_77648_10_) {

		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

		if (b == ModBlocks.reactor_research || b == ModBlocks.machine_stardar) {

			if(stack.getTagCompound() == null)
				stack.setTagCompound(new NBTTagCompound());

			if(!world.isRemote) {
				SendMessage(player,"Position Set!");
			}

			stack.getTagCompound().setInteger("x", x);
			stack.getTagCompound().setInteger("y", y);
			stack.getTagCompound().setInteger("z", z);

			world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.techBoop"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
			player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
			return true;
		}
		else if(b == ModBlocks.machine_dish_controller)
		{
			if(stack.getTagCompound() == null) return false;

			// Get Core
			MachineDishControl controllerBlock = (MachineDishControl)b;
			int[] corePos = controllerBlock.findCore(world, x, y, z);

			// Get TileEntity
			TileEntityDishControl entity = (TileEntityDishControl)world.getTileEntity(
				new net.minecraft.util.math.BlockPos(corePos[0],
				corePos[1],
				corePos[2])
			);

			if(entity == null) return false;

			if(!world.isRemote) {
				SendMessage(player,"Stardar Linked!");
				entity.TryLink(stack);
			}

			world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.techBoop"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
			player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
			return true;
		}

		return false;
	}

	private void SendMessage(EntityPlayer player, String text)
	{
		player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
			.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
			.next("] ").color(TextFormatting.DARK_AQUA)
			.next(text).color(TextFormatting.GREEN).flush());
	}

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		if (itemstack.getTagCompound() != null) {
			list.add("x: " + itemstack.getTagCompound().getInteger("x"));
			list.add("y: " + itemstack.getTagCompound().getInteger("y"));
			list.add("z: " + itemstack.getTagCompound().getInteger("z"));
		} else {
			list.add("No reactor selected!");
		}
	}
}
