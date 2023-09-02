package ru.aniby.felmonpunishments.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class FPInvocation {
    private final @NotNull CommandSender source;
    private final @NotNull Command command;
    private final @NotNull String label;
    private final String[] arguments;

    public FPInvocation(@NotNull CommandSender source, @NotNull Command command, @NotNull String label, String[] arguments) {
        this.source = source;
        this.command = command;
        this.label = label;
        this.arguments = arguments;
    }

    public @NotNull CommandSender source() {
        return source;
    };
    public @NotNull Command command() {
        return command;
    };
    public @NotNull String label() {
        return label;
    };
    public String[] arguments() {
        return arguments;
    };
}
