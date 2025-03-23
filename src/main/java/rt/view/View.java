package rt.view;

import rt.presenter.Presenter;

public interface View {
    void setPresenter(Presenter presenter);

    void startInteractions();

    void show();

    void load(String folderIDString, String dateFromString, String dateToString);

    void write();

    void clear();

    void stop();

    void logout();

    void print(String text, boolean needNextLine);

    String askParameter(String who, String question);

}
