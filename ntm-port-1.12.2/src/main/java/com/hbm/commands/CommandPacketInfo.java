package com.hbm.commands;

import com.hbm.config.GeneralConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.main.MainRegistry;
import com.hbm.util.BobMathUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.TimeUnit;

import static com.hbm.handler.threading.PacketThreading.totalCnt;

public class CommandPacketInfo extends CommandBase {

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
		return "ntmpackets";
	}


	public String getCommandUsage(ICommandSender sender) {
		return TextFormatting.RED + "/ntmpackets [info/resetState/toggleThreadingStatus/forceLock/forceUnlock]";
	}


	public void processCommand(ICommandSender sender, String[] args) {

		if (args.length > 0) {
			switch (args[0]) {
				case "resetState":
					PacketThreading.hasTriggered = false;
					PacketThreading.clearCnt = 0;
					return;
				case "toggleThreadingStatus":
					GeneralConfig.enablePacketThreading = !GeneralConfig.enablePacketThreading; // Force toggle.
					PacketThreading.init(); // Reinit threads.
					sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Packet sending status toggled to " + GeneralConfig.enablePacketThreading + "."));
					return;
				case "forceLock":
					PacketThreading.lock.lock(); // oh my fucking god never do this please unless you really have to
					sender.sendMessage(new TextComponentString(TextFormatting.RED + "Packet thread lock acquired, this may freeze the main thread!"));
					MainRegistry.logger.error("Packet thread lock acquired by {}, this may freeze the main thread!", sender.getName());
					return;
				case "forceUnlock":
					PacketThreading.lock.unlock();
					MainRegistry.logger.warn("Packet thread lock released by {}.", sender.getName());
					return;
				case "info":
					sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "NTM Packet Debugger v1.2"));

					if (PacketThreading.isTriggered() && GeneralConfig.enablePacketThreading)
						sender.sendMessage(new TextComponentString(TextFormatting.RED + "Packet Threading Errored, check log."));
					else if (GeneralConfig.enablePacketThreading)
						sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Packet Threading Active"));
					else
						sender.sendMessage(new TextComponentString(TextFormatting.RED + "Packet Threading Inactive"));

					sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Thread Pool Info"));
					sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "# Threads (total): " + PacketThreading.threadPool.getPoolSize()));
					sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "# Threads (core): " + PacketThreading.threadPool.getCorePoolSize()));
					sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "# Threads (idle): " + (PacketThreading.threadPool.getPoolSize() - PacketThreading.threadPool.getActiveCount())));
					sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "# Threads (maximum): " + PacketThreading.threadPool.getMaximumPoolSize()));

					for (ThreadInfo thread : ManagementFactory.getThreadMXBean().dumpAllThreads(false, false))
						if (thread.getThreadName().startsWith(PacketThreading.threadPrefix)) {
							sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "Thread Name: " + thread.getThreadName()));
							sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Thread ID: " + thread.getThreadId()));
							sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Thread state: " + thread.getThreadState()));
							sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Locked by: " + (thread.getLockOwnerName() == null ? "None" : thread.getLockName())));
						}

					sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "Packet Info: "));
					sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Amount total: " + totalCnt));
					sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Amount remaining: " + PacketThreading.threadPool.getQueue().size()));

					if (totalCnt != 0)
						sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "% Remaining to process: " + BobMathUtil.roundDecimal(((double) PacketThreading.threadPool.getQueue().size() / totalCnt) * 100, 2) + "%"));

					sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Time spent waiting on thread(s) last tick: " + BobMathUtil.roundDecimal(TimeUnit.MILLISECONDS.convert(PacketThreading.nanoTimeWaited, TimeUnit.NANOSECONDS), 4) + "ms"));
					return;
			}
		}
		sender.sendMessage(new TextComponentString(getCommandUsage(sender)));
	}
}
