package rt.model.auxillaries;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Note {
    private final LocalDateTime msgTime;
    private final String senderName;
    private String msgLink;
    private final String text;

    public Note(Integer msgTimeUNIX, String senderName, String text) {
        this.msgTime = convertDateTime(msgTimeUNIX);
        this.senderName = senderName;
        this.text = text;
    }

    public void setMsgLink(String link) {
        msgLink = link;
    }

    private LocalDateTime convertDateTime(Integer msgTimeUNIX) {
        Instant instant = Instant.ofEpochSecond(msgTimeUNIX);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private String getStringDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.getDefault());
        return dateTime.format(formatter);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append(System.lineSeparator())
                .append("Время: ").append(getStringDateTime(msgTime))
                .append(System.lineSeparator())
                .append("Автор: ").append(senderName)
                .append(System.lineSeparator())
                .append("Ссылка: ").append(msgLink)
                .append(System.lineSeparator())
                .append("Текст: ").append(text)
                .append(System.lineSeparator())
                .append("}")
                .append(System.lineSeparator());
        return builder.toString();
    }
}
