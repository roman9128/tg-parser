package rt.infrastructure.config;

import rt.infrastructure.notifier.Notifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ApiHandler {
    private static final Properties properties = new Properties();
    private static final String applicationPropertyFileName = "application.properties";
    private static int apiID;
    private static String apiHash;

    static {
        try (InputStream inputStream = ApiHandler.class.getClassLoader().getResourceAsStream(applicationPropertyFileName)) {
            if (inputStream != null) {
                properties.load(inputStream);
                apiID = Integer.parseInt(properties.getProperty("api.ID"));
                apiHash = properties.getProperty("api.hash");
            }
        } catch (IOException ex) {
            Notifier.getInstance().addNotification("Не удалось загрузить параметры для входа. Завершаю работу...");
            System.exit(3);
        }
    }

    public static int getApiID() {
        return apiID;
    }

    public static String getApiHash() {
        return apiHash;
    }
}