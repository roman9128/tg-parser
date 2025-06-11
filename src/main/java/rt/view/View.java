package rt.view;

import rt.presenter.Presenter;

public interface View {
    void setPresenters(Presenter presenter1, Presenter presenter2, Presenter presenter3, Presenter presenter4);

    void startInteractions();

    void startNotificationListener();

    void stopNotificationListener();

    void show();

    void load(String folderIDString, String dateFromString, String dateToString);

    void find(String[] args);

    void write(String value);

    void classify();

    void clear();

    void stopParser();

    void logout();

    void print(String text, boolean needNextLine);

    String askParameter(String who, String question);
}