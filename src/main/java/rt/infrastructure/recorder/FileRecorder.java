package rt.infrastructure.recorder;

import rt.infrastructure.config.PropertyHandler;
import rt.infrastructure.notifier.Notifier;
import rt.model.note.Note;
import rt.model.service.FileRecorderService;
import rt.model.service.NoteStorageService;

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
    public void write(boolean writeAllNotes) {
        if (storage.noAnyNotes() && writeAllNotes) {
            Notifier.getInstance().addNotification("Нечего записывать. Сначала нужно загрузить сообщения");
            return;
        }
        if (storage.noSuitableNotes() && !writeAllNotes) {
            Notifier.getInstance().addNotification("Нет отобранных сообщений");
            return;
        }
        if (writeAllNotes) {
            Notifier.getInstance().addNotification("Начинаю запись в файл (" + storage.getAllNotesQuantity() + " сообщ. всего)");
        } else {
            Notifier.getInstance().addNotification("Начинаю запись в файл (" + storage.getSuitableNotesQuantity() + " сообщ. всего)");
        }
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
                    Notifier.getInstance().addNotification("Ошибка при записи: " + e.getMessage());
                }
            }
            try {
                FileRecorder.writeToFile(PropertyHandler.getFilePath(), note.toString());
            } catch (IOException e) {
                Notifier.getInstance().addNotification("Ошибка при записи в файл: " + e.getMessage());
            }
        }
        Notifier.getInstance().addNotification("Запись сообщений в файл закончена");
        if (writeAllNotes) {
            storage.clearAll();
        } else {
            storage.clearChosen();
        }
    }
}