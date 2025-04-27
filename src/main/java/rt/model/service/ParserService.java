package rt.model.service;

public interface ParserService extends AutoCloseable {
    void waitForExit() throws InterruptedException;

    void show();

    void loadChannelsHistory(String folderIDString, String dateFromString, String dateToString);

    void close();

    void logout();
}
