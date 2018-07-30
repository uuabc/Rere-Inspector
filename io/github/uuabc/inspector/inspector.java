package io.github.uuabc.inspector;

import ninja.leaping.configurate.commented.*;
import ninja.leaping.configurate.loader.*;
import io.github.uuabc.inspector.cmdexecutors.InspectorExecutor;
import io.github.uuabc.inspector.cmdexecutors.ToggleInspectorExecutor;
import io.github.uuabc.inspector.cmdexecutors.commonhandle;
import io.github.uuabc.inspector.cmdexecutors.purgeInspectorExecutor;
import io.github.uuabc.inspector.cmdexecutors.rRollback;
import io.github.uuabc.inspector.cmdexecutors.reloadInspectorExecutor;
import io.github.uuabc.inspector.listeners.ChangeInventoryListener;
import io.github.uuabc.inspector.listeners.ExplosionListener;
import io.github.uuabc.inspector.listeners.PlayerBreakBlockListener;
import io.github.uuabc.inspector.listeners.PlayerInteractBlockListener;
import io.github.uuabc.inspector.listeners.PlayerJoinListener;
import io.github.uuabc.inspector.listeners.PlayerPlaceBlockListener;
import io.github.uuabc.inspector.utilities.DatabaseManager;
import io.github.uuabc.inspector.utilities.Region;

import org.spongepowered.api.plugin.*;
import com.google.inject.*;

import org.spongepowered.api.config.*;
import com.google.common.collect.*;
import java.io.*;
import java.nio.file.Path;

import org.spongepowered.api.text.*;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.command.args.*;
import org.slf4j.Logger;
import org.spongepowered.api.*;
import org.spongepowered.api.command.*;
import org.spongepowered.api.event.*;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.world.*;
import ninja.leaping.configurate.*;
import java.util.stream.*;
import java.util.*;

@Plugin(id = "inspector", name = "Inspector", version = "0.7.2-rere", description = "This plugin enables servers to monitor griefing and provides rollback.", dependencies = {})
public class Inspector {
	private DatabaseManager databaseManager;
	private static Inspector instance;
	public static CommentedConfigurationNode config;

