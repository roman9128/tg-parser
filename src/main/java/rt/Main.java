package rt;

import rt.service_manager.ServiceManager;
import rt.view.View;
import rt.view.console.ConsoleUI;
import rt.view.gui.SwingUI;

public class Main {

    public static void main(String[] args) {
        View view = new SwingUI();
        ServiceManager serviceManager = new ServiceManager(view);
        view.setServiceManager(serviceManager);
        serviceManager.init();
    }
}