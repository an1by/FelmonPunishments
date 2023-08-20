package ru.aniby.felmonpunishments.commands.offense;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.commands.FPInvocation;
import ru.aniby.felmonpunishments.punishment.warn.Warn;
import ru.aniby.felmonpunishments.utils.CommandUtils;
import ru.aniby.felmonpunishments.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class TicketCommand implements FPCommand {
    @Getter
    private final FPArgumenter argumenter = new FPArgumenter(
            "intruder", "victim", "time", "reason"
    );

    @Override
    public boolean isDiscordAvailable() {
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "ticket";
    }

    @Override
    public void execute(Object object) {
        if (!(object instanceof FPInvocation invocation))
            return;
        String executor = argumenter.getExecutor(invocation);
        if (executor == null || !hasPermission(invocation))
            return;
        String intruder = argumenter.getAnyString(invocation, "intruder");
        String victim = argumenter.getAnyString(invocation, "victim");
        Long time = argumenter.getTime(invocation);
        String reason = argumenter.getReason(invocation);
        if (intruder == null || victim == null || time == null || reason == null) {
            CommandUtils.send(invocation, CommandUtils.Message.WRONG_ARGUMENTS);
            return;
        }
        if (time < TimeUtils.currentTime() + TimeUtils.day) {
            CommandUtils.send(invocation, CommandUtils.Message.DAY_OR_MORE);
            return;
        }
        

        // Execute
        Warn warn = Warn.createTicket(
                intruder, executor, time, victim, reason, true
        );
        if (warn.getId() <= 0) {
            CommandUtils.send(invocation, CommandUtils.Message.EXISTS_SIMILAR);
            return;
        }

        // Mesage
        warn.notifyEverywhere();

        CommandUtils.send(invocation, "&aВы успешно выписали штраф игроку " + intruder + "!");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 0 -> list = CommandUtils.Completer.players("");
            case 1 -> list = CommandUtils.Completer.players(args[0]);
            case 2 -> list = CommandUtils.Completer.players(args[1]);
            case 3 -> list = CommandUtils.Completer.time(args[2]);
            default -> list.add("[Причина и условия снятия]");
        }
        return list;
    }
}
