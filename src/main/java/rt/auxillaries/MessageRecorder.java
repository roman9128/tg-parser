package rt.auxillaries;

import java.io.FileWriter;
import java.io.IOException;

public class MessageRecorder {

    public void writeToFile(String text) {
        try (FileWriter fileWriter = new FileWriter(PropertyHandler.getFilePath(), true)) {
            fileWriter.write(text);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
