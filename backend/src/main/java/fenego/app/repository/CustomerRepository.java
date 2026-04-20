package fenego.app.repository;

import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerUserDTO;
import fenego.app.jpa.Customer;
import fenego.app.jpa.CustomerAddress;
import fenego.app.jpa.CustomerSegmentAssignment;

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

   public List<Customer> findCustomersByDomain(String domainName, int offset, int limit, String customerNo)
{
    String sql = """
        select
            c.CUSTOMERNO as id,
            c.CUSTOMERNO as customerNo,
            c.CUSTOMERTYPEID as customerType,
            coalesce(typeAv.STRINGVALUE, 'Customer') as type,
            coalesce(ca.COMPANYNAME1, ca.ADDRESSNAME, c.CUSTOMERNO) as displayName,
            coalesce(ca.COMPANYNAME1, ca.ADDRESSNAME, c.CUSTOMERNO) as companyName,
            ca.EMAIL as email,
            cast(1 as bit) as active
        from DOMAININFORMATION di
        join CUSTOMER c on di.DOMAINID = c.DOMAINID
        outer apply (
            select top 1 *
            from CUSTOMERADDRESS ca
            where ca.CUSTOMERID = c.UUID
        ) ca
        outer apply (
            select top 1 av.STRINGVALUE
            from CUSTOMER_AV av
            where av.OWNERID = c.UUID
              and av.NAME = 'CustomerType'
            order by av.LOCALIZEDFLAG asc, av.LOCALEID asc
        ) typeAv
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
        Customer customer = new Customer();
        customer.setId(rs.getString("id"));
        customer.setCustomerNo(rs.getString("customerNo"));
        customer.setCustomerType(rs.getString("customerType"));
        customer.setType(rs.getString("type"));
        customer.setDisplayName(rs.getString("displayName"));
        customer.setCompanyName(rs.getString("companyName"));
        customer.setEmail(rs.getString("email"));
        customer.setActive(rs.getBoolean("active"));
        return customer;
    });
}

    public int countCustomersByDomain(String domainName, String customerNo)
    {
        String sql = """
            select count(distinct c.UUID)
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

    public CustomerDetailResponse findCustomerDetailById(String customerId)
{
    String sql = """
        select top 1
            c.CUSTOMERNO as customerNo,
            coalesce(mainAddr.COMPANYNAME1, mainAddr.ADDRESSNAME, c.CUSTOMERNO) as companyName,
            c.CUSTOMERTYPEID as customerType,
            'STANDARD' as budgetPriceType,
            coalesce(typeAv.STRINGVALUE, 'Customer') as type,

            coalesce(invoiceAddr.ADDRESSID, fallbackAddr.ADDRESSID) as invoice_id,
            coalesce(invoiceAddr.ADDRESSNAME, fallbackAddr.ADDRESSNAME) as invoice_addressName,
            coalesce(invoiceAddr.FIRSTNAME, fallbackAddr.FIRSTNAME) as invoice_firstName,
            coalesce(invoiceAddr.LASTNAME, fallbackAddr.LASTNAME) as invoice_lastName,
            coalesce(invoiceAddr.COMPANYNAME1, fallbackAddr.COMPANYNAME1) as invoice_companyName1,
            coalesce(invoiceAddr.ADDRESSLINE1, fallbackAddr.ADDRESSLINE1) as invoice_addressLine1,
            coalesce(invoiceAddr.POSTALCODE, fallbackAddr.POSTALCODE) as invoice_postalCode,
            coalesce(invoiceAddr.COUNTRYCODE, fallbackAddr.COUNTRYCODE) as invoice_country,
            coalesce(invoiceAddr.COUNTRYCODE, fallbackAddr.COUNTRYCODE) as invoice_countryCode,
            coalesce(invoiceAddr.CITY, fallbackAddr.CITY) as invoice_city,
            coalesce(invoiceAddr.ADDRESSLINE1, fallbackAddr.ADDRESSLINE1) as invoice_street,
            cast(0 as bit) as invoice_shipFromAddress,
            cast(0 as bit) as invoice_serviceToAddress,
            cast(0 as bit) as invoice_installToAddress,
            cast(1 as bit) as invoice_invoiceToAddress,
            cast(0 as bit) as invoice_shipToAddress,
            coalesce(invoiceAddr.COMPANYNAME1, fallbackAddr.COMPANYNAME1, invoiceAddr.ADDRESSNAME, fallbackAddr.ADDRESSNAME, '') as invoice_company,

            coalesce(shipAddr.ADDRESSID, fallbackAddr.ADDRESSID) as ship_id,
            coalesce(shipAddr.ADDRESSNAME, fallbackAddr.ADDRESSNAME) as ship_addressName,
            coalesce(shipAddr.FIRSTNAME, fallbackAddr.FIRSTNAME) as ship_firstName,
            coalesce(shipAddr.LASTNAME, fallbackAddr.LASTNAME) as ship_lastName,
            coalesce(shipAddr.COMPANYNAME1, fallbackAddr.COMPANYNAME1) as ship_companyName1,
            coalesce(shipAddr.ADDRESSLINE1, fallbackAddr.ADDRESSLINE1) as ship_addressLine1,
            coalesce(shipAddr.POSTALCODE, fallbackAddr.POSTALCODE) as ship_postalCode,
            coalesce(shipAddr.COUNTRYCODE, fallbackAddr.COUNTRYCODE) as ship_country,
            coalesce(shipAddr.COUNTRYCODE, fallbackAddr.COUNTRYCODE) as ship_countryCode,
            coalesce(shipAddr.CITY, fallbackAddr.CITY) as ship_city,
            coalesce(shipAddr.ADDRESSLINE1, fallbackAddr.ADDRESSLINE1) as ship_street,
            cast(0 as bit) as ship_shipFromAddress,
            cast(0 as bit) as ship_serviceToAddress,
            cast(0 as bit) as ship_installToAddress,
            cast(0 as bit) as ship_invoiceToAddress,
            cast(1 as bit) as ship_shipToAddress,
            coalesce(shipAddr.COMPANYNAME1, fallbackAddr.COMPANYNAME1, shipAddr.ADDRESSNAME, fallbackAddr.ADDRESSNAME, '') as ship_company
        from CUSTOMER c
        outer apply (
            select top 1 *
            from CUSTOMERADDRESS a
            where a.CUSTOMERID = c.UUID
        ) mainAddr
        outer apply (
            select top 1 *
            from CUSTOMERADDRESS a
            where a.CUSTOMERID = c.UUID
        ) fallbackAddr
        outer apply (
            select top 1 *
            from CUSTOMERADDRESS a
            where a.CUSTOMERID = c.UUID
              and a.USAGE = 2
        ) invoiceAddr
        outer apply (
            select top 1 *
            from CUSTOMERADDRESS a
            where a.CUSTOMERID = c.UUID
              and a.USAGE = 3
        ) shipAddr
        outer apply (
            select top 1 av.STRINGVALUE
            from CUSTOMER_AV av
            where av.OWNERID = c.UUID
              and av.NAME = 'CustomerType'
            order by av.LOCALIZEDFLAG asc, av.LOCALEID asc
        ) typeAv
        where c.CUSTOMERNO = :customerId
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("customerId", customerId);

    List<CustomerDetailResponse> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
        CustomerDetailResponse response = new CustomerDetailResponse();
        response.setCustomerNo(rs.getString("customerNo"));
        response.setCompanyName(rs.getString("companyName"));
        response.setCustomerType(rs.getString("customerType"));
        response.setBudgetPriceType(rs.getString("budgetPriceType"));
        response.setType(rs.getString("type"));

        CustomerAddress invoice = new CustomerAddress();
        invoice.setId(rs.getString("invoice_id"));
        invoice.setAddressName(rs.getString("invoice_addressName"));
        invoice.setFirstName(rs.getString("invoice_firstName"));
        invoice.setLastName(rs.getString("invoice_lastName"));
        invoice.setCompanyName1(rs.getString("invoice_companyName1"));
        invoice.setAddressLine1(rs.getString("invoice_addressLine1"));
        invoice.setPostalCode(rs.getString("invoice_postalCode"));
        invoice.setCountry(rs.getString("invoice_country"));
        invoice.setCountryCode(rs.getString("invoice_countryCode"));
        invoice.setCity(rs.getString("invoice_city"));
        invoice.setStreet(rs.getString("invoice_street"));
        invoice.setShipFromAddress(rs.getBoolean("invoice_shipFromAddress"));
        invoice.setServiceToAddress(rs.getBoolean("invoice_serviceToAddress"));
        invoice.setInstallToAddress(rs.getBoolean("invoice_installToAddress"));
        invoice.setInvoiceToAddress(rs.getBoolean("invoice_invoiceToAddress"));
        invoice.setShipToAddress(rs.getBoolean("invoice_shipToAddress"));
        invoice.setCompany(rs.getString("invoice_company"));
        response.setPreferredInvoiceToAddress(invoice);

        CustomerAddress ship = new CustomerAddress();
        ship.setId(rs.getString("ship_id"));
        ship.setAddressName(rs.getString("ship_addressName"));
        ship.setFirstName(rs.getString("ship_firstName"));
        ship.setLastName(rs.getString("ship_lastName"));
        ship.setCompanyName1(rs.getString("ship_companyName1"));
        ship.setAddressLine1(rs.getString("ship_addressLine1"));
        ship.setPostalCode(rs.getString("ship_postalCode"));
        ship.setCountry(rs.getString("ship_country"));
        ship.setCountryCode(rs.getString("ship_countryCode"));
        ship.setCity(rs.getString("ship_city"));
        ship.setStreet(rs.getString("ship_street"));
        ship.setShipFromAddress(rs.getBoolean("ship_shipFromAddress"));
        ship.setServiceToAddress(rs.getBoolean("ship_serviceToAddress"));
        ship.setInstallToAddress(rs.getBoolean("ship_installToAddress"));
        ship.setInvoiceToAddress(rs.getBoolean("ship_invoiceToAddress"));
        ship.setShipToAddress(rs.getBoolean("ship_shipToAddress"));
        ship.setCompany(rs.getString("ship_company"));
        response.setPreferredShipToAddress(ship);

        return response;
    });

    return results.isEmpty() ? null : results.get(0);
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
    public List<String> findSegmentIdsByCustomerNo(String customerNo)
{
    String sql = """
        select distinct
            ugua.USERGROUPID as segmentId
        from CUSTOMER c
        join CUSTOMERPROFILEASSIGNMENT cpa
            on c.UUID = cpa.CUSTOMERID
        join USERGROUPUSERASSIGNMENT ugua
            on ugua.USERID = cpa.PROFILEID
        where c.CUSTOMERNO = :customerNo
        order by ugua.USERGROUPID
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("customerNo", customerNo);

    return jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getString("segmentId"));
}

public List<CustomerSegmentAssignment> findCustomerSegmentAssignmentsByDomain(String domainName)
{
    String sql = """
        select distinct
            c.CUSTOMERNO as customerNo,
            ugua.USERGROUPID as segmentId
        from DOMAININFORMATION di
        join CUSTOMER c
            on di.DOMAINID = c.DOMAINID
        join CUSTOMERPROFILEASSIGNMENT cpa
            on c.UUID = cpa.CUSTOMERID
        join USERGROUPUSERASSIGNMENT ugua
            on ugua.USERID = cpa.PROFILEID
        where di.DOMAINNAME = :domainName
        order by c.CUSTOMERNO, ugua.USERGROUPID
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("domainName", domainName);

    return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
        CustomerSegmentAssignment assignment = new CustomerSegmentAssignment();
        assignment.setCustomerNo(rs.getString("customerNo"));
        assignment.setSegmentId(rs.getString("segmentId"));
        return assignment;
    });
}
}