package rt.infrastructure.analyzer;

import rt.infrastructure.notifier.Notifier;
import rt.model.notification.Notification;
import rt.model.service.AnalyzerService;
import rt.model.service.NoteStorageService;
import rt.nlp.NLPClassifier;
import rt.nlp.NLPModelFinder;
import rt.utils.RussianLanguageTokenizer;

import java.io.IOException;
import java.util.Map;

public class AnalyzerImpl implements AnalyzerService {

    private final NoteStorageService storage;
    private final NLPClassifier classifier = new NLPClassifier();

    public AnalyzerImpl(NoteStorageService storage) {
        this.storage = storage;
        try {
            classifier.setModels(new NLPModelFinder().getModels());
        } catch (IOException e) {
            Notifier.getInstance().addNotification(new Notification(e.getMessage(), true));
        }
    }

    @Override
    public void classify() {
        if (storage.noAnyNotes()) {
            Notifier.getInstance().addNotification(new Notification("Нечего анализировать", true));
            return;
        }
        Notifier.getInstance().addNotification(new Notification("Начинаю анализировать", false));
        storage.getNotesCommonPool().forEach(note -> {
            String noteText = note.getText();
            Map<String, Double> topics = classifyNotes(noteText);
            note.setKeyWords(topics);
        });
        if (!storage.noSuitableNotes()) {
            storage.getChosenNotesPool().forEach(note -> {
                String noteText = note.getText();
                Map<String, Double> topics = classifyNotes(noteText);
                note.setKeyWords(topics);
            });
        }
        Notifier.getInstance().addNotification(new Notification("Анализ закончен", true));
    }

    private Map<String, Double> classifyNotes(String text) {
        String[] tokenizedText = RussianLanguageTokenizer.tokenize(text);
        return classifier.classify(tokenizedText);
    }
}