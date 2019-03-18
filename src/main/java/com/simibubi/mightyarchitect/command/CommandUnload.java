package com.simibubi.mightyarchitect.command;

import com.simibubi.mightyarchitect.control.ArchitectManager;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.IClientCommand;

public class CommandUnload extends CommandBase implements IClientCommand {

	@Override
	public String getName() {
		return "unload";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/unload";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			ArchitectManager.unload();
		}
		
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
		return false;
	}

}
