package org.zeplinko.chrono.poc.services;

import org.zeplinko.chrono.poc.constants.Constants;
import org.zeplinko.chrono.poc.helpers.TransactionHelper;
import org.zeplinko.chrono.poc.helpers.TriggerHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zeplinko.chrono.poc.models.Trigger;
import org.zeplinko.chrono.poc.utils.CommonUtils;

import java.util.Optional;

@Service
@Slf4j
public class TransactionalSingleTriggerService {
    private final TriggerHelper triggerHelper;

    private final TransactionHelper transactionHelper;

    public TransactionalSingleTriggerService(TriggerHelper triggerHelper, TransactionHelper transactionHelper) {
        this.triggerHelper = triggerHelper;
        this.transactionHelper = transactionHelper;
    }

    public void processSingleTriggerInTwoQuery() {
        while (true) {
            try {
                log.info("Starting transactional trigger processing");
                Optional<Trigger> triggerOptional = transactionHelper.fetchSingleTrigger();
                if (triggerOptional.isEmpty()) {
                    CommonUtils.waitForMills(100L);
                }
                Trigger trigger = triggerOptional.get();
                triggerHelper.sendOneToKafka(trigger, Constants.KAFKA_TOPIC);
            } catch (Exception ex) {
                log.error("Error occurred while processing record: {}", ex.getMessage(), ex);
            }
        }
    }

}
