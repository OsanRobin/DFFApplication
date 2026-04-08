package fenego.app.dto;



import java.util.ArrayList;
import java.util.List;

import fenego.app.jpa.Customer;

public class CustomerListResponse
{
    private int offset;
    private int limit;
    private int count;
    private List<Customer> data = new ArrayList<>();

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

    public List<Customer> getData()
    {
        return data;
    }

    public void setData(List<Customer> data)
    {
        this.data = data;
    }
}