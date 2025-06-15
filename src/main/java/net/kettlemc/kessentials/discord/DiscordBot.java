package net.kettlemc.kessentials.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.kettlemc.kcommon.language.AdventureUtil;
import net.kettlemc.kessentials.Essentials;
import net.kettlemc.kessentials.config.DiscordConfiguration;
import net.kettlemc.kessentials.config.Messages;
import net.kettlemc.kessentials.discord.command.SlashCommand;
import net.kettlemc.kessentials.discord.command.SlashCommandListener;
import net.kettlemc.kessentials.discord.command.commands.ListSlashCommand;
import net.kettlemc.kessentials.discord.command.commands.StopServerCommand;
import net.kettlemc.kessentials.discord.listener.MessageListener;
import net.kettlemc.kessentials.discord.listener.ReadyListener;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class DiscordBot {

    private JDA jda;

    public boolean enable() {
        try {
            String token = DiscordConfiguration.DISCORD_TOKEN.getValue();

            JDABuilder builder = JDABuilder.createDefault(token);
            builder.setActivity(Activity.playing(DiscordConfiguration.DISCORD_BOT_STATUS.getValue().replace("<online>", String.valueOf(Bukkit.getOnlinePlayers().size()))));
            builder.setStatus(OnlineStatus.ONLINE);
            builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
            this.jda = builder.build();

            jda.addEventListener(new ReadyListener(this));
            jda.addEventListener(new MessageListener());
            jda.addEventListener(new SlashCommandListener());
            jda.awaitReady();
            this.registerCommands();
            return true;
        } catch (Exception e) {
            Essentials.instance().getLogger().log(Level.SEVERE, "Discord bot failed to start.", e);
            return false;
        }
    }

    public void registerCommands() {

        // Add commands to the map
        new ListSlashCommand().register();
        new StopServerCommand().register();

        // Delete any existing commands (prevents duplicates)
        this.jda.retrieveCommands().complete().forEach(cmd -> cmd.delete().queue());

        // Add commands to guilds
        this.jda.getGuilds().forEach(guild -> {
            CommandListUpdateAction commands = guild.updateCommands();
            SlashCommand.commandMap.keySet().forEach(key -> SlashCommand.commandMap.get(key).construct(commands));
            commands.queue();
        });
    }

    public void shutdown() {
        getMessageChannelById(DiscordConfiguration.DISCORD_CHANNEL_ID.getValue()).sendMessage(AdventureUtil.componentToLegacy(Messages.DISCORD_SHUTDOWN.value())).complete();
        jda.shutdown();
    }

    public MessageChannel getMessageChannelById(long id) {
        return jda.getTextChannelById(id);
    }

    public void sendMessage(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }

    public void sendMessage(long channelId, String message) {
        sendMessage(getMessageChannelById(channelId), message);
    }

    public void sendMessage(String message) {
        sendMessage(DiscordConfiguration.DISCORD_CHANNEL_ID.getValue(), message);
    }

    public void sendEmbed(String author, String body, String authorImage) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(author, null, authorImage);
        builder.setDescription(body);
        this.getMessageChannelById(DiscordConfiguration.DISCORD_CHANNEL_ID.getValue()).sendMessageEmbeds(builder.build()).queue();
    }

    public JDA getJDA() {
        return this.jda;
    }

    public void updateStatusDelayed() {
        Bukkit.getScheduler().runTaskLater(Essentials.instance(), this::updateStatus, 20L);
    }

    public void updateStatus() {
        String status = DiscordConfiguration.DISCORD_BOT_STATUS.getValue().replace("<online>", String.valueOf(Bukkit.getOnlinePlayers().size()));
        this.getJDA().getPresence().setActivity(Activity.playing(status));
    }
}
