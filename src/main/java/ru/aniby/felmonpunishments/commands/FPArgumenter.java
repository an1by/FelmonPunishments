package ru.aniby.felmonpunishments.commands;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonapi.FelmonUtils;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.player.FPPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FPArgumenter {
    @Getter
    @NotNull
    private final List<@NotNull FPCommandOption> argumentList;

    public FPArgumenter() {
        argumentList = new ArrayList<>();
    }

    public FPArgumenter(@NotNull String... argumentNames) {
        this.argumentList = new ArrayList<>();
        for (String name : argumentNames) {
            FPCommandOption commandOption = Arrays.stream(FPCommandOption.values())
                    .filter(o -> o.getName().equals(name))
                    .findFirst().orElse(null);
            if (commandOption != null)
                argumentList.add(commandOption);
            else
                FelmonPunishments.getInstance().getLogger().warning("Invalid argument name: " + name);
        }
    }

    public FPArgumenter(@NotNull FPCommandOption... arguments) {
        this.argumentList = Arrays.asList(arguments);
    }

    public FPArgumenter(@NotNull List<FPCommandOption> arguments) {
        this.argumentList = arguments;
    }

    public boolean contains(@NotNull String key) {
        return this.argumentList.stream()
                .filter(a -> a.getName().equals(key))
                .findFirst().orElse(null) != null;
    }

    public int getArgumentIndex(@NotNull String key) {
        try {
            FPCommandOption option = FPCommandOption.valueOf(key.toUpperCase());
            return this.argumentList.indexOf(option);
        } catch (IllegalArgumentException ignored) {
            return -1;
        }
    }

    public @Nullable String getExecutor(@NotNull Object object) {
        if (object instanceof FPInvocation invocation) {
            return invocation.source().getName();
        } else if (object instanceof SlashCommandInteractionEvent event) {
            Member member = event.getMember();
            if (member != null) {
                String nickname = member.getNickname();
                FPPlayer fpPlayer = FPPlayer.get(nickname);
                if (fpPlayer != null && Objects.equals(fpPlayer.getDiscordId(), member.getId()))
                    return nickname;
            }
        }
        return null;
    }

    public @Nullable String getUsername(@NotNull Object object, @NotNull String key) {
        String nickname = null;
        if (object instanceof FPInvocation invocation) {
            int index = getArgumentIndex(key);
            if (invocation.arguments().length > index && index >= 0) {
                try {
                    nickname = invocation.arguments()[index];
                } catch (NumberFormatException ignored) {}
            }
        } else if (object instanceof SlashCommandInteractionEvent event) {
            OptionMapping option = event.getOption(key);
            if (option != null){
                String data = null;
                try {
                    data = option.getAsString();
                } catch (IllegalStateException ignored) {}
                if (data != null) {
                    if (data.contains("@") && data.length() > 16) {
                        data = data.replaceAll("[<@>]", "");
                        FPPlayer player = FPPlayer.getWithDiscordId(data);
                        if (player != null)
                            nickname = player.getUsername();
                    } else nickname = data;
                }
            }
        }
        return nickname;
    }

    public @Nullable String getAnyString(@NotNull Object object, @NotNull String key) {
        String data = null;
        if (object instanceof FPInvocation invocation) {
            int index = getArgumentIndex(key);
            if (invocation.arguments().length > index && index >= 0) {
                try {
                    data = invocation.arguments()[index];
                } catch (NumberFormatException ignored) {}
            }
        } else if (object instanceof SlashCommandInteractionEvent event) {
            OptionMapping option = event.getOption(key);
            if (option != null){
                try {
                    data = option.getAsString();
                } catch (IllegalStateException ignored) {}
            }
        }
        return data;
    }

    public @Nullable Integer getAnyInteger(@NotNull Object object, @NotNull String key) {
        Long data = getAnyNumber(object, key);
        if (data != null) {
            try {
                return Math.toIntExact(data);
            } catch (ArithmeticException ignored) {}
        }
        return null;
    }

    public @Nullable Long getAnyNumber(@NotNull Object object, @NotNull String key) {
        Long data = null;
        if (object instanceof FPInvocation invocation) {
            int index = getArgumentIndex(key);
            if (invocation.arguments().length > index && index >= 0) {
                try {
                    data = Long.parseLong(invocation.arguments()[index]);
                } catch (NumberFormatException ignored) {}
            }
        } else if (object instanceof SlashCommandInteractionEvent event) {
            OptionMapping option = event.getOption(key);
            if (option != null){
                try {
                    data = option.getAsLong();
                } catch (IllegalStateException ignored) {}
            }
        }
        return data;
    }

    public static @NotNull String reasonFormatting(int startsFrom, @NotNull String... arguments) {
        List<String> reasonList = new ArrayList<>(Arrays.asList(arguments));
        reasonList.subList(0, startsFrom).clear();
        return String.join(" ", reasonList);
    }

    public @Nullable String getReason(@NotNull Object object) {
        FPCommandOption msOption = FPCommandOption.REASON;
        String text = null;
        if (argumentList.contains(msOption)) {
            if (object instanceof FPInvocation invocation) {
                int index = this.argumentList.size() - 1;
                if (invocation.arguments().length > index) {
                    text = reasonFormatting(index, invocation.arguments());
                }
            } else if (object instanceof SlashCommandInteractionEvent event) {
                OptionMapping option = event.getOption(msOption.getName());
                if (option != null)
                    text = option.getAsString();
            }
        }
        if (text != null && text.length() > 250) {
            text = text.substring(0, 251);
        }
        return text;
    }

    public @Nullable Long getTime(@NotNull Object object) {
        String timeString = getAnyString(object, "time");
        if (timeString != null) {
            long timestamp = FelmonUtils.Time.parseTime(timeString);
            if (timestamp > 0) {
                return FelmonUtils.Time.currentTime() + timestamp + 60000L;
            }
        }
        return null;
    }
}
