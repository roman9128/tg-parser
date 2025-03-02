package rt.auxillaries;

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
    private static final String filePath;
    private static final int messagesToStop;
    private static final int messagesToDownload;


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
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load parsing properties", ex);
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
