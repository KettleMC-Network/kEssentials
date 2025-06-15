package net.kettlemc.kessentials.discord.command.commands;

import io.github.almightysatan.slams.Placeholder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.kettlemc.kcommon.language.AdventureUtil;
import net.kettlemc.kessentials.Essentials;
import net.kettlemc.kessentials.config.Messages;
import net.kettlemc.kessentials.discord.command.SlashCommand;
import org.bukkit.Bukkit;

public class StopServerCommand extends SlashCommand {

    public StopServerCommand() {
        super("stop");
    }

    @Override
    public void onExecute(SlashCommandInteractionEvent event, Member member, MessageChannel channel) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply(AdventureUtil.componentToLegacy(Messages.DISCORD_NO_PERMISSION.value())).queue();
            return;
        }

        OptionMapping secondsOptions = event.getOption("seconds");
        long seconds;
        if (secondsOptions != null) {
            seconds = secondsOptions.getAsLong();
        } else {
            seconds = 0;
        }

        if (seconds == 0) {
            event.reply(AdventureUtil.componentToLegacy(Messages.DISCORD_RESTART_INSTANT.value())).queue();
            Bukkit.getServer().shutdown();
            return;
        }

        event.reply(AdventureUtil.componentToLegacy(Messages.DISCORD_RESTART.value(Placeholder.of("seconds", (ctx, args) -> String.valueOf(seconds))))).queue();
        Bukkit.getServer().broadcastMessage(AdventureUtil.componentToLegacy(Messages.DISCORD_RESTART_MINECRAFT.value(Placeholder.of("seconds", (ctx, args) -> String.valueOf(seconds)))));
        Bukkit.getScheduler().runTaskLater(Essentials.instance(), () -> Bukkit.getServer().shutdown(), seconds * 20L);
    }

    @Override
    public void construct(CommandListUpdateAction commands) {
        OptionData secondsOption = new OptionData(OptionType.INTEGER, "seconds", "The amount of seconds to wait before stopping the server.");
        CommandData commandData = Commands.slash("stop", "Stops the server after a certain amount of seconds.").addOptions(secondsOption);
        commands.addCommands(commandData);
    }

}
