package io.github.uuabc.inspector.utilities;

import org.spongepowered.api.world.*;
import java.util.*;

public class Region
{
    private UUID owner;
    private Location<World> pointA;
    private Location<World> pointB;
    
    public Region(final UUID owner, final Location<World> pointA, final Location<World> pointB) {
        this.owner = owner;
        this.pointA = pointA;
        this.pointB = pointB;
    }
    
    public UUID getOwner() {
        return this.owner;
    }
    
    public Location<World> getPointA() {
        return this.pointA;
    }
    
    public Location<World> getPointB() {
        return this.pointB;
    }
    
    public void setOwner(final UUID owner) {
        this.owner = owner;
    }
    
    public void setPointA(final Location<World> pointA) {
        this.pointA = pointA;
    }
    
    public void setPointB(final Location<World> pointB) {
        this.pointB = pointB;
    }
    
}
