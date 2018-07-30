package io.github.uuabc.inspector.listeners;

import org.spongepowered.api.event.block.*;
import org.spongepowered.api.entity.living.player.*;
import org.spongepowered.api.event.filter.cause.*;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;

import java.util.List;

import org.spongepowered.api.block.*;
import org.spongepowered.api.world.*;

import io.github.uuabc.inspector.Inspector;


import org.spongepowered.api.event.*;

public class PlayerPlaceBlockListener {
	@Listener
	public void onPlayerPlaceBlock(final ChangeBlockEvent.Place event, @First final Player player) {
		if (!Inspector.config.getNode(new Object[] { "worlds" ,player.getWorld().getName()})
				.getBoolean()) {
			return;
		}
		if (!(player instanceof Player)) {
			return;
		}


		for (final Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			final Location<World> transactionLocation = (Location<World>) ((BlockSnapshot) transaction.getFinal())
					.getLocation().get();
			Inspector.instance().getDatabaseManager().updateBlockInformation(transactionLocation.getBlockX(),
					transactionLocation.getBlockY(), transactionLocation.getBlockZ(),
					((World) transactionLocation.getExtent()).getUniqueId(), player.getUniqueId().toString(), player.getName(),
					transaction.getOriginal().getState(), transaction.getFinal().getState());

		}
	}

/*	@Listener
	public void ChangeSignEvent(final ChangeSignEvent event,@First final Player player) {
		if (!Inspector.config.getNode(new Object[] { "worlds",player.getWorld().getName() })
				.getBoolean()) {
			return;
		}
        if (!event.isCancelled()) {
            final ImmutableSignData block = event.getOriginalText();
            List<Text> s = block.lines().get();
        	player.sendMessage(Text.of(s.toString()));
        }
    }*/

	
	
	
}
