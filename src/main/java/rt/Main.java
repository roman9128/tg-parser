package rt;

import rt.model.core.AppService;
import rt.presenter.Presenter;
import rt.view.console.ConsoleUI;

public class Main {

    public static void main(String[] args) {
        Presenter presenter = new Presenter(new AppService(), new ConsoleUI());
        presenter.initService();
    }
}