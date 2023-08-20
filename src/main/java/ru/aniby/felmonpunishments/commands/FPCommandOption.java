package ru.aniby.felmonpunishments.commands;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum MSCommandOption {
    VICTIM("Пострадавший", String.class),
    NICKNAME("Никнейм игрока", String.class),
    PLAYER("Игрок", String.class),
    TIME("Длительность (Пример: 1d2h3m4s)", String.class),
    NUMBER("Номер", Integer.class),
    REASON("Причина", String.class),
    INTRUDER("Нарушитель", String.class);

    @Getter
    private final @NotNull String description;
    @Getter
    private final @NotNull Class<?> optionClass;

    public @NotNull String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    MSCommandOption(@NotNull String description, @NotNull Class<?> optionClass) {
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
    
    public OptionData getOptionData() {
        return (new OptionData(getOptionType(), getName(), getDescription())).setRequired(true);
    }
}
