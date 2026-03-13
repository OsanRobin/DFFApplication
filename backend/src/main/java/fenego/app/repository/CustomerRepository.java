package fenego.app.repository;

import fenego.app.dto.CustomerAddressDTO;
import fenego.app.dto.CustomerDTO;
import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerUserDTO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomerRepository
{
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CustomerRepository(NamedParameterJdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CustomerDTO> findCustomersByDomain(String domainName, int offset, int limit, String customerNo)
    {
        String sql = """
            select
                c.CUSTOMERNO as id,
                c.CUSTOMERNO as customerNo,
                'SMB' as customerType,
                coalesce(addr.COMPANYNAME1, addr.ADDRESSNAME, c.CUSTOMERNO) as displayName,
                coalesce(addr.COMPANYNAME1, addr.ADDRESSNAME, c.CUSTOMERNO) as companyName,
                addr.EMAIL as email,
                cast(1 as bit) as active
            from DOMAININFORMATION di
            join CUSTOMER c on di.DOMAINID = c.DOMAINID
            outer apply (
                select top 1
                    ca.COMPANYNAME1,
                    ca.ADDRESSNAME,
                    ca.EMAIL
                from CUSTOMERADDRESS ca
                where ca.CUSTOMERID = c.UUID
                order by ca.UUID
            ) addr
            where di.DOMAINNAME = :domainName
              and (:customerNo is null or c.CUSTOMERNO like '%' + :customerNo + '%')
            order by c.CUSTOMERNO
            offset :offset rows fetch next :limit rows only
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("domainName", domainName)
                .addValue("customerNo", customerNo == null || customerNo.isBlank() ? null : customerNo)
                .addValue("offset", offset)
                .addValue("limit", limit);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            CustomerDTO dto = new CustomerDTO();
            dto.setId(rs.getString("id"));
            dto.setCustomerNo(rs.getString("customerNo"));
            dto.setCustomerType(rs.getString("customerType"));
            dto.setDisplayName(rs.getString("displayName"));
            dto.setCompanyName(rs.getString("companyName"));
            dto.setEmail(rs.getString("email"));
            dto.setActive(rs.getBoolean("active"));
            return dto;
        });
    }

    public int countCustomersByDomain(String domainName, String customerNo)
    {
        String sql = """
            select count(*)
            from DOMAININFORMATION di
            join CUSTOMER c on di.DOMAINID = c.DOMAINID
            where di.DOMAINNAME = :domainName
              and (:customerNo is null or c.CUSTOMERNO like '%' + :customerNo + '%')
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("domainName", domainName)
                .addValue("customerNo", customerNo == null || customerNo.isBlank() ? null : customerNo);

        Integer result = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return result == null ? 0 : result;
    }

   public CustomerDetailResponse findCustomerDetailByCustomerNo(String customerId)
{
    String sql = """
        select top 1
            c.CUSTOMERNO as customerNo,
            coalesce(addr.COMPANYNAME1, addr.ADDRESSNAME, c.CUSTOMERNO) as companyName
        from CUSTOMER c
        outer apply (
            select top 1
                ca.COMPANYNAME1,
                ca.ADDRESSNAME
            from CUSTOMERADDRESS ca
            where ca.CUSTOMERID = c.UUID
        ) addr
        where c.CUSTOMERNO = :customerId
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("customerId", customerId);

    List<CustomerDetailResponse> result = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
        CustomerDetailResponse dto = new CustomerDetailResponse();
        dto.setCustomerNo(rs.getString("customerNo"));
        dto.setCompanyName(rs.getString("companyName"));
        dto.setCustomerType("SMB");
        dto.setBudgetPriceType("gross");
        dto.setType("SMBCustomer");

        CustomerAddressDTO emptyInvoice = new CustomerAddressDTO();
        emptyInvoice.setId("");
        emptyInvoice.setAddressName("");
        emptyInvoice.setFirstName("");
        emptyInvoice.setLastName("");
        emptyInvoice.setCompanyName1(rs.getString("companyName"));
        emptyInvoice.setAddressLine1("");
        emptyInvoice.setPostalCode("");
        emptyInvoice.setCountry("");
        emptyInvoice.setCountryCode("");
        emptyInvoice.setCity("");
        emptyInvoice.setStreet("");
        emptyInvoice.setShipFromAddress(false);
        emptyInvoice.setServiceToAddress(false);
        emptyInvoice.setInstallToAddress(false);
        emptyInvoice.setInvoiceToAddress(false);
        emptyInvoice.setShipToAddress(false);
        emptyInvoice.setCompany(rs.getString("companyName"));

        CustomerAddressDTO emptyShip = new CustomerAddressDTO();
        emptyShip.setId("");
        emptyShip.setAddressName("");
        emptyShip.setFirstName("");
        emptyShip.setLastName("");
        emptyShip.setCompanyName1(rs.getString("companyName"));
        emptyShip.setAddressLine1("");
        emptyShip.setPostalCode("");
        emptyShip.setCountry("");
        emptyShip.setCountryCode("");
        emptyShip.setCity("");
        emptyShip.setStreet("");
        emptyShip.setShipFromAddress(false);
        emptyShip.setServiceToAddress(false);
        emptyShip.setInstallToAddress(false);
        emptyShip.setInvoiceToAddress(false);
        emptyShip.setShipToAddress(false);
        emptyShip.setCompany(rs.getString("companyName"));

        dto.setPreferredInvoiceToAddress(emptyInvoice);
        dto.setPreferredShipToAddress(emptyShip);

        return dto;
    });

    return result.isEmpty() ? null : result.get(0);
}

    public List<CustomerUserDTO> findUsersByCustomerId(String customerId)
    {
        String sql = """
            select bp.BUSINESSPARTNERNO as businessPartnerNo
            from CUSTOMER c
            join CUSTOMERPROFILEASSIGNMENT cpa on c.UUID = cpa.CUSTOMERID
            join BASICPROFILE bp on cpa.PROFILEID = bp.UUID
            where c.CUSTOMERNO = :customerId
            order by bp.BUSINESSPARTNERNO
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerId", customerId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            CustomerUserDTO dto = new CustomerUserDTO();
            dto.setBusinessPartnerNo(rs.getString("businessPartnerNo"));
            dto.setLogin(rs.getString("businessPartnerNo"));
            dto.setName(rs.getString("businessPartnerNo"));
            dto.setFirstName("");
            dto.setLastName("");
            dto.setActive(true);
            dto.setRoleIds(List.of());
            dto.setRoleNames(List.of());
            dto.setBudgetPeriod("none");
            dto.setPendingOneTimeRequisitionsCount(0);
            dto.setPendingRecurringRequisitionsCount(0);
            return dto;
        });
    }

    private CustomerAddressDTO mapAddress(
            String id,
            String addressName,
            String firstName,
            String lastName,
            String companyName1,
            String addressLine1,
            String postalCode,
            String countryCode,
            String city,
            String street,
            boolean invoiceToAddress,
            boolean shipToAddress
    )
    {
        if (id == null || id.isBlank())
        {
            return null;
        }

        CustomerAddressDTO address = new CustomerAddressDTO();
        address.setId(id);
        address.setAddressName(addressName);
        address.setFirstName(firstName);
        address.setLastName(lastName);
        address.setCompanyName1(companyName1);
        address.setAddressLine1(addressLine1);
        address.setPostalCode(postalCode);
        address.setCountry(countryCode);
        address.setCountryCode(countryCode);
        address.setCity(city);
        address.setStreet(street);
        address.setShipFromAddress(false);
        address.setServiceToAddress(false);
        address.setInstallToAddress(false);
        address.setInvoiceToAddress(invoiceToAddress);
        address.setShipToAddress(shipToAddress);
        address.setCompany(companyName1);
        return address;
    }

    private String firstNonBlank(String... values)
    {
        for (String value : values)
        {
            if (value != null && !value.isBlank())
            {
                return value;
            }
        }
        return null;
    }
}