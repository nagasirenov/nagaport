package com.hbm.commands;

import com.hbm.items.ICustomizable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandCustomize extends CommandBase {


		@Override
	public String getName() {
		return getCommandName();
	}

	@Override
	public String getUsage(net.minecraft.command.ICommandSender sender) {
		return getCommandUsage(sender);
	}

	@Override
	public void execute(net.minecraft.server.MinecraftServer server, net.minecraft.command.ICommandSender sender, String[] args) throws net.minecraft.command.CommandException {
		processCommand(sender, args);
	}

public String getCommandName() {
		return "ntmcustomize";
	}


	public String getCommandUsage(ICommandSender sender) {
		return "/ntmcustomize";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}


	public void processCommand(ICommandSender sender, String[] args) {
		if(!(sender instanceof EntityPlayer)) {
			sender.sendMessage(new TextComponentString("Customization is only available to players!").setStyle(new Style().setColor(TextFormatting.RED)));
			return;
		}

		EntityPlayer player = (EntityPlayer) sender;

		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) == null || !(com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof ICustomizable)) {
			sender.sendMessage(new TextComponentString("You have to hold a customizable item to use this command!").setStyle(new Style().setColor(TextFormatting.RED)));
			return;
		}

		ICustomizable item = (ICustomizable) com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem();
		item.customize(player, com.hbm.util.NativeHeldItem.getMainHandNullable(player), args);
	}
}
