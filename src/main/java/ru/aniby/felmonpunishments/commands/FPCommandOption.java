package ru.aniby.felmonpunishments.commands;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum FPCommandOption {
    VICTIM("Пострадавший", OfflinePlayer.class),
    NICKNAME("Никнейм игрока", String.class),
    PLAYER("Игрок", OfflinePlayer.class),
    TIME("Длительность (Пример: 1d2h3m4s)", String.class),
    NUMBER("Номер", Integer.class),
    REASON("Причина", String.class),
    INTRUDER("Нарушитель", OfflinePlayer.class);

    @Getter
    private final @NotNull String description;
    @Getter
    private final @NotNull Class<?> optionClass;

    public @NotNull String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    FPCommandOption(@NotNull String description, @NotNull Class<?> optionClass) {
        this.description = description;
        this.optionClass = optionClass;
    }

    public @NotNull OptionType getOptionType() {
        OptionType type = OptionType.STRING;
        if (optionClass.equals(Integer.class)) {
            type = OptionType.INTEGER;
        }
        return type;
    }

    public boolean isPlayerOption() {
        return optionClass.equals(OfflinePlayer.class);
    }
    
    public OptionData getOptionDataList() {
        return new OptionData(getOptionType(), getName(), getDescription()).setRequired(true);
    }
}
