package fenego.app.repository;

import fenego.app.dto.SavedCustomerSearchDTO;
import fenego.app.dto.SaveCustomerSearchRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SavedCustomerSearchRepository
{
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SavedCustomerSearchRepository(NamedParameterJdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SavedCustomerSearchDTO> findByDomain(String domainName)
    {
        String sql = """
            select
                id,
                domain_name as domainName,
                name,
                query_text as query,
                customer_no as customerNo,
                type_filter as typeFilter,
                status_filter as statusFilter
            from saved_customer_search
            where domain_name = :domainName
            order by name asc
            """;

        return jdbcTemplate.query(
            sql,
            new MapSqlParameterSource("domainName", domainName),
            (rs, rowNum) -> {
                SavedCustomerSearchDTO dto = new SavedCustomerSearchDTO();
                dto.setId(rs.getLong("id"));
                dto.setDomainName(rs.getString("domainName"));
                dto.setName(rs.getString("name"));
                dto.setQuery(rs.getString("query"));
                dto.setCustomerNo(rs.getString("customerNo"));
                dto.setTypeFilter(rs.getString("typeFilter"));
                dto.setStatusFilter(rs.getString("statusFilter"));
                return dto;
            }
        );
    }

    public boolean existsByDomainAndName(String domainName, String name)
    {
        String sql = """
            select count(*)
            from saved_customer_search
            where domain_name = :domainName
              and name = :name
            """;

        Integer count = jdbcTemplate.queryForObject(
            sql,
            new MapSqlParameterSource()
                .addValue("domainName", domainName)
                .addValue("name", name),
            Integer.class
        );

        return count != null && count > 0;
    }

    public void insert(SaveCustomerSearchRequest request)
    {
        String sql = """
            insert into saved_customer_search (
                domain_name,
                name,
                query_text,
                customer_no,
                type_filter,
                status_filter
            ) values (
                :domainName,
                :name,
                :query,
                :customerNo,
                :typeFilter,
                :statusFilter
            )
            """;

        jdbcTemplate.update(sql, toParams(request));
    }

    public void update(SaveCustomerSearchRequest request)
    {
        String sql = """
            update saved_customer_search
            set
                query_text = :query,
                customer_no = :customerNo,
                type_filter = :typeFilter,
                status_filter = :statusFilter
            where domain_name = :domainName
              and name = :name
            """;

        jdbcTemplate.update(sql, toParams(request));
    }
    public void deleteById(Long id)
{
    String sql = """
        delete from saved_customer_search
        where id = :id
        """;

    jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
}

public void updateName(Long id, String newName)
{
    String sql = """
        update saved_customer_search
        set name = :name
        where id = :id
        """;

    jdbcTemplate.update(
        sql,
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("name", newName)
    );
}

    private MapSqlParameterSource toParams(SaveCustomerSearchRequest request)
    {
        return new MapSqlParameterSource()
            .addValue("domainName", request.getDomainName())
            .addValue("name", request.getName())
            .addValue("query", request.getQuery())
            .addValue("customerNo", request.getCustomerNo())
            .addValue("typeFilter", request.getTypeFilter())
            .addValue("statusFilter", request.getStatusFilter());
    }
}