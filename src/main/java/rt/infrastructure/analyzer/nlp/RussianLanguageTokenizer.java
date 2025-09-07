package rt.infrastructure.analyzer.nlp;

import com.github.demidko.aot.WordformMeaning;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.util.Arrays;
import java.util.Set;

class RussianLanguageTokenizer {

    private static final SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.RUSSIAN);
    private static final Set<String> stopWords = WordsLoader.loadWordsSet("nlp/dictionaries/stop.txt");
    private static final Set<String> toRemoveStrings = WordsLoader.loadWordsSet("nlp/dictionaries/remove.txt");

    static String[] tokenize(String text) {
        text = text
                .toLowerCase()
                .replaceAll("ё", "е")
                .replaceAll("—", "-")
                .replaceAll("[^\\p{L}\\s-]", "");
        String[] words = text.split("[\\s-]+");
        words = Arrays.stream(words)
                .filter(w -> w.length() > 1 && w.length() < 30)
                .filter(w -> !toRemove(w))
                .filter(w -> !isStopWord(w))
                .toArray(String[]::new);
        return lemmatizeAndStemmize(words);
    }

    private static String[] lemmatizeAndStemmize(String[] wordsToChange) {
        for (int i = 0; i < wordsToChange.length; i++) {
            var meanings = WordformMeaning.lookupForMeanings(wordsToChange[i]);
            if (meanings.isEmpty()) {
                wordsToChange[i] = stemmer.stem(wordsToChange[i]).toString();
            } else {
                wordsToChange[i] = meanings.getFirst().getLemma().toString();
            }
        }
        return Arrays.stream(wordsToChange)
                .filter(w -> !isStopWord(w))
                .toArray(String[]::new);
    }

    private static boolean toRemove(String word) {
        if (toRemoveStrings.size() == 1 && toRemoveStrings.contains("")) {
            return false;
        }
        return toRemoveStrings.stream().anyMatch(word::contains);
    }

    private static boolean isStopWord(String word) {
        return stopWords.contains(word);
    }
}