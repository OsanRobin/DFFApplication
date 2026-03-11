package fenego.app.dto;

public class CustomerDetailResponse
{
    private String customerNo;
    private String companyName;
    private String customerType;
    private String budgetPriceType;
    private String type;
    private CustomerAddressDTO preferredInvoiceToAddress;
    private CustomerAddressDTO preferredShipToAddress;

    public String getCustomerNo() { return customerNo; }
    public void setCustomerNo(String customerNo) { this.customerNo = customerNo; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }

    public String getBudgetPriceType() { return budgetPriceType; }
    public void setBudgetPriceType(String budgetPriceType) { this.budgetPriceType = budgetPriceType; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public CustomerAddressDTO getPreferredInvoiceToAddress() { return preferredInvoiceToAddress; }
    public void setPreferredInvoiceToAddress(CustomerAddressDTO preferredInvoiceToAddress) { this.preferredInvoiceToAddress = preferredInvoiceToAddress; }

    public CustomerAddressDTO getPreferredShipToAddress() { return preferredShipToAddress; }
    public void setPreferredShipToAddress(CustomerAddressDTO preferredShipToAddress) { this.preferredShipToAddress = preferredShipToAddress; }
}