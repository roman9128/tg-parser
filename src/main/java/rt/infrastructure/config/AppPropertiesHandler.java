package rt.infrastructure.config;

import rt.infrastructure.notifier.Notifier;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppPropertiesHandler {

    private static final Properties properties = new Properties();
    private static final String appPropertyFileName = "./app.properties";
    private static String filePath = "./messages.txt"; // default value
    private static int messagesToStop = 5000; // default value
    private static int messagesToDownload = 100; // default value
    private static int autoload = 0; // default value (off)

    static {
        try (InputStream inputStream = new FileInputStream(appPropertyFileName)) {
            properties.load(inputStream);
            filePath = properties.getProperty("file");
            messagesToStop = getMessageCountParameter(properties.getProperty("stop"));
            messagesToDownload = getMessageCountParameter(properties.getProperty("messages"));
            autoload = getAutoloadParameter(properties.getProperty("autoload"));
        } catch (Exception e) {
            Notifier.getInstance().addNotification("Не удалось загрузить параметры парсинга. Использую значения по умолчанию");
            createFileWithProperties();
        }
    }

    private static int getMessageCountParameter(String count) {
        try {
            int param = Integer.parseInt(count);
            if (param > 100) {
                return param;
            } else return 100;
        } catch (NumberFormatException e) {
            return 100;
        }
    }

    private static int getAutoloadParameter(String autoload) {
        try {
            int param = Integer.parseInt(autoload);
            if (param > 0) {
                return param;
            } else return 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static void createFileWithProperties() {
        try (FileWriter fileWriter = new FileWriter(appPropertyFileName, false)) {
            fileWriter.write(
                    "file=" + filePath + System.lineSeparator() +
                            "stop=" + messagesToStop + System.lineSeparator() +
                            "messages=" + messagesToDownload + System.lineSeparator() +
                            "autoload=" + autoload);
        } catch (IOException e) {
            Notifier.getInstance().addNotification("Не удалось создать файл с настройками по умолчанию. " + e.getMessage());
        }
    }

    public static String getFilePath() {
        return filePath;
    }

    public static int getMessagesToDownload() {
        return messagesToDownload;
    }

    public static int getMessagesToStop() {
        return messagesToStop;
    }

    static void setMessagesToDownload(int newMessagesToDownload) {
        messagesToDownload = newMessagesToDownload;
        createFileWithProperties();
    }
}