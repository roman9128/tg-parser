package rt.model.note;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Note {
    private final Long messageID;
    private final Long senderID;
    private final String senderName;
    private final LocalDateTime msgTime;
    private final String text;
    private String msgLink;
    private final Map<String, Double> keyWords = new HashMap<>();

    public Note(Long messageID, Long senderID, Integer msgTimeUNIX, String senderName, String text) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.msgTime = convertDateTime(msgTimeUNIX);
        this.senderName = senderName;
        this.text = text;
    }

    public void setMsgLink(String link) {
        msgLink = link;
    }

    public void setKeyWords(Map<String, Double> keyWords) {
        this.keyWords.putAll(keyWords.entrySet()
                .stream()
                .filter(e -> e.getValue() > 55)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private LocalDateTime convertDateTime(Integer msgTimeUNIX) {
        Instant instant = Instant.ofEpochSecond(msgTimeUNIX);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private String getStringDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.getDefault());
        return dateTime.format(formatter);
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

    public Map<String, Double> getKeyWords() {
        return keyWords;
    }

    private String getKeyWordsAsText() {
        if (keyWords.isEmpty()) {
            return "не определена";
        } else {
            return stringify(keyWords);
        }
    }

    private String stringify(Map<String, Double> keyWords) {
        Map<String, Double> sortedMap = keyWords.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return sortedMap.entrySet().stream()
                .map(e -> {
                    String res;
                    if (e.getValue() < 70) {
                        res = e.getKey() + " (возможно)";
                    } else {
                        res = e.getKey();
                    }
                    return res;
                })
                .collect(Collectors.joining(", "));
    }

    public boolean hasLink() {
        return msgLink != null;
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
                .append("Время: ").append(getStringDateTime(msgTime))
                .append(System.lineSeparator())
                .append("Автор: ").append(senderName)
                .append(System.lineSeparator())
                .append("Ссылка: ").append(msgLink)
                .append(System.lineSeparator())
                .append("Текст: ").append(text)
                .append(System.lineSeparator())
                .append("Тема: ").append(getKeyWordsAsText())
                .append(System.lineSeparator())
                .append("}")
                .append(System.lineSeparator());
        return builder.toString();
    }
}