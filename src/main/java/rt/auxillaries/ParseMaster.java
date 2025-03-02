package rt.auxillaries;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class ParseMaster {

    public static Integer parseInteger(String numberString) {
        try {
            return Integer.parseInt(numberString);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long parseUnixTime(String dateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate date = LocalDate.parse(dateString, formatter);
            return date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        } catch (Exception e) {
            return null;
        }
    }
}
