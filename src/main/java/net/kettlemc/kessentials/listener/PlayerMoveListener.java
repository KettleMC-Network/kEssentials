package net.kettlemc.kessentials.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import static net.kettlemc.kessentials.command.FreezeCommand.FROZEN_PLAYERS;

public class PlayerMoveListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!FROZEN_PLAYERS.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        if (event.getFrom().getY() != event.getTo().getY() || event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
            event.setCancelled(true);
        }
    }
}
