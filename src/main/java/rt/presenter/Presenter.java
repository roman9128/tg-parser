package rt.presenter;

import rt.model.core.UserBotService;
import rt.view.Interactable;


public class Presenter {

    private UserBotService service;
    private final Interactable interactable;

    public Presenter(Interactable interactable) {
        this.interactable = interactable;
    }

    public void setService(UserBotService service){
        this.service = service;
    }

    public void start(){
        interactable.start();
    }

    public void show() {
        service.show();
    }

    public void load(String folderIDString, String dateFromString, String dateToString) {
        service.loadHistory(folderIDString, dateFromString, dateToString);
    }

    public void write() {
        service.writeHistoryToFile();
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

    public void print() {

    }
}
