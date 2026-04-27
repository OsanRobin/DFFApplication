package fenego.app.dto;

import java.util.List;

public class BulkActionRequest
{
    private List<String> customerIds;
    private String action;
    private String attributeName;
    private String attributeValue;

    public List<String> getCustomerIds()
    {
        return customerIds;
    }

    public void setCustomerIds(List<String> customerIds)
    {
        this.customerIds = customerIds;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getAttributeName()
    {
        return attributeName;
    }

    public void setAttributeName(String attributeName)
    {
        this.attributeName = attributeName;
    }

    public String getAttributeValue()
    {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue)
    {
        this.attributeValue = attributeValue;
    }
}