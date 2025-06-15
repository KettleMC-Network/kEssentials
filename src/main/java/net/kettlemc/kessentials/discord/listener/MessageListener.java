package net.kettlemc.kessentials.discord.listener;

import io.github.almightysatan.slams.Placeholder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kettlemc.kcommon.java.StringUtil;
import net.kettlemc.kessentials.Essentials;
import net.kettlemc.kessentials.config.DiscordConfiguration;
import net.kettlemc.kessentials.config.Messages;

import java.util.Collections;
import java.util.List;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE) || event.getAuthor().isBot() || event.getChannel().getIdLong() != DiscordConfiguration.DISCORD_CHANNEL_ID.getValue())
            return;
        List<Role> roles;

        if (event.getMember() == null) {
            Essentials.instance().getLogger().warning("Member is null for message: " + event.getMessage().getContentDisplay());
            roles = Collections.emptyList();
        } else {
            roles = event.getMember().getRoles();
        }

        String rank = roles.isEmpty() ? DiscordConfiguration.DEFAULT_RANK.getValue() : StringUtil.stripEmojis(roles.get(0).getName()).trim();

        Essentials.instance().messages().broadcastMessage(Messages.DISCORD_CHAT_FORMAT_MINECRAFT,
                false,
                Placeholder.of("name", (ctx, value) -> event.getAuthor().getName()),
                Placeholder.of("message", (ctx, value) -> event.getMessage().getContentDisplay()),
                Placeholder.of("rank", (ctx, value) -> rank)
        );
    }

}
