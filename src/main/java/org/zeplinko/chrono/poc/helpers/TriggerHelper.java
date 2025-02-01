package org.zeplinko.chrono.poc.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.zeplinko.chrono.poc.models.Trigger;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class TriggerHelper {

    private final KafkaTemplate<String, Trigger> kafkaTemplate;

    public TriggerHelper(KafkaTemplate<String, Trigger> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendListToKafka(List<Trigger> triggerList, String KAFKA_TOPIC) {
        triggerList.forEach(trigger -> sendInternal(trigger, KAFKA_TOPIC));
        kafkaTemplate.flush();
    }

    public void sendOneToKafka(Trigger trigger, String KAFKA_TOPIC) {
        sendInternal(trigger, KAFKA_TOPIC);
        kafkaTemplate.flush();
    }

    private void sendInternal(Trigger trigger, String KAFKA_TOPIC) {
        kafkaTemplate.send(KAFKA_TOPIC, UUID.randomUUID().toString(), trigger);
    }
}
