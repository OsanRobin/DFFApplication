package fenego.app.service;

import fenego.app.dto.CustomerAddressDTO;
import fenego.app.dto.CustomerDTO;
import fenego.app.dto.CustomerDetailResponse;

import fenego.app.dto.CustomerListResponse;
import fenego.app.intershop.IntershopClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService
{
    private final IntershopClient intershopClient;

    public CustomerService(IntershopClient intershopClient)
    {
        this.intershopClient = intershopClient;
    }

    public CustomerListResponse getCustomers(String authenticationToken, int offset, int limit, String customerNo, String email)
    {
        return getMockCustomers(offset, limit, customerNo, email);
    }

    public CustomerDetailResponse getCustomerById(String customerId)
    {
        if ("10776".equals(customerId))
        {
            return buildTestaccountEcommerce();
        }

        if ("16280".equals(customerId))
        {
            return buildSimpleBusinessCustomer("16280", "Bulk testklant");
        }

        if ("TESTKSW1".equals(customerId))
        {
            return buildSimpleBusinessCustomer("TESTKSW1", "KSW Demo 1");
        }

        if ("P190301".equals(customerId))
        {
            return buildSimpleBusinessCustomer("P190301", "Angela Vroemen-Schepers");
        }

        throw new RuntimeException("Customer not found: " + customerId);
    }

    private CustomerListResponse getMockCustomers(int offset, int limit, String customerNo, String email)
    {
        List<CustomerDTO> allCustomers = List.of(
                createCustomer("P190301", "P190301", "SMB", "Angela Vroemen-Schepers", "Angela Vroemen-Schepers", "angela@test.com", true),
                createCustomer("16280", "16280", "SMB", "Bulk testklant", "Bulk testklant", "bulk@test.com", true),
                createCustomer("TESTKSW1", "TESTKSW1", "SMB", "KSW Demo 1", "KSW Demo 1", "ksw@test.com", true),
                createCustomer("10776", "10776", "SMB", "Testaccount eCommerce", "Testaccount eCommerce", "ecommerce@test.com", true)
        );

        List<CustomerDTO> filtered = allCustomers.stream()
                .filter(c -> customerNo == null || customerNo.isBlank()
                        || (c.getCustomerNo() != null && c.getCustomerNo().toLowerCase().contains(customerNo.toLowerCase())))
                .filter(c -> email == null || email.isBlank()
                        || (c.getEmail() != null && c.getEmail().toLowerCase().contains(email.toLowerCase())))
                .toList();

        int safeOffset = Math.max(offset, 0);
        int safeLimit = Math.max(limit, 1);

        int fromIndex = Math.min(safeOffset, filtered.size());
        int toIndex = Math.min(fromIndex + safeLimit, filtered.size());

        CustomerListResponse response = new CustomerListResponse();
        response.setOffset(safeOffset);
        response.setLimit(safeLimit);
        response.setCount(filtered.size());
        response.setData(filtered.subList(fromIndex, toIndex));

        return response;
    }

    private CustomerDTO createCustomer(String id, String customerNo, String customerType, String displayName,
                                       String companyName, String email, boolean active)
    {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(id);
        customer.setCustomerNo(customerNo);
        customer.setCustomerType(customerType);
        customer.setDisplayName(displayName);
        customer.setCompanyName(companyName);
        customer.setEmail(email);
        customer.setActive(active);
        return customer;
    }

    private CustomerDetailResponse buildTestaccountEcommerce()
    {
        CustomerDetailResponse customer = new CustomerDetailResponse();
        customer.setCustomerNo("10776");
        customer.setCompanyName("Testaccount eCommerce");
        customer.setCustomerType("SMB");
        customer.setBudgetPriceType("gross");
        customer.setType("SMBCustomer");
        customer.setPreferredInvoiceToAddress(buildInvoiceAddress10776());
        customer.setPreferredShipToAddress(buildShipAddress10776());
        return customer;
    }

    private CustomerDetailResponse buildSimpleBusinessCustomer(String customerNo, String companyName)
    {
        CustomerDetailResponse customer = new CustomerDetailResponse();
        customer.setCustomerNo(customerNo);
        customer.setCompanyName(companyName);
        customer.setCustomerType("SMB");
        customer.setBudgetPriceType("gross");
        customer.setType("SMBCustomer");

        CustomerAddressDTO address = new CustomerAddressDTO();
        address.setId("ADDR-" + customerNo);
        address.setAddressName("Main Address");
        address.setFirstName(customerNo);
        address.setLastName(companyName);
        address.setCompanyName1(companyName);
        address.setAddressLine1("Example Street 1");
        address.setPostalCode("1000 AA");
        address.setCountry("Netherlands");
        address.setCountryCode("NL");
        address.setCity("Amsterdam");
        address.setStreet("Example Street 1");
        address.setInvoiceToAddress(true);
        address.setShipToAddress(true);
        address.setCompany(companyName);

        customer.setPreferredInvoiceToAddress(address);
        customer.setPreferredShipToAddress(address);
        return customer;
    }

    private CustomerAddressDTO buildInvoiceAddress10776()
    {
        CustomerAddressDTO address = new CustomerAddressDTO();
        address.setId("N_ysGAAEhawAAAGURM8ADtYa");
        address.setAddressName("Address1-10776");
        address.setFirstName("10776");
        address.setLastName("Testaccount eCommerce");
        address.setCompanyName1("Testaccount eCommerce");
        address.setAddressLine1("Aan het Beekerkruis 1");
        address.setPostalCode("6191 LD");
        address.setCountry("Netherlands");
        address.setCountryCode("NL");
        address.setCity("BEEK");
        address.setStreet("Aan het Beekerkruis 1");
        address.setShipFromAddress(false);
        address.setServiceToAddress(false);
        address.setInstallToAddress(false);
        address.setInvoiceToAddress(true);
        address.setShipToAddress(true);
        address.setCompany("Testaccount eCommerce");
        return address;
    }

    private CustomerAddressDTO buildShipAddress10776()
    {
        CustomerAddressDTO address = new CustomerAddressDTO();
        address.setId("eMasGAAEnxkAAAGUQc8ADtYa");
        address.setAddressName("Address2-10776");
        address.setFirstName("10776");
        address.setLastName("Testaccount eCommerce");
        address.setCompanyName1("Testaccount eCommerce");
        address.setAddressLine1("Aan het Beekerkruis 1");
        address.setPostalCode("6191 LD");
        address.setCountry("Netherlands");
        address.setCountryCode("NL");
        address.setCity("BEEK");
        address.setStreet("Aan het Beekerkruis 1");
        address.setShipFromAddress(false);
        address.setServiceToAddress(false);
        address.setInstallToAddress(false);
        address.setInvoiceToAddress(false);
        address.setShipToAddress(true);
        address.setCompany("Testaccount eCommerce");
        return address;
    }
}