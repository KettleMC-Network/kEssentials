package net.kettlemc.kessentials.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kettlemc.kessentials.config.DiscordConfiguration;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        SlashCommand command;

        if ((command = SlashCommand.commandMap.get(commandName)) != null) {
            if (DiscordConfiguration.DISCORD_ALLOW_EVERY_CHANNEL_FOR_COMMANDS.getValue() || event.getChannel().getIdLong() == DiscordConfiguration.DISCORD_CHANNEL_ID.getValue()) {
                command.onExecute(event, event.getMember(), event.getMessageChannel());
            }
        }
    }
}