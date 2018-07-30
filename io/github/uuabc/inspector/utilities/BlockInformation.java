package io.github.uuabc.inspector.utilities;

import org.spongepowered.api.world.*;
import org.spongepowered.api.block.*;

public class BlockInformation
{
    private Location<World> location;
    private BlockState oldBlockSnapshot;
    
    public BlockInformation(final Location<World> location, final BlockState oldBlockSnapshot) {
        this.location = location;
        this.oldBlockSnapshot = oldBlockSnapshot;
    }
    
    public Location<World> getLocation() {
        return this.location;
    }
    
    public BlockState getOldBlockSnapshot() {
        return this.oldBlockSnapshot;
    }
}
