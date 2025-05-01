package rt.infrastructure.storage;

import rt.model.note.Note;

import java.util.Arrays;
import java.util.Map;

class NoteFinder {

    boolean textMeetsConditions(Note note, String how, String[] what) {
        if (note.getText().isBlank()) {
            return false;
        }
        String text = note.getText().toLowerCase();
        return switch (how) {
            case "or" -> Arrays.stream(what).anyMatch(text::contains);
            case "and" -> Arrays.stream(what).allMatch(text::contains);
            case "not" -> Arrays.stream(what).noneMatch(text::contains);
            default -> false;
        };
    }

    boolean topicsMeetConditions(Note note, String how, Map<String, Double> what) {
        if (note.getKeyWords().isEmpty()) {
            return false;
        }
        Map<String, Double> keyWords = note.getKeyWords();
        return switch (how) {
            case "or" -> what.keySet().stream().anyMatch(k -> {
                Double keyWordValue = keyWords.get(k);
                Double userInputValue = what.get(k);
                if (keyWordValue == null || userInputValue == null) {
                    return false;
                }
                return keyWordValue >= userInputValue;
            });
            case "and" -> what.keySet().stream().allMatch(k -> {
                Double keyWordValue = keyWords.get(k);
                Double userInputValue = what.get(k);
                if (keyWordValue == null || userInputValue == null) {
                    return false;
                }
                return keyWordValue >= userInputValue;
            });
            case "not" -> what.keySet().stream().noneMatch(k -> {
                Double keyWordValue = keyWords.get(k);
                Double userInputValue = what.get(k);
                if (keyWordValue == null || userInputValue == null) {
                    return false;
                }
                return keyWordValue >= userInputValue;
            });
            default -> false;
        };
    }
}