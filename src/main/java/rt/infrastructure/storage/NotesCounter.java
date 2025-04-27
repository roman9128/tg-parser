package rt.infrastructure.storage;

import java.util.HashMap;

class NotesCounter {

    private final HashMap<String, Integer> argsMap = new HashMap<>();

    void count(String text, String arg) {
        if (text.toLowerCase().contains(arg.toLowerCase())) {
            argsMap.put(arg, argsMap.getOrDefault(arg, 0) + 1);
        }
    }

    HashMap<String, Integer> getArgsMap() {
        return argsMap;
    }

    void clear() {
        argsMap.clear();
    }
}