package ru.aniby.felmonpunishments.punishment.ban;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonapi.FelmonUtils;
import ru.aniby.felmonpunishments.configuration.FPMainConfig;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.commands.punishment.PunishmentsCommand;
import ru.aniby.felmonpunishments.player.FPPlayer;
import ru.aniby.felmonpunishments.punishment.Punishment;
import ru.aniby.felmonpunishments.punishment.PunishmentType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Ban extends Punishment {
    @Override
    public PunishmentType getType() {
        return this.getExpireTime() > 0L ? PunishmentType.TEMPORARY_BAN : PunishmentType.PERMANENT_BAN;
    }

    public Ban(@NotNull String intruder, @NotNull String admin, @NotNull String reason, long expireTime, boolean save) {
        super(intruder, admin, expireTime, reason);
        if (save) {
            BanManager.put(this);
            changeRoles();
        } else {
            BanManager.putNoSafe(this);
        }
    }

    public void kickIntruder() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(FelmonPunishments.getInstance(), () -> {
            Player player = Bukkit.getPlayer(getIntruder());
            if (player != null)
                player.kick(getPunishmentMessage(), PlayerKickEvent.Cause.BANNED);
        }, 1L);
    }

    @Override
    public int searchInDatabase() {
        if (this.getId() > 0)
            return this.getId();
        String str = "SELECT id FROM " + FPMainConfig.MySQL.Tables.bans + " WHERE intruder=? AND admin=? AND reason=? AND expireTime=? LIMIT 1";
        int id = -1;
        try {
            PreparedStatement preparedStmt = FelmonPunishments.getDatabaseConnection().prepareStatement(str);
            preparedStmt.setString(1, this.getIntruder());
            preparedStmt.setString(2, this.getAdmin());
            preparedStmt.setString(3, this.getReason());
            preparedStmt.setLong(4, this.getExpireTime());

            // executing SELECT query
            ResultSet rs = preparedStmt.executeQuery();

            while (rs.next()) {
                int new_id = rs.getInt(1);
                if (new_id > 0)
                    id = new_id;
            }
            rs.close();
            preparedStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return id;
    }

    @Override
    public int save() {
        int id = searchInDatabase();
        if (id <= 0) {
            // Roles
            try {
                // MySQL
                String str = "INSERT INTO " + FPMainConfig.MySQL.Tables.bans + " (intruder, admin, reason, expireTime) VALUES  (?, ?, ?, ?);";
                PreparedStatement preparedStmt = FelmonPunishments.getDatabaseConnection().prepareStatement(str, Statement.RETURN_GENERATED_KEYS);
                preparedStmt.setString(1, this.getIntruder());
                preparedStmt.setString(2, this.getAdmin());
                preparedStmt.setString(3, this.getReason());
                preparedStmt.setLong(4, this.getExpireTime());

                preparedStmt.execute();
                ResultSet rs = preparedStmt.getGeneratedKeys();
                if (rs.next()) {
                    id = rs.getInt(1);
                    this.setId(id);
                }
                rs.close();
                preparedStmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return id;
    }

    public void changeRoles() {
        FPPlayer player = FPPlayer.get(this.getIntruder());
        if (this.getRevokedBy() == null)
            player.punishWithRole("ban");
        else
            player.revokePunishWithRole("ban");
    }

    @Override
    public void revoke(String revokedBy) {
        try {
            String str = "UPDATE " + FPMainConfig.MySQL.Tables.bans + " SET active = ? WHERE id = ?;";

            PreparedStatement preparedStmt = FelmonPunishments.getDatabaseConnection().prepareStatement(str);
            preparedStmt.setBoolean(1, false);
            preparedStmt.setInt(2, this.getId());

            preparedStmt.execute();
            preparedStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        BanManager.remove(this.getId());
        this.setRevokedBy(revokedBy);
        changeRoles();
    }

    @Override
    public Component getPunishmentMessage() {
        Component component = Component.text("Вы были забанены " + this.getAdmin() + "!", NamedTextColor.RED).append(
                Component.text("\nПричина: ", NamedTextColor.AQUA)
        ).append(
                Component.text(this.getReason(), NamedTextColor.WHITE)
        );
        if (this.getType() == PunishmentType.TEMPORARY_BAN) {
            component = component.append(
                    Component.text("\nИстекает: ", NamedTextColor.GREEN)
            ).append(
                    Component.text(FelmonUtils.Time.toDisplay(this.getExpireTime()), NamedTextColor.WHITE)
            );
        }
//        component = component.append(
//                Component.text("\n\nОплатить разбан (300р): ", NamedTextColor.GREEN)
//        ).append(
//                Component.text("https://qiwi.com/n/KOTOHACKER", NamedTextColor.WHITE)
//        );
        return component;
    }

    @Override
    public Component getHoverMessage() {
        Component component = Component.text("Нарушитель ", NamedTextColor.GOLD).append(
                Component.text(this.getIntruder(), NamedTextColor.GREEN)
        ).append(
                Component.text("\nВыдал бан: ", NamedTextColor.AQUA)
        ).append(
                Component.text(this.getAdmin(), PunishmentsCommand.getAdminColor())
        ).append(
                Component.text("\nПричина: ", NamedTextColor.AQUA)
        ).append(
                Component.text(this.getReason(), NamedTextColor.WHITE)
        );
        if (this.getType() == PunishmentType.TEMPORARY_BAN) {
            component = component.append(
                    Component.text("\nИстекает: ", NamedTextColor.AQUA)
            ).append(
                    Component.text(FelmonUtils.Time.toDisplay(this.getExpireTime()), PunishmentsCommand.getTimeColor())
            );
        }
        return component;
    }
}
