package org.zeplinko.chrono.poc.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.zeplinko.chrono.poc.models.Trigger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonUtils {
    public static void waitForMills(long timeInMillis) {
        try {
            Thread.sleep(timeInMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
        }
    }

    public static RowMapper<Trigger> getTriggerRowMapper(long currentTime) {
        return (rs, rowNum) -> Trigger.builder()
                .id(rs.getString("id"))
                .acquiredAt(currentTime)
                .build();
    }
}
