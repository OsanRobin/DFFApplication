package fenego.app.dto;

public class SegmentCustomerDTO
{
    private String id;
    private String customerNo;
    private String name;
    private String type;
    private String status;

    public SegmentCustomerDTO() {}

    public SegmentCustomerDTO(String id, String customerNo, String name, String type, String status)
    {
        this.id = id;
        this.customerNo = customerNo;
        this.name = name;
        this.type = type;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerNo() { return customerNo; }
    public void setCustomerNo(String customerNo) { this.customerNo = customerNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}