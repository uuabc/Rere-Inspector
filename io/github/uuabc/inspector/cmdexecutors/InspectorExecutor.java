package io.github.uuabc.inspector.cmdexecutors;

import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.command.args.*;
import io.github.uuabc.inspector.Inspector;

import org.spongepowered.api.command.*;

public class InspectorExecutor implements CommandExecutor {
	public CommandResult execute(final CommandSource src, final CommandContext ctx) throws CommandException {
		src.sendMessage(Inspector.getMessage().getmessage("inspector.use",Inspector.instance().getPluginContainer().getVersion().get()));
		if (src.hasPermission("inspector.toggle"))
			src.sendMessage(Inspector.getMessage().getmessage("inspector.toggle.help"));
		if (src.hasPermission("inspector.rollback"))
			src.sendMessage(Inspector.getMessage().getmessage("inspector.rollback.help"));
		if (src.hasPermission("inspector.pruge"))
			src.sendMessage(Inspector.getMessage().getmessage("inspector.purge.help"));
		if (src.hasPermission("inspector.reload"))
			src.sendMessage(Inspector.getMessage().getmessage("inspector.reload.help"));
		return CommandResult.success();
	}
}
