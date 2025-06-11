package rt.infrastructure.analyzer;

import rt.infrastructure.notifier.Notifier;
import rt.model.notification.Notification;
import rt.model.service.AnalyzerService;
import rt.model.service.NoteStorageService;

import rt.nlp.NLPService;

import java.util.Map;

public class AnalyzerImpl implements AnalyzerService {

    private final NoteStorageService storage;
    private final NLPService service;

    public AnalyzerImpl(NoteStorageService storage, NLPService service) {
        this.storage = storage;
        this.service = service;
    }

    @Override
    public void classify() {
        if (storage.noAnyNotes()) {
            Notifier.getInstance().addNotification(new Notification("Нечего анализировать", true));
            return;
        }
        Notifier.getInstance().addNotification(new Notification("Начинаю анализировать", true));
        storage.getNotesCommonPool().forEach(note -> {
            String noteText = note.getText();
            Map<String, Double> topics = service.classify(noteText);
            note.setKeyWords(topics);
        });
        if (!storage.noSuitableNotes()) {
            storage.getChosenNotesPool().forEach(note -> {
                String noteText = note.getText();
                Map<String, Double> topics = service.classify(noteText);
                note.setKeyWords(topics);
            });
        }
        Notifier.getInstance().addNotification(new Notification("Анализ закончен", true));
    }
}