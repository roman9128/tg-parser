package rt.infrastructure.config;

public final class ParsingPropertiesChanger {

    public void setMessagesAmountToDownloadFromOneChannelIfStopDateIsNotSet(int messagesToDownload) {
        if (100 > messagesToDownload || messagesToDownload > ParsingPropertiesHandler.getMessagesToStop()) {
            messagesToDownload = (messagesToDownload < 100)
                    ? 100
                    : ParsingPropertiesHandler.getMessagesToStop();
        }
        ParsingPropertiesHandler.messagesToDownload = messagesToDownload;
        ParsingPropertiesHandler.createFileWithProperties(messagesToDownload);
    }
}
