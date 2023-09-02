package ru.aniby.felmonpunishments.configuration;

import ru.aniby.felmonapi.configuration.FelmonComment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FPMainConfig {
    @FelmonComment(text = "")
    public static class MySQL {
        public static String host = "localhost";
        public static String user = "root";
        public static String password;
        public static String database = "felmonpunishments";

        public static class Tables {
            public static String mutes = "mutes";
            public static String warns = "warns";
            public static String bans = "bans";
        }
    }

    public static class Discord {
        public static String botToken;
        public static class Server {
            public static String guild;
            public static HashMap<String, Object> channels = new HashMap<>(
                    Map.of(
                            "mutes", "",
                            "warns", "",
                            "bans", "",
                            "tickets", ""
                    )
            );
            public static HashMap<String, Object> groups = new HashMap<>(
                    Map.of(
                            "admin", "",
                            "moderator", "",
                            "helper", ""
                    )
            );
            
            public static List<String> revokeOnBan = new ArrayList<>(
                    List.of("moderator", "helper")
            );
        }
    }
}
