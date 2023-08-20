package ru.aniby.felmonpunishments.commands.punishment;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.commands.FPArgumenter;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.commands.FPInvocation;
import ru.aniby.felmonpunishments.punishment.ban.Ban;
import ru.aniby.felmonpunishments.punishment.ban.BanManager;
import ru.aniby.felmonpunishments.punishment.mute.Mute;
import ru.aniby.felmonpunishments.punishment.mute.MuteManager;
import ru.aniby.felmonpunishments.punishment.warn.Warn;
import ru.aniby.felmonpunishments.punishment.warn.WarnManager;
import ru.aniby.felmonpunishments.utils.CommandUtils;
import ru.aniby.felmonpunishments.utils.TimeUtils;

public class PunishmentsCommand implements FPCommand {
    @Getter
    private final FPArgumenter argumenter = new FPArgumenter(
            "player"
    );

    @Override
    public boolean isDiscordAvailable() {
        return false;
    }

    @Getter
    private static final TextColor adminColor = TextColor.fromHexString("#fb2c2f");
    @Getter
    private static final TextColor timeColor = TextColor.fromHexString("#ffdb27");

    @Override
    public void execute(Object object) {
        if (!(object instanceof FPInvocation invocation))
            return;
        String executor = argumenter.getExecutor(invocation);
        if (!hasPermission(invocation))
            return;
        String intruder;
        if (invocation.arguments().length == 1) {
            if (!invocation.source().hasPermission(getPermission() + ".other"))
                return;
            intruder = argumenter.getAnyString(invocation, "player");
        }
        else if (executor != null && invocation.source() instanceof Player player) {
            intruder = player.getName();
        } else {
            intruder = null;
        }

        if (intruder == null) {
            CommandUtils.send(invocation, CommandUtils.Message.WRONG_ARGUMENTS);
            return;
        }
        Component component = Component.text("Наказания игрока " + intruder, NamedTextColor.GREEN);

        // Ban
        Ban ban = BanManager.playerBans.stream().filter(
                b -> b.getIntruder().equals(intruder)
        ).findFirst().orElse(null);
        component = component.append(
                Component.text("\nБан: ", NamedTextColor.AQUA)
        );
        if (ban != null) {
            if (ban.getExpireTime() > 0L) {
                component = component.append(
                        Component.text("до " + TimeUtils.toDisplay(ban.getExpireTime()) + " ", timeColor)
                );
            }
            component = component.append(
                    Component.text("от ", NamedTextColor.WHITE)
            ).append(
                    Component.text(ban.getAdmin(), adminColor)
            );
        } else {
            component = component.append(
                    Component.text("нет", NamedTextColor.WHITE)
            );
        }

        // Warns
        component = component.append(
                Component.text("\nВарны: ", NamedTextColor.AQUA)
        );
        int warnCount = 0;
        for (Warn warn : WarnManager.getPlayerWarns()) {
            if (warn.getIntruder().equals(intruder)) {
                warnCount += 1;
                component = component.append(
                        Component.text("\n#" + warn.getId(), warn.getType().getTextColor())
                ).append(
                        Component.text(" от ", NamedTextColor.WHITE)
                ).append(
                        Component.text(warn.getAdmin(), adminColor)
                );
            }
        }
        if (warnCount == 0) {
            component = component.append(
                    Component.text("нет", NamedTextColor.WHITE)
            );
        }

        // Mute
        Mute mute = MuteManager.getPlayerMutes().stream().filter(
                b -> b.getIntruder().equals(intruder)
        ).findFirst().orElse(null);
        component = component.append(
                Component.text("\nМут: ", NamedTextColor.AQUA)
        );
        if (mute != null) {
            component = component.append(
                    Component.text("до " + TimeUtils.toDisplay(mute.getExpireTime()), timeColor)
            ).append(
                    Component.text(" от ", NamedTextColor.WHITE)
            ).append(
                    Component.text(mute.getAdmin(), adminColor)
            );
        } else {
            component = component.append(
                    Component.text("нет", NamedTextColor.WHITE)
            );
        }

        // Message
        invocation.source().sendMessage(component);
    }

    @Override
    public @NotNull String getName() {
        return "punishments";
    }
}
