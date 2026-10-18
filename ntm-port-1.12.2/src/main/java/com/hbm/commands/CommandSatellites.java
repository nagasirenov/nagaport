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
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CommandSatellites extends CommandBase {
	@Override
	public String getCommandName() {
		return "ntmsatellites";
	}

	@Override
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

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(!(sender instanceof EntityPlayer)) {
			sender.sendMessage(new TextComponentTranslation( "commands.satellite.should_be_run_as_player").setStyle(new Style().setColor(TextFormatting.RED)));
			return;
		}
		EntityPlayer player = getCommandSenderAsPlayer(sender);

		switch (args[0]) {
			case "orbit":
				if(player.getHeldItem().getItem() instanceof ISatChip && player.getHeldItem().getItem() != ModItems.sat_chip) {
					int freq = ISatChip.getFreqS(player.getHeldItem());
					if(args.length >= 2) freq = parseInt(sender, args[1]);
					Satellite.orbit(
						player.worldObj,
						Satellite.getIDFromItem(player.getHeldItem().getItem()),
						freq,
						player.posX, player.posY, player.posZ
					);
					player.getHeldItem().stackSize -= 1;
					sender.sendMessage(new TextComponentTranslation("commands.satellite.satellite_orbited").setStyle(new Style().setColor(TextFormatting.GREEN)));
				} else {
					sender.sendMessage(new TextComponentTranslation("commands.satellite.not_a_satellite").setStyle(new Style().setColor(TextFormatting.RED)));
				}
				break;
			case "descend":
				int freq = parseInt(sender, args[1]);
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
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
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
			ChunkCoordinates coords = sender.getPlayerCoordinates();
			return getListOfStringsFromIterableMatchingLastWord(args, SatelliteSavedData.getData(sender.getEntityWorld(), coords.posX, coords.posZ).sats.keySet().stream().map(String::valueOf).collect(Collectors.toList()));
		}
		return Collections.emptyList();
	}
}
