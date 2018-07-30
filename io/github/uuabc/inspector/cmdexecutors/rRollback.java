package io.github.uuabc.inspector.cmdexecutors;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.World;

import com.google.common.collect.Lists;

import io.github.uuabc.inspector.Inspector;
import io.github.uuabc.inspector.utilities.BlockInformation;
import io.github.uuabc.inspector.utilities.Region;
import io.github.uuabc.inspector.utilities.Utils;

public class rRollback implements CommandExecutor {
	Inspector p = Inspector.instance();

	public CommandResult execute(final CommandSource src, final CommandContext ctx) {

		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			final Optional<String> time = ctx.getOne("time");
			final Optional<String> option1 = ctx.getOne("option1");
			final Optional<String> option2 = ctx.getOne("option2");
			if (!(src instanceof Player)) {
				src.sendMessage(Inspector.getMessage().getmessage("inspector.rollback.not.player"));
				return;
			}
			final Player player = (Player) src;

			String lclDate = null;

			if (time.isPresent()) {
				lclDate = commonhandle.parseTime(time.get());
				if (lclDate == null) {
					player.sendMessage(Inspector.getMessage().getmessage("inspector.rollback.error.time", time.get()));
					return;
				}
			}
			String targetPlayer = null;
			String targetPlayeruuid = null;
			Integer ra = null;
			if (option1.isPresent()) {
				if ((option1.get().contains("u:") && option1.get().contains("r:"))
						|| (!option1.get().contains("u:") && !option1.get().contains("r:"))) {
					player.sendMessage(
							Inspector.getMessage().getmessage("inspector.rollback.error.time", option1.get()));
					return;
				}
				if (option1.get().contains("u:")) {
					targetPlayer = option1.get().replaceFirst("u:", "");
					targetPlayeruuid = Inspector.instance().getDatabaseManager().getPlayeUUIDfrName(targetPlayer);
					if (targetPlayeruuid == null) {
						player.sendMessage(Inspector.getMessage().getmessage("inspector.rollback.error.user", option1.get()));
						return;
					}
				}
				if (option1.get().contains("r:")) {
					try {
						ra = Integer.valueOf(option1.get().replace("r:", ""));
					} catch (Exception e) {
						player.sendMessage(Inspector.getMessage().getmessage("inspector.rollback.error.radius", option1.get()));
					}
				}
			}

			if (option2.isPresent()) {
				if ((option2.get().contains("u:") && option2.get().contains("r:"))
						|| (!option2.get().contains("u:") && !option2.get().contains("r:"))) {
					player.sendMessage(
							Inspector.getMessage().getmessage("inspector.rollback.error.time", option2.get()));
					return;
				}
				if (option2.get().contains("u:")) {
					targetPlayer = option2.get().replaceFirst("u:", "");
					targetPlayeruuid = Inspector.instance().getDatabaseManager().getPlayeUUIDfrName(targetPlayer);
					if (targetPlayeruuid == null) {
						player.sendMessage(Inspector.getMessage().getmessage("inspector.rollback.error.user", option2.get()));
						return;
					}
				}
				if (option2.get().contains("r:")) {
					try {
						ra = Integer.valueOf(option2.get().replace("r:", ""));
					} catch (Exception e) {
						player.sendMessage(Inspector.getMessage().getmessage("inspector.rollback.error.radius", option2.get()));
					}
				}
			}

			// Inspector.instance().getLogger().info(radius.get());
			// Inspector.instance().getLogger().info(radius.get());

			int xmin = 0;
			int xmax = 0;
			int ymin = 0;
			int ymax = 0;
			int zmin = 0;
			int zmax = 0;

			if (ra != null) {
				xmin = player.getLocation().getBlockX() - ra;
				xmax = player.getLocation().getBlockX() + ra;
				ymin = player.getLocation().getBlockY() - ra;
				ymax = player.getLocation().getBlockY() + ra;
				zmin = player.getLocation().getBlockZ() - ra;
				zmax = player.getLocation().getBlockZ() + ra;
			}

			if (ra == null) {

				final Optional<Region> optionalRegion = Utils.getRegion(player.getUniqueId());
				if (!optionalRegion.isPresent()) {
					player.sendMessage(Inspector.getMessage().getmessage("inspector.rollback.no.region"));
					return;
				}
				final Region region = optionalRegion.get();

				if (region.getPointA() == null && region.getPointB() == null) {
					player.sendMessage(Inspector.getMessage().getmessage("inspector.rollback.missing.point"));
					return;
				}

				if (!((World) region.getPointA().getExtent()).getUniqueId()
						.equals(((World) region.getPointB().getExtent()).getUniqueId())) {
					player.sendMessage(Inspector.getMessage().getmessage("inspector.rollback.different.worlds"));
					return;
				}

				xmin = region.getPointA().getBlockX();
				xmax = region.getPointB().getBlockX();
				ymin = region.getPointA().getBlockY();
				ymax = region.getPointB().getBlockY();
				zmin = region.getPointA().getBlockZ();
				zmax = region.getPointB().getBlockZ();
			}

			int sum = 0;
			// Inspector.instance().getLogger().info(lclDate);
			UUID wuu = player.getWorld().getUniqueId();
			List<BlockInformation> results = p.getDatabaseManager().getBlockInformationAt(wuu, targetPlayeruuid,
					lclDate, xmin, xmax, ymin, ymax, zmin, zmax);

			for (BlockInformation blockInfo : results) {
				if (!blockInfo.getLocation().getBlock().equals(blockInfo.getOldBlockSnapshot())) {
					blockInfo.getLocation().setBlock(blockInfo.getOldBlockSnapshot());
				}
			}
			sum = results.size();
			final List<Text> blockChanges = Lists.newArrayList();

			final Text blockChange = Text.builder()
					.append(new Text[] { Inspector.getMessage().getmessage("inspector.rollback.text.box1",lclDate)})
					.append(new Text[] { Text.builder()
							.append(new Text[] {  Inspector.getMessage().getmessage("inspector.rollback.text.box2",targetPlayer) })
							.onHover((HoverAction<?>) TextActions.showText(Text
									.of(new Object[] { Inspector.getMessage().getmessage("inspector.rollback.text.box21", targetPlayeruuid )})))
							.build() })
					.append(new Text[] { Text.builder()
							.append(new Text[] {Inspector.getMessage().getmessage("inspector.rollback.text.box3" ,String.valueOf(xmin),String.valueOf(ymin),String.valueOf(zmin)) })
							.build() })
					.append(new Text[] { Text.builder()
							.append(new Text[] { Inspector.getMessage().getmessage("inspector.rollback.text.box4" ,String.valueOf(xmax),String.valueOf(ymax),String.valueOf(zmax)) })
							.build() })
					.append(new Text[] { Text.builder()
							.append(new Text[] { Inspector.getMessage().getmessage("inspector.rollback.text.box5",String.valueOf(sum)) })
							.build() })
					.build();
			blockChanges.add(blockChange);
			final PaginationService paginationService = (PaginationService) Sponge.getServiceManager()
					.provide((Class<?>) PaginationService.class).get();
			final PaginationList.Builder paginationBuilder = paginationService.builder()
					.title(Inspector.getMessage().getmessage("inspector.rollback.text.box0"))
					.padding(Inspector.getMessage().getmessage("inspector.rollback.text.boxpadding")).contents((Iterable<Text>) blockChanges);
			paginationBuilder.sendTo((MessageReceiver) player);
			return;
		}).submit((Object) Inspector.instance());
		src.sendMessage(Inspector.getMessage().getmessage("inspector.rollback.text.box.wait"));
		return CommandResult.success();
	}

}
