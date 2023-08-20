package ru.aniby.felmonpunishments.commands.punishment;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.punishment.PunishmentType;
import ru.aniby.felmonpunishments.punishment.RevokedPunishment;
import ru.aniby.felmonpunishments.punishment.warn.Warn;
import ru.aniby.felmonpunishments.punishment.warn.WarnManager;
import ru.aniby.felmonpunishments.utils.CommandUtils;

import java.util.ArrayList;
import java.util.List;

public class UnwarnCommand implements FPCommand {
    @Getter
    private final FPArgumenter argumenter = new FPArgumenter(
            "intruder", "number"
    );

    @Override
    public void execute(Object object) {
        if (!isRightObject(object))
            return;

        String executor = argumenter.getExecutor(object);
        if (executor == null || !hasPermission(object))
            return;

        String intruder = argumenter.getAnyString(object, "intruder");
        Integer number = argumenter.getAnyInteger(object, "number");
        if (intruder == null || number == null) {
            CommandUtils.send(object, CommandUtils.Message.WRONG_ARGUMENTS);
            return;
        }

        Warn warn = WarnManager.getPlayerWarns().stream().filter(
                w -> w.getId() == number && w.getType() != PunishmentType.TICKET
        ).findFirst().orElse(null);

        if (warn == null) {
            CommandUtils.send(object, CommandUtils.Message.PUNISHMENT_NOT_EXISTS);
            return;
        }
        // Execute
        warn.revoke(executor);

        // Message
        new RevokedPunishment(warn).notifyEverywhere();

        CommandUtils.send(object, "&aВы успешно сняли предупреждение с игрока " + intruder + "!");
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
                            && warn.getType() != PunishmentType.TICKET)
                        list.add(strId);
                }
            }
            default -> {}
        }
        return list;
    }

    @Override
    public @NotNull String getName() {
        return "unwarn";
    }
}
