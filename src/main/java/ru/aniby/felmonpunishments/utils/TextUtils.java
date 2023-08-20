package ru.aniby.felmonpunishments.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextUtils {
    public static String standardName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static @NotNull String reasonFormatting(int startsFrom, @NotNull String... arguments) {
        List<String> reasonList = new ArrayList<>(Arrays.asList(arguments));
        reasonList.subList(0, startsFrom).clear();
        return String.join(" ", reasonList);
    }

    public static @NotNull String formatForDiscord(@NotNull String text) {
        return text.replaceAll("_", "\\_");
    }
}
