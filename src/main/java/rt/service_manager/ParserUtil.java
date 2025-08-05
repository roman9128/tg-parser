package rt.service_manager;

import java.time.*;
import java.time.format.DateTimeFormatter;

class ParserUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    static Long parseLongOrGetZero(String numberString) {
        try {
            return Long.parseLong(numberString);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    static Integer parseIntegerOrGetZero(String numberString) {
        try {
            return Integer.parseInt(numberString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static Long parseUnixDateStartOfDay(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, FORMATTER);
            return date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        } catch (Exception e) {
            return 0L;
        }
    }

    static Long parseUnixDateEndOfDay(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, FORMATTER).plusDays(1L);
            return date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    static LocalDate parseStringToLocalDateOrGetNull(String dateString) {
        try {
            return LocalDate.parse(dateString, FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}