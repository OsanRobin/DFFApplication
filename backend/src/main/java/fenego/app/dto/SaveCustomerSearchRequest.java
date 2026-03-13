package fenego.app.dto;

public class SaveCustomerSearchRequest
{
    private String domainName;
    private String name;
    private String query;
    private String customerNo;
    private String typeFilter;
    private String statusFilter;
    private boolean overwrite;

    public String getDomainName()
    {
        return domainName;
    }

    public void setDomainName(String domainName)
    {
        this.domainName = domainName;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public String getCustomerNo()
    {
        return customerNo;
    }

    public void setCustomerNo(String customerNo)
    {
        this.customerNo = customerNo;
    }

    public String getTypeFilter()
    {
        return typeFilter;
    }

    public void setTypeFilter(String typeFilter)
    {
        this.typeFilter = typeFilter;
    }

    public String getStatusFilter()
    {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter)
    {
        this.statusFilter = statusFilter;
    }

    public boolean isOverwrite()
    {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite)
    {
        this.overwrite = overwrite;
    }
}