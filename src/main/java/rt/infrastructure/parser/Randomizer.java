package rt.infrastructure.parser;

import java.util.Random;

class Randomizer {
    private static final Random random = new Random();

    static int giveNumber() {
        return random.nextInt(100, 500);
    }
}