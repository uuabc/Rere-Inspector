package io.github.uuabc.inspector.cmdexecutors;

import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.entity.living.player.*;
import io.github.uuabc.inspector.Inspector;

import org.spongepowered.api.command.*;

public class ToggleInspectorExecutor implements CommandExecutor
{
    public CommandResult execute(final CommandSource src, final CommandContext ctx) throws CommandException {
        if (src instanceof Player) {
            final Player player = (Player)src;
            if (Inspector.inspectorEnabledPlayers.contains(player.getUniqueId())) {
                Inspector.inspectorEnabledPlayers.remove(player.getUniqueId());
                player.sendMessage(Inspector.getMessage().getmessage("inspector.toggle.off"));
            }
            else {
                Inspector.inspectorEnabledPlayers.add(player.getUniqueId());
                player.sendMessage(Inspector.getMessage().getmessage("inspector.toggle.on"));
            }
        }
        else {
            src.sendMessage(Inspector.getMessage().getmessage("inspector.toggle.not.player"));
        }
        return CommandResult.success();
    }
}