	public static ConfigurationLoader<CommentedConfigurationNode> configurationManager;
	public static Set<UUID> inspectorEnabledPlayers;
	public static Set<Region> regions;
	private Set<String> blockWhiteList;
	private static Message message;
	
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path ConfigDir;
	@Inject
	private PluginContainer pluginContainer;
	@Inject
	private Logger logger;
	@Inject
	@DefaultConfig(sharedRoot = false)
	private File dConfig;
	@Inject
	@DefaultConfig(sharedRoot = false)
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
		try {
			this.loadConfig();
		} catch (IOException exception) {
			this.getLogger().error("The default configuration could not be loaded or created!");
		}
		commandregister();
		loadmessage();
		getDatabaseManager().gamestartdb();
	}

	public void loadmessage() {
		Inspector.message=new Message(ConfigDir,"message.conf");
	}
	
	public static Message getMessage() {
		return message;
	}

	@Listener
	public void onServerShuttingDown(final GameStoppingServerEvent event) {
		DatabaseManager.conInsertclose();
		if (Inspector.config.getNode(new Object[] { "auto_Purge" ,"Enable"}).getBoolean()) {
			final String hours = Inspector.config.
					getNode(new Object[] { "auto_Purge" ,"timeThreshold"}).getString();
			this.getDatabaseManager().clearExpiredData(commonhandle.parseTime(hours));

		}
	}

	public void commandregister() {
		this.getLogger().info("Inspector loading...");
		Inspector.instance = this;
		this.databaseManager = new DatabaseManager();
		final HashMap<List<String>, CommandSpec> inspectorSubcommands = new HashMap<List<String>, CommandSpec>();
		inspectorSubcommands.put(Arrays.asList("reload"),
				CommandSpec.builder().description((Text) Text.of("reload Inspector")).permission("inspector.reload")
						.executor((CommandExecutor) new reloadInspectorExecutor()).build());
		inspectorSubcommands.put(Arrays.asList("purge"),
				CommandSpec.builder().description((Text) Text.of("clean up the expired data"))
						.permission("inspector.purge").arguments(GenericArguments.string((Text) Text.of("time")))
						.executor((CommandExecutor) new purgeInspectorExecutor()).build());
		inspectorSubcommands.put(Arrays.asList("toggle"),
				CommandSpec.builder().description((Text) Text.of("Toggle Inspector Command"))
						.permission("inspector.toggle").executor((CommandExecutor) new ToggleInspectorExecutor())
						.build());
		inspectorSubcommands.put(Arrays.asList("rollback"),
				CommandSpec.builder().description((Text) Text.of("Rollback Command")).permission("inspector.rollback")
						.arguments(GenericArguments.seq(
								GenericArguments.onlyOne(GenericArguments.string((Text) Text.of("time"))),
								GenericArguments.optional(
										GenericArguments.onlyOne(GenericArguments.string((Text) Text.of("option1")))),
								GenericArguments.optional(
										GenericArguments.onlyOne(GenericArguments.string((Text) Text.of("option2"))))))
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

		Sponge.getEventManager().registerListeners((Object) this, (Object) new ChangeInventoryListener());

		this.getLogger().info("Inspector loaded!");
		this.getLogger().warn("The Inspector you using now was NOT from the Official Thread!");
		this.getLogger().warn(
				" base on mcbbs(http://mcbbs.tvt.im/forum.php?mod=redirect&goto=findpost&ptid=660997&pid=12750382)");
		this.getLogger().warn("and  https://github.com/hsyyid/Inspector");
	}

	public void loadConfig() throws IOException {
		
		if (this.dConfig.exists()) {
			Iterator<World> ite = Sponge.getServer().getWorlds().iterator();
			Inspector.configurationManager = this.confManager;
			Inspector.config = this.confManager.load();
			while (ite.hasNext()) {
				String worldname = ite.next().getName();
				if (Inspector.config.getNode(new Object[] { "worlds", worldname }).isVirtual()) {
					this.getLogger().warn("Discovery of the new world:" + worldname);
					Inspector.config.getNode(new Object[] { "worlds" }).getNode(new Object[] { worldname })
							.setValue((Object) false);
				}
			}
			this.confManager.save((ConfigurationNode) Inspector.config);
		}
		if (!this.dConfig.exists()) {
			this.dConfig.createNewFile();
			Inspector.config = this.confManager.load();
			Inspector.config.getNode(new Object[] {"lang"}).setValue((Object) "en_US");
			Inspector.config.getNode(new Object[] { "database", "database" }).setValue((Object) "h2").setComment("mysql,h2,sqlite");
			Inspector.config.getNode(new Object[] { "database", "mysql", "host" }).setValue((Object) "localhost");
			Inspector.config.getNode(new Object[] { "database", "mysql", "port" }).setValue((Object) "3306");
			Inspector.config.getNode(new Object[] { "database", "mysql", "username" }).setValue((Object) "username");
			Inspector.config.getNode(new Object[] { "database", "mysql", "password" }).setValue((Object) "pass");
			Inspector.config.getNode(new Object[] { "database", "mysql", "database" }).setValue((Object) "minecraft");
			Inspector.config.getNode(new Object[] { "inspector", "select", "tool" })
					.setValue((Object) "minecraft:diamond_hoe");
			Iterator<World> ite = Sponge.getServer().getWorlds().iterator();
			while (ite.hasNext()) {
				String worldname = ite.next().getName();
				Inspector.config.getNode(new Object[] { "worlds" ,worldname}).setValue((Object) false);
						
			}

			Inspector.config.getNode(new Object[] { "blockWhiteList" }).setValue((Object) "");
			this.confManager.save((ConfigurationNode) Inspector.config);
		}
		Inspector.configurationManager = this.confManager;
		Inspector.config = this.confManager.load();
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
					.setValue((Object) "t:4w").setComment(
							"Time,in hours,inspector will automatically clean up the data before this time threshold(default t:4w)");
			Inspector.config.getNode(new Object[] { "auto_Purge" }).getNode(new Object[] { "Enable" })
					.setValue((Object) true).setComment(
							"if enabled,inspector will automatically clean up the expired data when server is closing(default true)");
			this.confManager.save((ConfigurationNode) Inspector.config);
		}
	}

	public static CommentedConfigurationNode getConfig() {
	
		return Inspector.config;
	}

	public Set<String> getBlockWhiteList() {
		return this.blockWhiteList;
	}

	static {
		Inspector.inspectorEnabledPlayers = Sets.newHashSet();
		Inspector.regions = Sets.newHashSet();
	}
}
