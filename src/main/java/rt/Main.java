package rt;

import rt.model.UserBotManager;

public class Main {

    public static void main(String[] args) {
        try {
            new UserBotManager().start();
        } catch (Exception e) {
            System.out.println("Не получается запуститься");
        }
    }
}