package rt.infrastructure.analyzer.ner;

import rt.model.entity.Entity;

import java.util.List;
import java.util.Set;

public class NERService {
    private final EntityLoader entityLoader = new EntityLoader();
    private final EntityFinder entityFinder = new EntityFinder();
    private final List<Entity> loadedEntities;

    public NERService() {
        loadedEntities = entityLoader.loadAndGet();
        entityFinder.setEntities(loadedEntities);
    }

    public Set<Entity> findAllByNameOrSynonym(String text) {
        return entityFinder.findAllByNameOrSynonym(text);
    }

    public Set<Entity> findAllByCategory(String text) {
        return entityFinder.findAllByCategory(text);
    }

    public Set<Entity> findAllByTag(String text) {
        return entityFinder.findAllByTag(text);
    }

    public Set<Entity> searchAllFields(String text) {
        return entityFinder.searchAllFields(text);
    }

    public Set<Entity> extractEntitiesByPartialName(String text) {
        return entityFinder.extractEntitiesByPartialName(text);
    }

    public Set<Entity> extractEntitiesByExactName(String text) {
        return entityFinder.extractEntitiesByExactName(text);
    }
}
