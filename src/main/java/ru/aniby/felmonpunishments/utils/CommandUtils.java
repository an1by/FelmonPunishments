package ru.aniby.felmonpunishments.utils;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.node.NodeType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    public static class Completer {
        public static @NotNull List<String> punishmentPlayers(@NotNull List<? extends Punishment> punishments, @Nullable String argument) {
            List<String> list = new ArrayList<>();
            if (argument != null) {
                for (Punishment punishment : punishments)
                    if (argument.isEmpty() || punishment.getIntruder().startsWith(argument))
                        list.add(punishment.getIntruder());
            }
            return list;
        }

        public static @NotNull List<String> players(@Nullable String argument) {
            List<String> list = new ArrayList<>();
            if (argument != null) {
                for (Player player : Bukkit.getOnlinePlayers())
                    if (argument.isEmpty() || player.getName().startsWith(argument))
                        list.add(player.getName());
            }
            return list;
        }

        public static @NotNull List<String> time(@Nullable String argument) {
            List<String> list = new ArrayList<>();
            if (argument != null && argument.length() > 0 && Character.isDigit(
                    argument.charAt(argument.length() - 1)
            )) {
                for (char ch : new char[] {'d', 'h', 'm', 's'}) {
                    list.add(argument + ch);
                }
            } else {
                for(int i = 1; i <= 10; i++) {
                    list.add(String.valueOf(i));
                }
            }
            return list;
        }
    }

    public static void send(@NotNull Object object, @NotNull String text) {
        final TextComponent component = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        if (object instanceof FPInvocation invocation) {
            invocation.source().sendMessage(component);
        }
        else if (object instanceof SlashCommandInteractionEvent event) {
            final String miniMessageString = MiniMessage.miniMessage().serialize(
                    component
            ).replaceAll("<[^>]*>", "");
            event.reply(miniMessageString).setEphemeral(true).queue();
        }
    }

    public static void send(@NotNull Object object, @NotNull CommandUtils.Message error) {
        if (object instanceof FPInvocation invocation) {
            invocation.source().sendMessage(error.getComponent());
        }
        else if (object instanceof SlashCommandInteractionEvent event) {
            event.reply(error.getText()).setEphemeral(true).queue();
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
                    type == PunishmentType.TICKET ? "tickets" : "punishments",
                    discordId == null ? "" : "<@" + discordId + ">",
                    embedBuilder
            );
        }
    }

    public enum Message {
        ERROR_IN_PROCESSING("Произошла ошибка при обработке!", NamedTextColor.RED),
        PUNISHMENT_NOT_EXISTS("Наказание не найдено!", NamedTextColor.RED),
        EXISTS_SIMILAR("В базе данных уже есть подобная запись!", NamedTextColor.RED),
        NOW_BANNED("Игрок уже находится в бане!", NamedTextColor.RED),
        NOW_MUTED("У игрока уже присутствует мут!", NamedTextColor.RED),
        DAY_OR_MORE("Это наказание можно выдать только с минимальным сроком в 1 день!", NamedTextColor.RED),
        PLAYER_NOT_FOUND("Игрок с такими данными не найден!", NamedTextColor.RED),
        CANT_PUNISH("Вы не можете наказать этого игрока!", NamedTextColor.RED),
        NOT_ENOUGH_PERMISSIONS("Недостаточно прав!", NamedTextColor.RED),
        WRONG_ARGUMENTS("Неверные аргументы для команды!", NamedTextColor.RED);
        
        @Getter
        private final String text;
        
        @Getter
        private final TextColor textColor;

        Message(String text, TextColor textColor) {
            this.text = text;
            this.textColor = textColor;
        }
        
        public Component getComponent() {
            return Component.text(text).color(textColor);
        }
    }
}
