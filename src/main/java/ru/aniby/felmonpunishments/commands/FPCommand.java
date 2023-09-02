package ru.aniby.felmonpunishments.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.aniby.felmonapi.FelmonUtils;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.configuration.FPMessagesConfig;
import ru.aniby.felmonpunishments.discord.DiscordUtils;
import ru.aniby.felmonpunishments.discord.FPLinkedGuild;
import ru.aniby.felmonpunishments.player.FPPlayer;
import ru.aniby.felmonpunishments.utils.CommandUtils;

import java.util.*;

public interface FPCommand extends CommandExecutor, TabCompleter {
    default boolean isRightObject(Object object) {
        return object instanceof SlashCommandInteractionEvent || object instanceof FPInvocation;
    }

    @NotNull String getName();

    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        execute(new FPInvocation(sender, command, label, args));
        return true;
    }

    default void onDiscordCommand(SlashCommandInteractionEvent event) {
        execute(event);
    }

    default boolean hasPermission(Object object) {
        return hasPermission(object, getPermission());
    }

    default boolean hasPermission(Object object, @Nullable String permission) {
        if (permission == null)
            return true;
        if (object instanceof FPInvocation invocation) {
            return hasPermission(invocation, permission);
        }
        else if (object instanceof SlashCommandInteractionEvent event) {
            return hasPermission(event, permission);
        }
        return false;
    }

    default boolean hasPermission(FPInvocation invocation, @Nullable String permission) {
        return permission == null || invocation.source().hasPermission(permission);
    }

    default boolean hasPermission(SlashCommandInteractionEvent event, @Nullable String permission) {
        if (permission == null)
            return true;
        Member member = event.getMember();
        if (member == null)
            return false;
        if (getArgumenter() != null) {
            String nickname = member.getNickname();
            if (nickname != null) {
                FPPlayer fpPlayer = FPPlayer.get(member.getNickname());
                User lpUser = fpPlayer.getLuckpermsUser();
                if (CommandUtils.hasPermission(lpUser, permission))
                    return true;
            }

            FPLinkedGuild linkedGuild = DiscordUtils.getLinkedGuild();
            if (linkedGuild != null) {
                HashMap<Group, Role> sync = linkedGuild.getGroups();
                for (Group primaryGroup : sync.keySet()) {
                    if (CommandUtils.hasPermission(primaryGroup, permission)) {
                        Role role = sync.get(primaryGroup);
                        if (member.getRoles().contains(role)) {
                            return true;
                        }
                    }
                }
            }
        }
        event.getHook().setEphemeral(true).sendMessage(FPMessagesConfig.notEnoughPermissions.getText()).queue();
//        event.reply(FPMessagesConfig.notEnoughPermissions.getText()).setEphemeral(true).queue();
        return false;
    }

    default void execute(Object object) {};

    default boolean isDiscordAvailable() {
        return true;
    }

    default @NotNull CommandData slashCommandData() {
        CommandDataImpl commandData = new CommandDataImpl(getName(), getDescription());
        if (getArgumenter() != null) {
            List<OptionData> arguments = getArgumenter().getArgumentList().stream()
                    .map(FPCommandOption::getOptionDataList)
                    .sorted((var1, var2) -> Boolean.compare(var1.isRequired(), var2.isRequired()))
                    .toList();
            commandData.addOptions(
                    arguments.stream().filter(OptionData::isRequired).toList()
            );
            commandData.addOptions(
                    arguments.stream().filter(a -> !a.isRequired()).toList()
            );
        }
        return commandData;
    }

    FPArgumenter getArgumenter();

    default @Nullable String getPermission() {
        PluginCommand command = FelmonPunishments.getInstance().getCommand(getName());
        return command == null ? null : command.getPermission();
    };

    default @NotNull String getDescription() {
        PluginCommand command = FelmonPunishments.getInstance().getCommand(getName());
        return command == null ? "" : command.getDescription();
    };

    @Override
    default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (getArgumenter() != null) {
            FPCommandOption option = null;
            try {
                option = getArgumenter().getArgumentList().get(args.length - 1);
            } catch (IndexOutOfBoundsException exception) {
                if (getArgumenter().getArgumentList().get(
                        getArgumenter().getArgumentList().size() - 1
                ).equals(FPCommandOption.REASON)) {
                    option = FPCommandOption.REASON;
                }
            }

            if (option != null) {
                String value = null;
                if (option != FPCommandOption.REASON) {
                    int index = getArgumenter().getArgumentList().indexOf(option);
                    if (args.length - 1 >= index)
                        value = args[index];
                }

                switch (option) {
                    case PLAYER, NICKNAME, INTRUDER, VICTIM -> list = FelmonUtils.Completer.players(value);
                    case TIME -> list = FelmonUtils.Completer.time(value);
                    case REASON -> list.add("[Причина]");
                }
            }
        }
        return list;
    }
}
