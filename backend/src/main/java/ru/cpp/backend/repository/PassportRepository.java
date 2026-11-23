package ru.cpp.backend.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.cpp.backend.dto.LatestWeighingDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PassportRepository {

    /** Порог подсветки отклонения веса, %. Один раз тут, используется и в SQL, и в Java. */
    public static final BigDecimal ALERT_PERCENT = BigDecimal.valueOf(5);

    /** "Сырая" строка из UNION: коды ещё не переведены на русский. */
    public record RawEvent(String kind, Integer id, Instant eventTime, String title,
                           String details, String source, String responsible, boolean alert) {
    }

    // Текстовый блок """...""" (Java 15+): многострочная строка без \n и склеек "+".
    // Первая ветка задаёт имена и типы столбцов для всех остальных.
    // :batchId и :alertPercent - именованные параметры (аналог :name в PDO). Значения подставляются
    // безопасно, SQL-инъекция невозможна.
    private static final String EVENTS_UNION = """
            SELECT 'INDICATOR' AS kind, i.indicator_id AS id, i.measured_at AS event_time,
                   i.indicator_type AS title,
                   CAST(trim_scale(i.value) AS text) || ' ' || i.unit AS details,
                   i.source_system AS source,
                   CAST(NULL AS text) AS responsible,
                   FALSE AS alert
            FROM indicator i
            WHERE i.batch_id = :batchId

            UNION ALL
            SELECT 'INCIDENT', n.incident_id, n.occurred_at, n.incident_type,
                   concat_ws('. ', n.description,
                       'Статус: ' || CASE n.status WHEN 'OPEN' THEN 'открыт'
                                                   WHEN 'IN_PROGRESS' THEN 'в работе'
                                                   ELSE 'устранён' END),
                   n.source_system, CAST(NULL AS text),
                   n.status <> 'RESOLVED'
            FROM incident n
            WHERE n.batch_id = :batchId

            UNION ALL
            SELECT 'VET', t.treatment_id, CAST(t.treatment_date AS timestamptz), t.treatment_type,
                   concat_ws(', ', t.drug_name, t.dosage, t.method, t.notes),
                   'Ручной ввод', concat_ws(' ', u.surname, u.name),
                   FALSE
            FROM vet_treatment t
            JOIN app_user u ON u.user_id = t.responsible_user_id
            WHERE t.batch_id = :batchId

            UNION ALL
            SELECT 'WEIGHING', w.weighing_id, CAST(w.weighing_date AS timestamptz), 'WEIGHING',
                   'Возраст ' || w.age_days || ' дн., выборка ' || w.sample_size
                   || ' гол.: факт ' || CAST(trim_scale(w.actual_avg_weight) AS text)
                   || ' г, план ' || CAST(trim_scale(w.planned_avg_weight) AS text)
                   || ' г, отклонение ' || CAST(trim_scale(w.deviation_percent) AS text) || '%',
                   'Ручной ввод', concat_ws(' ', u.surname, u.name),
                   abs(w.deviation_percent) > :alertPercent
            FROM control_weighing w
            JOIN app_user u ON u.user_id = w.responsible_user_id
            WHERE w.batch_id = :batchId

            UNION ALL
            SELECT 'SLAUGHTER', s.slaughter_result_id, CAST(s.slaughter_date AS timestamptz), s.quality_result,
                   'Поголовье ' || s.actual_count
                   || ', живая масса ' || CAST(trim_scale(s.final_live_weight) AS text) || ' кг'
                   || ', выход ' || CAST(trim_scale(s.product_yield_percent) AS text) || '%'
                   || ', сохранность ' || CAST(trim_scale(s.survival_rate_percent) AS text) || '%',
                   'Ручной ввод', concat_ws(' ', u.surname, u.name),
                   s.quality_result <> 'COMPLIANT'
            FROM slaughter_result s
            JOIN app_user u ON u.user_id = s.responsible_user_id
            WHERE s.batch_id = :batchId
            """;

    private final NamedParameterJdbcTemplate jdbc;

    // NamedParameterJdbcTemplate создаёт Spring Boot сам (он входит в starter-data-jpa), как и EntityManager
    public PassportRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<RawEvent> findEvents(Integer batchId, String kind, int limit, int offset) {
        MapSqlParameterSource params = baseParams(batchId)
                .addValue("limit", limit)
                .addValue("offset", offset);
        String sql = "SELECT e.* FROM (" + EVENTS_UNION + ") e"
                + kindFilter(kind, params)
                + " ORDER BY e.event_time DESC, e.kind, e.id DESC"
                + " LIMIT :limit OFFSET :offset";

        // (rs, rowNum) -> ... : лямбда, превращающая одну строку результата в объект
        return jdbc.query(sql, params, (rs, rowNum) -> new RawEvent(
                rs.getString("kind"),
                rs.getInt("id"),
                rs.getObject("event_time", OffsetDateTime.class).toInstant(),
                rs.getString("title"),
                rs.getString("details"),
                rs.getString("source"),
                rs.getString("responsible"),
                rs.getBoolean("alert")));
    }

    public Integer countEvents(Integer batchId, String kind) {
        MapSqlParameterSource params = baseParams(batchId);
        String sql = "SELECT count(*) FROM (" + EVENTS_UNION + ") e" + kindFilter(kind, params);
        Integer total = jdbc.queryForObject(sql, params, Integer.class);
        return total == null ? 0 : total;
    }

    public Optional<LatestWeighingDto> findLatestWeighing(Integer batchId) {
        String sql = """
                SELECT weighing_date, age_days, actual_avg_weight, planned_avg_weight, deviation_percent
                FROM control_weighing
                WHERE batch_id = :batchId
                ORDER BY weighing_date DESC, weighing_id DESC
                LIMIT 1
                """;
        return jdbc.query(sql, baseParams(batchId), (rs, rowNum) -> {
            BigDecimal deviation = rs.getBigDecimal("deviation_percent");
            return new LatestWeighingDto(
                    rs.getObject("weighing_date", LocalDate.class),
                    rs.getInt("age_days"),
                    rs.getBigDecimal("actual_avg_weight"),
                    rs.getBigDecimal("planned_avg_weight"),
                    deviation,
                    deviation.abs().compareTo(ALERT_PERCENT) > 0);   // BigDecimal сравнивают через compareTo
        }).stream().findFirst();
    }

    public Optional<BigDecimal> findSurvivalPercent(Integer batchId) {
        String sql = "SELECT survival_rate_percent FROM slaughter_result WHERE batch_id = :batchId";
        return jdbc.query(sql, baseParams(batchId), (rs, rowNum) -> rs.getBigDecimal(1))
                .stream().findFirst();
    }

    // ---- вспомогательное ----

    private MapSqlParameterSource baseParams(Integer batchId) {
        return new MapSqlParameterSource()
                .addValue("batchId", batchId)
                .addValue("alertPercent", ALERT_PERCENT);
    }

    /** Добавляет WHERE по типу, только если фильтр выбран. Фрагмент SQL - константа, не ввод пользователя. */
    private String kindFilter(String kind, MapSqlParameterSource params) {
        if (kind == null) {
            return "";
        }
        params.addValue("kind", kind);
        return " WHERE e.kind = :kind";
    }
}