package rt.model.auxillaries;

import java.io.FileWriter;
import java.io.IOException;

public class MessageRecorder {

    public void writeToFile(String text) throws IOException {
        try (FileWriter fileWriter = new FileWriter(PropertyHandler.getFilePath(), true)) {
            fileWriter.write(text);
        }
    }
}
