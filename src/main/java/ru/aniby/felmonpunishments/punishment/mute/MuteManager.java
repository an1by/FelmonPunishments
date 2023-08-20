package ru.aniby.felmonpunishments.punishment.mute;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.database.MySQL;
import ru.aniby.felmonpunishments.punishment.RevokedPunishment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MuteManager {
    @Getter
    private static final List<Mute> playerMutes = new ArrayList<>();

    public static boolean isMuted(@NotNull String username) {
        return getPlayerMutes().stream().filter(b -> b.getIntruder().equalsIgnoreCase(username)).findFirst().orElse(null) != null;
    }

    public static boolean put(Mute mute) {
        mute.save();
        if (mute.getId() <= 0)
            return false;
        return putNoSafe(mute);
    }

    public static boolean remove(int id) {
        return MuteManager.getPlayerMutes().removeIf(m -> m.getId() == id);
    }

    public static boolean putNoSafe(Mute mute) {
        return playerMutes.add(mute);
    }

    public static void load() {
        String query = "SELECT * FROM " + MySQL.Tables.getMutes() + " WHERE active = ?;";
        try {
            PreparedStatement preparedStmt = FelmonPunishments.getDatabaseConnection().prepareStatement(query);
            preparedStmt.setBoolean(1, true);

            ResultSet rs = preparedStmt.executeQuery();

            while (rs.next()) {
                String intruder = rs.getString("intruder");
                String admin = rs.getString("admin");
                String reason = rs.getString("reason");
                long timestamp = rs.getLong("expireTime");
                Mute mute = new Mute(intruder, admin, timestamp, reason, false);
                int id = rs.getInt("id");
                mute.setId(id);
                putNoSafe(mute);
            }
            rs.close();
            preparedStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void check() {
        for(int i = 0; i < MuteManager.getPlayerMutes().size(); i++){
            Mute mute = MuteManager.getPlayerMutes().get(i);
            if (mute.getRemainingTime() <= 0) {
                mute.revoke("Система");
                new RevokedPunishment(mute).notifyEverywhere();
            }
        }
    }

    public static void createTableIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS `" + MySQL.Tables.getMutes() + "` ( `id` INT NOT NULL AUTO_INCREMENT, `active` BOOLEAN NOT NULL DEFAULT TRUE, `intruder` VARCHAR(64) NOT NULL, `admin` VARCHAR(64) NOT NULL, `reason` LONGTEXT NOT NULL, `expireTime` BIGINT DEFAULT 0, PRIMARY KEY (`id`) );";
        try {
            Statement stmt = FelmonPunishments.getDatabaseConnection().createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
