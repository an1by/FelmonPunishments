package ru.aniby.felmonpunishments.commands.punishment;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonapi.FelmonUtils;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.configuration.FPMessagesConfig;
import ru.aniby.felmonpunishments.configuration.FPPunishmentsConfig;
import ru.aniby.felmonpunishments.punishment.warn.Warn;
import ru.aniby.felmonpunishments.utils.CommandUtils;

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

        if (!FPPunishmentsConfig.Fine.enabled) {
            CommandUtils.send(object, FPMessagesConfig.disabledCommand);
            return;
        }

        String executor = argumenter.getExecutor(object);
        if (executor == null || !hasPermission(object))
            return;
        String intruder = argumenter.getUsername(object, "intruder");
        String victim = argumenter.getUsername(object, "victim");
        Long time = argumenter.getTime(object);
        String reason = argumenter.getReason(object);
        if (intruder == null || victim == null || time == null || reason == null) {
            CommandUtils.send(object, FPMessagesConfig.wrongArguments);
            return;
        }

//        if (!CommandUtils.canPunish(executor, intruder)) {
//            CommandUtils.send(object, CommandUtils.Message.CANT_PUNISH);
//            return;
//        }

        if (time < FelmonUtils.Time.currentTime() + FelmonUtils.Time.day) {
            CommandUtils.send(object, FPMessagesConfig.dayOrMore);
            return;
        }

        // Execute
        Warn warn = Warn.createFine(
                intruder, executor, time, victim, reason, true
        );
        if (warn.getId() <= 0) {
            CommandUtils.send(object, FPMessagesConfig.existsSimilar);
            return;
        }

        // Mesage
        warn.notifyEverywhere();

        CommandUtils.send(object, "&aВы успешно выписали возобновляемый штраф игроку " + intruder + "!");
    }
}
