package io.github.uuabc.inspector.utilities;

import org.spongepowered.api.world.*;
import org.spongepowered.api.block.*;

import java.sql.Timestamp;

public class BlockInformation
{
    private Location<World> location;
    private BlockState oldBlockSnapshot;
    private BlockState newBlockSnapshot;
    private Timestamp timeEdited;
    private String playerUUID;
    private String playerName;
    
    public BlockInformation(final Location<World> location, final BlockState oldBlockSnapshot, final BlockState newBlockSnapshot, final Timestamp timeEdited, final String playerUniqueId, final String playerName) {
        this.location = location;
        this.oldBlockSnapshot = oldBlockSnapshot;
        this.newBlockSnapshot = newBlockSnapshot;
        this.timeEdited = timeEdited;
        this.playerUUID = playerUniqueId;
        this.playerName = playerName;
    }
    
    public Location<World> getLocation() {
        return this.location;
    }
    
    public BlockState getOldBlockSnapshot() {
        return this.oldBlockSnapshot;
    }
    
    public BlockState getNewBlockSnapshot() {
        return this.newBlockSnapshot;
    }
    
    public Timestamp getTimeEdited() {
        return this.timeEdited;
    }
    
    public String getPlayerUUID() {
        return this.playerUUID;
    }
    
    public String getPlayerName() {
        return this.playerName;
    }
}
