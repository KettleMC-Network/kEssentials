package net.kettlemc.kessentials.listener;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kettlemc.kcommon.worldguard.WorldGuardUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collection;

public class RegionListener implements Listener {

    @EventHandler
    public void onRegionEnter(PlayerMoveEvent event) {
        if (event.getFrom().getX() == event.getTo().getX() && event.getFrom().getY() == event.getTo().getY() && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }

        Collection<ProtectedRegion> currentRegions = WorldGuardUtil.getRegionsAt(event.getFrom());
        Collection<ProtectedRegion> newRegions = WorldGuardUtil.getRegionsAt(event.getTo());

        if (!currentRegions.equals(newRegions)) {
            for (ProtectedRegion region : newRegions) {
                if (!currentRegions.contains(region)) {
                }
            }
        }
    }

}
