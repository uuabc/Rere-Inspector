package io.github.uuabc.inspector.listeners;

import org.spongepowered.api.event.world.*;
import org.spongepowered.api.entity.living.player.*;
import org.spongepowered.api.event.filter.cause.*;
import io.github.uuabc.inspector.Inspector;

import org.spongepowered.api.data.*;
import org.spongepowered.api.block.*;
import org.spongepowered.api.world.*;
import org.spongepowered.api.event.*;

public class ExplosionListener
{
    @Listener
    public void onExplosion(final ExplosionEvent.Post event, @First final Player player) {
        if (!Inspector.config.getNode(new Object[] { "worlds" ,player.getWorld().getName()}).getBoolean()) {
            return;
        }
        for (final Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            final Location<World> transactionLocation = (Location<World>)((BlockSnapshot)transaction.getFinal()).getLocation().get();
            Inspector.instance().getDatabaseManager().updateBlockInformation(transactionLocation.getBlockX(), transactionLocation.getBlockY(), transactionLocation.getBlockZ(), ((World)transactionLocation.getExtent()).getUniqueId(), player.getUniqueId().toString(), player.getName(), transaction.getOriginal().getState(), transaction.getFinal().getState());
        }
    }
}
