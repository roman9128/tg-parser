package rt.presenter;

public interface PrinterScanner {
    void print(String text, boolean needNextLine);

    String askParameter(String who, String question);
}