package org.zeplinko.chrono.poc.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "triggerCallbacks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TriggerCallback {

    @Id
    private String id;
    private Long triggerId;

    public TriggerCallback(Long triggerId) {
        this.triggerId = triggerId;
    }
}
