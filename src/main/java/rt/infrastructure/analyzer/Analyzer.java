package rt.infrastructure.analyzer;

import rt.nlp.NLPService;

import java.io.IOException;
import java.util.Map;

public class Analyzer {

    private final NLPService service = new NLPService();

    public Analyzer() throws IOException {
    }

    public Map<String, Double> classify(String text) {
        return service.classify(text);
    }
//
//    public Set<String> makeNer(String text) {
//        return service.makeNer(text);
//    }
}