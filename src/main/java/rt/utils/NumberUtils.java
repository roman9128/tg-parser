package rt.utils;

import java.util.Random;

public class NumberUtils {
    private static final Random random = new Random();

    private NumberUtils() {
    }

    public static int giveRandomNumber() {
        return random.nextInt(100, 500);
    }

    public static Long parseLongOrGetZero(String numberString) {
        try {
            return Long.parseLong(numberString);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static Integer parseIntegerOrGetZero(String numberString) {
        try {
            return Integer.parseInt(numberString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static Integer parseIntegerOrGetNull(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}