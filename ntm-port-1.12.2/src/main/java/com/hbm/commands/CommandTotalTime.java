package com.hbm.commands;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;

public class CommandTotalTime extends CommandBase {

	@Override
	public String getName() {
		return "totaltime";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.time.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length > 1) {
			int i;

			if(args[0].equals("set")) {
				i = parseInt(args[1], 0);
				this.setTime(server, sender, i);
				notifyCommandListener(sender, this, "commands.time.set", new Object[] { Integer.valueOf(i) });
				return;
			}

			if(args[0].equals("add")) {
				i = parseInt(args[1], 0);
				this.addTime(server, sender, i);
				notifyCommandListener(sender, this, "commands.time.added", new Object[] { Integer.valueOf(i) });
				return;
			}
		}

		throw new WrongUsageException("commands.time.usage", new Object[0]);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] { "set", "add" }) : Collections.emptyList();
	}

	protected void setTime(MinecraftServer server, ICommandSender sender, int time) {
		for (WorldServer worldServer : server.worlds) {
			WorldInfo worldInfo = worldServer.getWorldInfo();
			worldInfo.setWorldTotalTime((long) time);
		}
	}

	protected void addTime(MinecraftServer server, ICommandSender sender, int time) {
		for (WorldServer worldServer : server.worlds) {
			WorldInfo worldInfo = worldServer.getWorldInfo();
			worldInfo.setWorldTotalTime(worldInfo.getWorldTotalTime() + (long) time);
		}
	}
}
