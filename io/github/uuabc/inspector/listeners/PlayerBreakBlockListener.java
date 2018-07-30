package io.github.uuabc.inspector.listeners;

import org.spongepowered.api.event.block.*;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.player.*;
import org.spongepowered.api.event.filter.cause.*;
import io.github.uuabc.inspector.Inspector;

import org.spongepowered.api.block.*;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.world.*;
import org.spongepowered.api.world.explosion.Explosion;

import org.spongepowered.api.event.*;
import org.spongepowered.api.data.*;
import java.util.*;

public class PlayerBreakBlockListener {

	
	@Listener
	public void onPlayerBreakBlock(final ChangeBlockEvent.Break event, @Root final Player player) {
		if (!Inspector.config.getNode(new Object[] { "worlds" , player.getWorld().getName()}).getBoolean()) {
			return;
		}
		final Iterator<?> var3 = event.getTransactions().iterator();
		final List<Transaction<BlockSnapshot>> list = (List<Transaction<BlockSnapshot>>) event.getTransactions();
		for (final Transaction<BlockSnapshot> transaction : list) {
			ItemStack itemStack = null;
			try {
				itemStack = ItemStack.builder().fromBlockSnapshot((BlockSnapshot) transaction.getOriginal()).build();
			} catch (Exception e) {
			}
			if (itemStack !=null && this.isBanned(itemStack, player)) {
				return;
			}
		}
		while (var3.hasNext()) {
			@SuppressWarnings("unchecked")
			final Transaction<BlockSnapshot> transaction2 = (Transaction<BlockSnapshot>) var3.next();
			final Location<World> tranLocation = (Location<World>) ((BlockSnapshot) transaction2.getOriginal())
					.getLocation().get();
			Inspector.instance().getDatabaseManager().updateBlockInformation(tranLocation.getBlockX(),
					tranLocation.getBlockY(), tranLocation.getBlockZ(),
					((World) tranLocation.getExtent()).getUniqueId(), player.getUniqueId().toString(), player.getName(),
					transaction2.getOriginal().getState(), transaction2.getFinal().getState());
		}
	}

	
	
	
	@Listener
	public void BlockBreak(ChangeBlockEvent.Break e, @Root Explosion ex) {

		if (!Inspector.config.getNode(new Object[] { "worlds",ex.getWorld().getName() }).getBoolean()) {
			return;
		}
		
		if (!ex.getSourceExplosive().isPresent()) return;
		String name = ex.getSourceExplosive().get().getType().getName();
		
		
		String UUIDname = null;
		String pname = null;
		if( ex.getSourceExplosive().get().getCreator().isPresent()) {
			UUIDname = ex.getSourceExplosive().get().getCreator().get().toString();
			pname = Inspector.instance().getDatabaseManager().getPlayerName(Inspector.instance().getDatabaseManager().getPlayerId(UUIDname));
		}
		
		for (final Transaction<BlockSnapshot> transaction : e.getTransactions()) {
			final Location<World> tranLocation = (Location<World>) ((BlockSnapshot) transaction.getFinal())
					.getLocation().get();
			Inspector.instance().getDatabaseManager().updateBlockInformation(tranLocation.getBlockX(),
					tranLocation.getBlockY(), tranLocation.getBlockZ(),
					((World) tranLocation.getExtent()).getUniqueId(),UUIDname+":"+name ,pname+":"+name  ,
					transaction.getOriginal().getState(), transaction.getFinal().getState());

		}
	}
	
	
	
	@Listener(order = Order.POST)
	public void onBlockBreak(ChangeBlockEvent.Break e, @Root Entity entity) {
		if (!Inspector.config.getNode(new Object[] { "worlds",entity.getWorld().getName() }).getBoolean()) {
			return;
		}
		String UUIDname = null;
		String pname = null;
		String name = entity.getType().getName();
		if( entity.getCreator().isPresent()) {
			UUIDname = entity.getCreator().get().toString();
			pname = Inspector.instance().getDatabaseManager().getPlayerName(Inspector.instance().getDatabaseManager().getPlayerId(UUIDname));
		}
		
		if (entity instanceof Player || entity instanceof Agent) return;
		for (final Transaction<BlockSnapshot> transaction : e.getTransactions()) {
			final Location<World> tranLocation = (Location<World>) ((BlockSnapshot) transaction.getFinal())
					.getLocation().get();
			
			
			Inspector.instance().getDatabaseManager().updateBlockInformation(tranLocation.getBlockX(),
					tranLocation.getBlockY(), tranLocation.getBlockZ(),
					((World) tranLocation.getExtent()).getUniqueId(),UUIDname+":"+name ,pname+":"+name ,
					transaction.getOriginal().getState(), transaction.getFinal().getState());

		}
	}
	
	
	private boolean isBanned(final ItemStack itemStack, final Player player) {
		final String itemType = itemStack.getType().getId();
		final DataContainer container = itemStack.toContainer();
		final DataQuery query = DataQuery.of('/', "UnsafeDamage");
		final Set<String> bwl = Inspector.instance().getBlockWhiteList();
		return bwl.contains(itemType) || bwl.contains(itemType + ":" + container.get(query).get().toString())
				|| bwl.contains(itemType + ":*");
	}
}
