package net.kettlemc.kessentials.teleport;

import io.github.almightysatan.slams.Placeholder;
import net.kettlemc.kessentials.Essentials;
import net.kettlemc.kessentials.config.Messages;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Teleportation {


    private static final Map<Player, Teleportation> TELEPORTATION = new ConcurrentHashMap<>();

    private final Player player;
    private final Location target;
    private final int cooldown;
    private final Runnable onFinish;

    private final Location start;

    private Teleportation(Player player, Location target, int cooldown, Runnable onFinish) {
        this.player = player;
        this.start = player.getLocation();
        this.target = target;
        this.cooldown = cooldown;
        this.onFinish = onFinish;
    }


    public static Teleportation prepare(Player player, Location location, int cooldown) {
        return new Teleportation(player, location, cooldown, () -> {
        });
    }

    public static Teleportation prepare(Player player, Location location, int cooldown, Runnable onFinish) {
        return new Teleportation(player, location, cooldown, onFinish);
    }

    public static boolean schedule(Teleportation teleportation) {
        if (TELEPORTATION.containsKey(teleportation.getPlayer())) {
            return false;
        }
        TELEPORTATION.put(teleportation.getPlayer(), teleportation);
        startScheduler(teleportation);
        return true;
    }

    private static void startScheduler(Teleportation teleportation) {

        Essentials.instance().messages().sendMessage(teleportation.getPlayer(), Messages.TELEPORTATION_COOLDOWN, Placeholder.of("cooldown", (ctx, args) -> String.valueOf(teleportation.cooldown)));

        new BukkitRunnable() {

            int amount = teleportation.cooldown * 2 - 1; // Amount is in seconds and check is done every half second after an initial delay of half a second

            @Override
            public void run() {

                // Check if player is online
                if (teleportation.getPlayer().isOnline()) {

                    // If the timer is done, teleport the player
                    if (amount == 0) {

                        // Teleport the player
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                teleportation.getPlayer().teleport(teleportation.getTarget());
                                teleportation.onFinish.run();
                            }
                        }.runTask(Essentials.instance());


                        Essentials.instance().messages().sendMessage(teleportation.getPlayer(), Messages.TELEPORTATION_SUCCESSFUL);
                        TELEPORTATION.remove(teleportation.getPlayer());
                        cancel();
                    }

                    // If the player moves, cancel the teleportation
                    if (teleportation.getPlayer().getLocation().distance(teleportation.start) > 0.2) {
                        Essentials.instance().messages().sendMessage(teleportation.getPlayer(), Messages.TELEPORTATION_CANCELLED);
                        TELEPORTATION.remove(teleportation.getPlayer());
                        teleportation.onFinish.run();
                        cancel();
                    }

                    amount--;

                } else { // If the player is not online, cancel the teleportation
                    TELEPORTATION.remove(teleportation.getPlayer());
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(Essentials.instance(), 10, 10);
    }

    public Player getPlayer() {
        return player;
    }

    public Location getTarget() {
        return target;
    }
}
