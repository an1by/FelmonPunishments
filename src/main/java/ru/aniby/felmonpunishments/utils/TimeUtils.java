package ru.aniby.felmonpunishments.utils;

import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Date;

public class TimeUtils {
    public static final long day = 86400000L;
    public static final PeriodFormatter timeFormatter = new PeriodFormatterBuilder()
            .appendDays().appendSuffix("d")
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .appendSeconds().appendSuffix("s")
            .toFormatter();

    public static long parseTime(String string) {
        return timeFormatter.parsePeriod(string).toStandardDuration().getStandardSeconds() * 1000L;
    }

    public static String toDisplay(long time) {
        var instance = java.time.Instant.ofEpochMilli(time);
        var zonedDateTime = java.time.ZonedDateTime.ofInstant(instance,java.time.ZoneId.of("Europe/Moscow"));
        var formatter = java.time.format.DateTimeFormatter.ofPattern("d.M.u HH:mm O");
        return zonedDateTime.format(formatter);
    }

    public static long currentTime() {
        return new Date().getTime();
    }
}
