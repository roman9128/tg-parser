package rt.model.preset;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Preset {

    private final String name;
    private final List<Integer> groupIds;
    private final List<Long> channelIds;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Preset(String name, List<Integer> groupIds, List<Long> channelIds, LocalDateTime start, LocalDateTime end) {
        this.name = name;
        this.groupIds = groupIds;
        this.channelIds = channelIds;
        this.start = start;
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public String getChannelsAndGroups() {
        String groups = groupIds.stream().map(Object::toString).collect(Collectors.joining(","));
        String channels = channelIds.stream().map(Objects::toString).collect(Collectors.joining(","));
        return groups + "," + channels;
    }

    public String getStart() {
        return start.format(formatter);
    }

    public String getEnd() {
        return end.format(formatter);
    }
}