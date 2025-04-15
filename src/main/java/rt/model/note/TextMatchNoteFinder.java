package rt.model.note;

import java.util.Arrays;

public abstract class TextMatchNoteFinder {

    protected String[] args = new String[]{};

    protected abstract boolean noteIsSuitable(Note note);

    protected void setArgs(String[] args) {
        this.args = args;
    }

    protected String getArgs(){
        return String.join(" ", Arrays.stream(args).toArray(String[]::new));
    }
}