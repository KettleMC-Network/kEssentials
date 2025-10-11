package net.kettlemc.kessentials.command;

import com.cryptomorin.xseries.XMaterial;
import net.kettlemc.kessentials.Essentials;
import net.kettlemc.kessentials.config.Configuration;
import net.kettlemc.kessentials.config.Messages;
import net.kettlemc.kessentials.teleport.Teleportation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RandomTeleportCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!Essentials.instance().checkPermission(sender, command, false)) {
            Essentials.instance().messages().sendMessage(sender, Messages.NO_PERMISSION);
            return true;
        }

        World world;
        Player target;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                Essentials.instance().messages().sendMessage(sender, Messages.RTP_USAGE);
                return true;
            }
            target = (Player) sender;
            world = target.getWorld();

        } else if (args.length == 1) {
            World tryWorld = Bukkit.getWorld(args[0]);
            Player tryPlayer = Bukkit.getPlayer(args[0]);

            if (tryWorld != null) {
                if (!(sender instanceof Player)) {
                    Essentials.instance().messages().sendMessage(sender, Messages.RTP_USAGE);
                    return true;
                }
                target = (Player) sender;
                world = tryWorld;

            } else if (tryPlayer != null) {
                target = tryPlayer;
                world = target.getWorld();

                if (!sender.equals(target) &&
                        !Essentials.instance().checkPermission(sender, "kessentials.rtp.others", false)) {
                    Essentials.instance().messages().sendMessage(sender, Messages.NO_PERMISSION);
                    return true;
                }

            } else {
                Essentials.instance().messages().sendMessage(sender, Messages.PLAYER_NOT_FOUND);
                return true;
            }

        } else if (args.length == 2) {
            world = Bukkit.getWorld(args[0]);
            if (world == null) {
                Essentials.instance().messages().sendMessage(sender, Messages.WORLD_NOT_FOUND);
                return true;
            }

            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                Essentials.instance().messages().sendMessage(sender, Messages.PLAYER_NOT_FOUND);
                return true;
            }

            if (!sender.equals(target) &&
                    !Essentials.instance().checkPermission(sender, "kessentials.rtp.others", false)) {
                Essentials.instance().messages().sendMessage(sender, Messages.NO_PERMISSION);
                return true;
            }

            if (!world.equals(target.getWorld()) &&
                    !Essentials.instance().checkPermission(sender, "kessentials.rtp.world", false)) {
                Essentials.instance().messages().sendMessage(sender, Messages.NO_PERMISSION);
                return true;
            }

        } else {
            Essentials.instance().messages().sendMessage(sender, Messages.RTP_USAGE);
            return true;
        }

        randomTeleport(sender, target, world);
        return true;
    }

    private void randomTeleport(CommandSender sender, Player player, World world) {
        Location center = player.getLocation();
        int radius = Configuration.RANDOM_TELEPORT_RADIUS.getValue();
        randomTeleport(sender, player, world, center, radius);
    }

    public static void randomTeleport(CommandSender sender, Player player, World world, Location center, int radius) {
        Location randomLocation = getRandomLocation(world, center, radius, 10);

        if (randomLocation == null) {
            if (sender.equals(player)) {
                Essentials.instance().messages().sendMessage(sender, Messages.RTP_FAILED);
            } else {
                Essentials.instance().messages().sendMessage(sender,
                        Messages.RTP_FAILED_OTHER.replace("%player%", player.getName()).replace("%world%", world.getName()));
            }
            return;
        }

        Block highestBlock = getHighestValidBlock(world, randomLocation.getBlockX(), randomLocation.getBlockZ());
        if (highestBlock != null) {
            randomLocation.setY(highestBlock.getY() + 1);
        }

        Teleportation.schedule(Teleportation.prepare(player, randomLocation, Configuration.TELEPORT_COUNTDOWN_SECONDS.getValue()));

        if (sender.equals(player)) {
            Essentials.instance().messages().sendMessage(sender,
                    Messages.RTP_SUCCESS_SELF.replace("%world%", world.getName()));
        } else {
            Essentials.instance().messages().sendMessage(sender,
                    Messages.RTP_SUCCESS_OTHER.replace("%player%", player.getName()).replace("%world%", world.getName()));
        }
    }

    public static Location getRandomLocation(World world, Location center, int radius, int maxAttempts) {
        for (int i = 0; i < maxAttempts; i++) {
            double x = center.getX() + (Math.random() * radius * 2) - radius;
            double z = center.getZ() + (Math.random() * radius * 2) - radius;
            int y = world.getHighestBlockYAt((int) x, (int) z);
            Location randomLocation = new Location(world, x, y, z);

            Block highestBlock = getHighestValidBlock(world, (int) x, (int) z);
            if (highestBlock != null) {
                return randomLocation;
            }
        }
        return null;
    }

    public static Block getHighestValidBlock(World world, int x, int z) {
        for (int y = world.getMaxHeight(); y >= 0; y--) {
            if (world.getBlockAt(x, y, z).getType() == XMaterial.WATER.get()) {
                return null;
            }
            if (world.getBlockAt(x, y, z).getType().isSolid()) {
                return world.getBlockAt(x, y, z);
            }
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> worlds = Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            worlds.addAll(players);
            return worlds;
        } else if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
