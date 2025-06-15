package net.kettlemc.kessentials.listener;

import net.kettlemc.kessentials.Essentials;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

public class EnchantingTableListener implements Listener {

    @EventHandler
    public void onEnchantingTableOpen(PrepareItemEnchantEvent event) {

        if (!event.getEnchanter().hasMetadata("kessentials.enchanting.level")) {
            return;
        }
        try {
            int level1 = event.getEnchanter().getMetadata("kessentials.enchanting.level").get(0).asInt();
            int level2 = event.getEnchanter().getMetadata("kessentials.enchanting.level").get(1).asInt();
            int level3 = event.getEnchanter().getMetadata("kessentials.enchanting.level").get(2).asInt();

            event.getExpLevelCostsOffered()[0] = level1;
            event.getExpLevelCostsOffered()[1] = level2;
            event.getExpLevelCostsOffered()[2] = level3;

        } catch (Exception e) {
            Essentials.instance().getLogger().log(java.util.logging.Level.SEVERE, "Error while setting custom enchanting levels for " + event.getEnchanter().getUniqueId(), e);
        } finally {
            event.getEnchanter().removeMetadata("kessentials.enchanting.level", Essentials.instance());
        }

    }

}
