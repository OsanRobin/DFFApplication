package fenego.app.dto;

import java.util.List;

public class CustomerSegmentListResponse
{
    private List<CustomerSegmentItemDTO> data;
    private CustomerSegmentInfoDTO info;

    public CustomerSegmentListResponse() {}

    public List<CustomerSegmentItemDTO> getData()
    {
        return data;
    }

    public void setData(List<CustomerSegmentItemDTO> data)
    {
        this.data = data;
    }

    public CustomerSegmentInfoDTO getInfo()
    {
        return info;
    }

    public void setInfo(CustomerSegmentInfoDTO info)
    {
        this.info = info;
    }
}