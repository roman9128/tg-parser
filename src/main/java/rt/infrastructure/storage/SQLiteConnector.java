package rt.infrastructure.storage;

import rt.infrastructure.notifier.Notifier;
import rt.model.entity.Entity;
import rt.model.note.Note;
import rt.model.search.MatchMode;
import rt.utils.JsonUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SQLiteConnector {
    private final String DB_URL = "jdbc:sqlite:./data/notes.db";

    public void createTables() {
        String createNotesTable = """
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

        String createEntitiesTable = """
                CREATE TABLE IF NOT EXISTS entities (
                    name TEXT NOT NULL,
                    synonyms TEXT,
                    category TEXT,
                    description TEXT,
                    tags TEXT,
                    PRIMARY KEY (name, category)
                )
                """;

        String createNoteEntityTable = """
                CREATE TABLE IF NOT EXISTS note_entities (
                    message_id INTEGER NOT NULL,
                    sender_id INTEGER NOT NULL,
                    entity_name TEXT NOT NULL,
                    entity_category TEXT NOT NULL,
                    FOREIGN KEY(message_id, sender_id) REFERENCES notes(message_id, sender_id),
                    FOREIGN KEY(entity_name, entity_category) REFERENCES entities(name, category),
                    PRIMARY KEY (message_id, sender_id, entity_name, entity_category)
                )
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {
            stmt.execute(createNotesTable);
            stmt.execute(createEntitiesTable);
            stmt.execute(createNoteEntityTable);
            Notifier.getInstance().addNotification("Все таблицы созданы успешно");
        } catch (SQLException e) {
            Notifier.getInstance().addNotification("Ошибка при создании таблиц: " + e.getMessage());
        }
    }

    public void addNote(Note note) {
        String insertNoteSQL = """
                INSERT OR IGNORE INTO notes(message_id, sender_id, sender_name, msg_time_unix, text, msg_link, topic)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        String insertEntitySQL = """
                INSERT OR IGNORE INTO entities(name, synonyms, category, description, tags)
                VALUES (?, ?, ?, ?, ?)
                """;

        String insertNoteEntitySQL = """
                INSERT OR IGNORE INTO note_entities(message_id, sender_id, entity_name, entity_category)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtNote = conn.prepareStatement(insertNoteSQL)) {
                pstmtNote.setLong(1, note.getMessageID());
                pstmtNote.setLong(2, note.getSenderID());
                pstmtNote.setString(3, note.getSenderName());
                pstmtNote.setInt(4, note.getMsgTimeUNIX());
                pstmtNote.setString(5, note.getText());
                pstmtNote.setString(6, note.getLink());
                pstmtNote.setString(7, JsonUtils.setToString(note.getTopic()));
                pstmtNote.executeUpdate();
            }

            for (Entity entity : note.getNer()) {
                try (PreparedStatement pstmtEntity = conn.prepareStatement(insertEntitySQL)) {
                    pstmtEntity.setString(1, entity.getName());
                    pstmtEntity.setString(2, JsonUtils.setToString(entity.getSynonyms()));
                    pstmtEntity.setString(3, entity.getCategory());
                    pstmtEntity.setString(4, entity.getDescription());
                    pstmtEntity.setString(5, JsonUtils.setToString(entity.getTags()));
                    pstmtEntity.executeUpdate();
                }

                try (PreparedStatement pstmtNoteEntity = conn.prepareStatement(insertNoteEntitySQL)) {
                    pstmtNoteEntity.setLong(1, note.getMessageID());
                    pstmtNoteEntity.setLong(2, note.getSenderID());
                    pstmtNoteEntity.setString(3, entity.getName());
                    pstmtNoteEntity.setString(4, entity.getCategory());
                    pstmtNoteEntity.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            Notifier.getInstance().addNotification("Ошибка при добавлении заметки: " + e.getMessage());
        }
    }

    public List<Note> findNotesByTopic(MatchMode how, String[] topics) {
        String whereClause = switch (how) {
            case OR -> Arrays.stream(topics)
                    .map(c -> "LOWER(n.topic) LIKE LOWER('%" + c + "%')")
                    .collect(Collectors.joining(" OR "));
            case AND -> Arrays.stream(topics)
                    .map(c -> "LOWER(n.topic) LIKE LOWER('%" + c + "%')")
                    .collect(Collectors.joining(" AND "));
            case NOT -> Arrays.stream(topics)
                    .map(c -> "LOWER(n.topic) NOT LIKE LOWER('%" + c + "%')")
                    .collect(Collectors.joining(" AND "));
        };

        String sql = """
                SELECT n.message_id, n.sender_id, n.sender_name, n.msg_time_unix, n.text, n.msg_link, n.topic,
                       e.name AS entity_name, e.synonyms AS entity_synonyms, e.category AS entity_category,
                       e.description AS entity_description, e.tags AS entity_tags
                FROM notes n
                LEFT JOIN note_entities ne
                ON n.message_id = ne.message_id AND n.sender_id = ne.sender_id
                LEFT JOIN entities e
                ON ne.entity_name = e.name AND ne.entity_category = e.category
                WHERE """ + whereClause + """
                ORDER BY n.message_id, n.sender_id
                """;

        Map<String, Note> notesMap = new LinkedHashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Long messageID = rs.getLong("message_id");
                Long senderID = rs.getLong("sender_id");
                String key = messageID + "_" + senderID;

                Note note = notesMap.get(key);
                if (note == null) {
                    String senderName = rs.getString("sender_name");
                    Integer msgTimeUNIX = rs.getInt("msg_time_unix");
                    String text = rs.getString("text");
                    String link = rs.getString("msg_link");
                    Set<String> topic = JsonUtils.stringToSet(rs.getString("topic"));

                    note = new Note(messageID, senderID, msgTimeUNIX, senderName, text, link, topic, new HashSet<>());
                    notesMap.put(key, note);
                }

                String entityName = rs.getString("entity_name");
                if (entityName != null) {
                    Set<String> synonyms = JsonUtils.stringToSet(rs.getString("entity_synonyms"));
                    String category = rs.getString("entity_category");
                    String description = rs.getString("entity_description");
                    Set<String> tags = JsonUtils.stringToSet(rs.getString("entity_tags"));

                    note.getNer().add(new Entity(entityName, synonyms, category, description, tags));
                }
            }
        } catch (SQLException e) {
            Notifier.getInstance().addNotification("Ошибка при поиске заметок: " + e.getMessage());
        }

        return new ArrayList<>(notesMap.values());
    }

    public List<Note> findNotesByEntities(MatchMode how, List<Entity> entities) {
        List<Note> notes = new ArrayList<>();
        if (entities == null || entities.isEmpty()) {
            return notes;
        }
        String placeholders = entities.stream()
                .map(e -> "(?, ?)")
                .collect(Collectors.joining(", "));
        String baseSql = """
                SELECT n.message_id, n.sender_id, n.sender_name, n.msg_time_unix, n.text, n.msg_link,
                       nt.topic,
                       ne.entity_name, ne.entity_category, ne.entity_description, ne.entity_tags
                FROM notes n
                LEFT JOIN note_topics nt ON n.message_id = nt.message_id AND n.sender_id = nt.sender_id
                LEFT JOIN note_entities ne ON n.message_id = ne.message_id AND n.sender_id = ne.sender_id
                """;
        switch (how) {
            case AND -> {
                baseSql += """
                        WHERE (n.message_id, n.sender_id) IN (
                            SELECT ne.message_id, ne.sender_id
                            FROM note_entities ne
                            WHERE (ne.entity_name, ne.entity_category) IN (%s)
                            GROUP BY ne.message_id, ne.sender_id
                            HAVING COUNT(DISTINCT ne.entity_name || '|' || ne.entity_category) = ?
                        )
                        """.formatted(placeholders);
            }
            case OR -> {
                baseSql += """
                        WHERE (ne.entity_name, ne.entity_category) IN (%s)
                        """.formatted(placeholders);
            }
            case NOT -> {
                baseSql += """
                        WHERE (n.message_id, n.sender_id) NOT IN (
                            SELECT ne.message_id, ne.sender_id
                            FROM note_entities ne
                            WHERE (ne.entity_name, ne.entity_category) IN (%s)
                        )
                        """.formatted(placeholders);
            }
            default -> {
                return notes;
            }
        }
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(baseSql)) {
            int idx = 1;
            for (Entity e : entities) {
                pstmt.setString(idx++, e.getName());
                pstmt.setString(idx++, e.getCategory());
            }
            if (how == MatchMode.AND) {
                pstmt.setInt(idx, entities.size());
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                Map<String, Note> noteMap = new HashMap<>();
                while (rs.next()) {
                    long messageId = rs.getLong("message_id");
                    long senderId = rs.getLong("sender_id");
                    String key = messageId + "_" + senderId;
                    Note note = noteMap.get(key);
                    if (note == null) {
                        note = new Note(
                                messageId,
                                senderId,
                                rs.getInt("msg_time_unix"),
                                rs.getString("sender_name"),
                                rs.getString("text"),
                                rs.getString("msg_link"),
                                new HashSet<>(),
                                new HashSet<>()
                        );
                        noteMap.put(key, note);
                    }
                    String topic = rs.getString("topic");
                    if (topic != null) {
                        note.getTopic().add(topic);
                    }
                    String entityName = rs.getString("entity_name");
                    if (entityName != null) {
                        String category = rs.getString("entity_category");
                        String description = rs.getString("entity_description");
                        String tagsStr = rs.getString("entity_tags");
                        Set<String> tags = (tagsStr == null || tagsStr.isBlank())
                                ? Set.of()
                                : Set.of(tagsStr.split(","));

                        note.getNer().add(new Entity(entityName, Set.of(), category, description, tags));
                    }
                }
                notes.addAll(noteMap.values());
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при поиске заметок по Entity: " + e.getMessage());
        }
        return notes;
    }

    public List<Note> getAllNotes() {
        Map<String, Note> notesMap = new LinkedHashMap<>();
        String sql = """
                SELECT n.message_id, n.sender_id, n.sender_name, n.msg_time_unix, n.text, n.msg_link, n.topic,
                       e.name AS entity_name, e.synonyms AS entity_synonyms, e.category AS entity_category,
                       e.description AS entity_description, e.tags AS entity_tags
                FROM notes n
                LEFT JOIN note_entities ne
                ON n.message_id = ne.message_id AND n.sender_id = ne.sender_id
                LEFT JOIN entities e
                ON ne.entity_name = e.name AND ne.entity_category = e.category
                ORDER BY n.message_id, n.sender_id
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Long messageID = rs.getLong("message_id");
                Long senderID = rs.getLong("sender_id");
                String key = messageID + "_" + senderID;

                Note note = notesMap.get(key);
                if (note == null) {
                    String senderName = rs.getString("sender_name");
                    Integer msgTimeUNIX = rs.getInt("msg_time_unix");
                    String text = rs.getString("text");
                    String link = rs.getString("msg_link");
                    Set<String> topic = JsonUtils.stringToSet(rs.getString("topic"));

                    note = new Note(messageID, senderID, msgTimeUNIX, senderName, text, link, topic, new HashSet<>());
                    notesMap.put(key, note);
                }

                String entityName = rs.getString("entity_name");
                if (entityName != null) {
                    Set<String> synonyms = JsonUtils.stringToSet(rs.getString("entity_synonyms"));
                    String category = rs.getString("entity_category");
                    String description = rs.getString("entity_description");
                    Set<String> tags = JsonUtils.stringToSet(rs.getString("entity_tags"));

                    note.getNer().add(new Entity(entityName, synonyms, category, description, tags));
                }
            }

        } catch (SQLException e) {
            Notifier.getInstance().addNotification("Ошибка при получении заметок: " + e.getMessage());
        }
        return new ArrayList<>(notesMap.values());
    }

    public List<Entity> getAllEntities() {
        List<Entity> entities = new ArrayList<>();
        String sql = "SELECT name, synonyms, category, description, tags FROM entities";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                String category = rs.getString("category");
                String description = rs.getString("description");
                Set<String> synonyms = JsonUtils.stringToSet(rs.getString("synonyms"));
                Set<String> tags = JsonUtils.stringToSet(rs.getString("tags"));

                Entity entity = new Entity(name, synonyms, category, description, tags);
                entities.add(entity);
            }

        } catch (SQLException e) {
            Notifier.getInstance().addNotification("Ошибка при получении entities: " + e.getMessage());
        }

        return entities;
    }

    public Integer getLastMessageTimeBySender(Long senderId) {
        String sql = """
                SELECT MAX(msg_time_unix) AS last_time
                FROM notes
                WHERE sender_id = ?
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, senderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int lastTime = rs.getInt("last_time");
                    if (!rs.wasNull()) {
                        return lastTime;
                    }
                }
            }
        } catch (SQLException e) {
            Notifier.getInstance().addNotification(
                    "Ошибка при получении времени последнего сообщения: " + e.getMessage()
            );
        }
        return null;
    }
}