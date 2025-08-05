package rt.infrastructure.preset;

import rt.infrastructure.notifier.Notifier;
import rt.model.preset.Preset;
import rt.model.service.PresetService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class Presetter implements PresetService {

    private final String presetPath = "./preset.txt";
    private final Map<String, Preset> allPresets = new HashMap<>();

    public Presetter() {
        readPresets();
    }

    @Override
    public void createPreset(String name, String source, LocalDate start, LocalDate end) {
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        Integer startDiff;
        Integer endDiff;
        if (start == null) {
            startDiff = null;
        } else {
            startDiff = Math.toIntExact((ChronoUnit.DAYS.between(start, now)));
        }
        if (end == null) {
            endDiff = null;
        } else {
            endDiff = Math.toIntExact((ChronoUnit.DAYS.between(end, now)));
        }
        allPresets.put(name, new Preset(name, source, startDiff, endDiff));
        writePresets();
    }

    @Override
    public Map<String, Preset> getAllPresets() {
        return allPresets;
    }

    @Override
    public Preset getPresetByName(String name) {
        return allPresets.get(name);
    }

    @Override
    public void removePresetByName(String name) {
        allPresets.remove(name);
        writePresets();
    }

    private void readPresets() {
        try (BufferedReader reader = new BufferedReader(new FileReader(presetPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] params = {"", "", "", ""};
                String[] text = line.split("~", 4);
                System.arraycopy(text, 0, params, 0, text.length);
                String name = params[0];
                String source = params[1];
                Integer startDiff = parseIntegerOrGetNull(params[2]);
                Integer endDiff = parseIntegerOrGetNull(params[3]);
                allPresets.put(name, new Preset(name, source, startDiff, endDiff));
            }
        } catch (IOException e) {
            Notifier.getInstance().addNotification(e.getMessage());
        }
    }

    private void writePresets() {
        try (FileWriter writer = new FileWriter(presetPath, false)) {
            // clear file
        } catch (IOException e) {
            Notifier.getInstance().addNotification(e.getMessage());
        }
        try (FileWriter fileWriter = new FileWriter(presetPath, true)) {
            for (Preset preset : allPresets.values()) {
                fileWriter.write(preset.toString() + System.lineSeparator());
            }
        } catch (IOException e) {
            Notifier.getInstance().addNotification(e.getMessage());
        }
    }

    private Integer parseIntegerOrGetNull(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}