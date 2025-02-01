package org.zeplinko.chrono.poc.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Trigger {
    private String id;
    private Long acquiredAt;
}
