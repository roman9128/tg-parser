package rt.model.utils;

import java.io.FileWriter;
import java.io.IOException;

public class FileRecorder {

    public static void writeToFile(String path, String text) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path, true)) {
            fileWriter.write(text);
        }
    }
}
