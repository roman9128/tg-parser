package rt.infrastructure.storage;

import rt.model.entity.Note;

import java.util.Arrays;

class TextMatchNoteFinder {

    boolean noteContainsOneOfArgs(Note note, String[] args) {
        if (note.getText().isEmpty() || note.getText().isBlank()) {
            return false;
        }
        return Arrays.stream(args).anyMatch(arg -> note.getText().toLowerCase().contains(arg.toLowerCase()));
    }
}