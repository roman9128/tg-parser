package rt.model.service;

import rt.model.preset.Preset;

import java.time.LocalDate;
import java.util.Map;

public interface PresetService {

    void createPreset(String name, String source, LocalDate start, LocalDate end);

    Map<String, Preset> getAllPresets();

    Preset getPresetByName(String name);

    void removePresetByName(String name);
}
