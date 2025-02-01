package org.zeplinko.chrono.poc.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zeplinko.chrono.poc.constants.Constants;
import org.zeplinko.chrono.poc.helpers.TransactionHelper;
import org.zeplinko.chrono.poc.helpers.TriggerHelper;
import org.zeplinko.chrono.poc.models.Trigger;
import org.zeplinko.chrono.poc.utils.CommonUtils;

import java.util.List;

@Service
@Slf4j
public class TransactionalBatchTriggerService {
    private final TriggerHelper triggerHelper;

    private final TransactionHelper transactionHelper;

    public TransactionalBatchTriggerService(TriggerHelper triggerHelper, TransactionHelper transactionHelper) {
        this.triggerHelper = triggerHelper;
        this.transactionHelper = transactionHelper;
    }

    public void processBatchTriggerInTwoQuery() {
        while (true) {
            try {
                log.info("Starting transactional batch trigger");
                List<Trigger> triggerList = transactionHelper.fetchMultipleTriggers();
                int updatedTriggers = triggerList.size();
                log.info("Updated {} records.", updatedTriggers);
                if (updatedTriggers == 0) {
                    CommonUtils.waitForMills(100L);
                    continue;
                }
                triggerHelper.sendListToKafka(triggerList, Constants.KAFKA_TOPIC);
            } catch (Exception ex) {
                log.error("Error occurred while processing batch: {}", ex.getMessage(), ex);
            }
        }
    }
}
