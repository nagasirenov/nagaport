package com.hbm.commands;

import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CommandSatellites extends CommandBase {

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
		return "ntmsatellites";
	}


	public String getCommandUsage(ICommandSender iCommandSender) {
		return String.format(Locale.US,
				"%s/%s orbit [frequency] %s- Launch the held satellite with optional frequency override.\n" +
				"%s/%s descend <frequency> %s- Deletes satellite by frequency.\n"+
				"%s/%s list %s- Lists all active satellites.",
				TextFormatting.GREEN, getCommandName(), TextFormatting.LIGHT_PURPLE,
				TextFormatting.GREEN, getCommandName(), TextFormatting.LIGHT_PURPLE,
				TextFormatting.GREEN, getCommandName(), TextFormatting.LIGHT_PURPLE
		);
	}


	public void processCommand(ICommandSender sender, String[] args) throws net.minecraft.command.CommandException {
		if(!(sender instanceof EntityPlayer)) {
			sender.sendMessage(new TextComponentTranslation( "commands.satellite.should_be_run_as_player").setStyle(new Style().setColor(TextFormatting.RED)));
			return;
		}
		EntityPlayer player = getCommandSenderAsPlayer(sender);

		switch (args[0]) {
			case "orbit":
				if(com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof ISatChip && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() != ModItems.sat_chip) {
					int freq = ISatChip.getFreqS(com.hbm.util.NativeHeldItem.getMainHandNullable(player));
					if(args.length >= 2) freq = parseInt(args[1]);
					Satellite.orbit(
						player.world,
						Satellite.getIDFromItem(com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem()),
						freq,
						player.posX, player.posY, player.posZ
					);
					com.hbm.util.NativeHeldItem.getMainHandNullable(player).shrink(1);
					sender.sendMessage(new TextComponentTranslation("commands.satellite.satellite_orbited").setStyle(new Style().setColor(TextFormatting.GREEN)));
				} else {
					sender.sendMessage(new TextComponentTranslation("commands.satellite.not_a_satellite").setStyle(new Style().setColor(TextFormatting.RED)));
				}
				break;
			case "descend":
				int freq = parseInt(args[1]);
				SatelliteSavedData data = SatelliteSavedData.getData(sender.getEntityWorld(), (int)player.posX, (int)player.posZ);
				if(data.sats.containsKey(freq)) {
					data.sats.remove(freq);
					data.markDirty();
					sender.sendMessage(new TextComponentTranslation( "commands.satellite.satellite_descended").setStyle(new Style().setColor(TextFormatting.GREEN)));
				} else {
					sender.sendMessage(new TextComponentTranslation( "commands.satellite.no_satellite").setStyle(new Style().setColor(TextFormatting.RED)));
				}
				break;
			case "list":
				data = SatelliteSavedData.getData(sender.getEntityWorld(), (int)player.posX, (int)player.posZ);
				if (data.sats.isEmpty()) {
					TextComponentTranslation message = new TextComponentTranslation("commands.satellite.no_active_satellites");
					message.getStyle().setColor(TextFormatting.RED);
					sender.sendMessage(message);
				} else {
					data.sats.forEach((listFreq, sat) -> {
						String messageText = String.valueOf(listFreq) + " - " + sat.getClass().getSimpleName();
						TextComponentString message = new TextComponentString(messageText);
						message.getStyle().setColor(TextFormatting.GREEN);
						sender.sendMessage(message);
					});
				}
				break;

		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public java.util.List<String> getTabCompletions(net.minecraft.server.MinecraftServer server, ICommandSender sender, String[] args, net.minecraft.util.math.BlockPos pos) {
		if(!(sender instanceof EntityPlayer)) {
			return Collections.emptyList();
		}
		if(args.length < 1) {
			return Collections.emptyList();
		}
		if(args.length == 1) {
			return getListOfStringsMatchingLastWord(args, "orbit", "descend", "list");
		}
		if (args[0].equals("descend")) {
			BlockPos coords = sender.getPosition();
			return getListOfStringsMatchingLastWord(args, SatelliteSavedData.getData(sender.getEntityWorld(), coords.getX(), coords.getZ()).sats.keySet().stream().map(String::valueOf).collect(Collectors.toList()));
		}
		return Collections.emptyList();
	}
}
