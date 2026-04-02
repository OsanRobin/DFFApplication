package fenego.app.dto;

public class SegmentDTO
{
    private String id;
    private String name;
    private String description;
    private String rule;
    private int matchedCustomers;
    private String lastUpdated;
    private boolean autoUpdated;

    public SegmentDTO() {}

    public SegmentDTO(String id, String name, String description, String rule, int matchedCustomers, String lastUpdated, boolean autoUpdated)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rule = rule;
        this.matchedCustomers = matchedCustomers;
        this.lastUpdated = lastUpdated;
        this.autoUpdated = autoUpdated;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRule() { return rule; }
    public void setRule(String rule) { this.rule = rule; }

    public int getMatchedCustomers() { return matchedCustomers; }
    public void setMatchedCustomers(int matchedCustomers) { this.matchedCustomers = matchedCustomers; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public boolean isAutoUpdated() { return autoUpdated; }
    public void setAutoUpdated(boolean autoUpdated) { this.autoUpdated = autoUpdated; }
}