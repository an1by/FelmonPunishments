package ru.aniby.felmonpunishments.commands.punishment;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.configuration.FPMessagesConfig;
import ru.aniby.felmonpunishments.punishment.RevokedPunishment;
import ru.aniby.felmonpunishments.punishment.mute.Mute;
import ru.aniby.felmonpunishments.punishment.mute.MuteManager;
import ru.aniby.felmonpunishments.utils.CommandUtils;

import java.util.ArrayList;
import java.util.List;

public class UnmuteCommand implements FPCommand {
    @Override
    public @NotNull String getName() {
        return "unmute";
    }
    @Getter
    private final FPArgumenter argumenter = new FPArgumenter(
            "intruder"
    );

    @Override
    public void execute(Object object) {
        if (!isRightObject(object))
            return;

        String executor = argumenter.getExecutor(object);
        if (executor == null || !hasPermission(object))
            return;
        String intruder = argumenter.getUsername(object, "intruder");
        if (intruder == null) {
            CommandUtils.send(object, FPMessagesConfig.wrongArguments);
            return;
        }

        // Execute
        Mute mute = MuteManager.getPlayerMutes().stream().filter(
                b -> b.getIntruder().equals(intruder)
        ).findFirst().orElse(null);
        if (mute == null) {
            CommandUtils.send(object, FPMessagesConfig.punishmentNotExists);
            return;
        }
        mute.revoke(executor);

        // Mesage
        new RevokedPunishment(mute).notifyEverywhere();

        CommandUtils.send(object, "&aВы успешно сняли мут с игрока " + intruder + "!");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 0 -> list = CommandUtils.punishmentPlayersCompleter(MuteManager.getPlayerMutes(), "");
            case 1 -> list = CommandUtils.punishmentPlayersCompleter(MuteManager.getPlayerMutes(), args[0]);
            default -> {}
        }
        return list;
    }
}
