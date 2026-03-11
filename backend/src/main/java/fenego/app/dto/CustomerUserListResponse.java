package fenego.app.dto;

import java.util.ArrayList;
import java.util.List;

public class CustomerUserListResponse
{
    private String type;
    private String name;
    private int amount;
    private List<CustomerUserDTO> elements = new ArrayList<>();
    private int offset;
    private int limit;
    private List<String> sortKeys = new ArrayList<>();

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    public List<CustomerUserDTO> getElements()
    {
        return elements;
    }

    public void setElements(List<CustomerUserDTO> elements)
    {
        this.elements = elements;
    }

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

    public List<String> getSortKeys()
    {
        return sortKeys;
    }

    public void setSortKeys(List<String> sortKeys)
    {
        this.sortKeys = sortKeys;
    }
}