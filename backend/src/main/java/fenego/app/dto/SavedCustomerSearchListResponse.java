package fenego.app.dto;



import java.util.ArrayList;
import java.util.List;

import fenego.app.jpa.SavedCustomerSearch;

public class SavedCustomerSearchListResponse
{
    private List<SavedCustomerSearch> data = new ArrayList<>();
    private int count;

    public List<SavedCustomerSearch> getData()
    {
        return data;
    }

    public void setData(List<SavedCustomerSearch> data)
    {
        this.data = data;
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }
}