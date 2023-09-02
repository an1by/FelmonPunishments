package ru.aniby.felmonpunishments.discord;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.commands.FPCommand;

public class DiscordUtils {
    @Getter
    private static FPLinkedGuild linkedGuild = null;
    @Getter
    private static JDA JDA = null;

    public static void start() {
        FelmonPunishments.getInstance().getLogger().info("[Discord] Init...");
        String token = FelmonPunishments.getInstance().getConfig().getString("discord.bot_token");

        JDABuilder builder = JDABuilder
                .createDefault(token)
                .addEventListeners(new JDAListener())
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .disableCache(
                        CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.ACTIVITY,
                        CacheFlag.CLIENT_STATUS, CacheFlag.CLIENT_STATUS, CacheFlag.STICKER,
                        CacheFlag.FORUM_TAGS, CacheFlag.ONLINE_STATUS
                )
                .setBulkDeleteSplittingEnabled(false)
                .setActivity(Activity.watching("за сервером"));

        JDA = builder.build();
    }

    public static void init() {
        String guildId = FelmonPunishments.getInstance().getConfig().getString("discord.server.guild");
        if (guildId != null) {
            Guild guild = JDA.getGuildById(guildId);
            if (guild != null) {
                linkedGuild = new FPLinkedGuild(guild);
                FelmonPunishments.getInstance().getLogger().info("[Discord] Sync Roles was init!");
                initCommands();
                FelmonPunishments.getInstance().getLogger().info("[Discord] Commands was init!");
                return;
            }
        }
        FelmonPunishments.getInstance().getLogger().warning("[Discord] Error while init!");
    }

    public static void initCommands() {
        Guild guild = linkedGuild.getGuild();

        CommandListUpdateAction updateAction = guild.updateCommands();
        for (FPCommand command : FelmonPunishments.getCommands()) {
            if (command.isDiscordAvailable())
                updateAction = updateAction.addCommands(
                        command.slashCommandData()
                );
        }
        updateAction.queue();
    }
}
