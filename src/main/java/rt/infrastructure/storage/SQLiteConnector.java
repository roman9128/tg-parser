package rt.infrastructure.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import rt.model.note.Note;

import java.sql.*;
import java.util.Set;

public class SQLiteConnector {
    private final String DB_URL = "jdbc:sqlite:./data/notes.db";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void createTable() {
        String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS notes (
                        message_id INTEGER NOT NULL,
                        sender_id INTEGER NOT NULL,
                        sender_name TEXT NOT NULL,
                        msg_time_unix INTEGER NOT NULL,
                        text TEXT NOT NULL,
                        msg_link TEXT,
                        topic TEXT,
                        created_at INTEGER NOT NULL DEFAULT (strftime('%s','now')),
                
                        PRIMARY KEY (message_id, sender_id)
                    )
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Таблица notes создана успешно");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
        }
    }

    public void addNote(Note note) {
        String sql = """
                INSERT OR IGNORE INTO notes(message_id, sender_id, sender_name, msg_time_unix, text, msg_link, topic)
                VALUES(?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, note.getMessageID());
            pstmt.setLong(2, note.getSenderID());
            pstmt.setString(3, note.getSenderName());
            pstmt.setInt(4, note.getMsgTimeUNIX());
            pstmt.setString(5, note.getText());
            pstmt.setString(6, note.getLink());
            pstmt.setString(7, setToString(note.getTopic()));

            pstmt.executeUpdate();
            System.out.println("Запись добавлена");

        } catch (SQLException e) {
            System.out.println("Ошибка при вставке записи: " + e.getMessage());
        }
    }


    private String setToString(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(set);
        } catch (JsonProcessingException e) {
            return "[]";        }
    }

    private Set<String> stringToSet(String str) {
        if (str == null || str.isBlank()) {
            return Set.of();
        }
        try {
            return objectMapper.readValue(str, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return Set.of();
        }
    }
}