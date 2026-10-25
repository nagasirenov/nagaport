package com.hbm.commands;

import com.hbm.handler.radiation.ChunkRadiationManager;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandRadiation extends CommandBase {


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
		return "ntmrad";
	}


	public String getCommandUsage(ICommandSender sender) {
		return "/ntmrad <set/clear>";
	}


	public void processCommand(ICommandSender sender, String[] args) throws net.minecraft.command.CommandException {

		if(args.length == 1 && "clear".equals(args[0])) {
			ChunkRadiationManager.proxy.clearSystem(sender.getEntityWorld());
			sender.sendMessage(new TextComponentString("Cleared radiation data!"));
			return;
		}

		if(args.length == 2 && "set".equals(args[0])) {
			float amount = (float) parseDouble(args[1], 0D, 100_000D);
			ChunkRadiationManager.proxy.setRadiation(sender.getEntityWorld(), sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ(), amount);
			sender.sendMessage(new TextComponentString("Radiation set."));
			return;
		}

		sender.sendMessage(new TextComponentString(TextFormatting.RED + getCommandUsage(sender)));
	}

}
