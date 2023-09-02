package ru.aniby.felmonpunishments.discord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.commands.FPCommand;

public class JDAListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent readyEvent) {
        DiscordUtils.init();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null)
            return;

        String commandName = event.getName();
        FPCommand command = FelmonPunishments.getFPCommand(commandName);
        if (command != null) {
            event.deferReply().setEphemeral(true).queue();
            command.onDiscordCommand(event);
        }
    }
}
