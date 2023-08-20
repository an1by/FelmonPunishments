package ru.aniby.felmonpunishments.database;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {
    public static class Tables {
        @Getter
        private static final @NotNull String players = "minestar_players";
        @Getter
        private static final @NotNull String warns = "minestar_warns";
        @Getter
        private static final @NotNull String bans = "minestar_bans";
        @Getter
        private static final @NotNull String mutes = "minestar_mutes";
        @Getter
        private static final @NotNull String offenses = "minestar_offenses";
    }
    @Getter
    private static String databaseName;

    public static Connection connect(String host, String database, String user, String password) {
        databaseName = database;
        try {
            String url = String.format("jdbc:mysql://%s/%s", host, database);
            url += "?autoReconnect=true&initialTimeout=1&useSSL=false";
            return DriverManager.getConnection(url, user, password);

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void disconnect(Connection connection) {
        try { connection.close(); } catch(SQLException ignored) { /*can't do anything */ }
    }
}
