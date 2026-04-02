package fenego.app.dto;

public class CustomerSegmentItemDTO
{
    private String id;
    private CustomerSegmentDataDTO data;

    public CustomerSegmentItemDTO() {}

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public CustomerSegmentDataDTO getData()
    {
        return data;
    }

    public void setData(CustomerSegmentDataDTO data)
    {
        this.data = data;
    }
}