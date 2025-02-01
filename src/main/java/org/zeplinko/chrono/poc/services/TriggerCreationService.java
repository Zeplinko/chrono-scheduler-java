package org.zeplinko.chrono.poc.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Service
@Slf4j
public class TriggerCreationService {

    private final JdbcTemplate jdbcTemplate;


    // TODO: Should be a configuration
    private static final int MAX_THREADS = 4;

    public TriggerCreationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setupEnvironment(int initialDelayInSeconds, int triggersPerSecond, int workloadTimeInSeconds) {
        final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
        deleteTriggersTableIfExists();
        createTriggersTable();
        List<? extends Future<?>> futures = insertTriggers(executorService, initialDelayInSeconds, triggersPerSecond, workloadTimeInSeconds);
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
    }

    private void deleteTriggersTableIfExists() {
        String sql = "DROP TABLE IF EXISTS triggers";
        jdbcTemplate.execute(sql);
    }

    private void createTriggersTable() {
        @SuppressWarnings("SqlNoDataSourceInspection")
        String sql = """
                CREATE TABLE public.triggers (\
                 id varchar NOT NULL,\
                 "version" bigint NOT NULL,\
                 next_fire_time bigint NOT NULL,\
                 status varchar NOT NULL,\
                 CONSTRAINT triggers_pk PRIMARY KEY (id)\
                );\
                """;
        jdbcTemplate.execute(sql);
    }

    private List<? extends Future<?>> insertTriggers(ExecutorService executorService, int initialDelayInSeconds, int triggersPerSecond, int workloadTimeInSeconds) {
        long firstTriggerTime = Instant.now().toEpochMilli() + initialDelayInSeconds * 1000L;
        List<Long> timestampList = LongStream.range(0, workloadTimeInSeconds)
                .map(timeInSeconds -> firstTriggerTime + timeInSeconds * 1000L)
                .boxed()
                .toList();

        return IntStream.range(0, MAX_THREADS)
                .boxed()
                .map(integer -> {
                    Runnable runnable = () -> {
                        for (int j = integer; j < timestampList.size(); j += MAX_THREADS) {
                            Long timestamp = timestampList.get(j);
                            insertTriggersBatch(timestamp, triggersPerSecond);
                        }
                    };
                    return runnable;
                })
                .map(runnable -> executorService.submit(runnable))
                .toList();
    }

    private void insertTriggersBatch(long timestamp, int triggersPerSecond) {
        @SuppressWarnings("SqlNoDataSourceInspection")
        String sql = "INSERT INTO triggers (id, version, next_fire_time, status) VALUES (?, ?, ?, ?)";
        List<Object[]> batchParams = IntStream.range(0, triggersPerSecond)
                .mapToObj(i -> new Object[]{
                        "TRIGGER_" + timestamp + "_" + i,
                        0,
                        timestamp,
                        "PENDING"
                }).toList();
        jdbcTemplate.batchUpdate(sql, batchParams);
    }
}
