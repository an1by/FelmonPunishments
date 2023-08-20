package ru.aniby.felmonpunishments.punishment;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Objects;


public enum PunishmentType {
    TICKET("штраф", Objects.requireNonNull(TextColor.fromHexString("#3f8aeb")), Color.decode("#3f8aeb")),
    PERMANENT_WARN("предупреждение", NamedTextColor.RED),
    TEMPORARY_WARN("временное предупреждение", NamedTextColor.GOLD),
    FINE("возобновляемый штраф", NamedTextColor.YELLOW),
    MUTE("мут", NamedTextColor.GRAY),
    TEMPORARY_BAN("временный бан", NamedTextColor.RED),
    PERMANENT_BAN("бессрочный бан", NamedTextColor.DARK_RED),
    UNKNOWN("неизвестное", NamedTextColor.BLACK);

    @Getter
    private final @NotNull String name;
    @Getter
    private final @NotNull TextColor textColor;
    private final @Nullable Color color;

    public Color getColor() {
        return color == null ? Color.decode(this.textColor.asHexString()) : color;
    }

    PunishmentType(@NotNull String name, @NotNull TextColor textColor) {
        this.name = name;
        this.textColor = textColor;
        this.color = null;
    }
    PunishmentType(@NotNull String name, @NotNull TextColor textColor, @Nullable Color color) {
        this.name = name;
        this.textColor = textColor;
        this.color = color;
    }
}