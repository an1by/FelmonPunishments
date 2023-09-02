package ru.aniby.felmonpunishments.punishment;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonapi.FelmonUtils;
import ru.aniby.felmonapi.database.DatabaseHolderInterface;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.punishment.ban.Ban;
import ru.aniby.felmonpunishments.punishment.mute.Mute;
import ru.aniby.felmonpunishments.punishment.warn.Warn;
import ru.aniby.felmonpunishments.utils.CommandUtils;

public class Punishment implements DatabaseHolderInterface {
    @Getter
    @Setter
    private int id = -1;
    @Getter
    private final @NotNull String intruder;
    @Getter
    private final @NotNull String admin;
    @Getter
    @Setter
    private String revokedBy = null;
    @Getter
    private final long expireTime;
    public int getExpireTimeInSeconds() {
        return Math.round(this.expireTime / 1000f);
    }
    @Getter
    private final @NotNull String reason;

    public Punishment(@NotNull String intruder, @NotNull String admin, long expireTime, @NotNull String reason) {
        FelmonPunishments.getPunishedPlayers().add(intruder);
        this.intruder = intruder;
        this.admin = admin;
        this.expireTime = expireTime;
        this.reason = reason;
    }

    public Component getPunishmentMessage() {
        return null;
    }

    public Component getHoverMessage() {
        return null;
    }

    public void revoke(String revokedBy) {
        //this.remove();
        this.setRevokedBy(revokedBy);
    }

    public long getRemainingTime() {
        return this.getExpireTime() - FelmonUtils.Time.currentTime();
    }

    public PunishmentType getType() {
        if (this instanceof Ban ban)
            return ban.getType();

        if (this instanceof Warn warn)
            return warn.getType();

        if (this instanceof Mute mute)
            return mute.getType();

        return PunishmentType.UNKNOWN;
    }

    public Component getDirectComponent() {
        String executorType = this.getType() == PunishmentType.TICKET
                ? "Сотрудник"
                : "Администратор";
        String uniqueType = this.getType().getName();
        if (this instanceof Warn)
            uniqueType += " #" + this.getId();
        String text = String.format(
                "%s %s выдал вам %s!",
                executorType, this.getAdmin(), uniqueType
        );
        return Component.text(text, NamedTextColor.RED);

    }

    public EmbedBuilder getDirectEmbed() {
        String uniqueType = this.getType().getName();
        String victim = null;
        String reasonText = "Причина";
        if (this instanceof Warn warn) {
            uniqueType += String.format(" #%s", this.getId());
            if (warn.getVictim() != null) {
                reasonText = "Причина и условия снятия";
                victim = FelmonUtils.Text.formatForDiscord(warn.getVictim());
            }
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(
                        String.format("%s выдал вам %s", FelmonUtils.Text.formatForDiscord(getAdmin()), uniqueType),
                        null,
                        CommandUtils.getHead(getAdmin())
                ).addField(
                        reasonText, getReason(), false
                );

        if (this.getExpireTime() > 0L)
            embedBuilder = embedBuilder.addField("Истекает", "<t:" + getExpireTimeInSeconds() + ":R>", true);

        if (victim != null) {
            embedBuilder = embedBuilder.addField(
                    "Пострадавший", victim, true
            );
        }

        return embedBuilder.setColor(this.getType().getColor());
    }
    public EmbedBuilder getChannelEmbed() {
        String uniqueType = this.getType().getName();
        String victim = null;
        String reasonText = "Причина";
        if (this instanceof Warn warn) {
            uniqueType += String.format(" #%s", this.getId());
            if (warn.getVictim() != null) {
                reasonText = "Причина и условия снятия";
                victim = FelmonUtils.Text.formatForDiscord(warn.getVictim());
            }
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(
                        String.format("%s получил %s", FelmonUtils.Text.formatForDiscord(getIntruder()), uniqueType),
                        null,
                        CommandUtils.getHead(getIntruder())
                ).addField(
                        reasonText, getReason(), false
                );

        if (this.getExpireTime() > 0L)
            embedBuilder = embedBuilder.addField("Истекает", "<t:" + getExpireTimeInSeconds() + ":R>", true);

        if (victim != null) {
            embedBuilder = embedBuilder.addField(
                    "Пострадавший", victim, true
            );
        }

        embedBuilder = embedBuilder.addField("Выдал", FelmonUtils.Text.formatForDiscord(getAdmin()), true);

        return embedBuilder.setColor(this.getType().getColor());
    }

    public void notifyEverywhere() {
        CommandUtils.notifyInChannel(
                intruder, this.getType(), this.getChannelEmbed()
        );
        CommandUtils.notifyInDirect(
                intruder, this.getDirectComponent(), this.getDirectEmbed()
        );
    }
}
