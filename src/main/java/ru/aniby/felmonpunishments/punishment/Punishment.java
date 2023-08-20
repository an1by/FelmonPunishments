package ru.aniby.felmonpunishments.punishment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.database.DatabaseHolderInterface;
import ru.aniby.felmonpunishments.punishment.ban.Ban;
import ru.aniby.felmonpunishments.punishment.mute.Mute;
import ru.aniby.felmonpunishments.punishment.warn.Warn;
import ru.aniby.felmonpunishments.utils.CommandUtils;
import ru.aniby.felmonpunishments.utils.TextUtils;
import ru.aniby.felmonpunishments.utils.TimeUtils;

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
        return this.getExpireTime() - TimeUtils.currentTime();
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
                victim = TextUtils.formatForDiscord(warn.getVictim());
            }
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(
                        String.format("%s выдал вам %s", TextUtils.formatForDiscord(getAdmin()), uniqueType),
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
                victim = TextUtils.formatForDiscord(warn.getVictim());
            }
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(
                        String.format("%s получил %s", TextUtils.formatForDiscord(getIntruder()), uniqueType),
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

        embedBuilder = embedBuilder.addField("Выдал", TextUtils.formatForDiscord(getAdmin()), true);

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


    @Override
    public @NotNull JsonObject toJSON() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("intruder", this.getIntruder());
        jsonObject.addProperty("admin", this.getAdmin());
        jsonObject.addProperty("reason", this.getReason());
        if (this.getExpireTime() > 0L)
            jsonObject.addProperty("expireTime", this.getExpireTime());
        if (this.getRevokedBy() != null)
            jsonObject.addProperty("revokedBy", this.getRevokedBy());
        return jsonObject;
    }

    public static @Nullable Punishment parseJSON(JsonObject jsonObject) {
        JsonElement intruder = jsonObject.get("intruder");
        JsonElement admin = jsonObject.get("admin");
        JsonElement reason = jsonObject.get("reason");
        if (intruder == null || admin == null || reason == null) {
            return null;
        }
        JsonElement expireTimeElement = jsonObject.get("expireTime");
        long expireTime = expireTimeElement == null ? 0L : expireTimeElement.getAsLong();

        Punishment punishment = new Punishment(intruder.getAsString(), admin.getAsString(), expireTime, reason.getAsString());

        JsonElement revokedByElement = jsonObject.get("expireTime");
        punishment.setRevokedBy(revokedByElement == null ? null : revokedByElement.getAsString());

        return punishment;
    }
}
