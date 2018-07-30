package io.github.uuabc.inspector.listeners;

import org.spongepowered.api.block.tileentity.carrier.Hopper;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.AffectSlotEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import io.github.uuabc.inspector.Inspector;

import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotIndex;

public class ChangeInventoryListener {

	@Listener
	public void ChangeInventoryEvent(final ChangeInventoryEvent.Transfer.Post event, @First final Player playe) {
		Inspector.instance().getLogger().info("ChangeInventoryEvent.Transfer.Post");
		if (!Inspector.config.getNode(new Object[] { "worlds" }).getNode(new Object[] { playe.getWorld().getName() })
				.getBoolean()) {
			return;
		}

		
		
		if (playe instanceof Player || !(event.getSourceInventory() instanceof CarriedInventory)
				|| !(event.getTargetInventory() instanceof CarriedInventory)) {
			return;
		}

		String cause = null;
		if (event.getCause().containsType(Hopper.class)) {
			cause = event.getCause().first(Hopper.class).get().getType().getName();
		}

		if (cause == null)
			return;
		for (SlotTransaction transaction : event.getTransactions()) {
			Inventory root = transaction.getSlot().root();
			if (!(root instanceof CarriedInventory))
				continue;

			CarriedInventory<?> carriedRoot = (CarriedInventory<?>) root;
			if (!carriedRoot.getCarrier().isPresent() || !(carriedRoot.getCarrier().get() instanceof BlockCarrier))
				continue;
			Location<World> Location = ((BlockCarrier) carriedRoot.getCarrier().get()).getLocation();

			int slotId = transaction.getSlot().getProperty(SlotIndex.class, "slotindex").map(SlotIndex::getValue)
					.orElse(-1);
			if (slotId >= root.capacity())
				continue;

			ItemStackSnapshot oldItem = transaction.getOriginal();
			ItemStackSnapshot newItem = transaction.getFinal();
			if (oldItem == newItem)
				continue;
			Inspector.instance().getDatabaseManager().updateContainerInformation(Location.getBlockX(),
					Location.getBlockY(), Location.getBlockZ(), Location.getExtent().getUniqueId(), playe.getUniqueId().toString(),
					playe.getName(),oldItem, newItem);

		}
	}

	@Listener
	public void onAffectSlot(AffectSlotEvent e, @First Player playe) {
		if (!Inspector.config.getNode(new Object[] { "worlds" }).getNode(new Object[] { playe.getWorld().getName() })
				.getBoolean()) {
			return;
		}
		if (e.getTransactions().isEmpty())
			return;
		if (!(e.getTransactions().get(0).getSlot().parent() instanceof CarriedInventory))
			return;

		BlockCarrier carrier = null;
		CarriedInventory<?> c = (CarriedInventory<?>) e.getTransactions().get(0).getSlot().parent();
		if (!c.getCarrier().isPresent())
			return;

		if (c.getCarrier().get() instanceof BlockCarrier) {
			carrier = (BlockCarrier) c.getCarrier().get();
		}

		if (carrier == null)
			return;

		Location<World> Location = carrier.getLocation();

		int containerSize = c.iterator().next().capacity();
		for (SlotTransaction transaction : e.getTransactions()) {
			int slotId = transaction.getSlot().getProperty(SlotIndex.class, "slotindex").map(SlotIndex::getValue)
					.orElse(-1);
			if (slotId >= containerSize)
				continue;

			ItemStackSnapshot oldItem = transaction.getOriginal();
			ItemStackSnapshot newItem = transaction.getFinal();
			if (oldItem == newItem)
				continue;

			Inspector.instance().getDatabaseManager().updateContainerInformation(Location.getBlockX(),
					Location.getBlockY(), Location.getBlockZ(), Location.getExtent().getUniqueId(), playe.getUniqueId().toString(),
					playe.getName(),oldItem, newItem);

		}

	}

}
