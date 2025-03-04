package rt.auxillaries;

import java.util.Random;

public class Randomizer {
    private static final Random random = new Random();

    public static int giveNumber() {
        return random.nextInt(100, 500);
    }
}
