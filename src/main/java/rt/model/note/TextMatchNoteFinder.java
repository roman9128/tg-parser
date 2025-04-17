package rt.model.note;

import java.util.Arrays;

public class TextMatchNoteFinder {

    protected boolean noteContainsOneOfArgs(Note note, String[] args) {
        if (note.getText().isEmpty() || note.getText().isBlank()) {
            return false;
        }
        return Arrays.stream(args).anyMatch(arg -> note.getText().toLowerCase().contains(arg.toLowerCase()));
    }
}