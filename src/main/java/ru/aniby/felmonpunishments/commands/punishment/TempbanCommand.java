package ru.aniby.felmonpunishments.commands.punishment;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.punishment.ban.Ban;
import ru.aniby.felmonpunishments.punishment.ban.BanManager;
import ru.aniby.felmonpunishments.utils.CommandUtils;

public class TempbanCommand implements FPCommand {
    @Getter
    private final FPArgumenter argumenter = new FPArgumenter(
            "intruder", "time", "reason"
    );

    @Override
    public @NotNull String getName() {
        return "tempban";
    }

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
        if (intruder == null || time == null || reason == null) {
            CommandUtils.send(object, CommandUtils.Message.WRONG_ARGUMENTS);
            return;
        }

//        if (!CommandUtils.canPunish(executor, intruder)) {
//            CommandUtils.send(object, CommandUtils.Message.CANT_PUNISH);
//            return;
//        }

        if (BanManager.getBan(intruder) != null) {
            CommandUtils.send(object, CommandUtils.Message.NOW_BANNED);
            return;
        }

        // Execute
        Ban ban = new Ban(intruder, executor, reason, time, true);
        if (ban.getId() <= 0) {
            CommandUtils.send(object, CommandUtils.Message.EXISTS_SIMILAR);
            return;
        }
        ban.kickIntruder();

        // Mesage
        ban.notifyEverywhere();

        CommandUtils.send(object, "&aВы успешно забанили игрока " + intruder + "!");
    }
}
