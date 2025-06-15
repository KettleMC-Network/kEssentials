package net.kettlemc.kessentials.command.gui;

import net.kettlemc.kcommon.java.NumberUtil;
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

public class EnchantingTableCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (args.length == 3) {

            if (!(sender instanceof Player)) {
                Essentials.instance().messages().sendMessage(sender, Messages.PLAYER_ONLY);
                return true;
            }

            if (!Essentials.instance().checkPermission(sender, command, false)) {
                Essentials.instance().messages().sendMessage(sender, Messages.NO_PERMISSION);
                return true;
            }

            Player player = (Player) sender;

            if (NumberUtil.isInteger(args[0]) && NumberUtil.isInteger(args[1]) && NumberUtil.isInteger(args[2])) {
                int level1 = Integer.parseInt(args[0]);
                int level2 = Integer.parseInt(args[1]);
                int level3 = Integer.parseInt(args[2]);

                openEnchanting(player, level1, level2, level3);
            } else {
                Essentials.instance().messages().sendMessage(sender, Messages.ENCHANTINGTABLE_USAGE);
            }

        } else if (args.length == 4) {

            if (!Essentials.instance().checkPermission(sender, command, true)) {
                Essentials.instance().messages().sendMessage(sender, Messages.NO_PERMISSION);
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                Essentials.instance().messages().sendMessage(sender, Messages.PLAYER_NOT_FOUND);
                return true;
            }

            if (NumberUtil.isInteger(args[1]) && NumberUtil.isInteger(args[2]) && NumberUtil.isInteger(args[3])) {
                int level1 = Integer.parseInt(args[1]);
                int level2 = Integer.parseInt(args[2]);
                int level3 = Integer.parseInt(args[3]);

                openEnchanting(target, level1, level2, level3);
            } else {
                Essentials.instance().messages().sendMessage(sender, Messages.ENCHANTINGTABLE_USAGE);
            }

        } else {
            Essentials.instance().messages().sendMessage(sender, Messages.ENCHANTINGTABLE_USAGE);
        }

        return true;
    }

    private void openEnchanting(Player player, int level1, int level2, int level3) {
        player.setMetadata("kessentials.enchanting.level", new FixedMetadataValue(Essentials.instance(), level1));
        player.setMetadata("kessentials.enchanting.level", new FixedMetadataValue(Essentials.instance(), level2));
        player.setMetadata("kessentials.enchanting.level", new FixedMetadataValue(Essentials.instance(), level3));
        player.openEnchanting(null, true);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 1) {
            return null;
        }
        return Collections.emptyList();
    }
}
