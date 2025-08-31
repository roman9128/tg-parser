package rt.infrastructure.storage;

import rt.model.note.Note;

import java.util.Arrays;
import java.util.Set;

class NoteFinder {

    boolean textMeetsConditions(Note note, String how, String[] what) {
        if (note.getText().isBlank()) {
            return false;
        }
        String text = note.getText().toLowerCase();
        String[] conditions = removeDuplicates(what);
        return switch (how) {
            case "or" -> Arrays.stream(conditions).anyMatch(text::contains);
            case "and" -> Arrays.stream(conditions).allMatch(text::contains);
            case "not" -> Arrays.stream(conditions).noneMatch(text::contains);
            default -> false;
        };
    }

    boolean topicsMeetConditions(Note note, String how, String[] what) {
        if (note.getTopic().isEmpty()) {
            return false;
        }
        Set<String> topic = note.getTopic();
        String[] conditions = removeDuplicates(what);
        return switch (how) {
            case "or" -> Arrays.stream(conditions).anyMatch(topic::contains);
            case "and" -> Arrays.stream(conditions).allMatch(topic::contains);
            case "not" -> Arrays.stream(conditions).noneMatch(topic::contains);
            default -> false;
        };
    }

    private String[] removeDuplicates(String[] array) {
        return Arrays.stream(array)
                .map(String::toLowerCase)
                .distinct()
                .toArray(String[]::new);
    }
}