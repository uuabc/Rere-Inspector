package io.github.uuabc.inspector.utilities;

import java.sql.Timestamp;
import org.spongepowered.api.block.BlockType;

public class BlockViewInfo {
private BlockType oldType;
private BlockType newType;
private String oldId;
private String newId;
private String playerName;
private String playerUUID ;
private Timestamp blocktime;


public BlockViewInfo(BlockType oldType, BlockType newType, String oldId, String newId,Timestamp blocktime,String playerName, String playerUUID) {
	this.oldType = oldType;
	this.newType = newType;
	this.oldId = oldId;
	this.newId = newId;
	this.playerName = playerName;
	this.playerUUID = playerUUID;
	this.blocktime = blocktime;
}



public Timestamp getBlocktime() {
	return blocktime;
}



public BlockType getOldType() {
	return oldType;
}



public BlockType getNewType() {
	return newType;
}



public String getOldId() {
	return oldId;
}



public String getNewId() {
	return newId;
}



public String getPlayerName() {
	return playerName;
}



public String getPlayerUUID() {
	return playerUUID;
}



}
