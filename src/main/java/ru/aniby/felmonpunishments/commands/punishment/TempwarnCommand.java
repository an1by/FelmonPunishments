package ru.aniby.felmonpunishments.commands.punishment;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.punishment.warn.Warn;
import ru.aniby.felmonpunishments.utils.CommandUtils;

import java.util.ArrayList;
import java.util.List;

public class TempwarnCommand implements FPCommand {
    @Getter
    private final FPArgumenter argumenter = new FPArgumenter(
            "intruder", "time", "reason"
    );

    @Override
    public void execute(Object object) {
        if (!isRightObject(object))
            return;
        String executor = argumenter.getExecutor(object);
        if (executor == null || !hasPermission(object))
            return;
        String intruder = argumenter.getAnyString(object, "intruder");
        Long time = argumenter.getTime(object);
        String reason = argumenter.getReason(object);
        if (intruder == null || reason == null || time == null) {
            CommandUtils.send(object, CommandUtils.Message.WRONG_ARGUMENTS);
            return;
        }

//        if (!CommandUtils.canPunish(executor, intruder)) {
//            CommandUtils.send(object, CommandUtils.Message.CANT_PUNISH);
//            return;
//        }

        // Execute
        Warn warn = Warn.createTemporaryWarn(intruder, executor, time, reason, true);
        if (warn.getId() <= 0) {
            CommandUtils.send(object, CommandUtils.Message.EXISTS_SIMILAR);
            return;
        }

        // Mesage
        warn.notifyEverywhere();

        //CommandUtils.checkWarns(server, intruder);

        CommandUtils.send(object, "&aВы успешно выдали временное предупреждение игроку " + intruder + "!");
   }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 0 -> list = CommandUtils.Completer.players("");
            case 1 -> list = CommandUtils.Completer.players(args[0]);
            case 2 -> list = CommandUtils.Completer.time(args[1]);
            default -> list.add("[Причина]");
        }
        return list;
    }


    @Override
    public @NotNull String getName() {
        return "tempwarn";
    }
}
