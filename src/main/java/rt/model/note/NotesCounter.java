package rt.model.note;

import java.util.HashMap;

public class NotesCounter {

    private HashMap<String, Integer> argsMap = new HashMap<>();

    protected void count(String text, String arg) {
        if (text.toLowerCase().contains(arg.toLowerCase())) {
            argsMap.put(arg, argsMap.getOrDefault(arg, 0) + 1);
        }
    }

    protected HashMap<String, Integer> getArgsMap() {
        return argsMap;
    }

    protected void clear() {
        argsMap.clear();
    }
}