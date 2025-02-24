package rt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyHandler {

    private static final Properties properties = new Properties();
    private static final String applicationPropertyFileName = "application.properties";
    private static final String parsingPropertyFileName = "parsing.properties";
    private static int apiID;
    private static String apiHash;
    private static String filePath;
    private static int messagesToDownload;


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
        try (InputStream inputStream = PropertyHandler.class.getClassLoader().getResourceAsStream(parsingPropertyFileName)) {
            if (inputStream != null) {
                properties.load(inputStream);
                filePath = properties.getProperty("file.path");
                messagesToDownload = Integer.parseInt(properties.getProperty("messages.count"));
            }
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
}
