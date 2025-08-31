package rt.model.note;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class Note {
    private final Long messageID;
    private final Long senderID;
    private final String senderName;
    private final Integer msgTimeUNIX;
    private final String text;
    private final String link;
    private final Set<String> topic;
    private final Set<String> ner = new TreeSet<>();

    public Note(Long messageID, Long senderID, Integer msgTimeUNIX, String senderName, String text, String link, Set<String> topic) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.msgTimeUNIX = msgTimeUNIX;
        this.senderName = senderName;
        this.text = text;
        this.link = link;
        this.topic = topic;
    }

    public void setNer(Set<String> ner) {
        this.ner.addAll(ner);
    }

    private String convertTimeUNIXtoString(Integer msgTimeUNIX) {
        Instant instant = Instant.ofEpochSecond(msgTimeUNIX);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.getDefault());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }

    public Long getMessageID() {
        return messageID;
    }

    public Long getSenderID() {
        return senderID;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getText() {
        return text;
    }

    public Integer getMsgTimeUNIX() {
        return msgTimeUNIX;
    }

    public String getLink() {
        return link;
    }

    public Set<String> getTopic() {
        return topic;
    }

    public Set<String> getNer() {
        return ner;
    }

    private String getTopicsAsText() {
        if (topic.isEmpty()) {
            return "не определена";
        } else {
            return String.join(", ", topic);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return Objects.equals(messageID, note.messageID) && Objects.equals(senderID, note.senderID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageID, senderID);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append(System.lineSeparator())
                .append("Время: ").append(convertTimeUNIXtoString(msgTimeUNIX))
                .append(System.lineSeparator())
                .append("Автор: ").append(senderName)
                .append(System.lineSeparator())
                .append("Ссылка: ").append(link)
                .append(System.lineSeparator())
                .append("Текст: ").append(text)
                .append(System.lineSeparator())
                .append("Тема: ").append(getTopicsAsText())
                .append(System.lineSeparator())
                .append("}")
                .append(System.lineSeparator());
        return builder.toString();
    }
}