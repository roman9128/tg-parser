package rt.model.notification;

public class Notification {

    private final String message;
    private final boolean needNextLine;

    public Notification(String message, boolean needNextLine) {
        this.message = message;
        this.needNextLine = needNextLine;
    }

    public String getMessage() {
        return message;
    }

    public boolean needNextLine() {
        return needNextLine;
    }
}
