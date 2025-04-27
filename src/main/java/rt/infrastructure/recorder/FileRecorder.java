package rt.infrastructure.recorder;

import rt.infrastructure.config.PropertyHandler;
import rt.model.entity.Note;
import rt.model.service.FileRecorderService;
import rt.model.service.NoteStorageService;
import rt.model.service.ResponsePrinter;

import java.io.FileWriter;
import java.io.IOException;

public class FileRecorder implements FileRecorderService {

    private final NoteStorageService storage;

    public FileRecorder(NoteStorageService storage) {
        this.storage = storage;
    }

    public static void writeToFile(String path, String text) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path, true)) {
            fileWriter.write(text);
        }
    }

    @Override
    public void write(ResponsePrinter printer, boolean writeAllNotes) {
        if (storage.noAnyNotes() && writeAllNotes) {
            printer.printResponse("Нечего записывать. Сначала нужно загрузить сообщения", true);
            return;
        }
        if (storage.noSuitableNotes() && !writeAllNotes) {
            printer.printResponse("Нет отобранных сообщений", true);
            return;
        }
        if (writeAllNotes) {
            printer.printResponse("Начинаю запись в файл (" + storage.getAllNotesQuantity() + " сообщ. всего)", true);
        } else {
            printer.printResponse("Начинаю запись в файл (" + storage.getSuitableNotesQuantity() + " сообщ. всего)", true);
        }
        printer.printResponse("Записываю. Подожди немного", false);
        String channelName = "";
        while (writeAllNotes ? !storage.noAnyNotes() : !storage.noSuitableNotes()) {
            Note note;
            if (writeAllNotes) {
                note = storage.takeNoteFromCommonPool();
            } else {
                note = storage.takeNoteFromChosenNotesPool();
            }
            String senderName = note.getSenderName();
            if (!channelName.equals(senderName)) {
                channelName = senderName;
                try {
                    FileRecorder.writeToFile(PropertyHandler.getFilePath(), ">>>>>>> Далее сообщения из канала " + channelName + System.lineSeparator());
                } catch (IOException e) {
                    printer.printResponse("Ошибка при записи: " + e.getMessage(), true);
                }
            }
            try {
                FileRecorder.writeToFile(PropertyHandler.getFilePath(), note.toString());
            } catch (IOException e) {
                printer.printResponse("Ошибка при записи в файл: " + e.getMessage(), true);
            }
        }
        printer.printResponse("Запись сообщений в файл закончена", true);
        if (writeAllNotes) {
            storage.clearAll();
        } else {
            storage.clearChosen();
        }
    }
}