package ru.aniby.felmonpunishments.discord;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.util.DiscordUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.commands.MSCommand;

import javax.annotation.Nonnull;

public class DiscordListener extends ListenerAdapter{
    @Subscribe
    public void onReady(@Nonnull DiscordReadyEvent event) {
        DiscordUtils.init();
        DiscordUtil.getJda().addEventListener(new DiscordListener());
    }

    @Subscribe
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null)
            return;
        String commandName = event.getName();
        MSCommand command = FelmonPunishments.getMSCommand(commandName);
        if (command != null)
            command.onDiscordCommand(event);
    }
}
