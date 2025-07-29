package rt.view;

import rt.service_manager.ServiceManager;

public interface View {

    void setServiceManager(ServiceManager serviceManager);

    void startInteractions();

    void startNotificationListener();

    void stopNotificationListener();

    void load(String folderIDString, String dateFromString, String dateToString);

    void find(String[] args);

    void write(String value);

    void classify();

    void clear();

    void stopParser();

    void logout();

    void print(String text);

    String askParameter(String who, String question);

    void setMaxAmount(String arg);
}