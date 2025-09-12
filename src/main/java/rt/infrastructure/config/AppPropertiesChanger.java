package rt.infrastructure.config;

public final class AppPropertiesChanger {

    public void setMessagesAmountToDownloadFromOneChannelIfStopDateIsNotSet(int messagesToDownload) {
        if (100 > messagesToDownload || messagesToDownload > AppPropertiesHandler.getMessagesToStop()) {
            messagesToDownload = (messagesToDownload < 100)
                    ? 100
                    : AppPropertiesHandler.getMessagesToStop();
        }
        AppPropertiesHandler.setMessagesToDownload(messagesToDownload);
    }
}
