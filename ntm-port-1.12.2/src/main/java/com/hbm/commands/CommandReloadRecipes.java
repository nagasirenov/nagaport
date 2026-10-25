package com.hbm.commands;

import com.hbm.config.ItemPoolConfigJSON;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.util.ChatBuilder;
import com.hbm.util.DamageResistanceHandler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandReloadRecipes extends CommandBase {


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
		return "ntmreload";
	}


	public String getCommandUsage(ICommandSender sender) {
		return "/ntmreload";
	}


	public void processCommand(ICommandSender sender, String[] args) {
		try {
			FluidContainerRegistry.clearRegistry(); // we do this first so IFluidRegisterListener can go wild with the registry
			Fluids.reloadFluids();
			FluidContainerRegistry.register();
			SerializableRecipe.initialize();
			ItemPoolConfigJSON.initialize();
			DamageResistanceHandler.init();

			sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Reload complete :)"));
		} catch(Exception ex) {
			sender.sendMessage(ChatBuilder.start("----------------------------------").color(TextFormatting.GRAY).flush());
			sender.sendMessage(ChatBuilder.start("An error has occoured during loading, consult the log for details.").color(TextFormatting.RED).flush());
			sender.sendMessage(ChatBuilder.start(ex.getLocalizedMessage()).color(TextFormatting.RED).flush());
			sender.sendMessage(ChatBuilder.start(ex.getStackTrace()[0].toString()).color(TextFormatting.RED).flush());
			sender.sendMessage(ChatBuilder.start("----------------------------------").color(TextFormatting.GRAY).flush());
			throw ex;
		}
	}
}
