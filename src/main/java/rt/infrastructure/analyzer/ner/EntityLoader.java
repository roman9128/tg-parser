package rt.infrastructure.analyzer.ner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import rt.model.entity.Entity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class EntityLoader {
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Entity> entities = new ArrayList<>();
    private final String PATH = "./ner";

    List<Entity> loadAndGet() {
        File folder = new File(PATH);
        if (!folder.exists() || !folder.isDirectory()) {
            return entities;
        }
        File[] jsonFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (jsonFiles == null) return entities;

        for (File file : jsonFiles) {
            try {
                List<Entity> entitiesFromFile = mapper.readValue(file, new TypeReference<>() {
                });
                entities.addAll(entitiesFromFile);
            } catch (IOException e) {
                // ignore
            }
        }
        return entities;
    }
}
