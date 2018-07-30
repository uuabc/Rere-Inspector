package io.github.uuabc.inspector.utilities;

import io.github.uuabc.inspector.Inspector;

import org.spongepowered.api.world.*;
import java.util.*;

public class Utils
{

    
    public static Object getConfigValue(final String configValue) {
        return Inspector.config.getNode((Object[])configValue.split("\\.")).getValue();
    }
    
    public static void addPointOrCreateRegionOf(final UUID playerUUID, final Location<World> point, final boolean secondary) {
        if (secondary) {
            for (final Region region : Inspector.regions) {
                if (region.getOwner().equals(playerUUID)) {
                    region.setPointB(point);
                    return;
                }
            }
            Inspector.regions.add(new Region(playerUUID, null, point));
        }
        else {
            for (final Region region : Inspector.regions) {
                if (region.getOwner().equals(playerUUID)) {
                    region.setPointA(point);
                    return;
                }
            }
            Inspector.regions.add(new Region(playerUUID, point, null));
        }
    }
    
    public static Optional<Region> getRegion(final UUID playerUUID) {
        for (final Region region : Inspector.regions) {
            if (region.getOwner().equals(playerUUID)) {
                return Optional.of(region);
            }
        }
        return Optional.empty();
    }
}



