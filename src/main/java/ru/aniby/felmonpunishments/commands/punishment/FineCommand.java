package ru.aniby.felmonpunishments.commands.punishment;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.punishment.warn.Warn;
import ru.aniby.felmonpunishments.utils.CommandUtils;
import ru.aniby.felmonpunishments.utils.TimeUtils;

public class FineCommand implements FPCommand {
    @Getter
    private final FPArgumenter argumenter = new FPArgumenter(
            "intruder", "victim", "time", "reason"
    );

    @Override
    public @NotNull String getName() {
        return "fine";
    }

    @Override
    public void execute(Object object) {
        if (!isRightObject(object))
            return;
        String executor = argumenter.getExecutor(object);
        if (executor == null || !hasPermission(object))
            return;
        String intruder = argumenter.getAnyString(object, "intruder");
        String victim = argumenter.getAnyString(object, "victim");
        Long time = argumenter.getTime(object);
        String reason = argumenter.getReason(object);
        if (intruder == null || victim == null || time == null || reason == null) {
            CommandUtils.send(object, CommandUtils.Message.WRONG_ARGUMENTS);
            return;
        }

//        if (!CommandUtils.canPunish(executor, intruder)) {
//            CommandUtils.send(object, CommandUtils.Message.CANT_PUNISH);
//            return;
//        }

        if (time < TimeUtils.currentTime() + TimeUtils.day) {
            CommandUtils.send(object, CommandUtils.Message.DAY_OR_MORE);
            return;
        }

        // Execute
        Warn warn = Warn.createFine(
                intruder, executor, time, victim, reason, true
        );
        if (warn.getId() <= 0) {
            CommandUtils.send(object, CommandUtils.Message.EXISTS_SIMILAR);
            return;
        }

        // Mesage
        warn.notifyEverywhere();

        CommandUtils.send(object, "&aВы успешно выписали возобновляемый штраф игроку " + intruder + "!");
    }
}
