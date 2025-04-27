package rt.infrastructure.analyzer;

import rt.model.service.AnalyzerService;
import rt.model.storage.NoteStorageService;
import rt.nlp.NLPClassifier;
import rt.nlp.NLPModelFinder;
import rt.utils.RussianLanguageTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalyzerImpl implements AnalyzerService {

    private NoteStorageService storage;
    private final NLPModelFinder modelFinder = new NLPModelFinder();
    private final NLPClassifier classifier = new NLPClassifier();


    @Override
    public void classify() {
        storage.getNotesCommonPool().forEach(note -> {
            String noteText = note.getText();
            List<String> topics = classifyNotes(noteText);
            note.setKeyWords(topics);
        });
        if (!storage.noSuitableNotes()) {
            storage.getChosenNotesPool().forEach(note -> {
                String noteText = note.getText();
                List<String> topics = classifyNotes(noteText);
                note.setKeyWords(topics);
            });
        }
    }

    private List<String> classifyNotes(String text) {
        try {
            String[] tokenizedText = RussianLanguageTokenizer.tokenize(text);
            modelFinder.findModels();
            classifier.setModels(modelFinder.getModels());
            return classifier.classify(tokenizedText);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean storageIsEmpty() {
        return storage.noAnyNotes();
    }

    @Override
    public void setStorage(NoteStorageService storage) {
        this.storage = storage;
    }
}
