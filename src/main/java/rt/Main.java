package rt;

import rt.model.core.UserBotService;
import rt.presenter.Presenter;
import rt.view.console.ConsoleUI;

public class Main {

    public static void main(String[] args) {
        Presenter presenter = new Presenter(new UserBotService(), new ConsoleUI());
        presenter.initService();
    }
}