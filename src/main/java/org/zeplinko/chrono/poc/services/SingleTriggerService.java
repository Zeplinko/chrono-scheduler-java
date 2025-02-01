package org.zeplinko.chrono.poc.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.zeplinko.chrono.poc.constants.Constants;
import org.zeplinko.chrono.poc.models.Trigger;
import org.zeplinko.chrono.poc.helpers.TriggerHelper;
import org.zeplinko.chrono.poc.utils.CommonUtils;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class SingleTriggerService {

    public static final String SQL = "WITH cte AS (SELECT id, version FROM triggers WHERE status = 'PENDING' ORDER BY id LIMIT 1) " +
            "UPDATE triggers SET status = 'ACQUIRED', version = cte.version + 1 FROM cte WHERE triggers.id = cte.id RETURNING triggers.*;";

    private final TriggerHelper triggerHelper;

    private final JdbcTemplate jdbcTemplate;

    public SingleTriggerService(TriggerHelper triggerHelper, JdbcTemplate jdbcTemplate) {
        this.triggerHelper = triggerHelper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void processSingleTriggerInOneQuery() {
        while (true) {
            try {
                long currentTime = Instant.now().toEpochMilli();
                List<Trigger> triggerList = jdbcTemplate.query(SQL, CommonUtils.getTriggerRowMapper(currentTime));
                int updatedRows = triggerList.size();
                log.info("Updated {} records.", updatedRows);

                if (updatedRows == 0) {
                    CommonUtils.waitForMills(100L);
                    continue;
                }

                Trigger first = triggerList.getFirst();
                triggerHelper.sendOneToKafka(first, Constants.KAFKA_TOPIC);
                log.info("Updated record with ID: {}", first.getId());

            } catch (Exception ex) {
                log.error("Error occurred while processing batch: {}", ex.getMessage());
            }
        }
    }

}
