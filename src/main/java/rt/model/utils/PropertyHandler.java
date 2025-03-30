package rt.model.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyHandler {

    private static final Properties properties = new Properties();
    private static final String applicationPropertyFileName = "application.properties";
    private static final String parsingPropertyFileName = "./parsing.properties";
    private static int apiID;
    private static String apiHash;
    private static String filePath = "./chronic.txt"; // default value
    private static int messagesToStop = 1000; // default value
    private static int messagesToDownload = 100; // default value

    static {
        try (InputStream inputStream = PropertyHandler.class.getClassLoader().getResourceAsStream(applicationPropertyFileName)) {
            if (inputStream != null) {
                properties.load(inputStream);
                apiID = Integer.parseInt(properties.getProperty("api.ID"));
                apiHash = properties.getProperty("api.hash");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load application properties", ex);
        }
        try (InputStream inputStream = new FileInputStream(parsingPropertyFileName)) {
            properties.load(inputStream);
            filePath = properties.getProperty("file.path");
            messagesToStop = Integer.parseInt(properties.getProperty("stop.count"));
            messagesToDownload = Integer.parseInt(properties.getProperty("messages.count"));
        } catch (IOException _) {
            try {
                FileRecorder.writeToFile(parsingPropertyFileName,
                        "file.path=" + filePath + System.lineSeparator() +
                                "stop.count=" + messagesToStop + System.lineSeparator() +
                                "messages.count=" + messagesToDownload);
            } catch (IOException _) {
            }
        }
    }

    public static int getApiID() {
        return apiID;
    }

    public static String getApiHash() {
        return apiHash;
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