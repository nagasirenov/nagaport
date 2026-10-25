package com.hbm.commands;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.hbm.lib.HbmWorld;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.gen.nbt.SpawnCondition;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class CommandLocate extends CommandBase {

	private static final int MAX_DISTANCE = 256;


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
		return "ntmlocate";
	}


	public String getCommandUsage(ICommandSender sender) {
		return String.format(Locale.US,
			"%s/%s structure <name> %s- Locates the nearest structure with a given name.",
			TextFormatting.GREEN, getCommandName(), TextFormatting.LIGHT_PURPLE
		);
	}


	public void processCommand(ICommandSender sender, String[] args) throws net.minecraft.command.CommandException {
		if(!(sender instanceof EntityPlayer))
			throw new PlayerNotFoundException("commands.generic.player.notFound");

		if(args.length == 0)
			throw new WrongUsageException(getCommandUsage(sender), new Object[0]);

		if(args[0].equals("structure")) {
			EntityPlayer player = (EntityPlayer) sender;

			SpawnCondition structure = NBTStructure.getStructure(args[1]);

			if(structure == null) {
				TextComponentTranslation message = new TextComponentTranslation("commands.locate.no_match");
				message.getStyle().setColor(TextFormatting.RED);
				sender.sendMessage(message);
				return;
			}

			int chunkX = MathHelper.floor(player.posX) / 16;
			int chunkZ = MathHelper.floor(player.posZ) / 16;

			ChunkPos pos = getNearestLocationTo(structure, player.world, chunkX, chunkZ);

			if(pos == null) {
				TextComponentTranslation message = new TextComponentTranslation("commands.locate.none_found");
				message.getStyle().setColor(TextFormatting.RED);
				sender.sendMessage(message);
				return;
			}

			TextComponentTranslation message = new TextComponentTranslation("commands.locate.success.coordinates", structure.name, pos.x * 16, pos.z * 16);
			message.getStyle().setColor(TextFormatting.GREEN);
			sender.sendMessage(message);
		} else if (args[0].equals("list")) {
			sender.sendMessage(new TextComponentString(String.join(", ", NBTStructure.listStructures())));
		} else {
			throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
		}
	}

	private ChunkPos getNearestLocationTo(SpawnCondition spawn, World world, int chunkX, int chunkZ) {
		if(HbmWorld.worldGenerator.getStructureAt(world, chunkX, chunkZ) == spawn)
			return new ChunkPos(chunkX, chunkZ);

		for(int radius = 1; radius < MAX_DISTANCE; radius++) {
			for(int x = chunkX - radius; x <= chunkX + radius; x++) {
				if(HbmWorld.worldGenerator.getStructureAt(world, x, chunkZ - radius) == spawn)
					return new ChunkPos(x, chunkZ - radius);
				if(HbmWorld.worldGenerator.getStructureAt(world, x, chunkZ + radius) == spawn)
					return new ChunkPos(x, chunkZ + radius);
			}
			for(int z = chunkZ - radius; z <= chunkZ + radius; z++) {
				if(HbmWorld.worldGenerator.getStructureAt(world, chunkX - radius, z) == spawn)
					return new ChunkPos(chunkX - radius, z);
				if(HbmWorld.worldGenerator.getStructureAt(world, chunkX + radius, z) == spawn)
					return new ChunkPos(chunkX + radius, z);
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getTabCompletions(net.minecraft.server.MinecraftServer server, ICommandSender sender, String[] args, net.minecraft.util.math.BlockPos targetPos) {
		if(args.length < 1)
			return Collections.emptyList();

		if(args.length == 1)
			return getListOfStringsMatchingLastWord(args, "structure", "list");

		if(args.length == 2 && args[0].equals("structure")) {
			List<String> structures = NBTStructure.listStructures();
			return getListOfStringsMatchingLastWord(args, structures.toArray(new String[structures.size()]));
		}

		return Collections.emptyList();
	}

}
