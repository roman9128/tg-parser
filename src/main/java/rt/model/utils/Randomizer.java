package rt.model.utils;

import java.util.Random;

public class Randomizer {
    private static final Random random = new Random();

    public static int giveNumber() {
        return random.nextInt(100, 500);
    }

    public static int giveSmallNumber(){
        return random.nextInt(60, 90);
    }
}