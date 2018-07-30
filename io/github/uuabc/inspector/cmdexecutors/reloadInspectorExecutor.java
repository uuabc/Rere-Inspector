package io.github.uuabc.inspector.cmdexecutors;

import org.spongepowered.api.command.spec.*;
import io.github.uuabc.inspector.Inspector;
import org.spongepowered.api.command.args.*;
import java.io.*;
import org.spongepowered.api.command.*;

public class reloadInspectorExecutor implements CommandExecutor
{
    Inspector inspector;
    
    public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
        this.inspector = Inspector.instance();
        try {
            this.inspector.loadConfig();
            this.inspector.loadmessage();
            src.sendMessage(Inspector.getMessage().getmessage("inspector.reload.success"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return CommandResult.success();
    }
}
