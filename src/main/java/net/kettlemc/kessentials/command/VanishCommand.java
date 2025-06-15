package net.kettlemc.kessentials.command;

import io.github.almightysatan.slams.Placeholder;
import net.kettlemc.kessentials.Essentials;
import net.kettlemc.kessentials.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collections;
import java.util.List;

public class VanishCommand implements CommandExecutor, TabCompleter {

    /**
     * Hides all vanished players from the target
     *
     * @param target The player to hide vanished players from
     */
    public static void hideVanished(Player target) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isVanished(player)) {
                target.hidePlayer(player);
            }
        }
    }

    /**
     * Hides the target from all players (enables vanish)
     *
     * @param target The player to hide
     */
    public static void hideFromAllPlayers(Player target) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hidePlayer(target);
        }
    }

    /**
     * Shows the target to all players (disables vanish)
     *
     * @param target The player to show
     */
    public static void showToAllPlayers(Player target) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showPlayer(target);
        }
    }

    /**
     * Checks if the target is vanished
     *
     * @param target The player to check
     * @return True if the player is vanished, false otherwise
     */
    public static boolean isVanished(Player target) {
        return target.hasMetadata("vanished") && target.getMetadata("vanished").get(0).asBoolean();
    }

    /**
     * Toggles the vanish state of the target
     *
     * @param sender The sender of the command
     * @param target The player to toggle the vanish state of
     */
    private void toggleVanish(CommandSender sender, Player target) {

        if (isVanished(target)) {
            target.setMetadata("vanished", new FixedMetadataValue(Essentials.instance(), false));
            Essentials.instance().messages().sendMessage(target, Messages.VANISH_UNVANISHED);
            showToAllPlayers(target);
        } else {
            target.setMetadata("vanished", new FixedMetadataValue(Essentials.instance(), true));
            Essentials.instance().messages().sendMessage(target, Messages.VANISH_VANISHED);
            hideFromAllPlayers(target);
        }

        if (sender != target) {
            Essentials.instance().messages().sendMessage(sender, isVanished(target) ? Messages.VANISH_VANISHED_OTHER : Messages.VANISH_UNVANISHED_OTHER, Placeholder.of("target", ((ctx, args) -> target.getName())));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!Essentials.instance().checkPermission(sender, command, false)) {
            Essentials.instance().messages().sendMessage(sender, Messages.NO_PERMISSION);
            return true;
        }

        if (args.length == 0) {

            if (sender instanceof Player) {
                toggleVanish(sender, (Player) sender);
            } else {
                Essentials.instance().messages().sendMessage(sender, Messages.VANISH_USAGE);
            }

        } else if (args.length == 1) {

            if (!Essentials.instance().checkPermission(sender, command, true)) {
                Essentials.instance().messages().sendMessage(sender, Messages.NO_PERMISSION);
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                Essentials.instance().messages().sendMessage(sender, Messages.PLAYER_NOT_FOUND);
                return true;
            }
            toggleVanish(sender, target);
        } else {
            Essentials.instance().messages().sendMessage(sender, Messages.VANISH_USAGE);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 1) {
            return null;
        }
        return Collections.emptyList();
    }

}
