package rt.infrastructure.analyzer.nlp;

import java.util.Map;

public class NLPService {

    private final NLPClassifier classifier = new NLPClassifier();

    public NLPService() {
        NLPModelFinder modelFinder = new NLPModelFinder();
        modelFinder.findModels();
        classifier.setModels(modelFinder.getModels());
    }

    public Map<String, Double> classify(String text) {
        String[] tokenizedText = RussianLanguageTokenizer.tokenize(text);
        return classifier.classify(tokenizedText);
    }
}
