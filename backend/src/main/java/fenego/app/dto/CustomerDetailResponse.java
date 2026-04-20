package fenego.app.dto;

import fenego.app.jpa.CustomerAddress;

import java.util.ArrayList;
import java.util.List;

public class CustomerDetailResponse
{
    private String customerNo;
    private String companyName;
    private String customerType;
    private String budgetPriceType;
    private String type;
    private CustomerAddress preferredInvoiceToAddress;
    private CustomerAddress preferredShipToAddress;
    private List<CustomerSegmentDTO> segments = new ArrayList<>();

    public String getCustomerNo()
    {
        return customerNo;
    }

    public void setCustomerNo(String customerNo)
    {
        this.customerNo = customerNo;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    public String getCustomerType()
    {
        return customerType;
    }

    public void setCustomerType(String customerType)
    {
        this.customerType = customerType;
    }

    public String getBudgetPriceType()
    {
        return budgetPriceType;
    }

    public void setBudgetPriceType(String budgetPriceType)
    {
        this.budgetPriceType = budgetPriceType;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public CustomerAddress getPreferredInvoiceToAddress()
    {
        return preferredInvoiceToAddress;
    }

    public void setPreferredInvoiceToAddress(CustomerAddress preferredInvoiceToAddress)
    {
        this.preferredInvoiceToAddress = preferredInvoiceToAddress;
    }

    public CustomerAddress getPreferredShipToAddress()
    {
        return preferredShipToAddress;
    }

    public void setPreferredShipToAddress(CustomerAddress preferredShipToAddress)
    {
        this.preferredShipToAddress = preferredShipToAddress;
    }

    public List<CustomerSegmentDTO> getSegments()
    {
        return segments;
    }

    public void setSegments(List<CustomerSegmentDTO> segments)
    {
        this.segments = segments;
    }
}