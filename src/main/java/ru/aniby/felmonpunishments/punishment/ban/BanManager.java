package ru.aniby.felmonpunishments.punishment.ban;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.database.MySQL;
import ru.aniby.felmonpunishments.punishment.RevokedPunishment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BanManager {
    @Getter
    public static List<Ban> playerBans = new ArrayList<>();

    public static Ban getBan(@NotNull String username) {
        return getPlayerBans().stream()
                .filter(b -> b.getIntruder().equalsIgnoreCase(username))
                .findFirst().orElse(null);
    }

    public static boolean put(Ban ban) {
        ban.save();
        if (ban.getId() <= 0 || playerBans.contains(ban))
            return false;
        return putNoSafe(ban);
    }

    public static boolean putNoSafe(Ban ban) {
        return playerBans.add(ban);
    }

    public static boolean remove(int id) {
        return playerBans.removeIf(b -> b.getId() == id);
    }

    public static void load() {
        String query = "SELECT * FROM " + MySQL.Tables.getBans() + " WHERE active = ?;";
        try {
            PreparedStatement preparedStmt = FelmonPunishments.getDatabaseConnection().prepareStatement(query);
            preparedStmt.setBoolean(1, true);

            ResultSet rs = preparedStmt.executeQuery();

            while (rs.next()) {
                String intruder = rs.getString("intruder");
                String admin = rs.getString("admin");
                String reason = rs.getString("reason");
                long timestamp = rs.getLong("expireTime");
                Ban ban = new Ban(intruder, admin, reason, timestamp, false);
                int id = rs.getInt("id");
                ban.setId(id);

                //putNoSafe(ban);
            }
            rs.close();
            preparedStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void check() {
        for (int i = 0; i < BanManager.playerBans.size(); i++){
            Ban ban = BanManager.playerBans.get(i);
            if (ban.getExpireTime() > 0L && ban.getRemainingTime() <= 0L) {
                ban.revoke("Система");
                new RevokedPunishment(ban).notifyEverywhere();
            }
        }
    }

    public static boolean createTableIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS `" + MySQL.Tables.getBans() + "` ( `id` INT NOT NULL AUTO_INCREMENT, `active` BOOLEAN NOT NULL DEFAULT TRUE, `intruder` VARCHAR(64) NOT NULL, `admin` VARCHAR(64) NOT NULL, `reason` LONGTEXT NOT NULL, `expireTime` BIGINT DEFAULT 0, PRIMARY KEY (`id`) );";
        try {
            Statement stmt = FelmonPunishments.getDatabaseConnection().createStatement();
            stmt.executeUpdate(query);
            stmt.close();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
