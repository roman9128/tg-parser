package rt.presenter;

import rt.model.core.AppService;
import rt.view.View;

import java.util.concurrent.CompletableFuture;

public class Presenter implements ServiceHelper {

    private final AppService service;
    private final View view;

    public Presenter(AppService service, View view) {
        this.service = service;
        this.view = view;
        view.setPresenter(this);
    }

    public void initService() {
        try {
            service.start(this);
        } catch (Exception e) {
            System.out.println("Ошибка при запуске приложения");
        }
    }

    @Override
    public void startInteractions() {
        CompletableFuture.runAsync(view::startInteractions).exceptionally(e -> {
            System.out.println("Ошибка в консольном потоке: " + e.getMessage());
            return null;
        });
    }

    public void show() {
        service.show();
    }

    public void load(String folderIDString, String dateFromString, String dateToString) {
        service.loadHistory(folderIDString, dateFromString, dateToString);
    }

    public void find(String argsAsString) {
        service.findNotes(argsAsString.split(" "));
    }

    public void write(String value) {
        service.writeHistoryToFile(value.isEmpty() || value.isBlank());
    }

    public void clear() {
        service.clear();
    }

    public void stop() {
        service.stop();
    }

    public void logout() {
        service.logout();
    }

    @Override
    public void print(String text, boolean needNextLine) {
        view.print(text, needNextLine);
    }

    @Override
    public String askParameter(String who, String question) {
        return view.askParameter(who, question);
    }
}