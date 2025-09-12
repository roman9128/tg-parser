package rt.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private DateTimeUtils() {
    }

    public static Long parseUnixDateStartOfDay(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, FORMATTER);
            return date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        } catch (Exception e) {
            return 0L;
        }
    }

    public static Long parseUnixDateEndOfDay(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, FORMATTER).plusDays(1L);
            return date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    public static LocalDate parseStringToLocalDateOrGetNull(String dateString) {
        try {
            return LocalDate.parse(dateString, FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    public static String calculateDate(Integer dateDiff) {
        if (dateDiff == null) return "";
        LocalDate now = LocalDate.now();
        return now.minusDays(dateDiff).format(FORMATTER);
    }
}