package fenego.app.dto;

import java.util.List;

public class SavedCustomerSearchListResponse
{
    private int count;
    private List<SavedCustomerSearchDTO> data;

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public List<SavedCustomerSearchDTO> getData()
    {
        return data;
    }

    public void setData(List<SavedCustomerSearchDTO> data)
    {
        this.data = data;
    }
}