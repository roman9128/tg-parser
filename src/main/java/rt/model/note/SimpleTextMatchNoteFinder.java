package rt.model.note;

import java.util.Arrays;

public class SimpleTextMatchNoteFinder extends TextMatchNoteFinder {

    protected boolean noteIsSuitable(Note note) {
        if (note.getText().isEmpty() || note.getText().isBlank()) {
            return false;
        }
        return Arrays.stream(args).anyMatch(arg -> note.getText().toLowerCase().contains(arg.toLowerCase()));
    }
}