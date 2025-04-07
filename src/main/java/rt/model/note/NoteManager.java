package rt.model.note;

import it.tdlight.jni.TdApi;

import java.util.concurrent.ConcurrentLinkedDeque;

public class NoteManager {
    private final ConcurrentLinkedDeque<Note> notes = new ConcurrentLinkedDeque<>();

    public void createNote(TdApi.Message message, String senderName) {
        String text = "";
        TdApi.MessageContent messageContent = message.content;
        switch (messageContent) {
            case TdApi.MessageText mt -> {
                text = mt.text.text;
            }
            case TdApi.MessagePhoto mp -> {
                text = "Фото. " + mp.caption.text;
            }
            case TdApi.MessageVideo mv -> {
                text = "Видео. " + mv.caption.text;
            }
            case TdApi.MessageDocument md -> {
                text = "Документ. " + md.caption.text;
            }
            default -> {
                text = "Сообщение без текста" + System.lineSeparator();
            }
        }
        notes.offer(new Note(message.id, message.chatId, message.date, senderName, text));
    }

    public Note take() {
        return notes.pollFirst();
    }

    public int getSize() {
        return notes.size();
    }

    public boolean isEmpty() {
        return notes.isEmpty();
    }

    public void clear() {
        notes.clear();
    }
}
