package fenego.app.repository;

import fenego.app.dto.CustomerAttributeDTO;
import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerUserAttributeDTO;
import fenego.app.dto.CustomerUserDTO;
import fenego.app.jpa.Customer;
import fenego.app.jpa.CustomerAddress;
import fenego.app.jpa.CustomerSegmentAssignment;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CustomerRepository
{
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CustomerRepository(NamedParameterJdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Customer> findCustomersByDomain(
            String domainName,
            int offset,
            int limit,
            String customerNo,
            String query,
            String type,
            String status,
            String segment)
    {
        String sql = """
            select
                x.id,
                x.customerNo,
                x.customerType,
                x.type,
                x.displayName,
                x.companyName,
                x.email,
                x.active,
                x.locations,
                x.customerList
            from (
                select
                    c.CUSTOMERNO as id,
                    c.CUSTOMERNO as customerNo,
                    c.CUSTOMERTYPEID as customerType,
                    coalesce(typeAv.STRINGVALUE, 'Customer') as type,
                    coalesce(mainAddr.COMPANYNAME1, mainAddr.ADDRESSNAME, c.CUSTOMERNO) as displayName,
                    coalesce(mainAddr.COMPANYNAME1, mainAddr.ADDRESSNAME, c.CUSTOMERNO) as companyName,
                    mainAddr.EMAIL as email,
                    case
                        when c.APPROVALSTATUS = 1 then cast(1 as bit)
                        else cast(0 as bit)
                    end as active,
                    count(distinct addrCount.ADDRESSID) as locations,
                    customerListAv.customerList as customerList,
                    row_number() over (
                        partition by c.UUID
                        order by c.CUSTOMERNO
                    ) as rn
                from DOMAININFORMATION di
                join CUSTOMER c
                    on di.DOMAINID = c.DOMAINID
                outer apply (
                    select top 1 *
                    from CUSTOMERADDRESS ca
                    where ca.CUSTOMERID = c.UUID
                    order by ca.ADDRESSID
                ) mainAddr
                outer apply (
                    select top 1 av.STRINGVALUE
                    from CUSTOMER_AV av
                    where av.OWNERID = c.UUID
                      and av.NAME = 'CustomerType'
                    order by av.LOCALIZEDFLAG asc, av.LOCALEID asc
                ) typeAv
                outer apply (
                    select top 1 coalesce(av.STRINGVALUE, av.TEXTVALUE) as customerList
                    from CUSTOMER_AV av
                    where av.OWNERID = c.UUID
                      and av.NAME = 'CustomerList'
                    order by av.LOCALIZEDFLAG asc, av.LOCALEID asc
                ) customerListAv
                left join CUSTOMERADDRESS addrCount
                    on addrCount.CUSTOMERID = c.UUID
                where di.DOMAINNAME = :domainName
                  and (:customerNo is null or c.CUSTOMERNO like '%' + :customerNo + '%')
                  and (
                        :query is null
                        or c.CUSTOMERNO like '%' + :query + '%'
                        or coalesce(mainAddr.COMPANYNAME1, mainAddr.ADDRESSNAME, c.CUSTOMERNO) like '%' + :query + '%'
                      )
                  and (:type is null or coalesce(typeAv.STRINGVALUE, 'Customer') = :type)
                  and (
                        :status is null
                        or (:status = 'Active' and c.APPROVALSTATUS = 1)
                        or (:status = 'Inactive' and c.APPROVALSTATUS <> 1)
                      )
                  and (
                        :segment is null
                        or exists (
                            select 1
                            from CUSTOMERPROFILEASSIGNMENT cpa2
                            join USERGROUPUSERASSIGNMENT ugua2
                                on ugua2.USERID = cpa2.PROFILEID
                            where cpa2.CUSTOMERID = c.UUID
                              and lower(ugua2.USERGROUPID) like '%' + lower(:segment) + '%'
                        )
                      )
                group by
                    c.UUID,
                    c.CUSTOMERNO,
                    c.CUSTOMERTYPEID,
                    c.APPROVALSTATUS,
                    typeAv.STRINGVALUE,
                    customerListAv.customerList,
                    mainAddr.COMPANYNAME1,
                    mainAddr.ADDRESSNAME,
                    mainAddr.EMAIL
            ) x
            where x.rn = 1
            order by x.customerNo
            offset :offset rows fetch next :limit rows only
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("domainName", domainName)
                .addValue("customerNo", isBlank(customerNo) ? null : customerNo.trim())
                .addValue("query", isBlank(query) ? null : query.trim())
                .addValue("type", isBlank(type) ? null : type.trim())
                .addValue("status", isBlank(status) ? null : status.trim())
                .addValue("segment", isBlank(segment) ? null : segment.trim())
                .addValue("offset", offset)
                .addValue("limit", limit);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapCustomer(rs));
    }

    public int countCustomersByDomain(
            String domainName,
            String customerNo,
            String query,
            String type,
            String status,
            String segment)
    {
        String sql = """
            select count(*)
            from CUSTOMER c
            where exists (
                select 1
                from DOMAININFORMATION di
                where di.DOMAINID = c.DOMAINID
                  and di.DOMAINNAME = :domainName
            )
              and (:customerNo is null or c.CUSTOMERNO like '%' + :customerNo + '%')
              and (
                    :query is null
                    or c.CUSTOMERNO like '%' + :query + '%'
                    or exists (
                        select 1
                        from CUSTOMERADDRESS ca
                        where ca.CUSTOMERID = c.UUID
                          and coalesce(ca.COMPANYNAME1, ca.ADDRESSNAME, c.CUSTOMERNO) like '%' + :query + '%'
                    )
                  )
              and (
                    :type is null
                    or coalesce((
                        select top 1 av.STRINGVALUE
                        from CUSTOMER_AV av
                        where av.OWNERID = c.UUID
                          and av.NAME = 'CustomerType'
                        order by av.LOCALIZEDFLAG asc, av.LOCALEID asc
                    ), 'Customer') = :type
                  )
              and (
                    :status is null
                    or (:status = 'Active' and c.APPROVALSTATUS = 1)
                    or (:status = 'Inactive' and c.APPROVALSTATUS <> 1)
                  )
              and (
                    :segment is null
                    or exists (
                        select 1
                        from CUSTOMERPROFILEASSIGNMENT cpa
                        join USERGROUPUSERASSIGNMENT ugua
                            on ugua.USERID = cpa.PROFILEID
                        where cpa.CUSTOMERID = c.UUID
                          and lower(ugua.USERGROUPID) like '%' + lower(:segment) + '%'
                    )
                  )
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("domainName", domainName)
                .addValue("customerNo", isBlank(customerNo) ? null : customerNo.trim())
                .addValue("query", isBlank(query) ? null : query.trim())
                .addValue("type", isBlank(type) ? null : type.trim())
                .addValue("status", isBlank(status) ? null : status.trim())
                .addValue("segment", isBlank(segment) ? null : segment.trim());

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
                order by a.ADDRESSID
            ) mainAddr
            outer apply (
                select top 1 *
                from CUSTOMERADDRESS a
                where a.CUSTOMERID = c.UUID
                order by a.ADDRESSID
            ) fallbackAddr
            outer apply (
                select top 1 *
                from CUSTOMERADDRESS a
                where a.CUSTOMERID = c.UUID
                  and a.USAGE = 2
                order by a.ADDRESSID
            ) invoiceAddr
            outer apply (
                select top 1 *
                from CUSTOMERADDRESS a
                where a.CUSTOMERID = c.UUID
                  and a.USAGE = 3
                order by a.ADDRESSID
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

    public List<CustomerAttributeDTO> findAttributesByCustomerNo(String customerNo)
    {
        String sql = """
            select
                av.NAME as name,
                coalesce(av.STRINGVALUE, convert(nvarchar(max), av.TEXTVALUE)) as value
            from CUSTOMER c
            join CUSTOMER_AV av
                on av.OWNERID = c.UUID
            where c.CUSTOMERNO = :customerNo
            order by av.NAME
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerNo", customerNo);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            CustomerAttributeDTO dto = new CustomerAttributeDTO();
            dto.setName(rs.getString("name"));
            dto.setValue(rs.getString("value"));
            return dto;
        });
    }

   public void saveCustomerAttribute(String customerNo, String name, String value)
{
    String updateSql = """
        update av
        set
            av.STRINGVALUE = :value,
            av.TEXTVALUE = null,
            av.LASTMODIFIED = getdate(),
            av.OCA = av.OCA + 1
        from CUSTOMER_AV av
        join CUSTOMER c
            on av.OWNERID = c.UUID
        where c.CUSTOMERNO = :customerNo
          and av.NAME = :name
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("customerNo", customerNo)
            .addValue("name", name)
            .addValue("value", value);

    int updated = jdbcTemplate.update(updateSql, params);

    if (updated > 0)
    {
        return;
    }

    String insertSql = """
        insert into CUSTOMER_AV
            (
                OWNERID,
                NAME,
                STRINGVALUE,
                TEXTVALUE,
                LOCALIZEDFLAG,
                LOCALEID,
                LASTMODIFIED,
                TYPE,
                OCA
            )
        select
            c.UUID,
            :name,
            :value,
            null,
            0,
            '',
            getdate(),
            1,
            0
        from CUSTOMER c
        where c.CUSTOMERNO = :customerNo
        """;

    int inserted = jdbcTemplate.update(insertSql, params);

    if (inserted == 0)
    {
        throw new IllegalStateException(
            "No CUSTOMER found with CUSTOMERNO: " + customerNo
        );
    }
}
    public List<CustomerUserDTO> findUsersByCustomerId(String customerId)
    {
        String sql = """
            select bp.BUSINESSPARTNERNO as businessPartnerNo
            from CUSTOMER c
            join CUSTOMERPROFILEASSIGNMENT cpa
                on c.UUID = cpa.CUSTOMERID
            join BASICPROFILE bp
                on cpa.PROFILEID = bp.UUID
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

    public List<Customer> findCustomersByCustomerNos(String domainName, List<String> customerNos)
    {
        if (customerNos == null || customerNos.isEmpty())
        {
            return List.of();
        }

        String sql = """
            select
                c.CUSTOMERNO as id,
                c.CUSTOMERNO as customerNo,
                c.CUSTOMERTYPEID as customerType,
                coalesce(typeAv.STRINGVALUE, 'Customer') as type,
                coalesce(mainAddr.COMPANYNAME1, mainAddr.ADDRESSNAME, c.CUSTOMERNO) as displayName,
                coalesce(mainAddr.COMPANYNAME1, mainAddr.ADDRESSNAME, c.CUSTOMERNO) as companyName,
                mainAddr.EMAIL as email,
                case
                    when c.APPROVALSTATUS = 1 then cast(1 as bit)
                    else cast(0 as bit)
                end as active,
                count(distinct addrCount.ADDRESSID) as locations,
                customerListAv.customerList as customerList
            from DOMAININFORMATION di
            join CUSTOMER c
                on di.DOMAINID = c.DOMAINID
            outer apply (
                select top 1 *
                from CUSTOMERADDRESS ca
                where ca.CUSTOMERID = c.UUID
                order by ca.ADDRESSID
            ) mainAddr
            outer apply (
                select top 1 av.STRINGVALUE
                from CUSTOMER_AV av
                where av.OWNERID = c.UUID
                  and av.NAME = 'CustomerType'
                order by av.LOCALIZEDFLAG asc, av.LOCALEID asc
            ) typeAv
            outer apply (
                select top 1 coalesce(av.STRINGVALUE, av.TEXTVALUE) as customerList
                from CUSTOMER_AV av
                where av.OWNERID = c.UUID
                  and av.NAME = 'CustomerList'
                order by av.LOCALIZEDFLAG asc, av.LOCALEID asc
            ) customerListAv
            left join CUSTOMERADDRESS addrCount
                on addrCount.CUSTOMERID = c.UUID
            where di.DOMAINNAME = :domainName
              and c.CUSTOMERNO in (:customerNos)
            group by
                c.UUID,
                c.CUSTOMERNO,
                c.CUSTOMERTYPEID,
                c.APPROVALSTATUS,
                typeAv.STRINGVALUE,
                customerListAv.customerList,
                mainAddr.COMPANYNAME1,
                mainAddr.ADDRESSNAME,
                mainAddr.EMAIL
            order by c.CUSTOMERNO
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("domainName", domainName)
                .addValue("customerNos", customerNos);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapCustomer(rs));
    }

    public List<Customer> findSubCustomersForCluster(String domainName, String clusterCustomerNo)
    {
        List<Customer> clusters = findCustomersByCustomerNos(domainName, List.of(clusterCustomerNo));

        if (clusters.isEmpty())
        {
            return List.of();
        }

        List<String> customerNos = parseCustomerList(clusters.get(0).getCustomerList());

        if (customerNos.isEmpty())
        {
            return List.of();
        }

        return findCustomersByCustomerNos(domainName, customerNos);
    }

    public List<Customer> findParentClustersForSubCustomer(String domainName, String subCustomerNo)
    {
        String sql = """
            select
                c.CUSTOMERNO as id,
                c.CUSTOMERNO as customerNo,
                c.CUSTOMERTYPEID as customerType,
                coalesce(typeAv.STRINGVALUE, 'Customer') as type,
                coalesce(mainAddr.COMPANYNAME1, mainAddr.ADDRESSNAME, c.CUSTOMERNO) as displayName,
                coalesce(mainAddr.COMPANYNAME1, mainAddr.ADDRESSNAME, c.CUSTOMERNO) as companyName,
                mainAddr.EMAIL as email,
                case
                    when c.APPROVALSTATUS = 1 then cast(1 as bit)
                    else cast(0 as bit)
                end as active,
                count(distinct addrCount.ADDRESSID) as locations,
                customerListAv.customerList as customerList
            from DOMAININFORMATION di
            join CUSTOMER c
                on c.DOMAINID = di.DOMAINID
            outer apply (
                select top 1 *
                from CUSTOMERADDRESS ca
                where ca.CUSTOMERID = c.UUID
                order by ca.ADDRESSID
            ) mainAddr
            outer apply (
                select top 1 av.STRINGVALUE
                from CUSTOMER_AV av
                where av.OWNERID = c.UUID
                  and av.NAME = 'CustomerType'
                order by av.LOCALIZEDFLAG asc, av.LOCALEID asc
            ) typeAv
            outer apply (
                select top 1 coalesce(av.STRINGVALUE, av.TEXTVALUE) as customerList
                from CUSTOMER_AV av
                where av.OWNERID = c.UUID
                  and av.NAME = 'CustomerList'
                order by av.LOCALIZEDFLAG asc, av.LOCALEID asc
            ) customerListAv
            left join CUSTOMERADDRESS addrCount
                on addrCount.CUSTOMERID = c.UUID
            where di.DOMAINNAME = :domainName
              and coalesce(typeAv.STRINGVALUE, 'Customer') = 'ClusterCustomer'
              and (
                    customerListAv.customerList = :subCustomerNo
                    or customerListAv.customerList like :subCustomerNo + char(9) + '%'
                    or customerListAv.customerList like '%' + char(9) + :subCustomerNo
                    or customerListAv.customerList like '%' + char(9) + :subCustomerNo + char(9) + '%'
                    or customerListAv.customerList like :subCustomerNo + ' %'
                    or customerListAv.customerList like '% ' + :subCustomerNo
                    or customerListAv.customerList like '% ' + :subCustomerNo + ' %'
                    or customerListAv.customerList like :subCustomerNo + '|%'
                    or customerListAv.customerList like '%|' + :subCustomerNo
                    or customerListAv.customerList like '%|' + :subCustomerNo + '|%'
                    or customerListAv.customerList like :subCustomerNo + ',%'
                    or customerListAv.customerList like '%,' + :subCustomerNo
                    or customerListAv.customerList like '%,' + :subCustomerNo + ',%'
                    or customerListAv.customerList like :subCustomerNo + ';%'
                    or customerListAv.customerList like '%;' + :subCustomerNo
                    or customerListAv.customerList like '%;' + :subCustomerNo + ';%'
                  )
            group by
                c.UUID,
                c.CUSTOMERNO,
                c.CUSTOMERTYPEID,
                c.APPROVALSTATUS,
                typeAv.STRINGVALUE,
                customerListAv.customerList,
                mainAddr.COMPANYNAME1,
                mainAddr.ADDRESSNAME,
                mainAddr.EMAIL
            order by c.CUSTOMERNO
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("domainName", domainName)
                .addValue("subCustomerNo", subCustomerNo);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapCustomer(rs));
    }

    public void deleteCustomerAttribute(String customerNo, String name)
{
    String sql = """
        delete av
        from CUSTOMER_AV av
        join CUSTOMER c
            on av.OWNERID = c.UUID
        where c.CUSTOMERNO = :customerNo
          and av.NAME = :name
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("customerNo", customerNo)
            .addValue("name", name);

    jdbcTemplate.update(sql, params);
}
public CustomerUserDTO findUserByCustomerIdAndBusinessPartnerNo(String customerId, String businessPartnerNo)
{
    String sql = """
        select top 1
            bp.BUSINESSPARTNERNO as businessPartnerNo
        from CUSTOMER c
        join CUSTOMERPROFILEASSIGNMENT cpa
            on c.UUID = cpa.CUSTOMERID
        join BASICPROFILE bp
            on cpa.PROFILEID = bp.UUID
        where c.CUSTOMERNO = :customerId
          and bp.BUSINESSPARTNERNO = :businessPartnerNo
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("customerId", customerId)
            .addValue("businessPartnerNo", businessPartnerNo);

    List<CustomerUserDTO> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
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

    return results.isEmpty() ? null : results.get(0);
}

public List<CustomerUserAttributeDTO> findUserAttributesByCustomerIdAndBusinessPartnerNo(
        String customerId,
        String businessPartnerNo)
{
    String sql = """
        select
            av.NAME as name,
            coalesce(av.STRINGVALUE, convert(nvarchar(max), av.TEXTVALUE)) as value
        from CUSTOMER c
        join CUSTOMERPROFILEASSIGNMENT cpa
            on c.UUID = cpa.CUSTOMERID
        join BASICPROFILE bp
            on cpa.PROFILEID = bp.UUID
        join BASICPROFILE_AV av
            on av.OWNERID = bp.UUID
        where c.CUSTOMERNO = :customerId
          and bp.BUSINESSPARTNERNO = :businessPartnerNo
        order by av.NAME
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("customerId", customerId)
            .addValue("businessPartnerNo", businessPartnerNo);

    return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
        CustomerUserAttributeDTO dto = new CustomerUserAttributeDTO();
        dto.setName(rs.getString("name"));
        dto.setValue(rs.getString("value"));
        return dto;
    });
}
public void saveUserAttribute(String businessPartnerNo, String name, String value)
{
    String updateSql = """
        update av
        set
            av.STRINGVALUE = :value,
            av.TEXTVALUE = null,
            av.LASTMODIFIED = getdate(),
            av.OCA = av.OCA + 1
        from BASICPROFILE_AV av
        join BASICPROFILE bp
            on av.OWNERID = bp.UUID
        where bp.BUSINESSPARTNERNO = :businessPartnerNo
          and av.NAME = :name
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("businessPartnerNo", businessPartnerNo)
            .addValue("name", name)
            .addValue("value", value);

    int updated = jdbcTemplate.update(updateSql, params);

    if (updated > 0)
    {
        return;
    }

    String insertSql = """
        insert into BASICPROFILE_AV
            (
                OWNERID,
                NAME,
                STRINGVALUE,
                TEXTVALUE,
                LOCALIZEDFLAG,
                LOCALEID,
                LASTMODIFIED,
                TYPE,
                OCA
            )
        select
            bp.UUID,
            :name,
            :value,
            null,
            0,
            '',
            getdate(),
            1,
            0
        from BASICPROFILE bp
        where bp.BUSINESSPARTNERNO = :businessPartnerNo
        """;

    int inserted = jdbcTemplate.update(insertSql, params);

    if (inserted == 0)
    {
        throw new IllegalStateException("No BASICPROFILE found with BUSINESSPARTNERNO: " + businessPartnerNo);
    }
}

public String findUserAttributeValue(String businessPartnerNo, String name)
{
    String sql = """
        select top 1
            coalesce(av.STRINGVALUE, convert(nvarchar(max), av.TEXTVALUE)) as value
        from BASICPROFILE bp
        join BASICPROFILE_AV av
            on av.OWNERID = bp.UUID
        where bp.BUSINESSPARTNERNO = :businessPartnerNo
          and av.NAME = :name
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("businessPartnerNo", businessPartnerNo)
            .addValue("name", name);

    List<String> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getString("value"));

    return results.isEmpty() ? "" : results.get(0);
}
public void deleteUserAttribute(String businessPartnerNo, String name)
{
    String sql = """
        delete av
        from BASICPROFILE_AV av
        join BASICPROFILE bp
            on av.OWNERID = bp.UUID
        where bp.BUSINESSPARTNERNO = :businessPartnerNo
          and av.NAME = :name
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("businessPartnerNo", businessPartnerNo)
            .addValue("name", name);

    jdbcTemplate.update(sql, params);
}

    private Customer mapCustomer(ResultSet rs) throws SQLException
    {
        Customer customer = new Customer();
        customer.setId(rs.getString("id"));
        customer.setCustomerNo(rs.getString("customerNo"));
        customer.setCustomerType(rs.getString("customerType"));
        customer.setType(rs.getString("type"));
        customer.setDisplayName(rs.getString("displayName"));
        customer.setCompanyName(rs.getString("companyName"));
        customer.setEmail(rs.getString("email"));
        customer.setActive(rs.getBoolean("active"));
        customer.setLocations(rs.getInt("locations"));
        customer.setCustomerList(rs.getString("customerList"));
        return customer;
    }

    private List<String> parseCustomerList(String value)
    {
        if (value == null || value.isBlank())
        {
            return List.of();
        }

        return List.of(value.split("[\\t|,;\\s]+")).stream()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}