package rt.model.note;

import java.util.Arrays;

public class NoteAnalyzer {

    protected boolean noteIsSuitable(Note note, String[] args) {
        if (note.getText().isEmpty() || note.getText().isBlank()) {
            return false;
        }
        return Arrays.stream(args).anyMatch(arg -> note.getText().toLowerCase().contains(arg.toLowerCase()));
    }
}