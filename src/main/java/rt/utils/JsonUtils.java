package rt.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;

public class JsonUtils {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private JsonUtils() {
    }

    public static String setToString(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(set);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    public static Set<String> stringToSet(String str) {
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