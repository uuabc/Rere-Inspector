package io.github.uuabc.inspector.utilities;

import java.sql.Timestamp;
import org.spongepowered.api.item.inventory.ItemStack;

public class ItemStackInformation {


	    private ItemStack oldItemStack;
	    private ItemStack newItemStack;
	    private Timestamp timeEdited;
	    private String playerUUID;
	    private String playerName;
	    

		public ItemStackInformation(final ItemStack oldItemStack, final ItemStack newItemStack, final Timestamp timeEdited, final String playerUUID, final String playerName) {
	        this.oldItemStack = oldItemStack;
	        this.newItemStack = newItemStack;
	        this.timeEdited = timeEdited;
	        this.playerUUID = playerUUID;
	        this.playerName = playerName;
	    }

		public ItemStack getOldItemStack() {
			return oldItemStack;
		}

		public ItemStack getNewItemStack() {
			return newItemStack;
		}

		public Timestamp getTimeEdited() {
			return timeEdited;
		}

		public String getPlayerUUID() {
			return playerUUID;
		}

		public String getPlayerName() {
			return playerName;
		}

	    
}
