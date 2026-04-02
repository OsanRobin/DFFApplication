package fenego.app.dto;

public class SegmentLogItemDTO
{
    private String id;
    private String direction;
    private String message;
    private String timestamp;

    public SegmentLogItemDTO() {}

    public SegmentLogItemDTO(String id, String direction, String message, String timestamp)
    {
        this.id = id;
        this.direction = direction;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}