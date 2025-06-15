package net.kettlemc.kessentials;

import net.kettlemc.kcommon.bukkit.ContentManager;
import net.kettlemc.kcommon.language.MessageManager;
import net.kettlemc.kessentials.command.*;
import net.kettlemc.kessentials.command.gui.EnchantingTableCommand;
import net.kettlemc.kessentials.command.gui.EnderchestCommand;
import net.kettlemc.kessentials.command.gui.WorkbenchCommand;
import net.kettlemc.kessentials.command.home.DeleteHomeCommand;
import net.kettlemc.kessentials.command.home.HomeCommand;
import net.kettlemc.kessentials.command.home.SetHomeCommand;
import net.kettlemc.kessentials.command.tpa.TPACommand;
import net.kettlemc.kessentials.command.tpa.TPAcceptCommand;
import net.kettlemc.kessentials.command.tpa.TPDenyCommand;
import net.kettlemc.kessentials.command.tpa.TPListCommand;
import net.kettlemc.kessentials.command.warp.DeleteWarpCommand;
import net.kettlemc.kessentials.command.warp.SetWarpCommand;
import net.kettlemc.kessentials.command.warp.SpawnCommand;
import net.kettlemc.kessentials.command.warp.WarpCommand;
import net.kettlemc.kessentials.config.Configuration;
import net.kettlemc.kessentials.config.DiscordConfiguration;
import net.kettlemc.kessentials.config.Messages;
import net.kettlemc.kessentials.discord.DiscordBot;
import net.kettlemc.kessentials.discord.listener.bukkit.DiscordAsyncChatListener;
import net.kettlemc.kessentials.discord.listener.bukkit.DiscordClearLaggListener;
import net.kettlemc.kessentials.discord.listener.bukkit.DiscordJoinQuitListener;
import net.kettlemc.kessentials.listener.*;
import net.kettlemc.kessentials.teleport.HomeHandler;
import net.kettlemc.kessentials.teleport.WarpHandler;
import net.kettlemc.kessentials.util.RestartTimer;
import net.kettlemc.kessentials.util.Util;
import net.kettlemc.klanguage.api.LanguageAPI;
import net.kettlemc.klanguage.bukkit.BukkitLanguageAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Essentials extends JavaPlugin {

    public static final LanguageAPI<Player> LANGUAGE_API = BukkitLanguageAPI.of();
    private static Essentials instance;

    private final ContentManager contentManager = new ContentManager(this);
    private BukkitAudiences adventure;
    private MessageManager messageManager;

    private DiscordBot discordBot;
    private HomeHandler homeHandler;
    private WarpHandler warpHandler;

    public static Essentials instance() {
        return instance;
    }

    /**
     * The luckperms instance
     *
     * @return The luckperms instance
     */
    public static LuckPerms luckPerms() {
        return LuckPermsProvider.get();
    }

    @Override
    public void onEnable() {

        instance = this;

        this.getLogger().info("Loading adventure support...");
        this.adventure = BukkitAudiences.create(this);

        this.getLogger().info("Registering config...");
        if (!Configuration.load()) {
            this.getLogger().severe("Failed to load config!");
        }

        this.getLogger().info("Loading messages...");
        if (!Messages.load()) {
            this.getLogger().severe("Failed to load messages!");
        }

        this.messageManager = new MessageManager(Messages.PREFIX, LANGUAGE_API, adventure);

        this.getLogger().info("Registering commands and listeners...");

        // Register all the time commands with one instance of the TimeCommands class
        TimeCommands timeCommands = new TimeCommands();
        TimeCommands.TIME_MAP.keySet().forEach(time -> contentManager.registerCommand(time, timeCommands));

        this.contentManager.registerCommand("tpa", new TPACommand());
        this.contentManager.registerCommand("tpaccept", new TPAcceptCommand());
        this.contentManager.registerCommand("tpdeny", new TPDenyCommand());
        this.contentManager.registerCommand("tplist", new TPListCommand());
        this.contentManager.registerCommand("gamemode", new GamemodeCommand());
        this.contentManager.registerCommand("suicide", new SuicideCommand());
        this.contentManager.registerCommand("f3d", new F3DCommand());
        this.contentManager.registerCommand("speed", new SpeedCommand());
        this.contentManager.registerCommand("fly", new FlyCommand());
        this.contentManager.registerCommand("chatclear", new ChatClearCommand());
        this.contentManager.registerCommand("enderchest", new EnderchestCommand());
        this.contentManager.registerCommand("teleportplayer", new TeleportPlayerCommand());
        this.contentManager.registerCommand("freeze", new FreezeCommand());
        this.contentManager.registerCommand("vanish", new VanishCommand());
        this.contentManager.registerCommand("heal", new HealCommand());
        this.contentManager.registerCommand("feed", new FeedCommand());
        this.contentManager.registerCommand("repair", new RepairCommand());
        this.contentManager.registerCommand("inventorysee", new InventorySeeCommand());
        this.contentManager.registerCommand("armorsee", new ArmorSeeCommand());
        this.contentManager.registerCommand("home", new HomeCommand());
        this.contentManager.registerCommand("sethome", new SetHomeCommand());
        this.contentManager.registerCommand("delhome", new DeleteHomeCommand());
        this.contentManager.registerCommand("warp", new WarpCommand());
        this.contentManager.registerCommand("setwarp", new SetWarpCommand());
        this.contentManager.registerCommand("delwarp", new DeleteWarpCommand());
        this.contentManager.registerCommand("spawn", new SpawnCommand());
        this.contentManager.registerCommand("kessentials", new KEssentialsCommand());
        this.contentManager.registerCommand("material", new MaterialCommand());
        this.contentManager.registerCommand("enchantingtable", new EnchantingTableCommand());
        this.contentManager.registerCommand("workbench", new WorkbenchCommand());

        // Disable all commands disabled in the config
        Configuration.DISABLED_COMMANDS.getValue().forEach(cmd -> Bukkit.getPluginCommand(cmd).setExecutor(new DisabledCommandExecutor()));

        this.contentManager.registerListener(new JoinQuitListener());
        this.contentManager.registerListener(new BlockListener());
        this.contentManager.registerListener(new PlayerMoveListener());
        this.contentManager.registerListener(new InventoryClickListener());
        this.contentManager.registerListener(new CommandListener());
        this.contentManager.registerListener(new EnchantingTableListener());

        this.getLogger().info("Loading Discord bot...");

        if (!DiscordConfiguration.load()) {
            this.getLogger().severe("Failed to load discord config!");
        }

        // If the token is the default value, don't start the bot (disabled)
        if (!DiscordConfiguration.DISCORD_TOKEN.getValue().equals(DiscordConfiguration.DISCORD_TOKEN.getDefaultValue())) {
            this.discordBot = new DiscordBot();
            if (!this.discordBot.enable()) {
                this.discordBot = null;
                DiscordConfiguration.unload();
            } else {
                this.contentManager.registerListener(new DiscordAsyncChatListener());
                this.contentManager.registerListener(new DiscordJoinQuitListener());
                if (Bukkit.getPluginManager().getPlugin(Util.CLEARLAGG_PLUGIN_NAME) != null)
                    this.contentManager.registerListener(new DiscordClearLaggListener());
                this.getLogger().info("Discord bot loaded successfully!");
            }
        } else {
            this.getLogger().info("No token provided, disabling discord bot...");
        }

        this.getLogger().info("Loading warps and homes...");
        this.warpHandler = new WarpHandler();
        this.warpHandler.loadWarps();
        this.homeHandler = new HomeHandler();
        this.homeHandler.init();

        this.getLogger().info("Starting restart timer...");
        RestartTimer.scheduleRestart();

    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabling plugin...");

        if (this.discordBot != null) {
            this.discordBot.shutdown();
        }

        Configuration.unload();
        DiscordConfiguration.unload();

        if (this.warpHandler != null) {
            this.warpHandler.unload();
        }

        if (this.homeHandler != null) {
            this.homeHandler.unload();
        }
        instance = null;
    }

    /**
     * Returns the adventure instance
     *
     * @return The adventure instance
     */
    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    /**
     * Checks if the sender has permission to run the command.
     * When other is true, it checks for the permission to run the command on other players.
     * When other is false, it checks for the permission to run the command on themselves.
     * When the sender is a console, it always returns true.
     * <p>
     * When the sender has the permission to run the command on other players, they also have the permission to run the command on themselves.
     *
     * @param sender  The sender to check
     * @param command The command to check
     * @param other   Whether to check for the permission to run the command on other players
     * @return True if the sender has permission to run the command, false otherwise
     */
    public boolean checkPermission(CommandSender sender, Command command, boolean other) {
        return (sender instanceof ConsoleCommandSender)
                || (sender.hasPermission(Configuration.PERMISSION_LAYOUT_OTHER.getValue().replace("%command%", command.getLabel())))
                || (!other && sender.hasPermission(Configuration.PERMISSION_LAYOUT.getValue().replace("%command%", command.getLabel())));
    }

    /**
     * Utility handler for sending messages to players
     *
     * @return The message manager
     */
    public MessageManager messages() {
        return this.messageManager;
    }

    /**
     * The discord bot instance
     *
     * @return The discord bot instance
     */
    public DiscordBot getDiscordBot() {
        return this.discordBot;
    }

    /**
     * The home handler instance
     *
     * @return The home handler instance
     */
    public HomeHandler homeHandler() {
        return this.homeHandler;
    }

    /**
     * The warp handler instance
     *
     * @return The warp handler instance
     */
    public WarpHandler warpHandler() {
        return this.warpHandler;
    }

}
