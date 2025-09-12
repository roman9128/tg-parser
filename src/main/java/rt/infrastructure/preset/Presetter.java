package rt.infrastructure.preset;

import rt.infrastructure.notifier.Notifier;
import rt.model.preset.Preset;
import rt.model.service.PresetService;
import rt.utils.NumberUtils;

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
    private final String DEFAULT_NAME = "Последний запрос";
    private final Map<String, Preset> allPresets = new HashMap<>();

    public Presetter() {
        readPresets();
    }

    @Override
    public void createPreset(String source, LocalDate start, LocalDate end) {
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
        allPresets.put(DEFAULT_NAME, new Preset(DEFAULT_NAME, source, startDiff, endDiff));
        writePresets();
    }

    @Override
    public void renamePreset(String oldName, String newName) {
        Preset presetWithOldName = getPresetByName(oldName.trim());
        if (presetWithOldName == null) {
            Notifier.getInstance().addNotification("Нет такого запроса");
            return;
        }
        getAllPresets().put(
                newName.replaceAll("~", ""),
                new Preset(
                        newName,
                        presetWithOldName.getSource(),
                        presetWithOldName.getStart(),
                        presetWithOldName.getEnd()));
        removePresetByName(oldName);
        Notifier.getInstance().addNotification("Новое название сохранено");
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
                Integer startDiff = NumberUtils.parseIntegerOrGetNull(params[2]);
                Integer endDiff = NumberUtils.parseIntegerOrGetNull(params[3]);
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
}