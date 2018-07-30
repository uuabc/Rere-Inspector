package io.github.uuabc.inspector.listeners;

import org.spongepowered.api.event.block.*;
import org.spongepowered.api.entity.living.player.*;
import org.spongepowered.api.text.*;
import org.spongepowered.api.world.*;
import com.google.common.collect.*;

import io.github.uuabc.inspector.Inspector;
import io.github.uuabc.inspector.utilities.BlockViewInfo;
import io.github.uuabc.inspector.utilities.ItemStackInformation;
import io.github.uuabc.inspector.utilities.Utils;

import org.spongepowered.api.text.format.*;
import org.spongepowered.api.text.action.*;
import org.spongepowered.api.*;
import org.spongepowered.api.text.channel.*;
import java.util.*;
import org.spongepowered.api.service.pagination.*;
import org.spongepowered.api.event.*;
import org.spongepowered.api.event.filter.cause.*;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.data.type.*;

public class PlayerInteractBlockListener {
	@Listener(order = Order.FIRST)
	public void onPlayerClickBlock(final InteractBlockEvent.Primary event, @First final Player player) {
		if (Inspector.inspectorEnabledPlayers.contains(player.getUniqueId())) {
			if (!Inspector.config.getNode(new Object[] { "worlds", player.getWorld().getName() }).getBoolean()) {
				player.sendMessage(Inspector.getMessage().getmessage("inspector.notenable.world"));
				return;
			}
			event.setCancelled(true);
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				final Optional<Location<World>> location = (Optional<Location<World>>) event.getTargetBlock()
						.getLocation();
				if (location.isPresent()) {
					final List<BlockViewInfo> information = Inspector.instance().getDatabaseManager()
							.getBlockInformationAt(location.get());

					if (information.size() == 0) {
						player.sendMessage(Inspector.getMessage().getmessage("inspector.no.blockinfo"));
						return;
					}
					final List<Text> blockChanges = Lists.newArrayList();
					for (final BlockViewInfo blockInfo : information) {
						String[] pname = null;
						String UUID = null;
						// Inspector.instance().getLogger().info( blockInfo.getPlayerName());
						pname = blockInfo.getPlayerName().split(":");
						UUID = blockInfo.getPlayerUUID().split(":")[0];
						final Text blockChange = Text.builder()
								.append(new Text[] { Inspector.getMessage().getmessage("inspector.blockinfo.box1",
										blockInfo.getBlocktime().toString()) })
								.append(new Text[] { Text.builder()
										.append(new Text[] { Inspector.getMessage().getmessage(
												"inspector.blockinfo.box2", pname.length >= 1 ? pname[0] : null,
												pname.length == 2 ? " Use:" : "", pname.length == 2 ? pname[1] : "") })
										.onHover((HoverAction<?>) TextActions.showText(
												Inspector.getMessage().getmessage("inspector.blockinfo.box21", UUID)))
										.build() })
								.append(new Text[] { Text.builder()
										.append(new Text[] {
												Inspector.getMessage().getmessage("inspector.blockinfo.box3"),
												Text.of(new Object[] { TextColors.GOLD, TextStyles.UNDERLINE,
														blockInfo.getOldType(), "\n" }) })
										.onHover((HoverAction<?>) TextActions.showText(Inspector.getMessage()
												.getmessage("inspector.blockinfo.box31", blockInfo.getOldId())))
										.build() })
								.append(new Text[] { Text.builder()
										.append(new Text[] {
												Inspector.getMessage().getmessage("inspector.blockinfo.box4"),
												Text.of(new Object[] { TextColors.GOLD, TextStyles.UNDERLINE,
														blockInfo.getNewType(), "\n" }) })
										.onHover((HoverAction<?>) TextActions.showText(Inspector.getMessage()
												.getmessage("inspector.blockinfo.box41", blockInfo.getNewId())))
										.build() })
								.build();
						blockChanges.add(blockChange);
					}
					final PaginationService paginationService = (PaginationService) Sponge.getServiceManager()
							.provide((Class<?>) PaginationService.class).get();
					final PaginationList.Builder paginationBuilder = paginationService.builder()
							.title(Inspector.getMessage().getmessage("inspector.blockinfo.box0"))
							.padding(Inspector.getMessage().getmessage("inspector.blockinfo.boxpadding"))
							.contents((Iterable<Text>) blockChanges);
					paginationBuilder.sendTo((MessageReceiver) player);
				}
			}).submit((Object) Inspector.instance());
		}
	}

	@Listener(order = Order.FIRST)
	public void onPlayerClickBlock(final InteractBlockEvent.Secondary event, @First final Player player) {
		if (Inspector.inspectorEnabledPlayers.contains(player.getUniqueId())) {
			if (!Inspector.config.getNode(new Object[] { "worlds", player.getWorld().getName() }).getBoolean()) {
				player.sendMessage(Inspector.getMessage().getmessage("inspector.notenable.world"));
				return;
			}
			event.setCancelled(true);
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				final Optional<Location<World>> location = (Optional<Location<World>>) event.getTargetBlock()
						.getLocation();
				if (location.isPresent()) {
					final ArrayList<ItemStackInformation> ItemS = Inspector.instance().getDatabaseManager()
							.getContainerInformationAt(location.get());
					if (ItemS.size() == 0) {
						player.sendMessage(Inspector.getMessage().getmessage("inspector.no.Containerinfo"));
						return;
					}
					final List<Text> blockChanges = Lists.newArrayList();
					for (final ItemStackInformation ItemInfo : ItemS) {
						final ItemStack oldItem = ItemInfo.getOldItemStack();
						final ItemStack newItem = ItemInfo.getNewItemStack();
						final String playerName = ItemInfo.getPlayerName();
						final String playerUUID = ItemInfo.getPlayerUUID();
						final Date time = ItemInfo.getTimeEdited();
						final Text blockChange = Text.builder()
								.append(new Text[] {
										Inspector.getMessage().getmessage("inspector.iteminfo.box1", time.toString()) })
								.append(new Text[] { Text.builder()
										.append(new Text[] { Inspector.getMessage()
												.getmessage("inspector.iteminfo.box2", playerName) })
										.onHover((HoverAction<?>) TextActions.showText(Inspector.getMessage()
												.getmessage("inspector.iteminfo.box21", playerUUID.toString())))
										.build() })
								.append(new Text[] { Text.builder()
										.append(new Text[] {
												Inspector.getMessage().getmessage("inspector.iteminfo.box3") })
										.append(new Text[] { Text.of(new Object[] { TextColors.GOLD,
												TextStyles.UNDERLINE, oldItem.getType() }) })
										.onHover((HoverAction<?>) TextActions
												.showText(Inspector.getMessage().getmessage("inspector.iteminfo.box31",
														oldItem.getType().getTranslation().get())))
										.append(new Text[] { Inspector.getMessage().getmessage(
												"inspector.iteminfo.box30", String.valueOf(oldItem.getQuantity())) })
										.build() })
								.append(new Text[] { Text.builder()
										.append(new Text[] {
												Inspector.getMessage().getmessage("inspector.iteminfo.box4") })
										.append(new Text[] {

												Text.of(new Object[] { TextColors.GOLD, TextStyles.UNDERLINE,
														newItem.getType() }) })
										.onHover((HoverAction<?>) TextActions
												.showText(Inspector.getMessage().getmessage("inspector.iteminfo.box41",
														newItem.getType().getTranslation().get())))
										.append(new Text[] { Inspector.getMessage().getmessage(
												"inspector.iteminfo.box40", String.valueOf(newItem.getQuantity())) })
										.build() })
								.build();
						blockChanges.add(blockChange);
					}
					final PaginationService paginationService = (PaginationService) Sponge.getServiceManager()
							.provide((Class<?>) PaginationService.class).get();
					final PaginationList.Builder paginationBuilder = paginationService.builder()
							.title(Inspector.getMessage().getmessage("inspector.iteminfo.box0"))
							.padding(Inspector.getMessage().getmessage("inspector.iteminfo.boxpadding"))
							.contents((Iterable<Text>) blockChanges);
					paginationBuilder.sendTo((MessageReceiver) player);
				}
			}).submit((Object) Inspector.instance());
		}
	}

	@Listener
	public void onPlayerLeftClickBlock(final InteractBlockEvent.Primary event, @Root final Player player) {
		if (player.hasPermission("inspector.region.use") && player.getItemInHand(HandTypes.MAIN_HAND).isPresent()
				&& player.getItemInHand(HandTypes.MAIN_HAND).get().getType().getName()
						.equals(Utils.getConfigValue("inspector.select.tool"))) {
			if (!Inspector.config.getNode(new Object[] { "worlds", player.getWorld().getName() }).getBoolean()) {
				player.sendMessage(Inspector.getMessage().getmessage("inspector.notenable.world"));
				return;
			}
			final Location<World> pointA = (Location<World>) event.getTargetBlock().getLocation().get();
			Utils.addPointOrCreateRegionOf(player.getUniqueId(), pointA, false);
			player.sendMessage(
					Inspector.getMessage().getmessage("inspector.set.positionA", String.valueOf(pointA.getBlockX()),
							String.valueOf(pointA.getBlockY()), String.valueOf(pointA.getBlockZ())));
			event.setCancelled(true);
		}
	}

	@Listener
	public void onPlayerRightClickBlock(final InteractBlockEvent.Secondary event, @Root final Player player) {
		if (player.hasPermission("inspector.region.use") && player.getItemInHand(HandTypes.MAIN_HAND).isPresent()
				&& player.getItemInHand(HandTypes.MAIN_HAND).get().getType().getName()
						.equals(Utils.getConfigValue("inspector.select.tool"))) {
			if (!Inspector.config.getNode(new Object[] { "worlds", player.getWorld().getName() }).getBoolean()) {
				player.sendMessage(Inspector.getMessage().getmessage("inspector.notenable.world"));
				return;
			}
			final Location<World> pointB = (Location<World>) event.getTargetBlock().getLocation().get();
			Utils.addPointOrCreateRegionOf(player.getUniqueId(), pointB, true);
			player.sendMessage(
					Inspector.getMessage().getmessage("inspector.set.positionA", String.valueOf(pointB.getBlockX()),
							String.valueOf(pointB.getBlockY()), String.valueOf(pointB.getBlockZ())));
			event.setCancelled(true);
		}
	}
}
