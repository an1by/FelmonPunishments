package ru.aniby.felmonpunishments.commands.punishment;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.punishment.RevokedPunishment;
import ru.aniby.felmonpunishments.punishment.ban.Ban;
import ru.aniby.felmonpunishments.punishment.ban.BanManager;
import ru.aniby.felmonpunishments.utils.CommandUtils;

import java.util.ArrayList;
import java.util.List;

public class UnbanCommand implements FPCommand {
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

        String intruder = argumenter.getAnyString(object, "intruder");
        if (intruder == null) {
            CommandUtils.send(object, CommandUtils.Message.WRONG_ARGUMENTS);
            return;
        }

        // Execute
        Ban foundedBan = BanManager.getBan(intruder);
        if (foundedBan == null) {
            CommandUtils.send(object, CommandUtils.Message.PUNISHMENT_NOT_EXISTS);
            return;
        }
        foundedBan.revoke(executor);

        // Mesage
        new RevokedPunishment(foundedBan).notifyEverywhere();

        CommandUtils.send(object, "&aВы успешно разбанили игрока " + intruder + "!");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 0 -> list = CommandUtils.Completer.punishmentPlayers(BanManager.getPlayerBans(), "");
            case 1 -> list = CommandUtils.Completer.punishmentPlayers(BanManager.getPlayerBans(), args[0]);
            default -> {}
        }
        return list;
    }

    @Override
    public @NotNull String getName() {
        return "unban";
    }
}
