package io.github.hsyyid.inspector;

//import me.flibio.updatifier.*;
import ninja.leaping.configurate.commented.*;
import ninja.leaping.configurate.loader.*;
import io.github.hsyyid.inspector.utilities.*;
import org.spongepowered.api.plugin.*;
import com.google.inject.*;
import org.spongepowered.api.config.*;
import com.google.common.collect.*;
import java.io.*;
import org.spongepowered.api.text.*;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.command.args.*;
import io.github.hsyyid.inspector.cmdexecutors.*;

import org.slf4j.Logger;
import org.spongepowered.api.*;
import org.spongepowered.api.command.*;
import io.github.hsyyid.inspector.listeners.*;
import org.spongepowered.api.event.*;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.world.*;
import ninja.leaping.configurate.*;
import java.util.stream.*;
import java.util.*;


//@Updatifier(repoName = "Inspector", repoOwner = "hsyyid", version = "v0.6.7-UnofficialThread")
@Plugin(id = "inspector", name = "Inspector", version = "0.7", description = "This plugin enables servers to monitor griefing and provides rollback.", dependencies = {
		@Dependency(id = "Updatifier", version = "1.0", optional = true) })
public class Inspector {
	private DatabaseManager databaseManager;
	private static Inspector instance;
	public static CommentedConfigurationNode config;
	public static ConfigurationLoader<CommentedConfigurationNode> configurationManager;
	public static Set<UUID> inspectorEnabledPlayers;
	public static Set<Region> regions;
	private Set<String> blockWhiteList;
	@Inject
	private PluginContainer pluginContainer;
	@Inject
	private Logger logger;
	@Inject
	@DefaultConfig(sharedRoot = true)
	private File dConfig;
	@Inject
	@DefaultConfig(sharedRoot = true)
	private ConfigurationLoader<CommentedConfigurationNode> confManager;

	public Inspector() {
		this.blockWhiteList = Sets.newHashSet();
	}

	public DatabaseManager getDatabaseManager() {
		return this.databaseManager;
	}

	public static Inspector instance() {
		return Inspector.instance;
	}

	public PluginContainer getPluginContainer() {
		return this.pluginContainer;
	}

	public Logger getLogger() {
		return this.logger;
	}

	@Listener
	public void onGameInit(final GameStartedServerEvent event) {
		this.getLogger().info("Inspector loading...");
		Inspector.instance = this;
		this.databaseManager = new DatabaseManager();
		try {
			this.loadConfig();
		} catch (IOException exception) {
			this.getLogger().error("The default configuration could not be loaded or created!");
		}
		final HashMap<List<String>, CommandSpec> inspectorSubcommands = new HashMap<List<String>, CommandSpec>();
		inspectorSubcommands.put(Arrays.asList("reload"),
				CommandSpec.builder().description((Text) Text.of("reload Inspector")).permission("inspector.reload")
						.executor((CommandExecutor) new reloadInspectorExecutor()).build());
		inspectorSubcommands.put(Arrays.asList("pruge"),
				CommandSpec.builder().description((Text) Text.of("clean up the expired data"))
						.permission("inspector.purge").arguments(GenericArguments.string((Text) Text.of("Hour")))
						.executor((CommandExecutor) new purgeInspectorExecutor()).build());
		inspectorSubcommands.put(Arrays.asList("toggle"),
				CommandSpec.builder().description((Text) Text.of("Toggle Inspector Command"))
						.permission("inspector.toggle").executor((CommandExecutor) new ToggleInspectorExecutor())
						.build());
		inspectorSubcommands.put(Arrays.asList("rollback"), CommandSpec.builder()
				.description((Text) Text.of("Rollback Command")).permission("inspector.rollback")
				.arguments(GenericArguments.seq(
						GenericArguments.onlyOne(GenericArguments.string((Text) Text.of("time"))),
						GenericArguments
						.optional(GenericArguments.onlyOne(GenericArguments.string((Text) Text.of("option1")))),
						GenericArguments
								.optional(GenericArguments.onlyOne(GenericArguments.string((Text) Text.of("option2"))))
						))
				.executor((CommandExecutor) new rRollback()).build());
		
		final CommandSpec inspectorCommandSpec = CommandSpec.builder().description((Text) Text.of("Inspector Command"))
				.permission("inspector.use").executor((CommandExecutor) new InspectorExecutor())
				.children((Map<List<String>, CommandSpec>) inspectorSubcommands).build();
		Sponge.getCommandManager().register((Object) this, (CommandCallable) inspectorCommandSpec,
				new String[] { "inspector", "ins", "insp" });
		Sponge.getEventManager().registerListeners((Object) this, (Object) new PlayerPlaceBlockListener());
		Sponge.getEventManager().registerListeners((Object) this, (Object) new PlayerInteractBlockListener());
		Sponge.getEventManager().registerListeners((Object) this, (Object) new PlayerBreakBlockListener());
		Sponge.getEventManager().registerListeners((Object) this, (Object) new ExplosionListener());
		Sponge.getEventManager().registerListeners((Object) this, (Object) new PlayerJoinListener());
		
		/*记录命令
		Sponge.getEventManager().registerListeners((Object) this, (Object) new ChangeInventoryListener());
		*/
		
		this.getLogger().info("Inspector loaded!");
		this.getLogger().warn("The Inspector you using now was NOT from the Official Thread!");
		this.getLogger().warn(
				" base on mcbbs(http://mcbbs.tvt.im/forum.php?mod=redirect&goto=findpost&ptid=660997&pid=12750382)");
		this.getLogger().warn("     https://github.com/hsyyid/Inspector");
	}

