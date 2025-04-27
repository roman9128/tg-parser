package rt.presenter.storage;

import rt.model.service.NoteStorageService;
import rt.presenter.Presenter;
import rt.view.View;

public class StoragePresenter implements Presenter {
    private final View view;
    private final NoteStorageService storage;

    public StoragePresenter(View view, NoteStorageService storage) {
        this.view = view;
        this.storage = storage;
    }

    public void find(String argsAsString) {
        findNotes(argsAsString.split("\\s+"));
    }

    private void findNotes(String[] args) {
        if (args.length == 0) {
            view.print("Не введены слова для поиска", true);
            return;
        }
        if (storage.noAnyNotes()) {
            view.print("Сначала нужно загрузить сообщения", true);
            return;
        }
        storage.findNotes(args);
        view.print("Поиск завершён", true);
        if (storage.noSuitableNotes()) {
            view.print("Нет отобранных сообщений", true);
        } else {
            view.print("Всего отобрано сообщений: " + storage.getSuitableNotesQuantity(), true);
            view.print("Количество сообщений, содержащих слова для поиска", true);
            view.print(storage.getStat(), true);
            view.print("Чтобы загрузить отобранные сообщения, введи команду write x", true);
        }
    }

    public void clear() {
        storage.clearAll();
        view.print("Все загруженные сообщения удалены", true);
    }
}
