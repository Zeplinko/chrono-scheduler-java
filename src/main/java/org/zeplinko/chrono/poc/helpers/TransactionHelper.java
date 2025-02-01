package org.zeplinko.chrono.poc.helpers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zeplinko.chrono.poc.models.Trigger;
import org.zeplinko.chrono.poc.utils.CommonUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TransactionHelper {

    private static final String FETCH_SINGLE_TRIGGER = "SELECT id FROM triggers WHERE status = 'PENDING' ORDER BY id LIMIT 1";
    private static final String ACQUIRE_SINGLE_TRIGGER = "UPDATE triggers SET status = 'ACQUIRED', version = version + 1 WHERE id = ?";
    private static final String FETCH_MULTIPLE_TRIGGERS = "SELECT * FROM triggers WHERE status = 'PENDING' ORDER BY id LIMIT ?";
    private static final int BATCH_SIZE = 5;

    private final JdbcTemplate jdbcTemplate;

    public TransactionHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Optional<Trigger> fetchSingleTrigger() {
        List<Trigger> triggerList = jdbcTemplate.query(
                FETCH_SINGLE_TRIGGER,
                CommonUtils.getTriggerRowMapper(Instant.now().toEpochMilli())
        );
        if (triggerList.isEmpty()) {
            return Optional.empty();
        }

        Trigger trigger = triggerList.getFirst();
        jdbcTemplate.update(
                ACQUIRE_SINGLE_TRIGGER,
                trigger.getId()
        );
        // TODO: Send the updated trigger list
        return Optional.of(trigger);
    }

    @Transactional
    public List<Trigger> fetchMultipleTriggers() {
        List<Trigger> triggerList = jdbcTemplate.query(
                FETCH_MULTIPLE_TRIGGERS,
                (rs, rowNum) -> Trigger.builder()
                        .build(),
                BATCH_SIZE
        );
        if (triggerList.isEmpty()) {
            return triggerList;
        }
        String idList = triggerList.stream()
                .map(trigger -> trigger.getId())
                .collect(Collectors.joining(","));
        jdbcTemplate.update(
                "UPDATE triggers SET status = 'ACQUIRED', version = version + 1 WHERE id IN (" + idList + ")"
        );
        // TODO: Return the updated triggerList;
        return triggerList;
    }
}