	@Listener
	public void onServerShuttingDown(final GameStoppingServerEvent event) {
		DatabaseManager.conInsertclose();
		if (Inspector.config.getNode(new Object[] { "auto_Purge" }).getNode(new Object[] { "Enable" }).getBoolean()) {
			final String hours = Inspector.config.getNode(new Object[] { "auto_Purge" })
					.getNode(new Object[] { "timeThreshold" }).getString();
			this.getDatabaseManager().clearExpiredData(commonhandle.parseTime(hours));
		}
	}

	public void loadConfig() throws IOException {
		final Iterator<World> ite = Sponge.getServer().getWorlds().iterator();
		if (this.dConfig.exists()) {
			Inspector.configurationManager = this.confManager;
			Inspector.config = this.confManager.load();
			while (ite.hasNext()) {
				String worldname = ite.next().getName();
				if (Inspector.config.getNode(new Object[] { "worlds",worldname}).isVirtual()) {
					this.getLogger().warn("Discovery of the new world:"+worldname);
					Inspector.config.getNode(new Object[] { "worlds" }).getNode(new Object[] { worldname })
							.setValue((Object) false);
				}
			}
			this.confManager.save((ConfigurationNode) Inspector.config);
		}
		if (!this.dConfig.exists()) {
			this.dConfig.createNewFile();
			Inspector.config = this.confManager.load();
			Inspector.config.getNode(new Object[] { "database", "mysql", "enabled" }).setValue((Object) false);
			Inspector.config.getNode(new Object[] { "database", "mysql", "host" }).setValue((Object) "localhost");
			Inspector.config.getNode(new Object[] { "database", "mysql", "port" }).setValue((Object) "3306");
			Inspector.config.getNode(new Object[] { "database", "mysql", "username" }).setValue((Object) "username");
			Inspector.config.getNode(new Object[] { "database", "mysql", "password" }).setValue((Object) "pass");
			Inspector.config.getNode(new Object[] { "database", "mysql", "database" }).setValue((Object) "minecraft");
			Inspector.config.getNode(new Object[] { "inspector", "select", "tool" })
					.setValue((Object) "minecraft:diamond_hoe");
			while (ite.hasNext()) {
				Inspector.config.getNode(new Object[] { "worlds" }).getNode(new Object[] { ite.next().getName() })
						.setValue((Object) false);
			}
			Inspector.config.getNode(new Object[] { "blockWhiteList" }).setValue((Object) "");
			this.confManager.save((ConfigurationNode) Inspector.config);
		}
		Inspector.configurationManager = this.confManager;
		Inspector.config = this.confManager.load();
		Inspector.config.getNode(new Object[] { "-Announcement" }).setValue(
				(Object) "The inspector you using now was NOT from the Official Thread!\nPost any issues you found to mcbbs(http://mcbbs.tvt.im/forum.php?mod=redirect&goto=findpost&ptid=660997&pid=12750382)");
		if (Inspector.config.getNode(new Object[] { "blockWhiteList" }).isVirtual()) {
			this.blockWhiteList.add("minecraft:bedrock");
			Inspector.config.getNode(new Object[] { "blockWhiteList" }).setValue((Object) this.blockWhiteList)
					.setComment(
							"Attention: the block type here should be specified the unsafeDamage,it means that if you want add Stone here, you should put \"minecraft:stone:0\" instead of\"minecraft:stone\"!");
		} else {
			this.blockWhiteList = (Set<String>) Inspector.config.getNode(new Object[] { "blockWhiteList" })
					.getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toSet());
		}
		if (Inspector.config.getNode(new Object[] { "auto_Purge" }).isVirtual()) {
			Inspector.config.getNode(new Object[] { "auto_Purge" }).getNode(new Object[] { "timeThreshold" })
					.setValue((Object) "t:72h").setComment(
							"Time,in hours,inspector will automatically clean up the data before this time threshold(default t:72h)");
			Inspector.config.getNode(new Object[] { "auto_Purge" }).getNode(new Object[] { "Enable" })
					.setValue((Object) true).setComment(
							"if enabled,inspector will automatically clean up the expired data when server is closing(default true)");
			this.confManager.save((ConfigurationNode) Inspector.config);
		}
	}

	public static ConfigurationLoader<CommentedConfigurationNode> getConfigManager() {
		return Inspector.configurationManager;
	}

	public Set<String> getBlockWhiteList() {
		return this.blockWhiteList;
	}

	static {
		Inspector.inspectorEnabledPlayers = Sets.newHashSet();
		Inspector.regions = Sets.newHashSet();
	}
}
