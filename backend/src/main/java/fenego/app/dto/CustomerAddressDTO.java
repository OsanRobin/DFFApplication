package fenego.app.dto;

public class CustomerAddressDTO
{
    private String id;
    private String addressName;
    private String firstName;
    private String lastName;
    private String companyName1;
    private String addressLine1;
    private String postalCode;
    private String country;
    private String countryCode;
    private String city;
    private String street;
    private boolean shipFromAddress;
    private boolean serviceToAddress;
    private boolean installToAddress;
    private boolean invoiceToAddress;
    private boolean shipToAddress;
    private String company;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAddressName() { return addressName; }
    public void setAddressName(String addressName) { this.addressName = addressName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCompanyName1() { return companyName1; }
    public void setCompanyName1(String companyName1) { this.companyName1 = companyName1; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public boolean isShipFromAddress() { return shipFromAddress; }
    public void setShipFromAddress(boolean shipFromAddress) { this.shipFromAddress = shipFromAddress; }

    public boolean isServiceToAddress() { return serviceToAddress; }
    public void setServiceToAddress(boolean serviceToAddress) { this.serviceToAddress = serviceToAddress; }

    public boolean isInstallToAddress() { return installToAddress; }
    public void setInstallToAddress(boolean installToAddress) { this.installToAddress = installToAddress; }

    public boolean isInvoiceToAddress() { return invoiceToAddress; }
    public void setInvoiceToAddress(boolean invoiceToAddress) { this.invoiceToAddress = invoiceToAddress; }

    public boolean isShipToAddress() { return shipToAddress; }
    public void setShipToAddress(boolean shipToAddress) { this.shipToAddress = shipToAddress; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
}