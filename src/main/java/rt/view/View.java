package rt.view;

import rt.presenter.Presenter;
import rt.presenter.parser.ParserPresenter;

import java.util.List;

public interface View {
    void setPresenters(List<Presenter> presenters);

    void startInteractions();

    void show();

    void load(String folderIDString, String dateFromString, String dateToString);

    void find(String argsAsString);

    void write(String value);

    void classify();

    void clear();

    void stop();

    void logout();

    void print(String text, boolean needNextLine);

    String askParameter(String who, String question);
}