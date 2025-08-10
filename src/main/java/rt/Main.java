package rt;

import rt.service_manager.ServiceManager;

public class Main {

    public static void main(String[] args) {
        ServiceManager serviceManager = new ServiceManager();
        serviceManager.init();
    }
}