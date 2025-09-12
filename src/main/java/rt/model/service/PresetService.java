package rt.model.service;

import rt.model.preset.Preset;

import java.time.LocalDate;
import java.util.Map;

public interface PresetService {

    void createPreset(String source, LocalDate start, LocalDate end);

    void renamePreset(String oldName, String newName);

    Map<String, Preset> getAllPresets();

    Preset getPresetByName(String name);

    void removePresetByName(String name);
}
