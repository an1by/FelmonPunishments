package ru.aniby.felmonpunishments.punishment;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.punishment.warn.Warn;
import ru.aniby.felmonpunishments.utils.CommandUtils;
import ru.aniby.felmonpunishments.utils.TextUtils;

import java.awt.*;

public record RevokedPunishment(@Getter @NotNull Punishment instance) {
    public static Color revokedTicketColor = Color.decode("#4912c9");
    public static TextColor revokedTicketTextColor = TextColor.fromHexString("#4912c9");
    public Component getDirectComponent() {
        String executorType = "Администратор";
        String uniqueType = this.instance.getType().getName();
        if (this.instance instanceof Warn) {
            uniqueType += " #" + this.instance.getId();

            if (this.instance.getType() == PunishmentType.TICKET) {
                executorType = "Сотрудник";
                if (this.instance.getRevokedBy().equalsIgnoreCase("Система")) {
                    return Component.text(
                            String.format("Вы просрочили %s от %s!", uniqueType, this.instance.getAdmin()),
                            revokedTicketTextColor
                    );
                }
            }
        }

        String text = String.format(
                "%s %s снял вам %s!",
                executorType, this.instance.getRevokedBy(), uniqueType
        );
        return Component.text(text, NamedTextColor.GREEN);

    }

    public EmbedBuilder getDirectEmbed() {
        String authorText = "%s, с вас сняли %s";
        String uniqueType = this.instance.getType().getName();
        boolean admin = true;
        if (this.instance instanceof Warn warn) {
            uniqueType += String.format(" #%s", this.instance.getId());
            if (warn.getType() == PunishmentType.TICKET) {
                if (warn.getRevokedBy().equalsIgnoreCase("Система")) {
                    authorText = "%s, вы просрочили %s";
                    admin = false;
                }
            }
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(
                        String.format(authorText, TextUtils.formatForDiscord(this.instance.getIntruder()), uniqueType),
                        null,
                        CommandUtils.getHead(this.instance.getAdmin())
                );

        if (!admin)
            return embedBuilder.setColor(revokedTicketColor);

        return embedBuilder.addField("Снял", TextUtils.formatForDiscord(this.instance.getRevokedBy()), true)
                .setColor(Color.GREEN);
    }

    public EmbedBuilder getChannelEmbed() {
        String authorText = "С %s сняли %s";
        String uniqueType = this.instance.getType().getName();
        boolean admin = true;
        if (this.instance instanceof Warn warn) {
            uniqueType += String.format(" #%s", this.instance.getId());
            if (warn.getType() == PunishmentType.TICKET) {
                if (warn.getRevokedBy().equalsIgnoreCase("Система")) {
                    authorText = "%s просрочил %s";
                    admin = false;
                }
            }
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(
                        String.format(authorText, TextUtils.formatForDiscord(this.instance.getIntruder()), uniqueType),
                        null,
                        CommandUtils.getHead(this.instance.getIntruder())
                );

        if (!admin)
            return embedBuilder.setColor(revokedTicketColor);

        return embedBuilder.addField("Снял", TextUtils.formatForDiscord(this.instance.getRevokedBy()), true)
                .setColor(Color.GREEN);
    }

    public void notifyEverywhere() {
        CommandUtils.notifyInChannel(
                this.instance.getIntruder(), this.instance.getType(), this.getChannelEmbed()
        );
        CommandUtils.notifyInDirect(
                this.instance.getIntruder(), this.getDirectComponent(), this.getDirectEmbed()
        );
    }
}
