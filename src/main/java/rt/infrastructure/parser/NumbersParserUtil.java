package rt.infrastructure.parser;

import java.time.*;
import java.time.format.DateTimeFormatter;

class NumbersParserUtil {

    static Integer parseInteger(String numberString) {
        try {
            return Integer.parseInt(numberString);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static Long parseUnixDateStartOfDay(String dateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate date = LocalDate.parse(dateString, formatter);
            return date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        } catch (Exception e) {
            return 0L;
        }
    }

    static Long parseUnixDateEndOfDay(String dateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate date = LocalDate.parse(dateString, formatter).plusDays(1L);
            return date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }
}