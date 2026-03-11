package fenego.app.dto;

import java.util.ArrayList;
import java.util.List;

public class CustomerListResponse
{
    private int offset;
    private int limit;
    private int count;
    private List<CustomerDTO> data = new ArrayList<>();

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public int getLimit()
    {
        return limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public List<CustomerDTO> getData()
    {
        return data;
    }

    public void setData(List<CustomerDTO> data)
    {
        this.data = data;
    }
}