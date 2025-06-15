package net.kettlemc.kessentials.discord.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.kettlemc.kessentials.Essentials;

import java.util.HashMap;

public abstract class SlashCommand {

    public static HashMap<String, SlashCommand> commandMap = new HashMap<>();

    private final String name;

    public SlashCommand(String name) {
        this.name = name;
    }

    public abstract void onExecute(SlashCommandInteractionEvent event, Member member, MessageChannel channel);

    public abstract void construct(CommandListUpdateAction commands);

    public void register() {
        if (!SlashCommand.commandMap.containsKey(this.name())) {
            Essentials.instance().getLogger().info("Adding discord slash command " + this.name() + " to the list.");
            SlashCommand.commandMap.put(this.name(), this);
        }
    }

    public String name() {
        return this.name;
    }

}
