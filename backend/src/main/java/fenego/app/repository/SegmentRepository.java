package fenego.app.repository;

import fenego.app.dto.SegmentLogItemDTO;
import fenego.app.jpa.Segment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SegmentRepository
{
    private final JdbcTemplate jdbcTemplate;

    public SegmentRepository(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Segment> findAll()
    {
        String sql = """
            SELECT
                id,
                COALESCE(name, id) AS name,
                description,
                rule_expression,
                matched_customers,
                CONVERT(varchar, last_updated, 120) AS last_updated,
                auto_updated
            FROM app_segments
            ORDER BY name
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Segment(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("rule_expression"),
                rs.getInt("matched_customers"),
                rs.getString("last_updated"),
                rs.getBoolean("auto_updated")
        ));
    }

    public List<SegmentLogItemDTO> findAllLogs()
    {
        String sql = """
            SELECT
                id,
                direction,
                message,
                CONVERT(varchar, created_at, 120) AS created_at
            FROM app_segment_log
            ORDER BY created_at DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SegmentLogItemDTO(
                rs.getString("id"),
                rs.getString("direction"),
                rs.getString("message"),
                rs.getString("created_at")
        ));
    }

    public void upsertSegmentFromIntershop(String id, String name, String description)
    {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_segments WHERE id = ?",
                Integer.class,
                id
        );

        if (count != null && count > 0)
        {
            String updateSql = """
                UPDATE app_segments
                SET
                    name = ?,
                    description = ?,
                    last_updated = GETDATE(),
                    source = 'INTERSHOP'
                WHERE id = ?
                """;

            jdbcTemplate.update(updateSql, name, description, id);
        }
        else
        {
            String insertSql = """
                INSERT INTO app_segments
                (id, name, description, rule_expression, matched_customers, last_updated, auto_updated, source)
                VALUES (?, ?, ?, NULL, 0, GETDATE(), 1, 'INTERSHOP')
                """;

            jdbcTemplate.update(insertSql, id, name, description);
        }
    }

    public void insertSyncLog(String direction, String message)
    {
        String sql = """
            INSERT INTO app_segment_log (id, segment_id, direction, message, created_at)
            VALUES (?, NULL, ?, ?, GETDATE())
            """;

        jdbcTemplate.update(sql, UUID.randomUUID().toString(), direction, message);
    }

    public void updateRuleExpression(String segmentId, String ruleExpression)
    {
        String sql = """
            UPDATE app_segments
            SET
                rule_expression = ?,
                last_updated = GETDATE(),
                auto_updated = 0
            WHERE id = ?
            """;

        jdbcTemplate.update(sql, ruleExpression, segmentId);
    }

    public void insertSegmentLog(String segmentId, String direction, String message)
    {
        String sql = """
            INSERT INTO app_segment_log (id, segment_id, direction, message, created_at)
            VALUES (?, ?, ?, ?, GETDATE())
            """;

        jdbcTemplate.update(sql, UUID.randomUUID().toString(), segmentId, direction, message);
    }

    public void insertSegment(String id, String name, String description, String rule)
    {
        String sql = """
            INSERT INTO app_segments
            (id, name, description, rule_expression, matched_customers, last_updated, auto_updated, source)
            VALUES (?, ?, ?, ?, 0, GETDATE(), 0, 'MANUAL')
            """;

        jdbcTemplate.update(sql, id, name, description, rule);
    }
}