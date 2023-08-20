package ru.aniby.felmonpunishments.commands.offense;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.commands.FPInvocation;
import ru.aniby.felmonpunishments.punishment.PunishmentType;
import ru.aniby.felmonpunishments.punishment.RevokedPunishment;
import ru.aniby.felmonpunishments.punishment.warn.Warn;
import ru.aniby.felmonpunishments.punishment.warn.WarnManager;
import ru.aniby.felmonpunishments.utils.CommandUtils;

import java.util.ArrayList;
import java.util.List;

public class UnticketCommand implements FPCommand {
    @Getter
    private final FPArgumenter argumenter = new FPArgumenter(
            "intruder", "number"
    );

    @Override
    public @NotNull String getName() {
        return "unticket";
    }

    @Override
    public boolean isDiscordAvailable() {
        return false;
    }

    @Override
    public void execute(Object object) {
        if (!(object instanceof FPInvocation invocation))
            return;

        String executor = argumenter.getExecutor(invocation);
        if (executor == null || !hasPermission(invocation))
            return;

        String intruder = argumenter.getAnyString(invocation, "intruder");
        Integer number = argumenter.getAnyInteger(invocation, "number");
        if (intruder == null || number == null) {
            CommandUtils.send(invocation, CommandUtils.Message.WRONG_ARGUMENTS);
            return;
        }

        Warn warn = WarnManager.getPlayerWarns().stream().filter(
                w -> w.getId() == number && w.getType() == PunishmentType.TICKET
        ).findFirst().orElse(null);

        if (warn == null) {
            CommandUtils.send(invocation, CommandUtils.Message.PUNISHMENT_NOT_EXISTS);
            return;
        }
        // Execute
        warn.revoke(executor);

        // Message
        new RevokedPunishment(warn).notifyEverywhere();

        CommandUtils.send(invocation, "&aВы успешно сняли штраф с игрока " + intruder + "!");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 0 -> list = CommandUtils.Completer.punishmentPlayers(WarnManager.getPlayerWarns(), "");
            case 1 -> list = CommandUtils.Completer.punishmentPlayers(WarnManager.getPlayerWarns(), args[0]);
            case 2 -> {
                for (Warn warn : WarnManager.getPerPlayerWarns(args[0])) {
                    String strId = String.valueOf(warn.getId());
                    if (strId.startsWith(args[1])
                            && warn.getIntruder().equals(args[0])
                            && warn.getType() == PunishmentType.TICKET)
                        list.add(strId);
                }
            }
            default -> {}
        }
        return list;
    }
}