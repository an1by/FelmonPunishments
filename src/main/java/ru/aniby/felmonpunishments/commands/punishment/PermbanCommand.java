package ru.aniby.felmonpunishments.commands.punishment;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.configuration.FPMessagesConfig;
import ru.aniby.felmonpunishments.configuration.FPPunishmentsConfig;
import ru.aniby.felmonpunishments.punishment.ban.Ban;
import ru.aniby.felmonpunishments.punishment.ban.BanManager;
import ru.aniby.felmonpunishments.utils.CommandUtils;

public class PermbanCommand implements FPCommand {
    @Getter
    private final FPArgumenter argumenter = new FPArgumenter(
            "intruder", "reason"
    );

    @Override
    public void execute(Object object) {
        if (!isRightObject(object))
            return;

        if (!FPPunishmentsConfig.Permban.enabled) {
            CommandUtils.send(object, FPMessagesConfig.disabledCommand);
            return;
        }

        String executor = argumenter.getExecutor(object);
        if (executor == null || !hasPermission(object))
            return;
        String intruder = argumenter.getUsername(object, "intruder");
        String reason = argumenter.getReason(object);
        if (intruder == null || reason == null) {
            CommandUtils.send(object, FPMessagesConfig.wrongArguments);
            return;
        }

//        if (!CommandUtils.canPunish(executor, intruder)) {
//            CommandUtils.send(object, CommandUtils.Message.CANT_PUNISH);
//            return;
//        }

        if (BanManager.getBan(intruder) != null) {
            CommandUtils.send(object, FPMessagesConfig.alreadyBanned);
            return;
        }

        Ban ban = new Ban(intruder, executor, reason, 0L, true);
        if (ban.getId() <= 0) {
            CommandUtils.send(object, FPMessagesConfig.existsSimilar);
            return;
        }

        // Mesage
        ban.kickIntruder();
        ban.notifyEverywhere();
        CommandUtils.send(object, "&aВы успешно забанили игрока " + intruder + "!");
    }

    @Override
    public @NotNull String getName() {
        return "permban";
    }
}
