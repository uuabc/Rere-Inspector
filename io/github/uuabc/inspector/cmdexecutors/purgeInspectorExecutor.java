package io.github.uuabc.inspector.cmdexecutors;

import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.command.args.*;

import org.spongepowered.api.entity.living.player.*;
import io.github.uuabc.inspector.Inspector;

import org.spongepowered.api.command.*;

public class purgeInspectorExecutor implements CommandExecutor {
	public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
		final String hour = (String) args.getOne("time").get();
		String time = commonhandle.parseTime(hour);
		if (src instanceof Player) {
			Inspector.instance().getLogger().info(
					Inspector.getMessage().getstringmessage("inspector.purge.player",((Player) src).getName(),time));
		} else {
			Inspector.instance().getLogger()
					.info(Inspector.getMessage().getstringmessage("inspector.purge.console",time));
		}
		Inspector.instance().getDatabaseManager().clearExpiredData(time);
		src.sendMessage(Inspector.getMessage().getmessage("inspector.purge.success"));
		return CommandResult.success();
	}
}
