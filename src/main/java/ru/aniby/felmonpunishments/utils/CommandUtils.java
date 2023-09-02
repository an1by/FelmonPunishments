package ru.aniby.felmonpunishments.utils;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.node.NodeType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.aniby.felmonapi.category.FelmonComponent;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.commands.FPInvocation;
import ru.aniby.felmonpunishments.discord.DiscordUtils;
import ru.aniby.felmonpunishments.discord.FPLinkedGuild;
import ru.aniby.felmonpunishments.player.FPPlayer;
import ru.aniby.felmonpunishments.punishment.Punishment;
import ru.aniby.felmonpunishments.punishment.PunishmentType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CommandUtils {

    @Getter
    private static final Sound notifySound = Sound.sound(
            Key.key("minecraft", "entity.experience_orb.pickup"),
            Sound.Source.PLAYER, 1, 1
    );

    @Getter
    private static final Color darkPurple = Color.decode(NamedTextColor.DARK_PURPLE.asHexString());

    public static boolean canPunish(@NotNull String executor, @NotNull String target) {
        FPPlayer fpExecutor = FPPlayer.get(executor);
        FPPlayer fpTarget = FPPlayer.get(target);
        if (hasPermission(fpTarget.getLuckpermsUser(), FelmonPunishments.getOverridePermission()))
            return false;
        return fpExecutor.getWeight() >= fpTarget.getWeight();
    }

    public static String getHead(String username) {
        return username.equalsIgnoreCase("консоль")
                || username.equalsIgnoreCase("система")
                || username.equalsIgnoreCase("console")
                ? "https://s.namemc.com/2d/skin/face.png?id=889ebfe1a6091200&scale=32"
                : "http://cravatar.eu/avatar/" + username + "/128.png";
    }

    public static @NotNull List<String> punishmentPlayersCompleter(@NotNull List<? extends Punishment> punishments, @Nullable String argument) {
        List<String> list = new ArrayList<>();
        if (argument != null) {
            for (Punishment punishment : punishments)
                if (argument.isEmpty() || punishment.getIntruder().startsWith(argument))
                    list.add(punishment.getIntruder());
        }
        return list;
    }

    public static void send(@NotNull Object object, @NotNull String text) {
        send(object, new FelmonComponent(text));
    }


    public static void send(@NotNull Object object, @NotNull FelmonComponent component) {
        if (object instanceof FPInvocation invocation)
            invocation.source().sendMessage(component.getComponent());
        else if (object instanceof SlashCommandInteractionEvent event) {
//            event.reply(component.getText()).setEphemeral(true).queue();
            event.getHook().setEphemeral(true).sendMessage(component.getText()).queue();
        }
    }

    public static void notifyInDirect(@NotNull String intruder, @Nullable Component component, @Nullable EmbedBuilder embedBuilder) {
        if (component != null) {
            Player player = Bukkit.getPlayer(intruder);
            if (player != null) {
                player.sendMessage(component);
                player.playSound(CommandUtils.getNotifySound());
            }
        }
        if (embedBuilder != null) {
            FPPlayer fpPlayer = FPPlayer.get(intruder);
            Bukkit.getScheduler().runTaskAsynchronously(FelmonPunishments.getInstance(),
                    () -> {
                        User discordUser = fpPlayer.getDiscordUser();
                        if (discordUser != null) {
                            discordUser.openPrivateChannel().flatMap(
                                    channel -> channel.sendMessageEmbeds(embedBuilder.build())
                            ).queue();
                        }
                    }
            );
        }
    }

    public static boolean hasPermission(@Nullable PermissionHolder holder, @Nullable String permission) {
        if (permission == null)
            return true;
        if (holder == null)
            return false;
        return holder.getNodes(NodeType.PERMISSION).stream()
                .filter(n -> n.getPermission().equals(permission) || n.getPermission().equals("*"))
                .findFirst().orElse(null) != null;
    }

    public static void notifyInChannel(@NotNull String intruder, @NotNull PunishmentType type, @Nullable EmbedBuilder embedBuilder) {
        if (embedBuilder == null)
            return;
        FPPlayer fpPlayer = FPPlayer.get(intruder);
        String discordId = fpPlayer.getDiscordId();
        FPLinkedGuild linkedGuild = DiscordUtils.getLinkedGuild();
        if (linkedGuild != null) {
            linkedGuild.send(
                    type.getChannelName(),
                    discordId == null ? "" : "<@" + discordId + ">",
                    embedBuilder
            );
        }
    }
}
