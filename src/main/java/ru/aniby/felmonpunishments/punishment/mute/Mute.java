package ru.aniby.felmonpunishments.punishment.mute;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.commands.punishment.PunishmentsCommand;
import ru.aniby.felmonpunishments.database.MySQL;
import ru.aniby.felmonpunishments.player.FPPlayer;
import ru.aniby.felmonpunishments.punishment.Punishment;
import ru.aniby.felmonpunishments.punishment.PunishmentType;
import ru.aniby.felmonpunishments.utils.TimeUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Mute extends Punishment {
    @Override
    public PunishmentType getType() {
        return PunishmentType.MUTE;
    }

    public Mute(@NotNull String intruder, @NotNull String admin,
                long expireTime, @NotNull String reason, boolean save) {
        super(intruder, admin, expireTime, reason);
        if (save) {
            MuteManager.put(this);
            changeRoles();
        } else {
            MuteManager.putNoSafe(this);
        }
    }

    @Override
    public int searchInDatabase() {
        if (this.getId() > 0)
            return this.getId();
        String str = "SELECT id FROM " + MySQL.Tables.getMutes() + " WHERE intruder=? AND admin=? AND reason=? AND expireTime=? LIMIT 1";
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

    public void changeRoles() {
        FPPlayer fpPlayer = FPPlayer.get(getIntruder());
        if (this.getRevokedBy() == null)
            fpPlayer.punishWithRole("mute");
        else
            fpPlayer.revokePunishWithRole("mute");
    }

    @Override
    public int save() {
        int id = searchInDatabase();
        try {
            if (id <= 0) {
                String str = "INSERT INTO " + MySQL.Tables.getMutes() + " (intruder, admin, reason, expireTime) VALUES  (?, ?, ?, ?);";
                PreparedStatement preparedStmt = FelmonPunishments.getDatabaseConnection().prepareStatement(str, Statement.RETURN_GENERATED_KEYS);
                preparedStmt.setString(1, this.getIntruder());
                preparedStmt.setString(2, this.getAdmin());
                preparedStmt.setString(3, this.getReason());
                preparedStmt.setLong(4, this.getExpireTime());

                preparedStmt.execute();
                ResultSet rs = preparedStmt.getGeneratedKeys();
                if (rs.next()){
                    id = rs.getInt(1);
                    this.setId(id);
                }
                rs.close();
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
            String str = "UPDATE " + MySQL.Tables.getMutes() + " SET active = ? WHERE id = ?;";

            PreparedStatement preparedStmt = FelmonPunishments.getDatabaseConnection().prepareStatement(str);
            preparedStmt.setBoolean(1, false);
            preparedStmt.setInt(2, this.getId());

            preparedStmt.execute();
            preparedStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        MuteManager.remove(this.getId());
        this.setRevokedBy(revokedBy);
        changeRoles();
    }

    public Component onChatMessage() {
        String text = String.format("&cУ вас мут! Истекает: %s", TimeUtils.toDisplay(this.getExpireTime()));
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    @Override
    public Component getPunishmentMessage() {
        return Component.text(this.getAdmin() + " выдал вам мут!", NamedTextColor.RED).append(
                Component.text("\nПричина: ", NamedTextColor.AQUA)
        ).append(
                Component.text(this.getReason(), NamedTextColor.WHITE)
        ).append(
                Component.text("\nИстекает: ", NamedTextColor.GREEN)
        ).append(
                Component.text(TimeUtils.toDisplay(this.getExpireTime()), NamedTextColor.WHITE)
        );
    }

    @Override
    public Component getHoverMessage() {
        Component component = Component.text("Нарушитель ", NamedTextColor.GOLD).append(
                Component.text(this.getIntruder(), NamedTextColor.GREEN)
        ).append(
                Component.text("\nВыдал мут: ", NamedTextColor.AQUA)
        ).append(
                Component.text(this.getAdmin(), PunishmentsCommand.getAdminColor())
        ).append(
                Component.text("\nПричина: ", NamedTextColor.AQUA)
        ).append(
                Component.text(this.getReason(), NamedTextColor.WHITE)
        ).append(
                Component.text("\nИстекает: ", NamedTextColor.AQUA)
        ).append(
                Component.text(TimeUtils.toDisplay(this.getExpireTime()), PunishmentsCommand.getTimeColor())
        );
        return component;
    }
}
