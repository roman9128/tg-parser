package rt.infrastructure.analyzer.ner;

import rt.model.entity.Entity;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class EntityFinder {

    private List<Entity> entities;
    private Map<String, Entity> nameMap;

    void setEntities(List<Entity> entities) {
        this.entities = entities;
        this.nameMap = new HashMap<>();
        buildNameMap();
    }

    private void buildNameMap() {
        for (Entity e : entities) {
            nameMap.put(e.getName().toLowerCase(), e);
            for (String syn : e.getSynonyms()) {
                nameMap.put(syn.toLowerCase(), e);
            }
        }
    }

    Set<Entity> findAllByNameOrSynonym(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        String lowerText = text.toLowerCase();
        return nameMap.entrySet().stream()
                .filter(entry -> entry.getKey().contains(lowerText))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    Set<Entity> findAllByCategory(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        String lowerText = text.toLowerCase();
        return entities.stream()
                .filter(e -> e.getCategory() != null && e.getCategory().toLowerCase().contains(lowerText))
                .collect(Collectors.toSet());
    }

    Set<Entity> findAllByTag(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        String lowerText = text.toLowerCase();
        return entities.stream()
                .filter(e -> e.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(lowerText)))
                .collect(Collectors.toSet());
    }

    Set<Entity> searchAllFields(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        String lowerText = text.toLowerCase();
        Set<Entity> resultSet = new HashSet<>();
        resultSet.addAll(findAllByNameOrSynonym(lowerText));
        resultSet.addAll(findAllByCategory(lowerText));
        resultSet.addAll(findAllByTag(lowerText));
        return resultSet;
    }

    Set<Entity> extractEntitiesByPartialName(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        String lowerText = text.toLowerCase();
        Set<Entity> resultSet = new HashSet<>();

        for (Entity e : entities) {
            if (e.getName() != null && lowerText.contains(e.getName().toLowerCase())) {
                resultSet.add(e);
                continue;
            }
            for (String syn : e.getSynonyms()) {
                if (syn != null && lowerText.contains(syn.toLowerCase())) {
                    resultSet.add(e);
                    break;
                }
            }
        }
        return resultSet;
    }

    Set<Entity> extractEntitiesByExactName(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        String lowerText = text.toLowerCase();
        Set<Entity> resultSet = new HashSet<>();
        for (Entity e : entities) {
            if (matchesExactWord(lowerText, e.getName())) {
                resultSet.add(e);
                continue;
            }
            for (String syn : e.getSynonyms()) {
                if (matchesExactWord(lowerText, syn)) {
                    resultSet.add(e);
                    break;
                }
            }
        }
        return resultSet;
    }

    private boolean matchesExactWord(String text, String word) {
        if (word == null || word.isBlank()) return false;
        String pattern = "\\b" + Pattern.quote(word.toLowerCase()) + "\\b";
        return Pattern.compile(pattern).matcher(text).find();
    }
}