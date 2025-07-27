package rt.infrastructure.config;

import rt.infrastructure.notifier.Notifier;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ParsingPropertiesHandler {

    private static final Properties properties = new Properties();
    private static final String parsingPropertyFileName = "./parsing.properties";
    private static String filePath = "./messages.txt"; // default value
    private static int messagesToStop = 5000; // default value
    static int messagesToDownload = 100; // default value

    static {
        try (InputStream inputStream = new FileInputStream(parsingPropertyFileName)) {
            properties.load(inputStream);
            filePath = properties.getProperty("file.path");
            messagesToStop = Integer.parseInt(properties.getProperty("stop.count"));
            messagesToDownload = Integer.parseInt(properties.getProperty("messages.count"));
        } catch (Exception e) {
            Notifier.getInstance().addNotification("Не удалось загрузить параметры парсинга. Использую значения по умолчанию");
            createFileWithProperties(messagesToDownload);
        }
    }

    static void createFileWithProperties(int messagesToDownloadValue) {
        try (FileWriter fileWriter = new FileWriter(parsingPropertyFileName, false)) {
            fileWriter.write(
                    "file.path=" + filePath + System.lineSeparator() +
                            "stop.count=" + messagesToStop + System.lineSeparator() +
                            "messages.count=" + messagesToDownloadValue);
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
}