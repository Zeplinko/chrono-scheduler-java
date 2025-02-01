package org.zeplinko.chrono.poc.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.zeplinko.chrono.poc.constants.Constants;
import org.zeplinko.chrono.poc.helpers.TriggerHelper;
import org.zeplinko.chrono.poc.models.Trigger;
import org.zeplinko.chrono.poc.utils.CommonUtils;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class BatchTriggerService {

        public static final String SQL = "WITH cte AS (SELECT id, version FROM triggers WHERE status = 'PENDING' ORDER BY id LIMIT ? FOR UPDATE SKIP LOCKED) " +
                "UPDATE triggers SET status = 'ACQUIRED', version = version + 1 WHERE (id, version) IN (SELECT id, version FROM cte) RETURNING triggers.*;";

    private static final int BATCH_SIZE = 5;

    private final TriggerHelper triggerHelper;

    private final JdbcTemplate jdbcTemplate;

    public BatchTriggerService(TriggerHelper triggerHelper, JdbcTemplate jdbcTemplate) {
        this.triggerHelper = triggerHelper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void processBatchTriggerInOneQuery() {
        while (true) {
            try {
                long currentTime = Instant.now().toEpochMilli();
                List<Trigger> triggerList = jdbcTemplate.query(SQL, CommonUtils.getTriggerRowMapper(currentTime), BATCH_SIZE);
                int updatedRows = triggerList.size();
                log.info("Updated {} records in batch.", updatedRows);

                if (updatedRows == 0) {
                    CommonUtils.waitForMills(100L);
                    continue;
                }

                triggerHelper.sendListToKafka(triggerList, Constants.KAFKA_TOPIC);

            } catch (Exception ex) {
                log.error("Error occurred while processing batch: {}", ex.getMessage());
            }
        }
    }

}
