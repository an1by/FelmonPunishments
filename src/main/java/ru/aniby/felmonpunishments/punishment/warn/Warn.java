package ru.aniby.felmonpunishments.punishment.warn;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.aniby.felmonapi.FelmonUtils;
import ru.aniby.felmonpunishments.configuration.FPMainConfig;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.commands.punishment.PunishmentsCommand;
import ru.aniby.felmonpunishments.punishment.Punishment;
import ru.aniby.felmonpunishments.punishment.PunishmentType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Warn extends Punishment {
    @Getter
    @Setter
    private String victim;
    @Getter
    @Setter
    private long startTime;

    @Override
    public PunishmentType getType() {
        if (this.getExpireTime() <= 0L)
            return PunishmentType.PERMANENT_WARN;
        if (this.getVictim() == null)
            return PunishmentType.TEMPORARY_WARN;
        if (this.getStartTime() > 0L)
            return PunishmentType.FINE;
        return PunishmentType.TICKET;
    }

    public static Warn createPermanentWarn(
            @NotNull String intruder, @NotNull String admin,
            @NotNull String reason, boolean save) {
        return new Warn(intruder, admin,
                0L, 0L, null, reason, save
        );
    }

    public static Warn createTemporaryWarn(
            @NotNull String intruder, @NotNull String admin,
            long expireTime, @NotNull String reason, boolean save) {
        return new Warn(intruder, admin,
                0L, expireTime, null, reason, save
        );
    }

    public static Warn createTicket(
            @NotNull String intruder, @NotNull String admin,
            long expireTime, @Nullable String victim, @NotNull String reason, boolean save) {
        return new Warn(intruder, admin,
                -1L, expireTime, victim, reason, save
        );
    }

    public static Warn createFine(
            @NotNull String intruder, @NotNull String admin,
            long expireTime, @Nullable String victim, @NotNull String reason, boolean save) {
        return new Warn(intruder, admin,
                FelmonUtils.Time.currentTime(), expireTime, victim, reason, save
        );
    }

    public Warn(@NotNull String intruder, @NotNull String admin,
                long startTime, long expireTime, @Nullable String victim, @NotNull String reason, boolean save) {
        super(intruder, admin, expireTime, reason);
        this.startTime = startTime;
        this.victim = victim;
        if (save)
            WarnManager.put(this);
        else
            WarnManager.putNoSafe(this);
    }

    public long getTotalTime() {
        return this.getStartTime() >= 100L && this.getExpireTime() > this.getStartTime()
                ? this.getExpireTime() - this.getStartTime()
                : 0L;
    }

    @Override
    public int searchInDatabase() {
        if (this.getId() > 0)
            return this.getId();
        String str = "SELECT id FROM " + FPMainConfig.MySQL.Tables.warns + " WHERE intruder=? AND admin=? AND victim=? AND reason=? LIMIT 1";
        int id = -1;
        try {
            PreparedStatement preparedStmt = FelmonPunishments.getDatabaseConnection().prepareStatement(str);
            preparedStmt.setString(1, this.getIntruder());
            preparedStmt.setString(2, this.getAdmin());
            preparedStmt.setString(3, this.getVictim());
            preparedStmt.setString(4, this.getReason());

            // executing SELECT query
            ResultSet rs = preparedStmt.executeQuery();

            while (rs.next()) {
                int new_id = rs.getInt(1);
                if (new_id > 0)
                    id = new_id;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return id;
    }

    @Override
    public int save() {
        int id = searchInDatabase();
        try {
            if (id <= 0) {
                String str = "INSERT INTO " + FPMainConfig.MySQL.Tables.warns + " (intruder, admin, victim, reason, startTime, expireTime) VALUES  (?, ?, ?, ?, ?, ?);";
                PreparedStatement preparedStmt = FelmonPunishments.getDatabaseConnection().prepareStatement(str, Statement.RETURN_GENERATED_KEYS);
                preparedStmt.setString(1, this.getIntruder());
                preparedStmt.setString(2, this.getAdmin());
                preparedStmt.setString(3, this.getVictim());
                preparedStmt.setString(4, this.getReason());
                preparedStmt.setLong(5, this.getStartTime());
                preparedStmt.setLong(6, this.getExpireTime());

                preparedStmt.execute();
                ResultSet rs = preparedStmt.getGeneratedKeys();
                if (rs.next()){
                    id = rs.getInt(1);
                    this.setId(id);
                }

                preparedStmt.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return id;
    }

    @Override
    public void revoke(String revokedBy) {
        try {
            String str = "UPDATE " + FPMainConfig.MySQL.Tables.warns + " SET active = ? WHERE id = ?;";

            PreparedStatement preparedStmt = FelmonPunishments.getDatabaseConnection().prepareStatement(str);
            preparedStmt.setBoolean(1, false);
            preparedStmt.setInt(2, this.getId());

            preparedStmt.execute();
            preparedStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        WarnManager.remove(this);
        this.setRevokedBy(revokedBy);
    }

    @Override
    public Component getPunishmentMessage() {
        Component component = Component.text(this.getAdmin(), PunishmentsCommand.getAdminColor()).append(
                Component.text(this.getIntruder(), NamedTextColor.GREEN)
        ).append(
                Component.text("выдал вам предупреждение!", NamedTextColor.AQUA)
        ).append(
                Component.text("\nПричина: ", NamedTextColor.AQUA)
        ).append(
                Component.text(this.getReason(), NamedTextColor.WHITE)
        );
        if (this.getType() == PunishmentType.FINE) {
            component = component.append(
                    Component.text("\nПострадавший: ", NamedTextColor.AQUA)
            ).append(
                    Component.text(this.getVictim(), NamedTextColor.GREEN)
            );
        }
        if (this.getType() == PunishmentType.TEMPORARY_WARN || this.getType() == PunishmentType.FINE) {
            component = component.append(
                    Component.text("\nИстекает: ", NamedTextColor.AQUA)
            ).append(
                    Component.text(FelmonUtils.Time.toDisplay(this.getExpireTime()), PunishmentsCommand.getTimeColor())
            );
        }
        return component;
    }

    @Override
    public Component getHoverMessage() {
        Component component = Component.text("Нарушитель ", NamedTextColor.GOLD).append(
                Component.text(this.getIntruder(), NamedTextColor.GREEN)
        ).append(
                Component.text("\nВыдал предупреждение: ", NamedTextColor.AQUA)
        ).append(
                Component.text(this.getAdmin(), PunishmentsCommand.getAdminColor())
        ).append(
                Component.text("\nПричина: ", NamedTextColor.AQUA)
        ).append(
                Component.text(this.getReason(), NamedTextColor.WHITE)
        );
        if (this.getType() == PunishmentType.FINE) {
            component = component.append(
                    Component.text("\nПострадавший: ", NamedTextColor.AQUA)
            ).append(
                    Component.text(this.getVictim(), NamedTextColor.GREEN)
            );
        }
        if (this.getType() == PunishmentType.TEMPORARY_WARN || this.getType() == PunishmentType.FINE) {
            component = component.append(
                    Component.text("\nИстекает: ", NamedTextColor.AQUA)
            ).append(
                    Component.text(FelmonUtils.Time.toDisplay(this.getExpireTime()), PunishmentsCommand.getTimeColor())
            );
        }
        return component;
    }
}
