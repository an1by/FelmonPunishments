package ru.aniby.felmonpunishments.punishment.warn;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonapi.FelmonUtils;
import ru.aniby.felmonpunishments.configuration.FPMainConfig;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.punishment.RevokedPunishment;
import ru.aniby.felmonpunishments.punishment.ban.Ban;
import ru.aniby.felmonpunishments.punishment.mute.MuteManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WarnManager {
    @Getter
    private static final CopyOnWriteArrayList<Warn> playerWarns = new CopyOnWriteArrayList<>();

    public static boolean put(Warn warn) {
        warn.save();
        if (warn.getId() <= 0)
            return false;
        return putNoSafe(warn);
    }

    public static boolean remove(Warn warn) {
        return playerWarns.remove(warn);
    }

    public static boolean putNoSafe(Warn warn) {
        return playerWarns.add(warn);
    }

    public static List<Warn> getPerPlayerWarns(@NotNull String username) {
        return getPlayerWarns().stream().filter(
                w -> w.getIntruder().equalsIgnoreCase(username)
        ).toList();
    }

    public static void checkPerPlayer(@NotNull String username) {
        List<Warn> warns = getPerPlayerWarns(username);

        List<Warn> orange = new ArrayList<>();
        List<Warn> red = new ArrayList<>();
        for (Warn warn : warns) {
            switch (warn.getType()) {
                case FINE -> {
                    if (warn.getRemainingTime() <= 0) {
                        warn.revoke("Система");
                        Warn red_warn = Warn.createPermanentWarn(
                                username, "Система", String.format("Истечение штрафа (#%s)", warn.getId()),
                                true
                        );
                        red_warn.notifyEverywhere();
                        red.add(red_warn);

                        long time = FelmonUtils.Time.currentTime() + (warn.getTotalTime() <= 0L
                                ? FelmonUtils.Time.parseTime("1d")
                                : warn.getTotalTime());
                        Warn yellow_warn = Warn.createFine(
                                username, "Система", time, warn.getVictim(),
                                String.format("Возобновление штрафа (#%s)", warn.getId()), true);
                        yellow_warn.notifyEverywhere();
                    }
                }
                case TEMPORARY_WARN -> {
                    if (warn.getRemainingTime() <= 0) {
                        warn.revoke("Система");
                        new RevokedPunishment(warn).notifyEverywhere();
                    } else orange.add(warn);
                }
                case TICKET -> {
                    if (warn.getRemainingTime() <= 0) {
                        warn.revoke("Система");
                        new RevokedPunishment(warn).notifyEverywhere();
                    }
                }
                case PERMANENT_WARN -> red.add(warn);
            }
        }
        if (orange.size() >= 2) {
            String reasonEx = String.format("2 временных предупреждения (#%s, #%s)",
                    orange.get(0).getId(), orange.get(1).getId());
            for (Warn w : orange) {
                w.revoke("Система");
            }
            Warn red_warn = Warn.createPermanentWarn(
                    username, "Система", reasonEx, true
            );
            red_warn.notifyEverywhere();
            red.add(red_warn);
        }
        if (red.size() >= 3) {
            String reasonEx = String.format("3 предупреждения (#%s, #%s, #%s)",
                    red.get(0).getId(), red.get(1).getId(), red.get(2).getId());
            for (Warn w : getPerPlayerWarns(username)) {
                w.revoke("Система");
            }
            MuteManager.getPlayerMutes().stream()
                    .filter(m -> m.getIntruder().equals(username)).findFirst()
                    .ifPresent(m -> m.revoke("Система"));
            Ban ban = new Ban(
                    username, "Система", reasonEx, 0L, true
            );
            ban.kickIntruder();
            ban.notifyEverywhere();
        }
    }

    public static void check() {
        for (String username : FelmonPunishments.getPunishedPlayers()) {
            checkPerPlayer(username);
        }
    }

    public static void load() {
        if (!FelmonPunishments.getFelmonConnection().reconnectIfClosed())
            return;

        String query = "SELECT * FROM " + FPMainConfig.MySQL.Tables.warns + " WHERE active = ?;";
        try {
            PreparedStatement preparedStmt = FelmonPunishments.getFelmonConnection().getConnection().prepareStatement(query);
            preparedStmt.setBoolean(1, true);

            ResultSet rs = preparedStmt.executeQuery();

            while (rs.next()) {
                String intruder = rs.getString("intruder");

                String admin = rs.getString("admin");
                String victim = rs.getString("victim");
                String reason = rs.getString("reason");
                long expireTime = rs.getLong("expireTime");
                long startTime = rs.getLong("startTime");
                Warn warn = new Warn(intruder, admin, startTime, expireTime, victim, reason, false);

                int id = rs.getInt("id");
                warn.setId(id);
            }
            rs.close();
            preparedStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createTableIfNotExists() {
        if (!FelmonPunishments.getFelmonConnection().reconnectIfClosed())
            return;

        String query = "CREATE TABLE IF NOT EXISTS `" + FPMainConfig.MySQL.Tables.warns + "` ( `id` INT NOT NULL AUTO_INCREMENT, `active` BOOLEAN NOT NULL DEFAULT TRUE, `intruder` VARCHAR(64) NOT NULL, `victim` VARCHAR(64) DEFAULT NULL, `admin` VARCHAR(64) NOT NULL, `reason` LONGTEXT NOT NULL, `startTime` BIGINT DEFAULT 0, `expireTime` BIGINT DEFAULT 10, PRIMARY KEY (`id`) );";
        try {
            Statement stmt = FelmonPunishments.getFelmonConnection().getConnection().createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
