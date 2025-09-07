package rt.infrastructure.analyzer;

import rt.model.entity.Entity;
import rt.infrastructure.analyzer.ner.*;
import rt.infrastructure.analyzer.nlp.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Analyzer {

    private final NLPService nlpService = new NLPService();
    private final NERService nerService = new NERService();

    public Map<String, Double> classify(String text) {
        return nlpService.classify(text);
    }

    public Set<Entity> recognizeNE(String text) {
        return nerService.extractEntitiesByPartialName(text);
    }
}