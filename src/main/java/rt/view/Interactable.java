package rt.view;

public interface Interactable {
    void start();

    void show();

    void load(String folderIDString, String dateFromString, String dateToString);

    void write();

    void clear();

    void stop();

    void logout();
}
