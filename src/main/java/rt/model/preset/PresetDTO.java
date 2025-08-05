package rt.model.preset;

import java.util.Set;

public record PresetDTO(String name, Set<String> folders, Set<String> channels, String start, String end) {
}
