package net.kettlemc.kessentials.listener;

import io.github.almightysatan.slams.Placeholder;
import net.kettlemc.kessentials.Essentials;
import net.kettlemc.kessentials.command.FreezeCommand;
import net.kettlemc.kessentials.command.VanishCommand;
import net.kettlemc.kessentials.config.Configuration;
import net.kettlemc.kessentials.config.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static net.kettlemc.kessentials.command.FreezeCommand.*;
import static net.kettlemc.kessentials.command.VanishCommand.*;

public class JoinQuitListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Essentials.instance().homeHandler().loadHomes(event.getPlayer().getUniqueId());

        Component component = Messages.JOIN_MESSAGE.value(Placeholder.of("player", (ctx, args) -> event.getPlayer().getName()));
        event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', LegacyComponentSerializer.legacyAmpersand().serialize(component)));

        // Hide vanished players from the player and disable the join message if the player is vanished
        hideVanished(event.getPlayer());
        if (isVanished(event.getPlayer())) {
            event.setJoinMessage("");
            hideFromAllPlayers(event.getPlayer());
        }

        Configuration.ON_JOIN_COMMANDS.getValue().forEach(command ->
                Essentials.instance().getServer().dispatchCommand(
                        Essentials.instance().getServer().getConsoleSender(),
                        command.replace("%player%", event.getPlayer().getName())
                )
        );

        // Set the player's speed to the frozen speed if they are frozen
        if (FreezeCommand.isFrozen(event.getPlayer())) {
            event.getPlayer().setWalkSpeed(FROZEN_SPEED);
            event.getPlayer().setFlySpeed(FROZEN_SPEED);
        } else {
            event.getPlayer().setWalkSpeed(DEFAULT_WALK_SPEED);
            event.getPlayer().setFlySpeed(DEFAULT_FLY_SPEED);
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Component component = Messages.QUIT_MESSAGE.value(Placeholder.of("player", (ctx, args) -> event.getPlayer().getName()));
        event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', LegacyComponentSerializer.legacyAmpersand().serialize(component)));

        // Disable the quit message if the player is vanished
        if (VanishCommand.isVanished(event.getPlayer())) {
            event.setQuitMessage("");
        }

        Essentials.instance().homeHandler().saveHomes(event.getPlayer().getUniqueId(), true);
    }

}
