package ru.aniby.felmonpunishments.commands.punishment;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.punishment.mute.Mute;
import ru.aniby.felmonpunishments.punishment.mute.MuteManager;
import ru.aniby.felmonpunishments.utils.CommandUtils;

public class MuteCommand implements FPCommand {
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

        if (MuteManager.isMuted(intruder)) {
            CommandUtils.send(object, CommandUtils.Message.NOW_MUTED);
            return;
        }

        // Execute
        Mute mute = new Mute(intruder, executor, time, reason, true);
        if (mute.getId() <= 0) {
            CommandUtils.send(object, CommandUtils.Message.EXISTS_SIMILAR);
            return;
        }

        // Mesage
        mute.notifyEverywhere();
        CommandUtils.send(object, "&aВы успешно выдали мут игроку " + intruder + "!");
    }


    @Override
    public @NotNull String getName() {
        return "mute";
    }
}
