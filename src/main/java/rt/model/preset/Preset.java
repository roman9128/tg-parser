package rt.model.preset;

public class Preset {

    private final String name;
    private final String source;
    private final Integer start;
    private final Integer end;

    public Preset(String name, String source, Integer start, Integer end) {
        this.name = name;
        this.source = source;
        this.start = start;
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getEnd() {
        return end;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("~").append(source);
        if (start != null) {
            sb.append("~").append(start);
        }
        if (end != null) {
            sb.append("~").append(end);
        }
        return sb.toString();
    }
}