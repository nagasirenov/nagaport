package com.hbm.commands;

import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.ChatBuilder;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandReapNetworks extends CommandBase {

	@Override
	public String getCommandName() {
		return "ntmreapnetworks";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/ntmreapnetworks";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {

		try {
			
			UniNodespace.activeNodeNets.forEach((net) -> {
				net.links.forEach((link) -> { ((GenNode)link).expired = true; });
				net.links.clear();
				net.providerEntries.clear();
				net.receiverEntries.clear();
			});
			UniNodespace.activeNodeNets.clear();
			UniNodespace.worlds.clear();

			sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Nodespace cleared :)"));
			
		} catch(Exception ex) {
			sender.sendMessage(ChatBuilder.start("----------------------------------").color(TextFormatting.GRAY).flush());
			sender.sendMessage(ChatBuilder.start("An error has occoured during network reap, consult the log for details.").color(TextFormatting.RED).flush());
			sender.sendMessage(ChatBuilder.start(ex.getLocalizedMessage()).color(TextFormatting.RED).flush());
			sender.sendMessage(ChatBuilder.start(ex.getStackTrace()[0].toString()).color(TextFormatting.RED).flush());
			sender.sendMessage(ChatBuilder.start("----------------------------------").color(TextFormatting.GRAY).flush());
			throw ex;
		}
	}
}
