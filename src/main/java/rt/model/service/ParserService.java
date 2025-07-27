package rt.model.service;

import java.util.Map;
import java.util.Set;

public interface ParserService extends AutoCloseable {
    void waitForExit() throws InterruptedException;

    Map<Integer, String> getFoldersIDsAndNames();

    Set<Long> getChatsInFolder(Integer folderID);

    Map<Long, String> getChannelsIDsAndNames();

    void loadChannelsHistory(Set<Long> from, Long dateFromUnix, Long dateToUnix);

    void close();

    void logout();

    void setMessagesToDownload(int value);
}
