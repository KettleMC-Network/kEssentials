package net.kettlemc.kessentials.discord.listener.bukkit;

import io.github.almightysatan.slams.Placeholder;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import net.kettlemc.kcommon.language.AdventureUtil;
import net.kettlemc.kessentials.Essentials;
import net.kettlemc.kessentials.config.DiscordConfiguration;
import net.kettlemc.kessentials.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class DiscordClearLaggListener implements Listener {


    private static int lastCleared = 0;

    public DiscordClearLaggListener() {
    }

    @EventHandler
    public void onClear(EntityRemoveEvent event) {

        lastCleared += event.getEntityList().size();

        // Event gets fired for every world, only send it once
        if (event.getWorld() == Bukkit.getWorlds().get(0) && event.getEntityList().size() >= DiscordConfiguration.MIN_CLEARLAGG_ITEMS.getValue()) {

            new BukkitRunnable() {

                @Override
                public void run() {
                    String message = AdventureUtil.componentToLegacy(Messages.DISCORD_CLEARLAGG.value(
                            Placeholder.of("items", (ctx, args) -> String.valueOf(lastCleared))
                    ));
                    Essentials.instance().getDiscordBot().sendMessage(message);
                    lastCleared = 0;
                }

            }.runTaskLater(Essentials.instance(), 40L);


        }

    }

}
