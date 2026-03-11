package fenego.app.service;

import fenego.app.dto.CustomerAddressDTO;
import fenego.app.dto.CustomerDTO;
import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerListResponse;
import fenego.app.dto.CustomerUserDTO;
import fenego.app.dto.CustomerUserListResponse;
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

    public CustomerUserListResponse getCustomerUsers(String customerId)
    {
        if ("10776".equals(customerId))
        {
            return buildUsersFor10776();
        }

        CustomerUserListResponse response = new CustomerUserListResponse();
        response.setType("UserLinkCollection");
        response.setName("users");
        response.setAmount(0);
        response.setOffset(0);
        response.setLimit(50);
        response.setSortKeys(List.of("name"));
        return response;
    }

    private CustomerListResponse getMockCustomers(int offset, int limit, String customerNo, String email)
    {
        List<CustomerDTO> allCustomers = List.of(
                createCustomer("P190301", "P190301", "SMB", "Angela Vroemen-Schepers", "Angela Vroemen-Schepers", "angela@test.com", "Enterprise", true),
                createCustomer("16280", "16280", "SMB", "Bulk testklant", "Bulk testklant", "bulk@test.com", "Standard", true),
                createCustomer("TESTKSW1", "TESTKSW1", "SMB", "KSW Demo 1", "KSW Demo 1", "ksw@test.com", "Premium", true),
                createCustomer("10776", "10776", "SMB", "Testaccount eCommerce", "Testaccount eCommerce", "ecommerce@test.com", "Standard", true)
        );

        List<CustomerDTO> filtered = allCustomers.stream()
                .filter(c ->
                        customerNo == null
                                || customerNo.isBlank()
                                || (c.getCustomerNo() != null
                                && c.getCustomerNo().toLowerCase().contains(customerNo.toLowerCase()))
                )
                .filter(c ->
                        email == null
                                || email.isBlank()
                                || (c.getEmail() != null
                                && c.getEmail().toLowerCase().contains(email.toLowerCase()))
                )
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

    private CustomerDTO createCustomer(
            String id,
            String customerNo,
            String customerType,
            String displayName,
            String companyName,
            String email,
            String segment,
            boolean active
    )
    {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(id);
        customer.setCustomerNo(customerNo);
        customer.setCustomerType(customerType);
        customer.setDisplayName(displayName);
        customer.setCompanyName(companyName);
        customer.setEmail(email);
        customer.setSegment(segment);
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

    private CustomerUserListResponse buildUsersFor10776()
    {
        CustomerUserListResponse response = new CustomerUserListResponse();
        response.setType("UserLinkCollection");
        response.setName("users");
        response.setOffset(0);
        response.setLimit(50);
        response.setSortKeys(List.of("name"));

        List<CustomerUserDTO> users = List.of(
                createUser("Harm Huijbregts", "harm.h", "Harm", "Huijbregts", true,
                        "0hdOKU4x6XcAAAFp8z4oPJaM",
                        List.of("APP_B2B_ACCOUNT_OWNER"),
                        List.of("Account Admin"),
                        "none", 0, 0),

                createUser("Iris Golsteijn", "iris.g", "Iris", "Golsteijn", true,
                        "markusgroenholm",
                        List.of(),
                        List.of(),
                        "none", 0, 0),

                createUser("Jeroen Nijs", "nijsj", "Jeroen", "Nijs", false,
                        "00000015",
                        List.of("APP_B2B_BUYER"),
                        List.of("Buyer"),
                        "none", 0, 0),

                createUser("JeroenDemo NijsDemo", "nijsjdemo", "JeroenDemo", "NijsDemo", false,
                        "00000016",
                        List.of("APP_B2B_BUYER"),
                        List.of("Buyer"),
                        "none", 0, 0),

                createUser("Mark Cox", "markcox", "Mark", "Cox", true,
                        "XpROKU4xOkUAAAFwvEdKeTSt",
                        List.of(),
                        List.of(),
                        "none", 0, 0),

                createUser("Mirjam Smetsers", "mirjam.s", "Mirjam", "Smetsers", true,
                        "mirjam.smetsers2",
                        List.of(),
                        List.of(),
                        "none", 0, 0),

                createUser("Roel Buyer", "dessertbuyer2", "Roel", "Buyer", true,
                        "cSVOKU4xb4IAAAGMDTpotIHp",
                        List.of(),
                        List.of(),
                        "none", 0, 0),

                createUser("Testaccount MyDaily", "willy", "Testaccount", "MyDaily", true,
                        "willy",
                        List.of("APP_DFF_BUYER", "APP_DFF_QUICK_ORDER_BUYER", "APP_DFF_ACCOUNT_OWNER", "APP_B2B_BUYER", "APP_B2B_ACCOUNT_OWNER"),
                        List.of("DFF Buyer", "DFF Quick Order Buyer", "DFF Account Owner", "Buyer", "Account Admin"),
                        "none", 0, 0),

                createUser("Testaccount eCommerce Personeel GROEP", "s10776", "Testaccount eCommerce", "Personeel GROEP", true,
                        "s10776",
                        List.of(),
                        List.of(),
                        "none", 0, 0),

                createUser("Willy Van Rijzingen", "willy.r", "Willy", "Van Rijzingen", true,
                        "qtBOKU4xT8oAAAFpOIAXnZXz",
                        List.of("APP_DFF_QUICK_ORDER_BUYER", "APP_DFF_ACCOUNT_OWNER", "APP_B2B_BUYER"),
                        List.of("DFF Quick Order Buyer", "DFF Account Owner", "Buyer"),
                        "none", 0, 0),

                createUser("Zuyderland Ariba", "d10776", "Zuyderland", "Ariba", true,
                        "d10776",
                        List.of("APP_B2B_ACCOUNT_OWNER"),
                        List.of("Account Admin"),
                        "none", 0, 0)
        );

        response.setElements(users);
        response.setAmount(users.size());

        return response;
    }

    private CustomerUserDTO createUser(String name,
                                       String login,
                                       String firstName,
                                       String lastName,
                                       boolean active,
                                       String businessPartnerNo,
                                       List<String> roleIds,
                                       List<String> roleNames,
                                       String budgetPeriod,
                                       int pendingOneTimeRequisitionsCount,
                                       int pendingRecurringRequisitionsCount)
    {
        CustomerUserDTO user = new CustomerUserDTO();
        user.setName(name);
        user.setLogin(login);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(active);
        user.setBusinessPartnerNo(businessPartnerNo);
        user.setRoleIds(roleIds);
        user.setRoleNames(roleNames);
        user.setBudgetPeriod(budgetPeriod);
        user.setPendingOneTimeRequisitionsCount(pendingOneTimeRequisitionsCount);
        user.setPendingRecurringRequisitionsCount(pendingRecurringRequisitionsCount);
        return user;
    }
}