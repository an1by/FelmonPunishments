package ru.aniby.felmonpunishments.commands.punishment;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.punishment.warn.Warn;
import ru.aniby.felmonpunishments.utils.CommandUtils;

public class WarnCommand implements FPCommand {
    @Getter
    private final FPArgumenter argumenter = new FPArgumenter(
            "intruder", "reason"
    );
    public final @NotNull String getName() {
        return "warn";
    }

    @Override
    public void execute(Object object) {

        if (!isRightObject(object))
            return;

        String executor = argumenter.getExecutor(object);
        if (executor == null || !hasPermission(object))
            return;
        String intruder = argumenter.getAnyString(object, "intruder");
        String reason = argumenter.getReason(object);
        if (intruder == null || reason == null) {
            CommandUtils.send(object, CommandUtils.Message.WRONG_ARGUMENTS);
            return;
        }

//        if (!CommandUtils.canPunish(executor, intruder)) {
//            CommandUtils.send(object, CommandUtils.Message.CANT_PUNISH);
//            return;
//        }

        // Execute
        Warn warn = Warn.createPermanentWarn(intruder, executor, reason, true);
        if (warn.getId() <= 0) {
            CommandUtils.send(object, CommandUtils.Message.EXISTS_SIMILAR);
            return;
        }

        // Mesage
        warn.notifyEverywhere();

        //CommandUtils.checkWarns(server, intruder);

        CommandUtils.send(object, "&aВы успешно выдали предупреждение игроку " + intruder + "!");
    }
}
