package rt.presenter.storage;

import rt.model.service.NoteStorageService;
import rt.presenter.Presenter;
import rt.view.View;

import java.util.HashMap;
import java.util.Map;

public class StoragePresenter implements Presenter {
    private final View view;
    private final NoteStorageService storage;

    public StoragePresenter(View view, NoteStorageService storage) {
        this.view = view;
        this.storage = storage;
    }

    public void find(String[] args) {
        if (storage.noAnyNotes()) {
            view.print("Сначала нужно загрузить сообщения");
            return;
        }
        String how = args[0].toLowerCase();
        String where = args[1].toLowerCase();
        String what = args[2].toLowerCase();
        if (how.isBlank() || where.isBlank() || what.isBlank()) {
            view.print("Не заданы параметры поиска");
            return;
        }

        if (!(how.equals("and") || how.equals("or") || how.equals("not"))) {
            view.print("Не введено логическое условие and, or или not");
            return;
        }

        if (where.equals("topic")) {
            findNotesByTopic(how, what.split("\\s+"));
        } else if (where.equals("text")) {
            findNotesByText(how, what.split("\\s+"));
        } else {
            view.print("Неверный параметр. Нужно topic или text");
            return;
        }

        view.print("Поиск завершён");
        if (storage.noSuitableNotes()) {
            view.print("Нет отобранных сообщений");
        } else {
            view.print("Всего отобрано сообщений: " + storage.getSuitableNotesQuantity());
            if (where.equals("text")) {
                view.print("Количество сообщений, содержащих слова для поиска");
                view.print(storage.getWordsStat());
            }
            view.print("Чтобы загрузить отобранные сообщения, введи команду write x");
        }
    }

    private void findNotesByTopic(String how, String[] what) {
        Map<String, Double> result = new HashMap<>();
        for (String s : what) {
            result.putAll(getParams(s));
        }
        storage.findNotesByTopic(how, result);
    }

    private Map<String, Double> getParams(String s) {
        String[] paramsPair = s.split("-", 2);
        Map<String, Double> result = new HashMap<>();
        try {
            result.put(paramsPair[0], Double.parseDouble(paramsPair[1]));
        } catch (Exception e) {
            result.put(paramsPair[0], 55.0); // default value
        }
        return result;
    }

    private void findNotesByText(String how, String[] what) {
        storage.findNotesByText(how, what);
    }

    public void clear() {
        storage.clearAll();
        view.print("Все загруженные сообщения удалены");
    }
}