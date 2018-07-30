package io.github.uuabc.inspector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

public class Message {
	private static File file;
	private static CommentedConfigurationNode mess;

	public Message(Path configDir, String filename) {
		file = Paths.get(configDir.toString(), filename).toFile();
		if (!file.exists()) {
			try {
				file.createNewFile();
				HoconConfigurationLoader messM = HoconConfigurationLoader.builder().setFile(file).build();
				mess = allmessage();
				messM.save(mess);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				mess = HoconConfigurationLoader.builder().setFile(file).build().load();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private CommentedConfigurationNode allmessage() {
		CommentedConfigurationNode almess;
		almess.getNode(new Object[] { "inspector.use" }).setValue((Object) "&9[Inspector]: &7Version: {0}");
		almess.getNode(new Object[] { "inspector.toggle.help" })
				.setValue((Object) "&9[Inspector]: &7toggle: &6Toggle Inspector Command");
		almess.getNode(new Object[] { "inspector.rollback.help" })
				.setValue((Object) "&9[Inspector]: &7rollback:&6Rollback Command,time format:  t:1d1h1m");
		almess.getNode(new Object[] { "inspector.purge.help" })
				.setValue((Object) "&9[Inspector]: &7purge: &6clean up the expired data");
		almess.getNode(new Object[] { "inspector.reload.help" })
				.setValue((Object) "&9[Inspector]: &7reload: &6reload Inspector");

		almess.getNode(new Object[] { "inspector.purge.success" })
				.setValue((Object) "&2Inspector data was clean up");
		almess.getNode(new Object[] { "inspector.purge.console" }).setValue(
				(Object) "Run the purge command from Console, attempt to clean the data before {0} ago");
		almess.getNode(new Object[] { "inspector.purge.player" })
				.setValue((Object) "{0}attempt to run the purge command to clean the data before{1} ago");

		almess.getNode(new Object[] { "inspector.reload.success" })
				.setValue((Object) "Inspecter config reloaded");

		almess.getNode(new Object[] { "inspector.rollback.not.player" }).setValue(
				(Object) "&9[Inspector]: &4Error! You may only use this command as an in-game player.");
		almess.getNode(new Object[] { "inspector.rollback.error.time" })
				.setValue((Object) "&9[Inspector]: &4Error! Invalid time {0} !");
		almess.getNode(new Object[] { "inspector.rollback.error.value" })
		.setValue((Object) "&9[Inspector]: &4Error! You give a Error value: {0}");
		almess.getNode(new Object[] { "inspector.rollback.error.user" })
		.setValue((Object) "&9[Inspector]: &4Error! Without this user: {0}");
		almess.getNode(new Object[] { "inspector.rollback.error.radius" })
		.setValue((Object) "&9[Inspector]: &4Error! You give a Error radius: {0} ,The format is: r:3");
		almess.getNode(new Object[] { "inspector.rollback.no.region" })
		.setValue((Object) "&9[Inspector]: &4Error! You have not selected a region yet.And your not set a radius");
		almess.getNode(new Object[] { "inspector.rollback.missing.point" })
		.setValue((Object) "&9[Inspector]: &4Error! You are missing one or more points for your region.And your not set a radiu");
		almess.getNode(new Object[] { "inspector.rollback.different.worlds" })
		.setValue((Object) "&9[Inspector]: &4Error! You may do a rollback for a region in two different worlds.And your not set a radius");
		
		almess.getNode(new Object[] { "inspector.rollback.text.box.wait" })
		.setValue((Object)"[Inspector]: wait...");
		almess.getNode(new Object[] { "inspector.rollback.text.box0" })
				.setValue((Object) "&9[Inspector] &6Rollback completed");
		almess.getNode(new Object[] { "inspector.rollback.text.box1" })
		.setValue((Object) "&7Recovery Time: &6{0} \n");
		almess.getNode(new Object[] { "inspector.rollback.text.box2" })
		.setValue((Object) "&7targetPlayer: &6{0} \n");
		almess.getNode(new Object[] { "inspector.rollback.text.box21" })
		.setValue((Object) "&7UUID:  &6{0}");
		almess.getNode(new Object[] { "inspector.rollback.text.box3" })
		.setValue((Object) "&7PointA: &6x:{0} y:{1} z:{2}\n");
		almess.getNode(new Object[] { "inspector.rollback.text.box4" })
		.setValue((Object) "&7PointB: &6x:{0} y:{1} z:{2}\n");
		almess.getNode(new Object[] { "inspector.rollback.text.box5" })
		.setValue((Object)"Total Blocks: {0}");
		almess.getNode(new Object[] { "inspector.rollback.text.boxpadding" })
		.setValue((Object)"-");
		
		almess.getNode(new Object[] { "inspector.toggle.on" })
		.setValue((Object)"&9[Inspector]: &7Toggled inspector &6on");
		almess.getNode(new Object[] { "inspector.toggle.off" })
		.setValue((Object)"&9[Inspector]: &7Toggled inspector &6off");
		almess.getNode(new Object[] { "inspector.toggle.not.player" })
		.setValue((Object)"&9[Inspector]: &4Error! You may only use this command as an in-game player.");
		
		almess.getNode(new Object[] { "inspector.notenable.world" })
		.setValue((Object)"Inspectator was not enable in this world!");
		
		almess.getNode(new Object[] { "inspector.no.blockinfo" })
		.setValue((Object)"&9[Inspector]: &7No information found for this block.");
		
		almess.getNode(new Object[] { "inspector.blockinfo.box0" })
		.setValue((Object)"&9[Inspector] &6Block Changes");
		almess.getNode(new Object[] { "inspector.blockinfo.box1" })
		.setValue((Object)"&7Time Edited: &6{0}\n");
		almess.getNode(new Object[] { "inspector.blockinfo.box2" })
		.setValue((Object)"&7Player Edited: &6{0} &7{1} &6{2} \n");
		almess.getNode(new Object[] { "inspector.blockinfo.box21" })
		.setValue((Object)"&7UUID: &6{0}");
		almess.getNode(new Object[] { "inspector.blockinfo.box3" })
		.setValue((Object)"&7Old Block ID: ");
		almess.getNode(new Object[] { "inspector.blockinfo.box31" })
		.setValue((Object)"&7ID: &6{0}");
		almess.getNode(new Object[] { "inspector.blockinfo.box4" })
		.setValue((Object)"&7New Block ID: ");
		almess.getNode(new Object[] { "inspector.blockinfo.box41" })
		.setValue((Object)"&7ID: &6{0}");
		almess.getNode(new Object[] { "inspector.blockinfo.boxpadding" })
		.setValue((Object)"-");
		
		
		almess.getNode(new Object[] { "inspector.no.Containerinfo" })
		.setValue((Object)"&9[Inspector]: &7No Container information found for this block.");
		almess.getNode(new Object[] { "inspector.iteminfo.box0" })
		.setValue((Object)"&9[Inspector] &6item Changes");
		almess.getNode(new Object[] { "inspector.iteminfo.box1" })
		.setValue((Object)"&7Time Edited: &6{0}\n");
		almess.getNode(new Object[] { "inspector.iteminfo.box2" })
		.setValue((Object)"&7Player Edited: &6{0}\n");
		almess.getNode(new Object[] { "inspector.iteminfo.box21" })
		.setValue((Object)"&7UUID: &6{0}");
		almess.getNode(new Object[] { "inspector.iteminfo.box3" })
		.setValue((Object)"&7Old Item ID: ");
		almess.getNode(new Object[] { "inspector.iteminfo.box30" })
		.setValue((Object)"&7count: {0}\n");
		almess.getNode(new Object[] { "inspector.iteminfo.box31" })
		.setValue((Object)"&7ID: &6{0}");
		almess.getNode(new Object[] { "inspector.iteminfo.box4" })
		.setValue((Object)"&7New Item ID: ");
		almess.getNode(new Object[] { "inspector.iteminfo.box40" })
		.setValue((Object)"&7count: {0}\n");
		almess.getNode(new Object[] { "inspector.iteminfo.box41" })
		.setValue((Object)"&7ID: &6{0}");
		almess.getNode(new Object[] { "inspector.iteminfo.boxpadding" })
		.setValue((Object)"-");
		
		almess.getNode(new Object[] { "inspector.set.positionA" })
		.setValue((Object)"&9[Inspector]: &7Set position A to {0} {1} {2}");
		almess.getNode(new Object[] { "inspector.set.positionB" })
		.setValue((Object)"&9[Inspector]: &7Set position B to {0} {1} {2}");
		
		return almess;
	}
	
	public String getstringmessage(String key) {
		return mess.getNode(new Object[] { key }).getValue().toString();
	}

	public String getstringmessage(String key, String... v) {
		String message = mess.getNode(new Object[] { key }).getValue().toString();
		for (int i = 0; i < v.length; i++) {
			message = message.replaceAll("\\{" + i + "\\}",  v[i]==null? "null":v[i]);
		}
		return message;
	}

	public Text getmessage(String key) {
		return TextSerializers.FORMATTING_CODE.deserialize(mess.getNode(new Object[] { key }).getValue().toString());
	}

	public Text getmessage(String key, String... v) {
		String message = mess.getNode(new Object[] { key }).getValue().toString();
		for (int i = 0; i < v.length; i++) {
			message = message.replaceAll("\\{" + i + "\\}", v[i]==null? "null":v[i]);
		}
		return TextSerializers.FORMATTING_CODE.deserialize(message);
	}
}
