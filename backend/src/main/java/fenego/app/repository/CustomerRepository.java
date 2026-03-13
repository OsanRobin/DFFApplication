package fenego.app.repository;

import fenego.app.dto.CustomerDTO;
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
                coalesce(ca.COMPANYNAME1, ca.ADDRESSNAME, c.CUSTOMERNO) as displayName,
                coalesce(ca.COMPANYNAME1, ca.ADDRESSNAME, c.CUSTOMERNO) as companyName,
                ca.EMAIL as email,
                cast(1 as bit) as active
            from DOMAININFORMATION di
            join CUSTOMER c on di.DOMAINID = c.DOMAINID
            left join CUSTOMERADDRESS ca on ca.CUSTOMERID = c.UUID
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
            select count(distinct c.UUID)
            from DOMAININFORMATION di
            join CUSTOMER c on di.DOMAINID = c.DOMAINID
            left join CUSTOMERADDRESS ca on ca.CUSTOMERID = c.UUID
            where di.DOMAINNAME = :domainName
              and (:customerNo is null or c.CUSTOMERNO like '%' + :customerNo + '%')
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("domainName", domainName)
                .addValue("customerNo", customerNo == null || customerNo.isBlank() ? null : customerNo);

        Integer result = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return result == null ? 0 : result;
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
}