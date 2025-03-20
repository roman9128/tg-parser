package rt;

import rt.model.core.UserBotService;
import rt.presenter.Presenter;
import rt.view.ConsoleUI;

public class Main {

    public static void main(String[] args) throws Exception {
        ConsoleUI consoleUI = new ConsoleUI();
        Presenter presenter = new Presenter(consoleUI);
        UserBotService service = new UserBotService();
        consoleUI.setPresenter(presenter);
        presenter.setService(service);
        service.start(presenter);
    }
}