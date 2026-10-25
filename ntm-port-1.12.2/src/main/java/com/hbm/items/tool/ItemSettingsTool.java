package com.hbm.items.tool;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.HbmKeybinds;
import com.hbm.interfaces.ICopiable;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.util.ChatBuilder;
import com.hbm.util.Either;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSettingsTool extends Item {

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean bool) {
		NBTTagList displayInfo;
		if(!(entity instanceof EntityPlayerMP)) return;

		if(com.hbm.util.NativeHeldItem.getMainHandNullable(((EntityPlayer) entity)) == stack && stack.hasTagCompound()) {
			EntityPlayer player = ((EntityPlayer) entity);
			int delay = stack.getTagCompound().getInteger("inputDelay");
			delay++;
			displayInfo = stack.getTagCompound().getTagList("displayInfo", 10);

			if (HbmPlayerProps.getData(player).getKeyPressed(HbmKeybinds.EnumKeybind.TOOL_ALT) && delay > 4) {
				int index = stack.getTagCompound().getInteger("copyIndex") + 1;
				if(index > displayInfo.tagCount() - 1) index = 0;
				stack.getTagCompound().setInteger("copyIndex", index);
				delay = 0;
			}

			stack.getTagCompound().setInteger("inputDelay", delay);
			if(world.getTotalWorldTime() % 5 != 0) return;

			if(displayInfo.tagCount() > 0){
				for (int j = 0; j < displayInfo.tagCount(); j++) {
					NBTTagCompound nbt = displayInfo.getCompoundTagAt(j);
					TextFormatting format = stack.getTagCompound().getInteger("copyIndex") == j ? TextFormatting.AQUA : TextFormatting.YELLOW;
					PacketDispatcher.wrapper.sendTo(
							new PlayerInformPacket(ChatBuilder.startTranslation(nbt.getString("info"))
									.color(format)
									.flush()
							, 897 + j, 4000 ), (EntityPlayerMP) entity);
				}
			}
		}

	}
	@SuppressWarnings("unchecked")
	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		list.add("Can copy the settings (filters, fluid ID, etc) of machines");
		list.add("Shift right-click to copy, right click to paste");
		list.add("Ctrl click on pipes to paste settings to multiple pipes");
		if(stack.getTagCompound() != null) {
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt.hasKey("tileName")){
				list.add(TextFormatting.BLUE + I18nUtil.resolveKey(nbt.getString("tileName") + ".name"));
			} else {
				list.add(TextFormatting.RED + " None ");
			}

		}
	}
	@Override
	public net.minecraft.util.EnumActionResult onItemUseFirst(EntityPlayer player, World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing side, float fX, float fY, float fZ, net.minecraft.util.EnumHand hand) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ(); net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		Either<TileEntity, Block> schrodinger = getCopyInfoSource(world, x, y, z);
		if(schrodinger == null) return net.minecraft.util.EnumActionResult.PASS;
		ICopiable copiable = schrodinger.cast();

		if(player.isSneaking()) {
			stack.setTagCompound(copiable.getSettings(world, x, y, z));
			if(stack.getTagCompound() != null) {

				stack.getTagCompound().setString("tileName", copiable.getSettingsSourceID(schrodinger));
				stack.getTagCompound().setInteger("copyIndex", 0);
				stack.getTagCompound().setInteger("inputDelay", 0);
				String[] info = copiable.infoForDisplay(world, x, y, z);
				if (info != null) {
					NBTTagList displayInfo = new NBTTagList();
					for (String str : info) {
						NBTTagCompound nbt = new NBTTagCompound();
						nbt.setString("info", str);
						displayInfo.appendTag(nbt);
					}
					stack.getTagCompound().setTag("displayInfo", displayInfo);
				}

				if (world.isRemote) {
					player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
							.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
							.next("] ").color(TextFormatting.DARK_AQUA)
							.next("Copied settings of " + copiable.getSettingsSourceDisplay(schrodinger)).color(TextFormatting.AQUA).flush());
				}
			} else {
				player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
						.nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
						.next("] ").color(TextFormatting.DARK_AQUA)
						.next("Copy failed, machine has no settings tool support: " + copiable.getSettingsSourceDisplay(schrodinger)).color(TextFormatting.RED).flush());
			}

		} else if(stack.hasTagCompound()) {
			int index = stack.getTagCompound().getInteger("copyIndex");
			copiable.pasteSettings(stack.getTagCompound(), index, world, player, x, y, z);
		}

		return !world.isRemote ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

	@Nullable
	private Either<TileEntity, Block> getCopyInfoSource(World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		if(te instanceof ICopiable) {
			return Either.left(te);
		}

		Block block = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
		if(block instanceof ICopiable) {
			return Either.right(block);
		}

		return null;
	}
}
