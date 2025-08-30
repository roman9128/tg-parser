package rt.infrastructure.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import rt.model.note.Note;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SQLiteConnector {
    private final String DB_URL = "jdbc:sqlite:notes.db";
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
                        key_words TEXT,
                
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

    public void upsertNote(Note note) {
        String sql = """
                INSERT OR REPLACE INTO notes(message_id, sender_id, sender_name, msg_time_unix, text, msg_link, key_words) 
                VALUES(?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, note.getMessageID());
            pstmt.setLong(2, note.getSenderID());
            pstmt.setString(3, note.getSenderName());
            pstmt.setInt(4, note.getMsgTimeUNIX());
            pstmt.setString(5, note.getText());
            pstmt.setString(6, note.getMsgLink());
            pstmt.setString(7, mapToString(note.getKeyWords()));

            pstmt.executeUpdate();
            System.out.println("Запись добавлена");

        } catch (SQLException e) {
            System.out.println("Ошибка при вставке записи: " + e.getMessage());
        }
    }


    private String mapToString(Map<String, Double> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            System.out.println("Ошибка при сериализации ключевых слов: " + e.getMessage());
            return "";
        }
    }

    private Map<String, Double> stringToMap(String str) {
        if (str == null || str.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(str, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            System.out.println("Ошибка при десериализации ключевых слов: " + e.getMessage());
            return new HashMap<>();
        }
    }
}